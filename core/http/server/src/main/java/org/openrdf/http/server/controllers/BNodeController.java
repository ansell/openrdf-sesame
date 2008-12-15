/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.server.repository.RepositoryInterceptor.getReadOnlyConnection;
import static org.openrdf.http.server.repository.RepositoryInterceptor.notSafe;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.exceptions.BadRequest;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.results.TupleResult;
import org.openrdf.results.impl.TupleResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
@Controller
public class BNodeController {

	@ModelAttribute
	@RequestMapping(method = POST, value = CONN_PATH + "/bnodes")
	public TupleResult post(HttpServletRequest request)
		throws StoreException, BadRequest
	{
		notSafe(request);
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		int size = 1;
		String amount = request.getParameter(Protocol.AMOUNT);
		if (amount != null) {
			try {
				size = Integer.parseInt(amount);
			}
			catch (NumberFormatException e) {
				throw new BadRequest(e.toString());
			}
		}
		ValueFactory vf = repositoryCon.getValueFactory();
		List<String> columns = Arrays.asList(Protocol.BNODE);
		List<BindingSet> bnodes = new ArrayList<BindingSet>(size);
		for (int i = 0; i < size; i++) {
			bnodes.add(new ListBindingSet(columns, vf.createBNode()));
		}
		return new TupleResultImpl(columns, bnodes);
	}

}
