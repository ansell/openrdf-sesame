/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
		return new String[] { "limit", "queryLn", "infer", "Accept", "Content-Type" };
	}

	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		TupleResultBuilder builder = getTupleResultBuilder(req, resp);
		builder.start("id", "description", "location", "server", "readable", "writeable", "default-limit",
				"default-queryLn", "default-infer", "default-Accept", "default-Content-Type", "upload-format",
				"query-format", "download-format");
		String id = info.getId();
		String desc = info.getDescription();
		URL loc = info.getLocation();
		URL server = getServer();
		builder.result(id, desc, loc, server, info.isReadable(), info.isWritable(), req.getParameter("limit"),
				req.getParameter("queryLn"), req.getParameter("infer"), req.getParameter("Accept"),
				req.getParameter("Content-Type"));
		// builder.binding("default-limit", req.getParameter("limit"));
		// builder.binding("default-queryLn", req.getParameter("queryLn"));
		// builder.binding("default-infer", req.getParameter("infer"));
		// builder.binding("default-Accept", req.getParameter("Accept"));
		// builder.binding("default-Content-Type",
		// req.getParameter("Content-Type"));
		for (RDFParserFactory parser : RDFParserRegistry.getInstance().getAll()) {
			String mimeType = parser.getRDFFormat().getDefaultMIMEType();
			String name = parser.getRDFFormat().getName();
			// TODO: How does this map to QueryResultHandler.handleSolution?
			builder.binding("upload-format", mimeType + " " + name);
		}
		for (QueryParserFactory factory : getInstance().getAll()) {
			String name = factory.getQueryLanguage().getName();
			// TODO: How does this map to QueryResultHandler.handleSolution?
			builder.binding("query-format", name + " " + name);
		}
		for (RDFWriterFactory writer : RDFWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getRDFFormat().getDefaultMIMEType();
			String name = writer.getRDFFormat().getName();
			// TODO: How does this map to QueryResultHandler.handleSolution?
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
