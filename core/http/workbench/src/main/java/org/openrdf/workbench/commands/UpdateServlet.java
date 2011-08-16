/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateServlet extends TransformationServlet {

	private Logger logger = LoggerFactory.getLogger(UpdateServlet.class);

	@Override
	public String[] getCookieNames() {
		return new String[] { "Content-Type" };
	}

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception, IOException
	{
		try {
			String updateString = req.getParameter("update");

			executeUpdate(updateString);

			resp.sendRedirect("summary");
		}
		catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			resp.setContentType("application/xml");
			PrintWriter out = resp.getWriter();
			TupleResultBuilder builder = new TupleResultBuilder(out);
			builder.transform(xslPath, "update.xsl");
			builder.start("error-message", "update");
			builder.link("info");
			builder.link("namespaces");

			String updateString = req.getParameter("update");
			builder.result(exc.getMessage(), updateString);
			builder.end();
		}
	}

	private void executeUpdate(String updateString)
		throws Exception
	{
		RepositoryConnection con = repository.getConnection();
		Update update;
		try {
			update = con.prepareUpdate(QueryLanguage.SPARQL, updateString);
			update.execute();
		}
		catch (RepositoryException e) {
			throw new BadRequestException(e.getMessage());
		}
		catch (MalformedQueryException e) {
			throw new BadRequestException(e.getMessage());
		}
		catch (UpdateExecutionException e) {
			throw new BadRequestException(e.getMessage());
		}
		finally {
			con.close();
		}
	}

	@Override
	public void service(PrintWriter out, String xslPath)
		throws RepositoryException
	{
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "update.xsl");
		builder.start();
		builder.link("info");
		builder.link("namespaces");
		builder.end();
	}

}