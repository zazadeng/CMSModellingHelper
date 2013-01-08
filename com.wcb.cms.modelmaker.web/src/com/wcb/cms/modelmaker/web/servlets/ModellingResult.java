package com.wcb.cms.modelmaker.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wcb.cms.modelmaker.api.AppInterface;
import com.wcb.cms.modelmaker.api.CMSRoseModellingResult;
import com.wcb.cms.modelmaker.api.ErrorMessages;

@WebServlet(
		// servlet name
		name = "ModellingResult",
		// servlet url pattern
		urlPatterns = {"/modelmaker/ModellingResult"},
		// async support needed
		asyncSupported = true
		// servlet init params
		//CANT SET THE FOLLOWING, error on genorimo server
		/*initParams = {
				@WebInitParam(name = "ThreadPoolSize", value = "3")
		}*/
		)
public class ModellingResult extends HttpServlet {

	private static final int TREAD_POOL_SIZE = 3;
	private static final String LOCAL_HOST = "localhost";//Persistent address
	private static final String SQL = "sql";//a POST parameter and should be mapped with the client
	private static final long serialVersionUID = 1L;
	private AppInterface application;
	private ExecutorService exec;

	@Override
	public void destroy() {
		exec.shutdown();
	}

	@Override
	protected void doPost( HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		/*HttpSession session = request.getSession(true);
		//The session is only valid for 5 seconds
		session.setMaxInactiveInterval(5);
		if (session.isNew()) {
			//do the work
		}else{
			//same client within the given 5 seconds.
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><h1>Take it easy!</h1></html>");
			out.flush();
			out.close();
			return;
		}*/
		request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);//why mention it again while you have that in the annotation... the container sometimes didn't pickup this meta-programming trick.
		response.setContentType("application/json");
		final AsyncContext async = request.startAsync(request, response);

		/*
		 * The purpose of this Runnable is to quickly
		 * release the request object for a NEW request object to come in
		 */
		exec.execute(new Runnable() {

			@Override
			public void run() {
				async.setTimeout(2000);//2s

				async.addListener(new AsyncListener() {
					@Override
					public void onComplete(AsyncEvent arg0) throws IOException {}

					@Override
					public void onError(AsyncEvent arg0) throws IOException {}

					@Override
					public void onStartAsync(AsyncEvent arg0) throws IOException {}

					@Override
					public void onTimeout(AsyncEvent event) throws IOException {
						async.getResponse().getWriter().println("TIME_OUT on SQL: "
								+ event.getSuppliedRequest().getParameter(SQL));
						async.complete();//INFORM the client
					}
				});
				try {
					System.out.println(async.getRequest().getRemoteAddr()+ "========>>>");
					CMSRoseModellingResult result = application
							.transformSelectQuery(async.getRequest().getParameter(SQL), LOCAL_HOST);
					PrintWriter out = async.getResponse().getWriter();
					/*
					 * ONLY sends the data down to the client
					 */
					out.println("{");
					out.print("\"sql\":\"");
					out.print(result.getCuramNonStandardSelectQuery());
					out.println("\",");
					out.print("\"input\":\"");
					out.print(parseStructMap(result.getInputStruct()));
					out.println("\",");
					out.print("\"output\":\"");
					out.print(parseStructMap(result.getOutputStruct()));
					out.println("\"");
					out.println("}");
					System.out.println(async.getRequest().getRemoteAddr() + "<<<========");
				} catch (Exception e) {
					try{
						try{
							async.getResponse().getWriter().println(e.getMessage());
						}catch(NullPointerException ee){
							//NO ERROR message
							//TODO: file this error for analysis. Better make it async...
							async.getResponse().getWriter().println("UNKNOWN ERROR ...");
							e.printStackTrace();
						}
					}catch(IOException ioe){
						//Response or Writer is null ... ?
						e.printStackTrace();
					}
				}finally{
					async.complete();//INFORM the client
				}


			}

		});

		/*
		 * Sequential implementation
		 */
		/*
		String selectQuery = request.getParameter(SQL);
		System.out.println(request.getRemoteAddr() + "=========>>>"+selectQuery);
		String filePath =
				LOCAL_HOST; //for redis, TODO get this from the client

		PrintWriter out = response.getWriter();
		try {
			CMSRoseModellingResult result = application.transformSelectQuery(selectQuery, filePath);
			String dynamicQueryStr = result.getCuramNonStandardSelectQuery();
			String inputStructStr =  parseStructMap(result.getInputStruct());
			String outputStructStr = parseStructMap(result.getOutputStruct());

			//ONLY sends the data down to the client
			out.println("{");
			out.print("\"sql\":\"");
			out.print(dynamicQueryStr);
			out.println("\",");
			out.print("\"input\":\"");
			out.print(inputStructStr);
			out.println("\",");
			out.print("\"output\":\"");
			out.print(outputStructStr);
			out.println("\"");
			out.println("}");

			System.out.println(request.getRemoteAddr() + "<<<=========");
		} catch (Exception e) {
			try{
				if(selectQuery.trim().length() == 0){
					out.println("Error: no sql enter!");
				}else{
					out.println(e.getMessage());
					e.printStackTrace();
				}
			}catch(NullPointerException ee){
				//NO ERROR message
				//TODO: file this error for analysis. Better make it async...
				out.println("UNKNOWN ERROR ...");
				e.printStackTrace();
			}
		}finally{
			out.flush();
			out.close();
		}
		 */

	}
	@Override
	public void init() throws ServletException {

		try {
			InitialContext ctx = new InitialContext();
			String jndiName = "osgi:service/"+AppInterface.class.getName();
			application = (AppInterface) ctx.lookup(jndiName);
			exec = Executors.newFixedThreadPool(TREAD_POOL_SIZE);
		} catch (NamingException e) {
			getServletContext().log("Can't find OSGi Service: ", e);
			throw new ServletException(ErrorMessages
					.error3(AppInterface.class.getName(), e.getExplanation()));
		}
	}

	private synchronized String parseStructMap(Map<String, String> aMap) {
		//final String endOfLineStr = "\r\n";
		final String endOfLineStr = "<br />";
		return aMap.toString().replace("{", "").replace("}", "").replaceAll(",", endOfLineStr).replaceAll("=", ":");
	}


}

