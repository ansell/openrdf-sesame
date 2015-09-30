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

import static org.eclipse.rdf4j.http.protocol.Protocol.QUERY_PARAM_NAME;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.http.server.repository.transaction.ActiveTransactionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;

/**
 * Base class for rendering query results.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public abstract class QueryResultView implements View {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Key by which the query result is stored in the model.
	 */
	public static final String QUERY_RESULT_KEY = "queryResult";

	/**
	 * Key by which the query result writer factory is stored in the model.
	 */
	public static final String FACTORY_KEY = "factory";

	/**
	 * Key by which a filename hint is stored in the model. The filename hint may
	 * be used to present the client with a suggestion for a filename to use for
	 * storing the result.
	 */
	public static final String FILENAME_HINT_KEY = "filenameHint";

	/**
	 * Key by which the id of the current transaction is stored in the model. If
	 * this is present, the QueryResultView will take care to release the
	 * connection back to the
	 * {@link org.eclipse.rdf4j.http.server.repository.transaction.ActiveTransactionRegistry}
	 * after processing the query result.
	 * 
	 * @since 2.8.5
	 */
	public static final String TRANSACTION_ID_KEY = "transactionID";

	public static final String HEADERS_ONLY = "headersOnly";

	@SuppressWarnings("rawtypes")
	public final void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		UUID txnId = null; 
		try {
			txnId = (UUID)model.get(TRANSACTION_ID_KEY);
			renderInternal(model, request, response);
		}
		finally {
			if (txnId != null) {
				ActiveTransactionRegistry.INSTANCE.returnTransactionConnection(txnId);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected abstract void renderInternal(Map model, HttpServletRequest request, HttpServletResponse response)
		throws IOException;

	protected void setContentType(HttpServletResponse response, FileFormat fileFormat)
		throws IOException
	{
		String mimeType = fileFormat.getDefaultMIMEType();
		if (fileFormat.hasCharset()) {
			Charset charset = fileFormat.getCharset();
			mimeType += "; charset=" + charset.name();
		}
		response.setContentType(mimeType);
	}

	@SuppressWarnings("rawtypes")
	protected void setContentDisposition(Map model, HttpServletResponse response, FileFormat fileFormat)
		throws IOException
	{
		// Report as attachment to make use in browser more convenient
		String filename = (String)model.get(FILENAME_HINT_KEY);

		if (filename == null || filename.length() == 0) {
			filename = "result";
		}

		if (fileFormat.getDefaultFileExtension() != null) {
			filename += "." + fileFormat.getDefaultFileExtension();
		}

		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
	}

	protected void logEndOfRequest(HttpServletRequest request) {
		if (logger.isInfoEnabled()) {
			String queryStr = request.getParameter(QUERY_PARAM_NAME);
			int qryCode = String.valueOf(queryStr).hashCode();
			logger.info("Request for query {} is finished", qryCode);
		}
	}

}
