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
package org.openrdf.http.server.repository.statements;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.http.server.repository.transaction.ActiveTransactionRegistry;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * View used to export statements. Renders the statements as RDF using a
 * serialization specified using a parameter or Accept header.
 * 
 * @author Herko ter Horst
 */
public class ExportStatementsView implements View {

	public static final String SUBJECT_KEY = "subject";

	public static final String PREDICATE_KEY = "predicate";

	public static final String OBJECT_KEY = "object";

	public static final String CONTEXTS_KEY = "contexts";

	public static final String USE_INFERENCING_KEY = "useInferencing";

	public static final String CONNECTION_KEY = "connection";

	public static final String TRANSACTION_ID_KEY = "transactionID";

	public static final String FACTORY_KEY = "factory";

	public static final String HEADERS_ONLY = "headersOnly";

	private static final ExportStatementsView INSTANCE = new ExportStatementsView();

	public static ExportStatementsView getInstance() {
		return INSTANCE;
	}

	private ExportStatementsView() {
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		UUID txnId = null;
		try {
			txnId = (UUID)model.get(TRANSACTION_ID_KEY);
			Resource subj = (Resource)model.get(SUBJECT_KEY);
			IRI pred = (IRI)model.get(PREDICATE_KEY);
			Value obj = (Value)model.get(OBJECT_KEY);
			Resource[] contexts = (Resource[])model.get(CONTEXTS_KEY);
			boolean useInferencing = (Boolean)model.get(USE_INFERENCING_KEY);
			RepositoryConnection conn = (RepositoryConnection)model.get(CONNECTION_KEY);

			boolean headersOnly = (Boolean)model.get(HEADERS_ONLY);

			RDFWriterFactory rdfWriterFactory = (RDFWriterFactory)model.get(FACTORY_KEY);

			RDFFormat rdfFormat = rdfWriterFactory.getRDFFormat();

			try {
				OutputStream out = response.getOutputStream();
				RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);

				response.setStatus(SC_OK);

				String mimeType = rdfFormat.getDefaultMIMEType();
				if (rdfFormat.hasCharset()) {
					Charset charset = rdfFormat.getCharset();
					mimeType += "; charset=" + charset.name();
				}
				response.setContentType(mimeType);

				String filename = "statements";
				if (rdfFormat.getDefaultFileExtension() != null) {
					filename += "." + rdfFormat.getDefaultFileExtension();
				}
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);

				if (!headersOnly) {
					if (conn == null) {
						conn = RepositoryInterceptor.getRepositoryConnection(request);
					}
					synchronized (conn) {
						conn.exportStatements(subj, pred, obj, useInferencing, rdfWriter, contexts);
					}
				}
				out.close();
			}
			catch (RDFHandlerException e) {
				throw new ServerHTTPException("Serialization error: " + e.getMessage(), e);
			}
			catch (RepositoryException e) {
				throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
			}
		}
		finally {
			if (txnId != null) {
				ActiveTransactionRegistry.INSTANCE.returnTransactionConnection(txnId);
			}
		}
	}

}
