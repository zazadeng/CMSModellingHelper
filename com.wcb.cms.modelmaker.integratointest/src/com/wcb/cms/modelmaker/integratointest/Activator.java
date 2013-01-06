package com.wcb.cms.modelmaker.integratointest;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import test.IntegrationTest;
public class Activator implements BundleActivator {

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Integration testing >>>>>>>> STARTED");
		Result testResult = org.junit.runner.JUnitCore.runClasses(IntegrationTest.class);
		if(testResult.wasSuccessful()){
			System.out.println("Integration testing >>>>>>>> SUCESSED");
		}
		for (Failure failure : testResult.getFailures()) {
			System.err.println(failure.getTrace().
					replaceFirst("test.IntegrationTest.runCase",
							"**********test.IntegrationTest***.runCase"));
		}
	}



	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
