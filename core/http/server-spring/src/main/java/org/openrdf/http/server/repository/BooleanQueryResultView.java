/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;

/**
 * View used to render boolean query results. Renders results in a format
 * specified using a parameter or Accept header.
 * 
 * @author Arjohn Kampman
 */
public class BooleanQueryResultView extends QueryResultView {

	private static final BooleanQueryResultView INSTANCE = new BooleanQueryResultView();

	public static BooleanQueryResultView getInstance() {
		return INSTANCE;
	}

	private BooleanQueryResultView() {
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		BooleanQueryResultWriterFactory brWriterFactory = (BooleanQueryResultWriterFactory)model.get(FACTORY_KEY);
		BooleanQueryResultFormat brFormat = brWriterFactory.getBooleanQueryResultFormat();

		response.setStatus(SC_OK);
		setContentType(response, brFormat);
		setContentDisposition(model, response, brFormat);

		OutputStream out = response.getOutputStream();
		try {
			BooleanQueryResultWriter qrWriter = brWriterFactory.getWriter(out);
			boolean value = (Boolean)model.get(QUERY_RESULT_KEY);
			qrWriter.write(value);
		}
		finally {
			out.close();
		}
		logEndOfRequest(request);
	}
}
