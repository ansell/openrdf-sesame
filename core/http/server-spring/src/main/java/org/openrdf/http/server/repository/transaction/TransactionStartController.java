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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.RioSettingImpl;

/**
 * Handles requests for transaction creation on a repository.
 * 
 * @since 2.8.0
 * @author Jeen Broekstra
 */
public class TransactionStartController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public TransactionStartController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_POST });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		ModelAndView result;

		Repository repository = RepositoryInterceptor.getRepository(request);

		String reqMethod = request.getMethod();

		if (METHOD_POST.equals(reqMethod)) {
			logger.info("POST transaction start");
			result = startTransaction(repository, request, response);
			logger.info("transaction started");
		}
		else {
			throw new ClientHTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed: "
					+ reqMethod);
		}
		return result;
	}

	private ModelAndView startTransaction(Repository repository, HttpServletRequest request,
			HttpServletResponse response)
		throws IOException, ClientHTTPException, ServerHTTPException
	{
		ProtocolUtil.logRequestParameters(request);
		Map<String, Object> model = new HashMap<String, Object>();

		IsolationLevel isolationLevel = null;
		final String isolationLevelString = request.getParameter(Protocol.ISOLATION_LEVEL_PARAM_NAME);
		if (isolationLevelString != null) {
			final IRI level = SimpleValueFactory.getInstance().createIRI(isolationLevelString);
			
			// FIXME this needs to be adapted to accommodate custom isolation levels
			// from third party stores.
			for (IsolationLevel standardLevel : IsolationLevels.values()) {
				if (standardLevel.getURI().equals(level)) {
					isolationLevel = standardLevel;
					break;
				}
			}
		}

		try {
			RepositoryConnection conn = repository.getConnection();

			ParserConfig config = conn.getParserConfig();
			config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
			config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
			config.addNonFatalError(BasicParserSettings.VERIFY_LANGUAGE_TAGS);
			conn.begin(isolationLevel);
			UUID txnId = UUID.randomUUID();

			ActiveTransactionRegistry.INSTANCE.register(txnId, conn);
			model.put(SimpleResponseView.SC_KEY, SC_CREATED);
			final StringBuffer txnURL = request.getRequestURL();
			txnURL.append("/" + txnId.toString());
			Map<String, String> customHeaders = new HashMap<String, String>();
			customHeaders.put("Location", txnURL.toString());
			model.put(SimpleResponseView.CUSTOM_HEADERS_KEY, customHeaders);
			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Transaction start error: " + e.getMessage(), e);
		}
	}

}
