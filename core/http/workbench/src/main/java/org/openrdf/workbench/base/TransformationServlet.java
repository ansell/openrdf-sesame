package org.openrdf.workbench.base;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.workbench.exceptions.MissingInitParameterException;
import org.openrdf.workbench.util.WorkbenchRequest;

public abstract class TransformationServlet extends BaseRepositoryServlet {
	private static final String TRANSFORMATIONS_PARAM = "transformations";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (config.getInitParameter(TRANSFORMATIONS_PARAM) == null)
			throw new MissingInitParameterException(TRANSFORMATIONS_PARAM);
	}

	public final void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getCharacterEncoding() == null) {
			req.setCharacterEncoding("UTF-8");
		}
		resp.setCharacterEncoding("UTF-8");
		String contextPath = req.getContextPath();
		String path = config.getInitParameter(TRANSFORMATIONS_PARAM);
		String xslPath = contextPath + path;
		try {
			WorkbenchRequest wreq = new WorkbenchRequest(repository, req);
			if ("POST".equals(req.getMethod())) {
				doPost(wreq, resp, xslPath);
			} else {
				service(wreq, resp, xslPath);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doPost(WorkbenchRequest wreq, HttpServletResponse resp,
			String xslPath) throws Exception {
		service(wreq, resp, xslPath);
	}

	protected void service(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception {
		resp.setContentType("application/xml");
		service(resp.getWriter(), xslPath);
	}

	protected void service(PrintWriter writer, String xslPath) throws Exception {
	}
}
