package org.openrdf.workbench.commands;

import static org.openrdf.rio.RDFWriterRegistry.getInstance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryServlet extends TransformationServlet {
	private Logger logger = LoggerFactory.getLogger(QueryServlet.class);

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception, IOException {
		Map<String, String> parameters = req.getSingleParameterMap();
		if (parameters.containsKey("Accept")) {
			String accept = parameters.get("Accept");
			RDFFormat format = RDFFormat.forMIMEType(accept);
			if (format != null) {
				resp.setContentType(accept);
				String ext = format.getDefaultFileExtension();
				String attachment = "attachment; filename=query." + ext;
				resp.setHeader("Content-disposition", attachment);
			}
		} else {
			resp.setContentType("application/xml");
		}
		PrintWriter out = resp.getWriter();
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(out));
			service(parameters, writer, xslPath);
			writer.flush();
		} catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			resp.setContentType("application/xml");
			TupleResultBuilder builder = new TupleResultBuilder(out);
			builder.transform(xslPath, "query.xsl");
			builder.start("error-message");
			builder.link("info");
			builder.link("namespaces");
			builder.result(exc.getMessage());
			builder.end();
		}
	}

	private void service(Map<String, String> parameters, PrintWriter out,
			String xslPath) throws Exception {
		RepositoryConnection con = repository.getConnection();
		try {
			TupleResultBuilder builder = new TupleResultBuilder(out);
			for (Namespace ns : con.getNamespaces().asList()) {
				builder.prefix(ns.getPrefix(), ns.getName());
			}
			if (parameters.containsKey("query")) {
				try {
					service(builder, out, xslPath, con, parameters);
				} catch (MalformedQueryException exc) {
					throw new BadRequestException(exc.getMessage(), exc);
				} catch (HTTPQueryEvaluationException exc) {
					if (exc.getCause() instanceof MalformedQueryException) {
						throw new BadRequestException(exc.getCause()
								.getMessage());
					}
					throw exc;
				}
			} else {
				builder.transform(xslPath, "query.xsl");
				builder.start();
				builder.link("info");
				builder.link("namespaces");
				builder.end();
			}
		} finally {
			con.close();
		}
	}

	private void service(TupleResultBuilder builder, PrintWriter out,
			String xslPath, RepositoryConnection con,
			Map<String, String> parameters) throws Exception {
		String ql = parameters.get("queryLn");
		String q = parameters.get("query");
		Query query = prepareQuery(con, QueryLanguage.valueOf(ql), q);
		if (parameters.containsKey("infer")) {
			boolean infer = Boolean.parseBoolean(parameters.get("infer"));
			query.setIncludeInferred(infer);
		}
		int limit = 0;
		if (parameters.containsKey("limit")) {
			limit = Integer.parseInt(parameters.get("limit"));
		}
		RDFFormat format = null;
		String accept = parameters.get("Accept");
		if (accept != null) {
			format = RDFFormat.forMIMEType(accept);
		}
		if (query instanceof TupleQuery) {
			builder.transform(xslPath, "tuple.xsl");
			builder.start();
			evaluateTupleQuery(builder, (TupleQuery) query, limit);
			builder.end();
		} else if (query instanceof GraphQuery && format == null) {
			builder.transform(xslPath, "graph.xsl");
			builder.start();
			evaluateGraphQuery(builder, (GraphQuery) query, limit);
			builder.end();
		} else if (query instanceof GraphQuery) {
			RDFWriterFactory factory = getInstance().get(format);
			RDFWriter writer = factory.getWriter(out);
			evaluateGraphQuery(writer, (GraphQuery) query);
		} else if (query instanceof BooleanQuery) {
			builder.transform(xslPath, "boolean.xsl");
			builder.start();
			evaluateBooleanQuery(builder, (BooleanQuery) query);
			builder.end();
		} else {
			throw new BadRequestException("Unknown query type: "
					+ query.getClass().getSimpleName());
		}
	}

	private Query prepareQuery(RepositoryConnection con, QueryLanguage ql,
			String q) throws RepositoryException, MalformedQueryException {
		try {
			return con.prepareQuery(ql, q);
		} catch (UnsupportedOperationException exc) {
			// TODO must be an http repository
			try {
				con.prepareTupleQuery(ql, q).evaluate().close();
				return con.prepareTupleQuery(ql, q);
			} catch (Exception malformed) {
				// guess its not a tuple query
			}
			try {
				con.prepareGraphQuery(ql, q).evaluate().close();
				return con.prepareGraphQuery(ql, q);
			} catch (Exception malformed) {
				// guess its not a graph query
			}
			try {
				con.prepareBooleanQuery(ql, q).evaluate();
				return con.prepareBooleanQuery(ql, q);
			} catch (Exception malformed) {
				// guess its not a boolean query
			}
			// let's assume it is an malformed tuple query
			return con.prepareTupleQuery(ql, q);
		}
	}

	private void evaluateTupleQuery(TupleResultBuilder builder,
			TupleQuery query, int limit) throws QueryEvaluationException {
		TupleQueryResult result = query.evaluate();
		try {
			String[] names = result.getBindingNames().toArray(new String[0]);
			builder.variables(names);
			builder.link("info");
			builder.flush();
			for (int l = 0; result.hasNext() && (l < limit || limit < 1); l++) {
				BindingSet set = result.next();
				Object[] values = new Object[names.length];
				for (int i = 0; i < names.length; i++) {
					values[i] = set.getValue(names[i]);
				}
				builder.result(values);
			}
		} finally {
			result.close();
		}
	}

	private void evaluateGraphQuery(TupleResultBuilder builder,
			GraphQuery query, int limit) throws QueryEvaluationException {
		GraphQueryResult result = query.evaluate();
		try {
			builder.variables("subject", "predicate", "object");
			builder.link("info");
			for (int l = 0; result.hasNext() && (l < limit || limit < 1); l++) {
				Statement st = result.next();
				builder.result(st.getSubject(), st.getPredicate(), st
						.getObject(), st.getContext());
			}
		} finally {
			result.close();
		}
	}

	private void evaluateGraphQuery(RDFWriter writer, GraphQuery query)
			throws QueryEvaluationException, RDFHandlerException {
		query.evaluate(writer);
	}

	private void evaluateBooleanQuery(TupleResultBuilder builder,
			BooleanQuery query) throws QueryEvaluationException {
		boolean result = query.evaluate();
		builder.link("info");
		builder.bool(result);
	}

}