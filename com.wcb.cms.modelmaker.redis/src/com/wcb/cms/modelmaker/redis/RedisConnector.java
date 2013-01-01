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

public class RedisConnector implements CMSEntityDtlsDB{

	private RedisAsyncConnection<String, String> asyncConnection;

	@Override
	public void addDomainDefinition(List<CMSEntityEntry> list) throws InterruptedException, ExecutionException, IOException{
		for (CMSEntityEntry cmsEntityEntry : list) {
			Future<String> future = cmsEntityEntry.getFutureDBValue();
			String string;
			string = future.get();
			cmsEntityEntry.setDomainDefinition(findPatternFrom("DomainDef:", string));
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
				Future<List<String>> futureKeys = asyncConnection.keys(entry.getTable() + "_" + entry.getColumn());
				setFutureValueIn(entry, futureKeys);

				if(entry.getPotentialTableList().size() > 1){
					for (String table : entry.getPotentialTableList()) {
						futureKeys = asyncConnection.keys(table + "_" + entry.getColumn());
						setFutureValueIn(entry, futureKeys);
					}

				}
			} catch (InterruptedException | ExecutionException e) {
				//That is ok
			}
		}
	}

	private String findPatternFrom(String pattern, String jsonString) throws IOException {
		Pattern compile = Pattern.compile(pattern+".+"+"((, )|})");
		Matcher matcher = compile.matcher(jsonString);
		while(matcher.find()){
			return matcher.group().replaceFirst(pattern, "").replaceFirst("}", "");
		}
		throw new IOException("This returned value \""
				+ jsonString
				+ "\" is not formatted correctly, please check on the persistent entry.");
	}

	private void setFutureValueIn(CMSEntityEntry entry, Future<List<String>> futureKeys)
			throws InterruptedException, ExecutionException {
		if(asyncConnection.awaitAll(futureKeys) == false){
			asyncConnection.awaitAll(1, TimeUnit.NANOSECONDS, futureKeys);
			setFutureValueIn(entry, futureKeys);
		}else{
			for (String aKey : futureKeys.get()) {
				entry.setFutureDBValue(asyncConnection.get(aKey));
			}
		}
	}

}
