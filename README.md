CMSModellingHelper
==================

a web app (java OSGi servlet) to help easing WCB CMS development modelling part effert

##Libs that we need(I use) to keep the complier happy

1. We need Eclipse to contain [WTP](http://www.eclipse.org/webtools)(Eclipse Web Tools Platform), there are bundles ((DTP)[http://www.eclipse.org/datatools]) for parsing sql queries; if we download the FOR JAVE EE DEVELOPER distribution will include needed bundles. 

2. Download [ANTLR](www.antlr.org)(antlr-runtime-3.1.3.jar); put this jar under com.wcb.cms.modelmaker.brain/BundleContent and reflesh this project, right click the jar and do "Build Path -> Add to Build Path". Put this jar in com.wcb.cms.modelmaker.persistent.sqlite/BundleContent, and do the same thing as mentioned before to add this jar into the build path of the project.

3. Download [SqlJet](sqljet.com)(sqljet-1.0.7.jar); put this jar under com.wcb.cms.modelmaker.persistent.sqlite/BundleContent and do the same thing as mentioned before to add this jar into the build path of the project.

4. Download [geronimo](http://geronimo.apache.org)(geronimo-tomcat7-javaee6-3.0.0), remember this location, it will be needed for the coming step.

5. In Eclipse, go to "Preferences -> Server -> Runtime Environment", click "Add...", in the dialog, click "Download additional server adapters" link; pick "geronimo V3.0 Server Adapter" to install; after installation, we will be prompted to restart Eclipse; go back to "Preferences -> Server -> Runtime Environment", click "Apache Geronimo V3.0", Browse to the location in the previous step.

6. To run the Eclipse plug-ins Junit tests, we need to set target platform, go to "Preferences -> Plug-in Development ->Target Platform", we will have a target definitions called "Apache Geronimo 3.0", please fix these broken locations to the server location mentioned before, and set this as the default. NOW we have more errors in Eclipse, since all the needed bundles loaded in Eclipse are not avaiable now. I put all of the following bundles(ignore those numbers in the names) from the local Eclipse installation(the one containing WTP bundles) to the hotbundles folder in Apache Geronimo Server directory to get the app up and running:

	com.ibm.icu_4.0.1.v20090822.jar
	javax.wsdl_1.5.1.v200806030408.jar
	javax.xml_1.3.4.v200902170245.jar
	net.sourceforge.lpg.lpgjavaruntime_1.1.0.v201004271650.jar
	org.apache.xml.resolver_1.2.0.v200902170519.jar
	org.apache.xml.serializer_2.7.1.v200902170519.jar
	org.eclipse.core.contenttype_3.4.1.R35x_v20090826-0451.jar
	org.eclipse.core.jobs_3.4.100.v20090429-1800.jar
	org.eclipse.core.runtime_3.5.0.v20090525.jar
	org.eclipse.datatools.common.doc.user_1.7.0.20090521092446.jar
	org.eclipse.datatools.doc.user_1.7.0.20090521092446.jar
	org.eclipse.datatools.intro_1.7.0.v200906031101.jar
	org.eclipse.datatools.modelbase.dbdefinition.source_1.0.1.v200906022249.jar
	org.eclipse.datatools.modelbase.dbdefinition_1.0.1.v200906022249.jar
	org.eclipse.datatools.modelbase.derby.source_1.0.0.v200906020900.jar
	org.eclipse.datatools.modelbase.derby_1.0.0.v200906020900.jar
	org.eclipse.datatools.modelbase.sql.query.source_1.0.2.v201002020730.jar
	org.eclipse.datatools.modelbase.sql.query_1.0.2.v201002020730.jar
	org.eclipse.datatools.modelbase.sql.source_1.0.3.v200912150851.jar
	org.eclipse.datatools.modelbase.sql.xml.query.source_1.0.0.v200906022249.jar
	org.eclipse.datatools.modelbase.sql.xml.query_1.0.0.v200906022249.jar
	org.eclipse.datatools.modelbase.sql_1.0.3.v200912150851.jar
	org.eclipse.datatools.sqltools.common.ui.source_1.0.0.v200906022302.jar
	org.eclipse.datatools.sqltools.data.ui.source_1.1.2.v200912080845.jar
	org.eclipse.datatools.sqltools.ddlgen.ui.source_1.0.0.v200906022302.jar
	org.eclipse.datatools.sqltools.doc.user.contexts_1.7.0.20090521092446.jar
	org.eclipse.datatools.sqltools.doc.user_1.7.0.20090521092446.jar
	org.eclipse.datatools.sqltools.parsers.sql.lexer.source_1.0.1.v200906030654.jar
	org.eclipse.datatools.sqltools.parsers.sql.lexer_1.0.1.v200906030654.jar
	org.eclipse.datatools.sqltools.parsers.sql.query.source_1.0.1.v200906022302.jar
	org.eclipse.datatools.sqltools.parsers.sql.query_1.0.1.v200906022302.jar
	org.eclipse.datatools.sqltools.parsers.sql.source_1.0.1.v200906022302.jar
	org.eclipse.datatools.sqltools.parsers.sql.xml.query.source_1.0.0.v200906022302.jar
	org.eclipse.datatools.sqltools.parsers.sql.xml.query_1.0.0.v200906022302.jar
	org.eclipse.datatools.sqltools.parsers.sql_1.0.1.v200906022302.jar
	org.eclipse.datatools.sqltools.sql.source_1.0.0.v200906022302.jar
	org.eclipse.datatools.sqltools.sql.ui.source_1.0.0.v200906022302.jar
	org.eclipse.emf.common_2.5.0.v200906151043.jar
	org.eclipse.emf.ecore_2.5.0.v200906151043.jar
	org.eclipse.equinox.app_1.2.1.R35x_v20091203.jar
	org.eclipse.equinox.common_3.5.1.R35x_v20090807-1100.jar
	org.eclipse.equinox.preferences_3.2.301.R35x_v20091117.jar
	org.eclipse.equinox.registry_3.4.100.v20090520-1800.jar
	org.eclipse.persistence.jpa.equinox.weaving_1.1.3.v20091002-r5404.jar
	org.hamcrest.core_1.1.0.v20090501071000.jar
	org.junit4_4.8.1.v20100525
	org.junit_4.8.2.v4_8_2_v20110321-1705

##Steps we need(I use) to get the web service avaiable

1.	a generated sqlite file through createDB method in App.java;
2.	get the web server geronimo(geronimo-tomcat7-javaee6-3.0.0) runnnig and deploy this OSGi bundle in; for development, download the OSGi Application Developement Tools though Eclipse; it will help making the bundle and deoploy the bundle to geronimo automatically.
3. 	localhost:8080 will give us the server; or http://localhost:8080/modelmaker/ will give us this web service.

##TODOs

- a dart client implementation will be used instead of the boring jsp page.
- a dart implementation will be used to generate a redis-script file; of course for the database, we will be using Redis instead of Sqlite, so the dependency on **ANTLR** and **SqlJet** will be a history.
- make the servlet async.