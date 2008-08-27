/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.util.Iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.IteratorIteration;

import org.openrdf.model.Namespace;
import org.openrdf.StoreException;

/**
 * {@link Namespace} typed {@link Iteration}.
 * 
 * @author James Leigh
 * 
 */
public class NamespaceIteration extends IteratorIteration<Namespace, StoreException> implements
		CloseableIteration<Namespace, StoreException>
{

	public NamespaceIteration(Iterator<? extends Namespace> iter) {
		super(iter);
	}

	public void close()
		throws StoreException
	{
		// do nothing
	}

}
