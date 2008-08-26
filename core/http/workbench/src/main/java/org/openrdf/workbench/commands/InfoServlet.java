/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import static org.openrdf.query.parser.QueryParserRegistry.getInstance;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class InfoServlet extends TransformationServlet {

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit" };
	}

	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		resp.setContentType("application/xml");
		PrintWriter out = resp.getWriter();
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.start("id", "description", "location", "server", "readable", "writeable", "limit",
				"upload-format", "query-format", "download-format");
		String id = info.getId();
		String desc = info.getDescription();
		URL loc = info.getLocation();
		URL server = getServer();
		builder.result(id, desc, loc, server, info.isReadable(), info.isWritable());
		builder.binding("limit", req.getParameter("limit"));
		for (RDFParserFactory parser : RDFParserRegistry.getInstance().getAll()) {
			String mimeType = parser.getRDFFormat().getDefaultMIMEType();
			String name = parser.getRDFFormat().getName();
			builder.binding("upload-format", mimeType + " " + name);
		}
		for (QueryParserFactory factory : getInstance().getAll()) {
			String name = factory.getQueryLanguage().getName();
			builder.binding("query-format", name + " " + name);
		}
		for (RDFWriterFactory writer : RDFWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getRDFFormat().getDefaultMIMEType();
			String name = writer.getRDFFormat().getName();
			builder.binding("download-format", mimeType + " " + name);
		}
		builder.end();
	}

	private URL getServer() {
		try {
			return manager.getLocation();
		}
		catch (MalformedURLException exc) {
			return null;
		}
	}

}
