/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

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
		return new String[]{"limit", "total_result_count"};
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
	protected void service(WorkbenchRequest req, HttpServletResponse resp, 
			TupleResultBuilder builder, RepositoryConnection con) 
					throws Exception {
		final Value value = req.getValue("resource");
		logger.info("resource = {}", value);
		
		// At worst, malicious parameter value could cause innacurate 
		// reporting of count in page.
		int count = req.getInt("know_total");
		if (count == 0) {
			count = this.processResource(con, builder, value, 0, 
					Integer.MAX_VALUE, false);
		}
		
		this.addTotalResultCountCookie(req, resp, count);
		final int offset = req.getInt("offset");
		int limit = req.getInt("limit");
		if (limit == 0) {
			limit = Integer.MAX_VALUE;
		}

		this.processResource(con, builder, value, offset, limit, true);
	}
	
	/**
	 * Query the repository for all instances of the given value, optionally
	 * writing the results into the HTTP response.
	 * 
	 * @param con the connection to the repository
	 * @param builder used for writing to the HTTP response
	 * @param value the value to query the repository for
	 * @param offset The result at which to start rendering results.
	 * @param limit The limit on the number of results to render.
	 * @param render If false, suppresses output to the HTTP response.
	 * @returns The count of all triples in the repository using the given 
	 * value.
	 */
	private int processResource(RepositoryConnection con, 
			final TupleResultBuilder builder, final Value value, 
			final int offset, final int limit, final boolean render) 
					throws RepositoryException {
		final ResultCursor rc = new ResultCursor(offset, limit, render);
		if (value instanceof Resource) {
			export(con, builder, rc, (Resource) value, null, null);
			logger.info("After subject, total = {}", 
					rc.getTotalResultCount());
		}
		
		if (value instanceof URI) {
			export(con, builder, rc, null, (URI) value, null);
			logger.info("After predicate, total = {}", 
					rc.getTotalResultCount());
		}
		
		if (value != null) {
			export(con, builder, rc, null, null, value);
			logger.info("After object, total = {}", 
					rc.getTotalResultCount());
		}
		
		if (value instanceof Resource) {
			export(con, builder, rc, null, null, null, (Resource) value);
			logger.info("After context, total = {}",
					rc.getTotalResultCount());
		}
		
		return rc.getTotalResultCount();
	}

	/**
	 * Render statements in the repository matching the given pattern to the
	 * HTTP response.
	 * 
	 * @param con the connection to the repository
	 * @param builder used for writing to the HTTP response
	 * @param rc used for keeping track of our location in the result set
	 * @param subj the triple subject
	 * @param pred the triple predicate
	 * @param obj the triple object
	 * @param ctx the triple context
	 */
	private void export(final RepositoryConnection con, 
			final TupleResultBuilder builder, final ResultCursor rc, 
			final Resource subj, final URI pred, final Value obj, 
			final Resource... ctx)
					throws RepositoryException {
		final RepositoryResult<Statement> result = con.getStatements(
				subj, pred, obj, true, ctx);
		try {
			while(result.hasNext()) {
				final Statement st = result.next();
				if (rc.mayRender()) {
					builder.result(st.getSubject(), st.getPredicate(), 
							st.getObject(), st.getContext());
				}

				rc.advance();
			}
		} finally {
			result.close();
		}
	}

	/**
	 * Class for keeping track of location within the result set, relative to
	 * offset and  limit.
	 * 
	 * @author Dale Visser
	 */
	private class ResultCursor {
		private Logger logger = LoggerFactory.getLogger(ResultCursor.class);

		private int remainingUntilFirst;
		private int totalResults = 0;
		private int renderedResults = 0;
		private final int limit;
		private final boolean render;
		
		/**
		 * @param offset the desired offset at which rendering should start
		 * @param limit the desired maximum number of results to render
		 * @param render if false, suppresses any rendering
		 */
		public ResultCursor(int offset, int limit, boolean render) {
			this.render = render;
			this.limit = limit > 0 ? limit : Integer.MAX_VALUE;
			this.remainingUntilFirst = offset >= 0 ? offset : 0;
		}
		
		/**
		 * Gets the total number of results. Only meant to be called after
		 * advance() has been called for all results in the set.
		 * 
		 * @returns the number of times advance() has been called
		 */
		public int getTotalResultCount() {
			return this.totalResults;
		}
		
		/**
		 * @returns whether the rendering limit has been reached yet.
		 */
		public boolean hasMore() {
			return this.renderedResults < this.limit;
		}
		
		/**
		 * @returns whether it is allowed to render the next result
		 */
		public boolean mayRender() {
			return this.render && 
					(this.remainingUntilFirst == 0 && this.hasMore());
		}
		
		/**
		 * Advances the cursor, incrementing the total count, and moving
		 * other internal counters.
		 */
		public void advance() {
			this.totalResults++;
			if (this.mayRender()) {
				this.renderedResults++;
			}
			
			if (this.remainingUntilFirst > 0) {
				this.remainingUntilFirst--;
			}
		}
	}
}