/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.IteratorIteration;

import org.openrdf.model.Namespace;
import org.openrdf.sail.SailException;

/**
 * {@link Namespace} typed {@link Iteration}.
 * 
 * @author James Leigh
 * 
 */
public class NamespaceIteration extends IteratorIteration<Namespace, SailException> implements
		CloseableIteration<Namespace, SailException>
{

	public NamespaceIteration(Iterator<? extends Namespace> iter) {
		super(iter);
	}

	public void close()
		throws SailException
	{
		// do nothing
	}

	public <C extends Collection<? super Namespace>> C addTo(C collection)
		throws SailException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<Namespace> asList()
		throws SailException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Namespace> asSet()
		throws SailException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
