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
package org.openrdf.workbench.commands;

import static org.openrdf.rio.RDFWriterRegistry.getInstance;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.Rio;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class ExportServlet extends TupleServlet {

	public ExportServlet() {
		super("export.xsl", "subject", "predicate", "object", "context");
	}

	@Override
	public String[] getCookieNames() {
		return new String[] { ExploreServlet.LIMIT, "Accept" };
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		if (req.isParameterPresent("Accept")) {
			String accept = req.getParameter("Accept");
			RDFFormat format = Rio.getWriterFormatForMIMEType(accept).orElseThrow(Rio.unsupportedFormat(accept));
			resp.setContentType(accept);
			String ext = format.getDefaultFileExtension();
			String attachment = "attachment; filename=export." + ext;
			resp.setHeader("Content-disposition", attachment);
			RepositoryConnection con = repository.getConnection();
			con.setParserConfig(NON_VERIFYING_PARSER_CONFIG);
			try {
				RDFWriterFactory factory = getInstance().get(format).orElseThrow(Rio.unsupportedFormat(format));
				if (format.getCharset() != null) {
					resp.setCharacterEncoding(format.getCharset().name());
				}
				con.export(factory.getWriter(resp.getOutputStream()));
			}
			finally {
				con.close();
			}
		}
		else {
			super.service(req, resp, xslPath);
		}
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp, TupleResultBuilder builder,
			RepositoryConnection con)
		throws Exception
	{
		int limit = ExploreServlet.LIMIT_DEFAULT;
		if (req.getInt(ExploreServlet.LIMIT) > 0) {
			limit = req.getInt(ExploreServlet.LIMIT);
		}
		RepositoryResult<Statement> result = con.getStatements(null, null, null, false);
		try {
			for (int i = 0; result.hasNext() && (i < limit || limit < 1); i++) {
				Statement st = result.next();
				builder.result(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
		finally {
			result.close();
		}
	}

}