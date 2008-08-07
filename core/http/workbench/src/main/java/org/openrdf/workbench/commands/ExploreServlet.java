package org.openrdf.workbench.commands;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExploreServlet extends TupleServlet {
	private Logger logger = LoggerFactory.getLogger(ExploreServlet.class);

	public ExploreServlet() {
		super("explore.xsl", "subject", "predicate", "object", "context");
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
		if (value instanceof Resource) {
			export(con, builder, (Resource) value, null, null);
		}
		if (value instanceof URI) {
			export(con, builder, null, (URI) value, null);
		}
		export(con, builder, null, null, value);
		if (value instanceof Resource) {
			export(con, builder, null, null, null, (Resource) value);
		}
	}

	private void export(RepositoryConnection con, TupleResultBuilder builder,
			Resource subj, URI pred, Value obj, Resource... ctx)
			throws RepositoryException {
		RepositoryResult<Statement> result = con.getStatements(subj, pred, obj,
				true, ctx);
		try {
			while (result.hasNext()) {
				Statement st = result.next();
				builder.result(st.getSubject(), st.getPredicate(), st
						.getObject(), st.getContext());
			}
		} finally {
			result.close();
		}
	}

}