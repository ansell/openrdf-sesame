package org.openrdf.workbench.base;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.workbench.exceptions.MissingInitParameterException;
import org.openrdf.workbench.util.WorkbenchRequest;

public abstract class TransformationServlet extends BaseRepositoryServlet {

	private static final String COOKIE_AGE_PARAM = "cookie-max-age";

	private static final String TRANSFORMATIONS_PARAM = "transformations";

	private Map<String, String> defaults = new HashMap<String, String>();

	@Override
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		if (config.getInitParameter(TRANSFORMATIONS_PARAM) == null)
			throw new MissingInitParameterException(TRANSFORMATIONS_PARAM);
		if (config != null) {
			Enumeration<?> names = config.getInitParameterNames();
			while (names.hasMoreElements()) {
				String name = (String)names.nextElement();
				String value = config.getInitParameter(name);
				if (name.startsWith("default-")) {
					defaults.put(name.substring("default-".length()), value);
				}
			}
		}
	}

	public String[] getCookieNames() {
		return new String[0];
	}

	public final void service(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		if (req.getCharacterEncoding() == null) {
			req.setCharacterEncoding("UTF-8");
		}
		resp.setCharacterEncoding("UTF-8");
		String contextPath = req.getContextPath();
		String path = config.getInitParameter(TRANSFORMATIONS_PARAM);
		String xslPath = contextPath + path;
		try {
			WorkbenchRequest wreq = new WorkbenchRequest(repository, req, defaults);
			updateCookies(wreq, resp);
			if ("POST".equals(req.getMethod())) {
				doPost(wreq, resp, xslPath);
			}
			else {
				service(wreq, resp, xslPath);
			}
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doPost(WorkbenchRequest wreq, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		service(wreq, resp, xslPath);
	}

	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception
	{
		resp.setContentType("application/xml");
		service(resp.getWriter(), xslPath);
	}

	protected void service(PrintWriter writer, String xslPath)
		throws Exception
	{
	}

	private void updateCookies(WorkbenchRequest req, HttpServletResponse resp) {
		for (String name : getCookieNames()) {
			if (req.isParameterPresent(name)) {
				addCookie(req, resp, name);
			}
		}
	}

	private void addCookie(WorkbenchRequest req, HttpServletResponse resp, String name) {
		Cookie cookie = new Cookie(name, req.getParameter(name));
		if (req.getContextPath() != null) {
			cookie.setPath(req.getContextPath());
		}
		else {
			cookie.setPath("/");
		}
		cookie.setMaxAge(parseInt(config.getInitParameter(COOKIE_AGE_PARAM)));
		addCookie(req, resp, cookie);
	}

	private void addCookie(WorkbenchRequest req, HttpServletResponse resp, Cookie cookie) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (cookie.getName().equals(c.getName())) {
					if (cookie.getValue().equals(c.getValue())) {
						// cookie already exists
						// tell the browser we are using it
						resp.addHeader("Vary", "Cookie");
					}
				}
			}
		}
		resp.addCookie(cookie);
	}
}
