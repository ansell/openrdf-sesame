/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import static org.openrdf.rio.RDFWriterRegistry.getInstance;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class ExportServlet extends TupleServlet {

	private static final int LIMIT_DEFAULT = 100;

	public ExportServlet() {
		super("export.xsl", "subject", "predicate", "object", "context");
	}

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit" };
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		Map<String, String> parameters = req.getSingleParameterMap();
		if (parameters.containsKey("Accept")) {
			String accept = parameters.get("Accept");
			RDFFormat format = RDFFormat.forMIMEType(accept);
			if (format != null) {
				resp.setContentType(accept);
				String ext = format.getDefaultFileExtension();
				String attachment = "attachment; filename=export." + ext;
				resp.setHeader("Content-disposition", attachment);
			}
			RepositoryConnection con = repository.getConnection();
			try {
				RDFWriterFactory factory = getInstance().get(format);
				con.export(factory.getWriter(resp.getWriter()));
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
	protected void service(WorkbenchRequest req, TupleResultBuilder builder, RepositoryConnection con)
		throws Exception
	{
		int limit = LIMIT_DEFAULT;
		if (req.getInt("limit") > 0) {
			limit = req.getInt("limit");
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