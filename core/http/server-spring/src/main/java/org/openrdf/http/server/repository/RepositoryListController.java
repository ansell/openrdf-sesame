/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import org.openrdf.http.server.HTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * Handles requests for the list of repositories available on this server.
 * 
 * @author Herko ter Horst
 */
public class RepositoryListController extends AbstractController {

	private RepositoryManager repositoryManager;

	public RepositoryListController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET });
	}

	public void setRepositoryManager(RepositoryManager repMan) {
		repositoryManager = repMan;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws HTTPException
	{
		try {
			List<String> bindingNames = Arrays.asList("uri", "id", "title", "readable", "writable");
			List<BindingSet> bindingSets = new ArrayList<BindingSet>();

			// Determine the repository's URI
			StringBuffer requestURL = request.getRequestURL();
			if (requestURL.charAt(requestURL.length() - 1) != '/') {
				requestURL.append('/');
			}
			String namespace = requestURL.toString();

			ValueFactory vf = new ValueFactoryImpl();
			for (RepositoryInfo info : repositoryManager.getAllRepositoryInfos()) {
				String id = info.getId();
				URI uri = vf.createURI(namespace, id);
				Literal idLit = vf.createLiteral(id);
				Literal title = vf.createLiteral(info.getDescription());
				Literal readable = vf.createLiteral(info.isReadable());
				Literal writable = vf.createLiteral(info.isWritable());

				BindingSet bindings = new ListBindingSet(bindingNames, uri, idLit, title, readable, writable);
				bindingSets.add(bindings);
			}

			TupleQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(request, response,
					TupleQueryResultWriterRegistry.getInstance());

			Map<String, Object> model = new HashMap<String, Object>();
			model.put(QueryResultView.QUERY_RESULT_KEY, new TupleQueryResultImpl(bindingNames, bindingSets));
			model.put(QueryResultView.FILENAME_HINT_KEY, "repositories");
			model.put(QueryResultView.FACTORY_KEY, factory);

			return new ModelAndView(TupleQueryResultView.getInstance(), model);
		}
		catch (RepositoryConfigException e) {
			throw new ServerHTTPException(e.getMessage(), e);
		}
	}
}
