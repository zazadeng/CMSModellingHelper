package com.wcb.cms.modelmaker.integratointest;

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
		System.out.println("Integration TESTING >>>>>>>>");
		new IntegrationTest("localhost");
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
	}

}
