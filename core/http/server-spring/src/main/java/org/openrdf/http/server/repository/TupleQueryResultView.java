/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultUtil;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;

/**
 * View used to render tuple query results. Renders results in a format
 * specified using a parameter or Accept header.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class TupleQueryResultView extends QueryResultView {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final TupleQueryResultView INSTANCE = new TupleQueryResultView();

	public static TupleQueryResultView getInstance() {
		return INSTANCE;
	}

	private TupleQueryResultView() {
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		TupleQueryResultWriterFactory qrWriterFactory = (TupleQueryResultWriterFactory)model.get(FACTORY_KEY);
		TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

		response.setStatus(SC_OK);
		setContentType(response, qrFormat);
		setContentDisposition(model, response, qrFormat);

		OutputStream out = response.getOutputStream();
		try {
			TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);
			TupleQueryResult tupleQueryResult = (TupleQueryResult)model.get(QUERY_RESULT_KEY);
			QueryResultUtil.report(tupleQueryResult, qrWriter);
		}
		catch (QueryEvaluationException e) {
			logger.error("Query evaluation error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Query evaluation error: " + e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
		finally {
			out.close();
		}
		logEndOfRequest(request);
	}
}
