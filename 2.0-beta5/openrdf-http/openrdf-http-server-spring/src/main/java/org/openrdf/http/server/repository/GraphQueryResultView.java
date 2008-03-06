/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.server.ClientRequestException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.UnsupportedRDFormatException;

/**
 * View used to render graph query results.
 * 
 * Renders the graph as RDF using a serialization specified using a parameter or
 * Accept header.
 * 
 * @author Herko ter Horst
 */
public class GraphQueryResultView extends QueryResultView {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final GraphQueryResultView INSTANCE = new GraphQueryResultView();

	public static GraphQueryResultView getInstance() {
		return INSTANCE;
	}

	private GraphQueryResultView() {
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		try {
			GraphQueryResult graphQueryResult = (GraphQueryResult)model.get(QUERY_RESULT_KEY);

			RDFWriterFactory rdfWriterFactory = ProtocolUtil.getAcceptableRDFWriterFactory(request, response);
			RDFFormat rdfFormat = rdfWriterFactory.getRDFFormat();

			OutputStream out = response.getOutputStream();
			RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);

			response.setStatus(SC_OK);
			response.setContentType(rdfFormat.getDefaultMIMEType());

			// to make use in browser more convenient
			String filename = (String)model.get(FILENAME_HINT_KEY);
			if (filename == null || filename.length() == 0) {
				filename = "result";
			}
			if (rdfFormat.getDefaultFileExtension() != null) {
				filename += "." + rdfFormat.getDefaultFileExtension();
			}
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);

			try {
				QueryResultUtil.report(graphQueryResult, rdfWriter);
			}
			finally {
				out.close();
			}
		}
		catch (UnsupportedRDFormatException e) {
			throw new ClientRequestException(SC_NOT_ACCEPTABLE, "Unsupported RDF format: " + e.getMessage());
		}
		catch (QueryEvaluationException e) {
			logger.error("Query evaluation error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Query evaluation error: " + e.getMessage());
		}
		catch (RDFHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
	}
}
