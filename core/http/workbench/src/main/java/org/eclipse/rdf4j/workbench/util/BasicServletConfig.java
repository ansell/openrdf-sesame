/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.workbench.util;

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
