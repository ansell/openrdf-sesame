/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import java.util.Random;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.http.HTTPBNode;


/**
 *
 * @author James Leigh
 */
public class HTTPValueFactory extends ValueFactoryImpl {
	private int key = new Random().nextInt();

	@Override
	public HTTPBNode createBNode() {
		return new HTTPBNode(super.createBNode().getID(), key);
	}

	@Override
	public HTTPBNode createBNode(String nodeID) {
		return new HTTPBNode(nodeID, key);
	}

	HTTPBNode createBNode(BNode bnode) {
		return new HTTPBNode(bnode.getID(), key);
	}

	boolean member(Resource... values) {
		if (values == null)
			return true;
		for (Resource value : values) {
			if (!member(value))
				return false;
		}
		return true;
	}

	boolean member(Value value) {
		if (value instanceof BNode) {
			if (value instanceof HTTPBNode) {
				HTTPBNode bnode = (HTTPBNode) value;
				return key == bnode.getKey();
			}
			return false;
		}
		return true;
	}

}
