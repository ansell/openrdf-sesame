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
package org.eclipse.rdf4j.workbench.commands;

import static org.eclipse.rdf4j.query.parser.QueryParserRegistry.getInstance;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.query.parser.QueryParserFactory;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterRegistry;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterRegistry;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.rio.RDFParserFactory;
import org.eclipse.rdf4j.rio.RDFParserRegistry;
import org.eclipse.rdf4j.rio.RDFWriterFactory;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.workbench.base.TransformationServlet;
import org.eclipse.rdf4j.workbench.util.TupleResultBuilder;
import org.eclipse.rdf4j.workbench.util.WorkbenchRequest;

public class InfoServlet extends TransformationServlet {

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "queryLn", "infer", "Accept", "Content-Type" };
	}

	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		String id = info.getId();
		
		// "Caching" of servlet instances can cause this request to succeed even
		// if the repository has been deleted. Client-side code using InfoServlet
		// for repository existential checks expects an error response when the id 
		// no longer exists. 
		if (null != id && !manager.hasRepositoryConfig(id)){
			throw new RepositoryConfigException(id + " does not exist.");
		}
		TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
		builder.start("id", "description", "location", "server", "readable", "writeable", "default-limit",
				"default-queryLn", "default-infer", "default-Accept", "default-Content-Type", "upload-format",
				"query-format", "graph-download-format", "tuple-download-format", "boolean-download-format");
		String desc = info.getDescription();
		URL loc = info.getLocation();
		URL server = getServer();
		builder.result(id, desc, loc, server, info.isReadable(), info.isWritable());
		builder.namedResult("default-limit", req.getParameter("limit"));
		builder.namedResult("default-queryLn", req.getParameter("queryLn"));
		builder.namedResult("default-infer", req.getParameter("infer"));
		builder.namedResult("default-Accept", req.getParameter("Accept"));
		builder.namedResult("default-Content-Type", req.getParameter("Content-Type"));
		for (RDFParserFactory parser : RDFParserRegistry.getInstance().getAll()) {
			String mimeType = parser.getRDFFormat().getDefaultMIMEType();
			String name = parser.getRDFFormat().getName();
			builder.namedResult("upload-format", mimeType + " " + name);
		}
		for (QueryParserFactory factory : getInstance().getAll()) {
			String name = factory.getQueryLanguage().getName();
			builder.namedResult("query-format", name + " " + name);
		}
		for (RDFWriterFactory writer : RDFWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getRDFFormat().getDefaultMIMEType();
			String name = writer.getRDFFormat().getName();
			builder.namedResult("graph-download-format", mimeType + " " + name);
		}
		for (TupleQueryResultWriterFactory writer : TupleQueryResultWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getTupleQueryResultFormat().getDefaultMIMEType();
			String name = writer.getTupleQueryResultFormat().getName();
			builder.namedResult("tuple-download-format", mimeType + " " + name);
		}
		for (BooleanQueryResultWriterFactory writer : BooleanQueryResultWriterRegistry.getInstance().getAll()) {
			String mimeType = writer.getBooleanQueryResultFormat().getDefaultMIMEType();
			String name = writer.getBooleanQueryResultFormat().getName();
			builder.namedResult("boolean-download-format", mimeType + " " + name);
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
