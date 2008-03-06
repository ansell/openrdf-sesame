/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

/**
 * @author Herko ter Horst
 */
public class QueryResultFunctions {

	public static final List<Binding> bindingsInOrder(List<String> bindingNames, BindingSet bindingSet) {
		List<Binding> result = new ArrayList<Binding>(bindingNames.size());

		for (String bindingName : bindingNames) {
			result.add(bindingSet.getBinding(bindingName));
		}

		return result;
	}
}
