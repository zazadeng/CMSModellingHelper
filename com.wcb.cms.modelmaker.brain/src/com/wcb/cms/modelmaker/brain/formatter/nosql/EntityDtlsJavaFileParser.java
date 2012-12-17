package com.wcb.cms.modelmaker.brain.formatter.nosql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EntityDtlsJavaFileParser  {

	private final ExecutorService pool;
	private final CompletionService<List<CMSEntityRecord>> completionService;
	private int tasks;
	
	public EntityDtlsJavaFileParser() {
		pool = Executors.newCachedThreadPool();
		//ExecutorCompletionService is a wrapper of Executors 
		//and it offers convenience methods for retrieving the most recently completed tasks
		completionService
		= new ExecutorCompletionService<List<CMSEntityRecord>>( pool);
		tasks = 0;
	}

	

	public List<CMSEntityRecord> getResult(){
		final List<CMSEntityRecord> list = new ArrayList<CMSEntityRecord>();
		for(int i = 0 ; i <tasks ; ++i){
			//NOTE: retrieve the most recently completed tasks 
			try{
				list.addAll(completionService.take().get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		shutDownThreadPool();
		return list;
	}

	public void parse(final File file) {
		tasks++;
		completionService.submit(new FileParsingTask(file));
	}
	
	
	
	private void shutDownThreadPool(){
		pool.shutdown(); // Disable new tasks from being submitted
		   try {
		     // Wait a while for existing tasks to terminate
		     if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
		       pool.shutdownNow(); // Cancel currently executing tasks
		       // Wait a while for tasks to respond to being cancelled
		       if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				System.err.println("Pool did not terminate");
			}
		     }
		   } catch (InterruptedException ie) {
		     // (Re-)Cancel if current thread also interrupted
		     pool.shutdownNow();
		     // Preserve interrupt status
		     Thread.currentThread().interrupt();
		   }
	}
	
}

class FileParsingTask implements Callable<List<CMSEntityRecord>>{
	
	private static final String ARROW = "->";
	
	private static final String ASSIGNMENT = "\\s*=\\s*.+;";
	private static final String PUBLIC_MODIFIER = "public\\s+[a-zA-Z_0-9\\.]+\\s+";
	private static final String STAR = "\\*\\s";
	private static final Pattern attributePattern = Pattern.compile(PUBLIC_MODIFIER + "\\w+" + ASSIGNMENT);
	private static final Pattern domainPattern = Pattern.compile(STAR + "([A-Z_0-9]+(_)?)+(\\s)" );
	
	
	final File file;
	public FileParsingTask(File file){
		this.file = file;
	}
	@Override
	public List<CMSEntityRecord> call() throws Exception {
		final String entityName = file.getName().replaceFirst("Dtls\\.java", "");
		final BufferedReader br = new BufferedReader(new FileReader(file));
		final List<CMSEntityRecord> entities =  new ArrayList<CMSEntityRecord>();
		CMSEntityRecord cmsEntity = new CMSEntityRecord();
		String line = "";
		while( ( line = br.readLine()) != null ){
			findPattern(line, cmsEntity);
			if(!(cmsEntity.getDomain().isEmpty() || cmsEntity.getAttribute().isEmpty())){
				entities.add(cmsEntity.name(entityName));
				cmsEntity = new CMSEntityRecord();//reset
			}
		}
		
		return entities;
	}
	
	private CMSEntityRecord findPattern(String line, CMSEntityRecord cmsEntity) {
		String domain = "";
		String attribute = "";
		if((domain = getDomainDefinition(line)).length()>0){
			cmsEntity.domain(domain.replaceFirst(STAR, "").trim());
		}else if((attribute = getAttribute(line)).length()>0){
			cmsEntity.attribute(attribute.replaceFirst(PUBLIC_MODIFIER, "").replaceFirst(ASSIGNMENT, ""));
		}
		
		return cmsEntity;
	}

	private String getAttribute(String line) {
		Matcher matcher = attributePattern.matcher(line);
		while(matcher.find()){     
			return matcher.group();
		}
		return "";
		
	}

	private String getDomainDefinition(String line) {
		if(line.matches("\\s+\\*\\s" + ARROW+".+")){
			return "";
		}
		Matcher matcher = domainPattern.matcher(line);
		while(matcher.find()){    
			return matcher.group();
		}
		return "";
		
	}
}
