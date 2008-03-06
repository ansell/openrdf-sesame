/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.http.server.ClientRequestException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;

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

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		boolean value = (Boolean)model.get(QUERY_RESULT_KEY);

		BooleanQueryResultWriterFactory brWriterFactory = ProtocolUtil.getAcceptableService(request, response,
				BooleanQueryResultWriterRegistry.getInstance());

		BooleanQueryResultFormat brFormat = brWriterFactory.getBooleanQueryResultFormat();

		try {
			OutputStream out = response.getOutputStream();
			BooleanQueryResultWriter qrWriter = brWriterFactory.getWriter(out);

			response.setStatus(SC_OK);
			response.setContentType(brFormat.getDefaultMIMEType());

			// to make use in browser more convenient
			String filename = (String)model.get(FILENAME_HINT_KEY);
			if (filename == null || filename.length() == 0) {
				filename = "result";
			}
			if (brFormat.getDefaultFileExtension() != null) {
				filename += "." + brFormat.getDefaultFileExtension();
			}
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);

			try {
				qrWriter.write(value);
			}
			finally {
				out.close();
			}
		}
		catch (UnsupportedQueryResultFormatException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
	}
}
