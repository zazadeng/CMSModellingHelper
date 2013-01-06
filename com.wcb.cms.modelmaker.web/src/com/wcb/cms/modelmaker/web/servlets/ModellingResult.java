package com.wcb.cms.modelmaker.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wcb.cms.modelmaker.api.AppInterface;
import com.wcb.cms.modelmaker.api.CMSRoseModellingResult;
import com.wcb.cms.modelmaker.api.ErrorMessages;




public class ModellingResult extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private AppInterface application;
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		//response.setContentType("text/plain");
		response.setContentType("application/json");
		//AsyncContext async = request.startAsync(request, response);
		//System.out.println(request.getParameterMap());
		PrintWriter out = response.getWriter();
		String selectQuery = request.getParameter("sql");

		System.out.println("=========>:"+selectQuery);




		String filePath =
				//		getServletContext().getInitParameter("Sqlite-File");
				"localhost"; //for redis

		try {
			CMSRoseModellingResult result = application.transformSelectQuery(selectQuery, filePath);
			String dynamicQueryStr = result.getCuramNonStandardSelectQuery();
			String inputStructStr =  parseStructMap(result.getInputStruct());
			String outputStructStr = parseStructMap(result.getOutputStruct());

			/* pass this job(representation of the data) to the client
			out.println("<HTML>");
			out.println("<BODY>");
			out.println("<H1>Transformed SQL</H1> <table border=\"1\"><tr><td>"+ dynamicQueryStr + "</td></tr></table>");

			out.println("<br /><H1>Input Struct</H1> <table border=\"1\"><tr><td>" + inputStructStr + "</td></tr></table>");

			out.println("<br /><H1>Output Struct</H1><table border=\"1\"><tr><td>"+ outputStructStr + "</td></tr></table>");
			out.println("</BODY>");
			out.println("</HTML>");*/

			/*
			 * ONLY sends the data down to the client
			 */
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

			//What the hell are u trying to do here?
			/*StringWriter stringWriter = new StringWriter();
			PrintWriter pw = new PrintWriter(stringWriter);
			pw.println("{");
			pw.print("\"sql\":\"");
			pw.print(dynamicQueryStr);
			pw.println("\",");
			pw.print("\"input\":\"");
			pw.print(inputStructStr);
			pw.println("\",");
			pw.print("\"output\":\"");
			pw.print(outputStructStr);
			pw.println("\"");
			pw.println("}");*/

			System.out.println("=========<:"+out);
			/*out.print(stringWriter);
			pw.close();*/
		} catch (Exception e) {
			if((selectQuery != null )&&(selectQuery.trim().length() == 0)){
				out.println("Error: no sql enter!");
			}else if((e.getMessage() !=null) && e.getMessage().contains("Unable to parse the input")){
				out.println("Invalid Query! Please enter a valid select query.");
				//TODO: save the error sql to a file for analysis. Better make it async...
			}else{
				out.println("ERROR!!");
				e.printStackTrace();
				//TODO: save the error sql and ERROR LOG to a file for analysis. Better make it async...
			}
		}finally{

			out.flush();

		}



		/*HttpSession session = request.getSession(true);
		// Set the session valid for 5 secs
		session.setMaxInactiveInterval(5);


		if (session.isNew()) {
			//do the work
		}*/
	}

	@Override
	public void init() throws ServletException {

		try {
			InitialContext ctx = new InitialContext();
			String jndiName = "osgi:service/"+AppInterface.class.getName();
			application = (AppInterface) ctx.lookup(jndiName);
		} catch (NamingException e) {
			getServletContext().log("Can't find OSGi Service: ", e);
			throw new ServletException(ErrorMessages
					.error3(AppInterface.class.getName(), e.getExplanation()));
		}
	}

	private String parseStructMap(Map<String, String> inputStructForRose) {

		String result = inputStructForRose.toString();
		//final String endOfLineStr = "\r\n";
		final String endOfLineStr = "<br />";
		return result.replace("{", "").replace("}", "").replaceAll(",", endOfLineStr).replaceAll("=", ":");

	}
}

