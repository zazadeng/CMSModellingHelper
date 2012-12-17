CMSModellingHelper
==================

a web app (java OSGi servlet) to help reducing WCB CMS modelling effert

Libs that we need(I use) to satisfy the complier
================================================
ANTLR(antlr-runtime-3.1.3.jar) -> a tool to generate the java lexer.
SqlJet(sqljet-1.0.7.jar) -> a pure java client for Sqlite.
WTP(Eclipse Web Tools Platform) -> there are bundles(specified in the bundle's manifest file) for parsing sql queries.

Steps we need(I use) to get the web service avaiable
====================================================
1.	a generated sqlite file through App.createDB;
2.	get the web server geronimo(geronimo-tomcat7-javaee6-3.0.0) runnnig and deploy this OSGi bundle in; for development, download the OSGi Application Developement Tools though Eclipse; it will help making the bundle and deoploy the bundle to geronimo automatically.
3. 	localhost:8080 will give us the server; 
	http://localhost:8080/modelmaker/ will give us this web service.

TODOs
=====
-- a dart client implementation will be used instead of the boring jsp page.
-- a dart implementation will be used to generate a redis-script file; of course for the database, we will be using Redis instead of Sqlite.
-- make the servlet async.