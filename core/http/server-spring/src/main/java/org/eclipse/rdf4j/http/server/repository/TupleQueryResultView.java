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

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.BasicQueryWriterSettings;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * View used to render tuple query results. Renders results in a format
 * specified using a parameter or Accept header.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class TupleQueryResultView extends QueryResultView {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected static final String DEFAULT_JSONP_CALLBACK_PARAMETER = "callback";

	protected static final Pattern JSONP_VALIDATOR = Pattern.compile("^[A-Za-z]\\w+$");

	private static final TupleQueryResultView INSTANCE = new TupleQueryResultView();

	public static TupleQueryResultView getInstance() {
		return INSTANCE;
	}

	private TupleQueryResultView() {
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	protected void renderInternal(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		TupleQueryResultWriterFactory qrWriterFactory = (TupleQueryResultWriterFactory)model.get(FACTORY_KEY);
		TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

		response.setStatus(SC_OK);
		setContentType(response, qrFormat);
		setContentDisposition(model, response, qrFormat);

		final Boolean headersOnly = (Boolean)model.get(HEADERS_ONLY);
		if (headersOnly == null || !headersOnly.booleanValue()) {
			OutputStream out = response.getOutputStream();
			try {
				TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);
				TupleQueryResult tupleQueryResult = (TupleQueryResult)model.get(QUERY_RESULT_KEY);

				if (qrWriter.getSupportedSettings().contains(BasicQueryWriterSettings.JSONP_CALLBACK)) {
					String parameter = request.getParameter(DEFAULT_JSONP_CALLBACK_PARAMETER);

					if (parameter != null) {
						parameter = parameter.trim();

						if (parameter.isEmpty()) {
							parameter = BasicQueryWriterSettings.JSONP_CALLBACK.getDefaultValue();
						}

						// check callback function name is a valid javascript function
						// name
						if (!JSONP_VALIDATOR.matcher(parameter).matches()) {
							throw new IOException("Callback function name was invalid");
						}

						qrWriter.getWriterConfig().set(BasicQueryWriterSettings.JSONP_CALLBACK, parameter);
					}
				}

				QueryResults.report(tupleQueryResult, qrWriter);
			}
			catch (QueryInterruptedException e) {
				logger.error("Query interrupted", e);
				response.sendError(SC_SERVICE_UNAVAILABLE, "Query evaluation took too long");
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
		}
		logEndOfRequest(request);
	}
}