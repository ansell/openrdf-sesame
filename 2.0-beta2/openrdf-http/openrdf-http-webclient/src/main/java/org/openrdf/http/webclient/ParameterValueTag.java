package org.openrdf.http.webclient;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

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
			out.write(Protocol.encodeParameter(name, value));
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
