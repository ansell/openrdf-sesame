/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.util.Iterator;

import info.aduna.iteration.Iteration;

import org.openrdf.model.Namespace;
import org.openrdf.query.impl.IteratorCursor;

/**
 * {@link Namespace} typed {@link Iteration}.
 * 
 * @author James Leigh
 * 
 */
public class NamespaceIteration extends IteratorCursor<Namespace>
{

	public NamespaceIteration(Iterator<? extends Namespace> iter) {
		super(iter);
	}

}
