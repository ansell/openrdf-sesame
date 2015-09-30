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

import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryResults;
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

	@SuppressWarnings("rawtypes")
	protected void renderInternal(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		RDFWriterFactory rdfWriterFactory = (RDFWriterFactory)model.get(FACTORY_KEY);
		RDFFormat rdfFormat = rdfWriterFactory.getRDFFormat();

		response.setStatus(SC_OK);
		setContentType(response, rdfFormat);
		setContentDisposition(model, response, rdfFormat);

		boolean headersOnly = (Boolean)model.get(HEADERS_ONLY);

		if (!headersOnly) {
			OutputStream out = response.getOutputStream();
			try {
				RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
				GraphQueryResult graphQueryResult = (GraphQueryResult)model.get(QUERY_RESULT_KEY);
				QueryResults.report(graphQueryResult, rdfWriter);
			}
			catch (QueryInterruptedException e) {
				logger.error("Query interrupted", e);
				response.sendError(SC_SERVICE_UNAVAILABLE, "Query evaluation took too long");
			}
			catch (QueryEvaluationException e) {
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
		}
		logEndOfRequest(request);
	}
}
