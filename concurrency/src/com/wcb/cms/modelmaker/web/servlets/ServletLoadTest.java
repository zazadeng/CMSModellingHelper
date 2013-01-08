package com.wcb.cms.modelmaker.web.servlets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

/**
 * spawns maxThreadCount of treads(use httpcomponents-client-4.2.2 to create httpclient) and run them as the same time to check concurrency issues...
 * NOTE:Run this as a "java application"...
 * 
 */
public class ServletLoadTest {

	public static final AtomicInteger counter = new AtomicInteger(0);

	public static final int maxThreadCount = 100; //CHANGE ME AS NEEDED
	public static void main(String[] arg) throws InterruptedException{
		new ServletLoadTest();
	}


	final String url = "http://localhost:8080/modelmaker/ModellingResult";//CHANGE ME AS NEEDED
	public ServletLoadTest() throws InterruptedException {


		ExecutorService executor = Executors.newCachedThreadPool();

		for (int i = 0; i < maxThreadCount; i++) {

			//make maxThreadCount time. i.e., feeds all these to "futures"
			executor.submit(new Runnable() {

				@Override
				public void run() {

					int count = counter.addAndGet(1);
					try {

						String actual = Request.Post(url)
								.bodyForm(Form.form().add("sql", "select claimid from claimcycle where claimcycleid=:cID").build())
								.execute().returnContent().asString();

						if((actual == null)|actual.isEmpty()){
							System.out.println(">***********request<" + count + ">");
							System.err.println("FAIL **************<");
						}


					}
					catch (Exception e) {
						System.out.println(">***********request<" + count + ">");
						System.err.println("EXCEPTION: "+e.getMessage());
					}

				}
			});
		}

		executor.shutdown();//run the "futures"
	}

}
