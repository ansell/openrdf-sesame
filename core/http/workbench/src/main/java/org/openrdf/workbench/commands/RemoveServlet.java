/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.StoreException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveServlet extends TransformationServlet {
	private Logger logger = LoggerFactory.getLogger(RemoveServlet.class);

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception, IOException {
		String s = req.getParameter("subj");
		String p = req.getParameter("pred");
		String o = req.getParameter("obj");
		String c = req.getParameter("context");
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				Resource subj = req.getResource("subj");
				URI pred = req.getURI("pred");
				Value obj = req.getValue("obj");
				if (subj == pred && pred == obj && obj == null)
					throw new BadRequestException("No values");
				if (req.isParameterPresent("context")) {
					Resource ctx = req.getResource("context");
					con.remove(subj, pred, obj, ctx);
				} else {
					con.remove(subj, pred, obj);
				}
			} catch (ClassCastException exc) {
				throw new BadRequestException(exc.getMessage(), exc);
			} finally {
				con.close();
			}
			resp.sendRedirect("summary");
		} catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			resp.setContentType("application/xml");
			PrintWriter out = resp.getWriter();
			TupleResultBuilder builder = new TupleResultBuilder(out);
			builder.transform(xslPath, "remove.xsl");
			builder.start("error-message", "subj", "pred", "obj", "context");
			builder.link("info");
			builder.result(exc.getMessage(), s, p, o, c);
			builder.end();
		}
	}

	@Override
	public void service(PrintWriter out, String xslPath)
			throws StoreException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "remove.xsl");
		builder.start();
		builder.link("info");
		builder.end();
	}

}