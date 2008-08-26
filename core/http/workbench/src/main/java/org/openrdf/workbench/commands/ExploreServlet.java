package org.openrdf.workbench.commands;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class ExploreServlet extends TupleServlet {
	private Logger logger = LoggerFactory.getLogger(ExploreServlet.class);

	public ExploreServlet() {
		super("explore.xsl", "subject", "predicate", "object", "context");
	}

	@Override
	public String[] getCookieNames() {
		return new String[]{"limit"};
	}

	@Override
	public void service(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception {
		try {
			super.service(req, resp, xslPath);
		} catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			PrintWriter out = resp.getWriter();
			TupleResultBuilder builder = new TupleResultBuilder(out);
			builder.transform(xslPath, "explore.xsl");
			builder.start("error-message");
			builder.link("info");
			builder.result(exc.getMessage());
			builder.end();
		}
	}

	@Override
	protected void service(WorkbenchRequest req, TupleResultBuilder builder,
			RepositoryConnection con) throws Exception {
		Value value = req.getValue("resource");
		int limit = req.getInt("limit");
		if (value instanceof Resource) {
			limit -= export(con, builder, limit, (Resource) value, null, null);
		}
		if (value instanceof URI) {
			limit -= export(con, builder, limit, null, (URI) value, null);
		}
		if (value != null) {
			limit -= export(con, builder, limit, null, null, value);
		}
		if (value instanceof Resource) {
			limit -= export(con, builder, limit, null, null, null, (Resource) value);
		}
	}

	private int export(RepositoryConnection con, TupleResultBuilder builder, int limit,
			Resource subj, URI pred, Value obj, Resource... ctx)
			throws RepositoryException {
		RepositoryResult<Statement> result = con.getStatements(subj, pred, obj,
				true, ctx);
		try {
			int count;
			for (count = 0; result.hasNext() && count < limit; count++) {
				Statement st = result.next();
				builder.result(st.getSubject(), st.getPredicate(), st
						.getObject(), st.getContext());
			}
			return count;
		} finally {
			result.close();
		}
	}

}