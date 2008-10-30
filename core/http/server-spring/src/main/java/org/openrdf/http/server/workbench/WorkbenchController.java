/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.workbench;

import static java.util.Arrays.asList;
import static org.openrdf.http.server.workbench.TupleXMLResultView.XSLT;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.query.parser.QueryParserRegistry;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class WorkbenchController implements Controller {
	private static final String HEADER_IFMODSINCE = "If-Modified-Since";
	private static final String HEADER_LASTMOD = "Last-Modified";

	private static final String QUERY_TYPE = "query-type";

	private QueryParserRegistry registry = QueryParserRegistry.getInstance();

	private String transformationPath = "";

	private String repositoryPath = "";

	private long lastModified = System.currentTimeMillis() / 1000 * 1000;

	public void setTransformationPath(String path) {
		this.transformationPath = path;
	}

	public void setRepositoriesPath(String path) {
		this.repositoryPath = path;
	}

	public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp)
		throws StoreException, IOException, FileUploadException
	{
		Map<String, String> defaults = getDefaultrequest();
		WorkbenchRequest request = new WorkbenchRequest(req, resp, defaults);
		long ifModifiedSince = request.getDateHeader(HEADER_IFMODSINCE);
		if (ifModifiedSince < lastModified) {
			request.setDateHeader(HEADER_LASTMOD, lastModified);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return null;
		}
		Map<String, String> model = new HashMap<String, String>();
		model.putAll(request.getSingleParameterMap());
		model.keySet().removeAll(getOptionalBindingNames());

		addBindings(request, model);

		return new ModelAndView(new TupleXMLResultView(), model);
	}

	private Collection<String> getOptionalBindingNames() {
		return asList("error-message", "repositoryId", "repository", "queryString", QUERY_TYPE, "limit");
	}

	private Collection<String> getSettingNames() {
		return asList("limit", "queryLn", "infer", "Accept", "Content-Type");
	}

	private Map<String, String> getDefaultrequest() {
		Map<String, String> defaults = new HashMap<String, String>();
		defaults.put("limit", "100");
		defaults.put("queryLn", "SPARQL");
		defaults.put("infer", "true");
		defaults.put("Accept", "application/rdf+xml");
		defaults.put("Content-Type", "application/rdf+xml");
		return defaults;
	}

	private void addBindings(WorkbenchRequest request, Map<String, String> model) {
		model.put("workbench", getWorkbenchPath(request));
		model.put("server", getServerPath(request));
		String queryString = getQueryString(request);
		if (queryString != null) {
			model.put("queryString", queryString);
		}
		String pathInfo = request.getPathInfo();
		if (pathInfo != null && pathInfo.charAt(0) == '/' && pathInfo.length() > 2) {
			String repositoryId;
			int idx = pathInfo.indexOf('/', 1);
			if (idx > 0) {
				repositoryId = pathInfo.substring(1, idx);
				String transformation = pathInfo.substring(idx + 1);
				model.put(XSLT, getTransformationPath(request, transformation));
				additionalBindings(request, model, transformation);
			}
			else {
				repositoryId = pathInfo.substring(1);
				model.put(XSLT, getTransformationPath(request, "summary"));
			}
			model.put("repositoryId", repositoryId);
			model.put("repository", getRepositoryPath(request, repositoryId));
		}
		else {
			model.put(XSLT, getTransformationPath(request, "repositories"));
		}
		addPreferenceBindings(request, model);
	}

	private String getWorkbenchPath(WorkbenchRequest request) {
		return request.getContextPath() + request.getServletPath();
	}

	private String getQueryString(WorkbenchRequest request) {
		return request.getQueryString();
	}

	private String getServerPath(WorkbenchRequest request) {
		return request.getContextPath();
	}

	private String getTransformationPath(WorkbenchRequest request, String transformation) {
		String xslPath = request.getContextPath() + transformationPath + "/";
		return xslPath + transformation + ".xsl";
	}

	private String getRepositoryPath(WorkbenchRequest request, String repositoryId) {
		return request.getContextPath() + repositoryPath + "/" + repositoryId;
	}

	private void additionalBindings(WorkbenchRequest request, Map<String, String> model, String transformation) {
		if ("query".equals(transformation)) {
			addQueryBindings(request, model);
		}
	}

	private void addQueryBindings(WorkbenchRequest request, Map<String, String> model) {
		String queryLn = request.getParameter("queryLn");
		String query = request.getParameter("query");
		if (queryLn != null && query != null) {
			QueryLanguage ql = QueryLanguage.valueOf(queryLn);
			if (ql != null) {
				QueryParserFactory factory = registry.get(ql);
				try {
					QueryParser parser = factory.getParser();
					ParsedQuery parsed = parser.parseQuery(query, "foo:bar");
					if (parsed instanceof ParsedBooleanQuery) {
						model.put(QUERY_TYPE, "boolean");
					}
					else if (parsed instanceof ParsedGraphQuery) {
						model.put(QUERY_TYPE, "graph");
					}
					else if (parsed instanceof ParsedTupleQuery) {
						model.put(QUERY_TYPE, "tuple");
					}
				}
				catch (MalformedQueryException e) {
					model.put("error-message", e.getMessage());
				}
			}
		}
	}

	private void addPreferenceBindings(WorkbenchRequest request, Map<String, String> model) {
		for (String name : getSettingNames()) {
			model.put(name, request.setting(name));
		}
	}

}
