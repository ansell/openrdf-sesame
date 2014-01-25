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
package org.openrdf.http.server.repository.transaction;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.apple.dnssd.TXTRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.webapp.views.EmptySuccessView;
import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Handles requests for transaction creation on a repository.
 * 
 * @since 2.8.0
 * @author Jeen Broekstra
 */
public class TransactionController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<UUID, RepositoryConnection> activeConnections = new HashMap<UUID, RepositoryConnection>();

	public TransactionController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { "PUT", "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		ModelAndView result;

		ActiveTransactionRegistry txnRegistry = ActiveTransactionRegistry.getInstance();
		
		String reqMethod = request.getMethod();
		UUID transactionId = getTransactionID(request);
		logger.debug("transaction id: {}", transactionId);
		RepositoryConnection connection = txnRegistry.getTransactionConnection(transactionId);

		try {
			if ("PUT".equals(reqMethod)) {
				logger.info("PUT transaction");
				result = processTransactionOperation(connection, request, response);
				logger.info("PUT transaction request finished.");
			}
			else if ("DELETE".equals(reqMethod)) {
				logger.info("DELETE transaction");

				connection.rollback();
				txnRegistry.deregister(transactionId, connection);
				connection.close();

				result = new ModelAndView(EmptySuccessView.getInstance());
				logger.info("PUT transaction request finished.");
			}
			else {
				throw new ClientHTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed: "
						+ reqMethod);
			}
		}
		finally {
			txnRegistry.returnTransactionConnection(transactionId);
		}
		return result;
	}

	private UUID getTransactionID(HttpServletRequest request)
		throws ClientHTTPException
	{
		String pathInfoStr = request.getPathInfo();
		logger.debug("path info: {}", pathInfoStr);

		UUID txnID = null;

		if (pathInfoStr != null && !pathInfoStr.equals("/")) {
			String[] pathInfo = pathInfoStr.substring(1).split("/");
			if (pathInfo.length > 0) {
				txnID = UUID.fromString(pathInfo[3]); // FIXME test
				logger.debug("txnID is '{}'", txnID);
			}
		}

		return txnID;
	}

	private ModelAndView processTransactionOperation(RepositoryConnection conn, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientHTTPException, ServerHTTPException
	{
		ProtocolUtil.logRequestParameters(request);
		Map<String, Object> model = new HashMap<String, Object>();

		try {
			// TODO read and execute transaction operation.
			model.put(SimpleResponseView.SC_KEY, SC_CREATED);
			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}
		catch (Exception e) {
			throw new ServerHTTPException("Transaction error: " + e.getMessage(), e);
		}
	}

}
