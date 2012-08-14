/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class BasicServletConfig implements ServletConfig {
	private String name;
	private ServletContext context;
	private Hashtable<String, String> params;

	public BasicServletConfig(String name, ServletContext context) {
		this.name = name;
		this.context = context;
		params = new Hashtable<String, String>();
	}

	@SuppressWarnings("unchecked")
	public BasicServletConfig(String name, ServletConfig config) {
		this(name, config.getServletContext());
		Enumeration<String> e = config.getInitParameterNames();
		while (e.hasMoreElements()) {
			String param = e.nextElement();
			params.put(param, config.getInitParameter(param));
		}
	}

	public BasicServletConfig(String name, ServletConfig config,
			Map<String, String> params) {
		this(name, config);
		this.params.putAll(params);
	}

	public BasicServletConfig(String name, ServletContext context,
			Map<String, String> params) {
		this.name = name;
		this.context = context;
		this.params = new Hashtable<String, String>(params);
	}

	public String getServletName() {
		return name;
	}

	public ServletContext getServletContext() {
		return context;
	}

	public Enumeration<String> getInitParameterNames() {
		return params.keys();
	}

	public String getInitParameter(String name) {
		return params.get(name);
	}

}
