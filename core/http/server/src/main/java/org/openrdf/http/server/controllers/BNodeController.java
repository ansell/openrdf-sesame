/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.server.interceptors.RepositoryInterceptor.getReadOnlyConnection;
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
import org.openrdf.http.server.helpers.Paths;
import org.openrdf.http.server.interceptors.ConditionalRequestInterceptor;
import org.openrdf.model.BNode;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
@Controller
public class BNodeController {

	@ModelAttribute
	@RequestMapping(method = POST, value = Paths.CONNECTION_BNODES)
	public TupleResult post(HttpServletRequest request)
		throws StoreException, BadRequest
	{
		int amount = getAmount(request);
		String nodeID = request.getParameter(Protocol.NODE_ID);

		ConditionalRequestInterceptor.notSafe(request);
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		ValueFactory vf = repositoryCon.getValueFactory();

		List<String> columns = Arrays.asList(Protocol.BNODE);
		List<BindingSet> bnodes = new ArrayList<BindingSet>(amount);
		for (int i = 0; i < amount; i++) {
			BNode bnode = createBNode(vf, nodeID, i);
			bnodes.add(new ListBindingSet(columns, bnode));
		}

		return new TupleResultImpl(columns, bnodes);
	}

	private int getAmount(HttpServletRequest request)
		throws BadRequest
	{
		String amount = request.getParameter(Protocol.AMOUNT);

		if (amount == null) {
			return 1;
		}

		try {
			return Integer.parseInt(amount);
		}
		catch (NumberFormatException e) {
			throw new BadRequest("Invalid amount parameter: " + amount);
		}
	}

	private BNode createBNode(ValueFactory vf, String nodeID, int i) {
		if (i == 0 && nodeID != null) {
			return vf.createBNode(nodeID);
		}
		return vf.createBNode();
	}
}
