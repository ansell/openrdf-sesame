/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import info.aduna.net.http.HttpClientUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Value;

public class ParameterValueTag extends TagSupport {

	private static final long serialVersionUID = -1315546760001420840L;

	private String name;

	private Value value;

	@Override
	public int doStartTag()
		throws JspException
	{
		// write the result to the page
		JspWriter out = pageContext.getOut();
		try {
			String encValue = Protocol.encodeValue(value);
			out.write(HttpClientUtil.encodeParameter(name, encValue));
		}
		catch (IOException e) {
			throw new JspException(e);
		}
		return SKIP_BODY;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Value value) {
		this.value = value;
	}
}
