/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BasicQueryWriterSettings;
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
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		TupleQueryResultWriterFactory qrWriterFactory = (TupleQueryResultWriterFactory)model.get(FACTORY_KEY);
		TupleQueryResultFormat qrFormat = qrWriterFactory.getTupleQueryResultFormat();

		response.setStatus(SC_OK);
		setContentType(response, qrFormat);
		setContentDisposition(model, response, qrFormat);

		final boolean headersOnly = model.containsKey(HEADERS_ONLY) ? (Boolean)model.get(HEADERS_ONLY): false;

		if (!headersOnly) {
			OutputStream out = response.getOutputStream();
			try {
				TupleQueryResultWriter qrWriter = qrWriterFactory.getWriter(out);
				TupleQueryResult tupleQueryResult = (TupleQueryResult)model.get(QUERY_RESULT_KEY);

				if(qrWriter.getSupportedSettings().contains(BasicQueryWriterSettings.JSONP_CALLBACK))
				{
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