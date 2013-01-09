package com.wcb.cms.modelmaker.redis;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lambdaworks.redis.RedisAsyncConnection;
import com.lambdaworks.redis.RedisClient;
import com.wcb.cms.modelmaker.api.CMSEntityDtlsDB;
import com.wcb.cms.modelmaker.api.CMSEntityEntry;
import com.wcb.cms.modelmaker.api.ErrorMessages;

public class RedisConnector implements CMSEntityDtlsDB{

	private RedisAsyncConnection<String, String> asyncConnection;

	/**
	 * NOTE: This constructor is needed to make use of Blueprint.
	 */
	public RedisConnector(){
		asyncConnection = null;
	}

	@Override
	public void addAttributeAndDomainDefinition(List<CMSEntityEntry> list) throws InterruptedException, ExecutionException, IOException{
		for (CMSEntityEntry cmsEntityEntry : list) {
			List<Future<String>> futureList = cmsEntityEntry.getFutureDBReturnValueList();
			if(futureList.isEmpty() && !cmsEntityEntry.isFunction()){
				throw new IOException(ErrorMessages.error1(cmsEntityEntry.getColumn(), cmsEntityEntry.getTable()));
			}
			for (Future<String> future : futureList) {
				String string = future.get();
				cmsEntityEntry.addToAttrDomainDefMap(
						findPatternFrom("EntityName:", string) + "."+ findPatternFrom("Attribute:", string),
						findPatternFrom("DomainDef:", string)
						);

			}
		}
	}

	@Override
	public void close() {
		//asyncConnection.clientKill(addr);
		asyncConnection.close();
	}

	@Override
	public void connect(String uri) {
		if(asyncConnection == null){
			RedisClient client = new RedisClient(uri);
			asyncConnection = client.connectAsync();

		}
	}

	@Override
	public void findInDB(
			List<CMSEntityEntry> sqlElements) {

		for (CMSEntityEntry entry : sqlElements) {
			try {
				for (String table : entry.getPotentialTableList()) {
					setFutureReturnValueIn(entry,
							asyncConnection.keys(table + "_" + entry.getColumn()));
				}
			} catch (InterruptedException | ExecutionException e) {
				//That is ok
			}
		}
	}

	private String findPatternFrom(final String pattern, final String jsonString) throws IOException {
		Pattern compile = Pattern.compile("("+ pattern+"\\w+"+","+")|(" + pattern+".+"+"}"+ ")");
		Matcher matcher = compile.matcher(jsonString);
		while(matcher.find()){
			return matcher.group().replaceFirst(pattern, "").replaceFirst("\\W", "");
		}
		throw new IOException(ErrorMessages.error2(jsonString));
	}

	private void setFutureReturnValueIn(CMSEntityEntry entry, Future<List<String>> futureTableColumnKeys)
			throws InterruptedException, ExecutionException {
		if(asyncConnection.awaitAll(futureTableColumnKeys) == false){
			asyncConnection.awaitAll(1, TimeUnit.NANOSECONDS, futureTableColumnKeys);
			setFutureReturnValueIn(entry, futureTableColumnKeys);
		}else{
			for (String aKey : futureTableColumnKeys.get()) {
				entry.addFutureDBValue(asyncConnection.get(aKey));
			}
		}
	}

}
