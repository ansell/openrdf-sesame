/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.workbench;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import info.aduna.xml.XMLUtil;

/**
 * @author James Leigh
 */
public class TupleXMLResultView implements View {

	public static final String XSLT = "transformation";

	public String getContentType() {
		return "application/xml";
	}

	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		printXMLResult(model, response.getWriter());
	}

	private void printXMLResult(Map<String, String> map, PrintWriter out) {
		out.println("<?xml version='1.0' encoding='utf8'?>");
		out.print("<?xml-stylesheet type='text/xsl' href='");
		out.print(map.get(XSLT));
		out.println("'?>");
		out.print("<sparql xmlns='");
		out.print("http://www.w3.org/2005/sparql-results#");
		out.println("'>");
		out.println("<head>");
		for (String name : map.keySet()) {
			out.print("<variable name='");
			out.print(XMLUtil.escapeSingleQuotedAttValue(name));
			out.println("'/>");
		}
		out.println("</head>");
		out.println("<results>");
		out.println("<result>");
		for (String name : map.keySet()) {
			String value = map.get(name);
			out.print("<binding name='");
			out.print(XMLUtil.escapeSingleQuotedAttValue(name));
			out.println("'>");
			out.print("<literal>");
			out.print(XMLUtil.escapeText(value));
			out.println("</literal>");
			out.println("</binding>");
		}
		out.println("</result>");
		out.println("</results>");
		out.println("</sparql>");
		out.close();
	}

}
