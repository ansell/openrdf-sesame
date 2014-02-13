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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.webapp.views.EmptySuccessView;
import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.OpenRDFException;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.Protocol.Action;
import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;

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
		Action action = Action.valueOf(request.getParameter(Protocol.ACTION_PARAM_NAME));

		Map<String, Object> model = new HashMap<String, Object>();

		try {
			switch (action) {
				case ADD:
					conn.add(request.getInputStream(), null, RDFFormat.BINARY);
					break;
				case DELETE:
					RDFParser parser = Rio.createParser(RDFFormat.BINARY, conn.getValueFactory());
					parser.setRDFHandler(new WildcardRDFRemover(conn));
					parser.parse(request.getInputStream(), null);
					break;
				case GET:
					// TODO
					break;
				case COMMIT:
					conn.commit();
					ActiveTransactionRegistry.getInstance().deregister(getTransactionID(request), conn);
					conn.close();
					break;
				case ROLLBACK:
					conn.rollback();
					ActiveTransactionRegistry.getInstance().deregister(getTransactionID(request), conn);
					conn.close();
					break;
				default:
					throw new ClientHTTPException("action not recognized: " + action);
			}

			model.put(SimpleResponseView.SC_KEY, HttpServletResponse.SC_OK);
			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}
		catch (OpenRDFException e) {
			throw new ServerHTTPException("Transaction handling error: " + e.getMessage(), e);
		}
	}

	private static class WildcardRDFRemover extends RDFHandlerBase {

		private final RepositoryConnection conn;

		public WildcardRDFRemover(RepositoryConnection conn) {
			super();
			this.conn = conn;
		}

		@Override
		public void handleStatement(Statement st)
			throws RDFHandlerException
		{
			Resource subject = SESAME.WILDCARD.equals(st.getSubject()) ? null : st.getSubject();
			URI predicate = SESAME.WILDCARD.equals(st.getPredicate()) ? null : st.getPredicate();
			Value object = SESAME.WILDCARD.equals(st.getObject()) ? null : st.getObject();
			try {
				conn.remove(subject, predicate, object, st.getContext());
			}
			catch (RepositoryException e) {
				throw new RDFHandlerException(e);
			}
		}

	}
}
