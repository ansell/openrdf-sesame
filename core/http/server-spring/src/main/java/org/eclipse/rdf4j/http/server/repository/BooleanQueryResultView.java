/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterFactory;

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
	protected void renderInternal(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		BooleanQueryResultWriterFactory brWriterFactory = (BooleanQueryResultWriterFactory)model.get(FACTORY_KEY);
		BooleanQueryResultFormat brFormat = brWriterFactory.getBooleanQueryResultFormat();

		response.setStatus(SC_OK);
		setContentType(response, brFormat);
		setContentDisposition(model, response, brFormat);

		boolean headersOnly = (Boolean)model.get(HEADERS_ONLY);

		if (!headersOnly) {
			OutputStream out = response.getOutputStream();
			try {
				BooleanQueryResultWriter qrWriter = brWriterFactory.getWriter(out);
				boolean value = (Boolean)model.get(QUERY_RESULT_KEY);
				qrWriter.handleBoolean(value);
			}
			catch (QueryResultHandlerException e) {
				if (e.getCause() != null && e.getCause() instanceof IOException) {
					throw (IOException)e.getCause();
				}
				else {
					throw new IOException(e);
				}
			}
			finally {
				out.close();
			}
		}
		logEndOfRequest(request);
	}
}
