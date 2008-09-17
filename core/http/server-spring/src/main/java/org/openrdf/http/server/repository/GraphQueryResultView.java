/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.StoreException;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryResultUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * View used to render graph query results. Renders the graph as RDF using a
 * serialization specified using a parameter or Accept header.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
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
		throws IOException
	{
		RDFWriterFactory rdfWriterFactory = (RDFWriterFactory)model.get(FACTORY_KEY);
		RDFFormat rdfFormat = rdfWriterFactory.getRDFFormat();

		response.setStatus(SC_OK);
		setContentType(response, rdfFormat);
		setContentDisposition(model, response, rdfFormat);

		OutputStream out = response.getOutputStream();
		try {
			RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
			GraphQueryResult graphQueryResult = (GraphQueryResult)model.get(QUERY_RESULT_KEY);
			Integer limit = (Integer)model.get(LIMIT);
			report(rdfWriter, graphQueryResult, limit);
		}
		catch (QueryInterruptedException e) {
			logger.error("Query interrupted", e);
			response.sendError(SC_SERVICE_UNAVAILABLE, "Query evaluation took too long");
		}
		catch (StoreException e) {
			logger.error("Query evaluation error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Query evaluation error: " + e.getMessage());
		}
		catch (RDFHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
		finally {
			out.close();
		}
		logEndOfRequest(request);
	}

	private void report(RDFWriter rdfWriter, GraphQueryResult graphQueryResult, Integer limit)
		throws RDFHandlerException, StoreException
	{
		try {
			rdfWriter.startRDF();
		
			for (Map.Entry<String, String> entry : graphQueryResult.getNamespaces().entrySet()) {
				String prefix = entry.getKey();
				String namespace = entry.getValue();
				rdfWriter.handleNamespace(prefix, namespace);
			}
		
			for (int i = 0; graphQueryResult.hasNext() && (limit == null || i < limit.intValue()); i++) {
				Statement st = graphQueryResult.next();
				rdfWriter.handleStatement(st);
			}
		
			rdfWriter.endRDF();
		}
		finally {
			graphQueryResult.close();
		}
	}
}
