/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.server.ClientRequestException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;

/**
 * View used to render tuple query results.
 * 
 * Renders results in a format specified using a parameter or Accept header.
 * 
 * @author Herko ter Horst
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
		throws Exception
	{
		TupleQueryResult tupleQueryResult = (TupleQueryResult)model.get(QUERY_RESULT_KEY);

		TupleQueryResultWriterFactory qrWriterFactory = ProtocolUtil.getAcceptableQueryResultWriterFactory(
				request, response);
		TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

		try {
			OutputStream out = response.getOutputStream();
			TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);

			response.setStatus(SC_OK);
			response.setContentType(qrFormat.getDefaultMIMEType());

			// to make use in browser more convenient
			String filename = (String)model.get(FILENAME_HINT_KEY);
			if (filename == null || filename.length() == 0) {
				filename = "result";
			}
			if (qrFormat.getDefaultFileExtension() != null) {
				filename += "." + qrFormat.getDefaultFileExtension();
			}
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);

			try {
				QueryResultUtil.report(tupleQueryResult, qrWriter);
			}
			finally {
				out.close();
			}
		}
		catch (UnsupportedQueryResultFormatException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (QueryEvaluationException e) {
			logger.error("Query evaluation error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Query evaluation error: " + e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.error("Serialization error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, "Serialization error: " + e.getMessage());
		}
	}
}
