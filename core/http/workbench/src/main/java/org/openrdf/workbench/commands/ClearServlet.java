package org.openrdf.workbench.commands;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearServlet extends TransformationServlet {
	private Logger logger = LoggerFactory.getLogger(ClearServlet.class);

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception, IOException {
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				if (req.isParameterPresent("context")) {
					con.clear(req.getResource("context"));
				} else {
					con.clear();
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
			builder.transform(xslPath, "clear.xsl");
			builder.start("error-message", "context");
			builder.link("info");
			builder.result(exc.getMessage(), req.getParameter("context"));
			builder.end();
		}
	}

	@Override
	public void service(PrintWriter out, String xslPath)
			throws RepositoryException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "clear.xsl");
		builder.start();
		builder.link("info");
		builder.end();
	}

}