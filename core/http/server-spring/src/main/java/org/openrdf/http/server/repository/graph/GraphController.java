/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository.graph;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.webapp.util.HttpServerUtil;
import info.aduna.webapp.views.EmptySuccessView;

import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.http.server.repository.statements.ExportStatementsView;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * Handles requests for manipulating the named graphs in a repository.
 * 
 * @author Jeen Broekstra
 */
public class GraphController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public GraphController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET, METHOD_POST, "PUT", "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		ModelAndView result;

		Repository repository = RepositoryInterceptor.getRepository(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		String reqMethod = request.getMethod();

		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET graph");
			result = getExportStatementsResult(repository, repositoryCon, request, response);
			logger.info("GET graph request finished.");
		}
		else if (METHOD_POST.equals(reqMethod)) {
			logger.info("POST data to graph");
			result = getAddDataResult(repository, repositoryCon, request, response, false);
			logger.info("POST data request finished.");
		}
		else if ("PUT".equals(reqMethod)) {
			logger.info("PUT data in graph");
			result = getAddDataResult(repository, repositoryCon, request, response, true);
			logger.info("PUT data request finished.");
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE data from graph");
			result = getDeleteDataResult(repository, repositoryCon, request, response);
			logger.info("DELETE data request finished.");
		}
		else {
			throw new ClientHTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed: "
					+ reqMethod);
		}
		return result;
	}

	private URI getGraphName(HttpServletRequest request, ValueFactory vf)
		throws ClientHTTPException
	{
		String requestURL = request.getRequestURL().toString();
		boolean isServiceRequest = requestURL.endsWith("/service");

		String queryString = request.getQueryString();

		if (isServiceRequest) {
			if (!"default".equalsIgnoreCase(queryString)) {
				URI graph = ProtocolUtil.parseGraphParam(request, vf);
				if (graph == null) {
					throw new ClientHTTPException(HttpServletResponse.SC_BAD_REQUEST,
							"Named or default graph expected for indirect reference request.");
				}
				return graph;
			}
			return null;
		}
		else {
			if (queryString != null) {
				throw new ClientHTTPException(HttpServletResponse.SC_BAD_REQUEST,
						"No parameters epxected for direct reference request.");
			}
			return vf.createURI(requestURL);
		}
	}

	/**
	 * Get all statements and export them as RDF.
	 * 
	 * @return a model and view for exporting the statements.
	 */
	private ModelAndView getExportStatementsResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response)
		throws ClientHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();

		URI graph = getGraphName(request, vf);

		RDFWriterFactory rdfWriterFactory = ProtocolUtil.getAcceptableService(request, response,
				RDFWriterRegistry.getInstance());

		Map<String, Object> model = new HashMap<String, Object>();

		model.put(ExportStatementsView.CONTEXTS_KEY, new Resource[] { graph });
		model.put(ExportStatementsView.FACTORY_KEY, rdfWriterFactory);
		model.put(ExportStatementsView.USE_INFERENCING_KEY, true);

		return new ModelAndView(ExportStatementsView.getInstance(), model);
	}

	/**
	 * Upload data to the graph.
	 */
	private ModelAndView getAddDataResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response, boolean replaceCurrent)
		throws IOException, ClientHTTPException, ServerHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());

		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new ClientHTTPException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported MIME type: " + mimeType);
		}

		ValueFactory vf = repository.getValueFactory();

		URI graph = getGraphName(request, vf);

		InputStream in = request.getInputStream();
		try {
			boolean wasAutoCommit = repositoryCon.isAutoCommit();
			repositoryCon.setAutoCommit(false);

			if (replaceCurrent) {
				repositoryCon.clear(graph);
			}
			repositoryCon.add(in, graph.toString(), rdfFormat, graph);

			repositoryCon.setAutoCommit(wasAutoCommit);

			return new ModelAndView(EmptySuccessView.getInstance());
		}
		catch (UnsupportedRDFormatException e) {
			throw new ClientHTTPException(SC_UNSUPPORTED_MEDIA_TYPE, "No RDF parser available for format "
					+ rdfFormat.getName());
		}
		catch (RDFParseException e) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_DATA, e.getMessage());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
		catch (IOException e) {
			throw new ServerHTTPException("Failed to read data: " + e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
		}
	}

	/**
	 * Delete data from the graph.
	 */
	private ModelAndView getDeleteDataResult(Repository repository, RepositoryConnection repositoryCon,
			HttpServletRequest request, HttpServletResponse response)
		throws ClientHTTPException, ServerHTTPException
	{
		ProtocolUtil.logRequestParameters(request);

		ValueFactory vf = repository.getValueFactory();

		URI graph = getGraphName(request, vf);

		try {
			repositoryCon.clear(graph);

			return new ModelAndView(EmptySuccessView.getInstance());
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository update error: " + e.getMessage(), e);
		}
	}
}
