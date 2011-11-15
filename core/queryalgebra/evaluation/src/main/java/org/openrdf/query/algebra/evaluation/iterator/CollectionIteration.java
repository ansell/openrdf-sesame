/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Collection;
import java.util.Iterator;

import info.aduna.iteration.CloseableIterationBase;

/**
 * An iteration to access a materialized {@link Collection} of BindingSets.
 * 
 * @author Andreas Schwarte
 */
public class CollectionIteration<E, X extends Exception> extends CloseableIterationBase<E, X> {

	
	protected final Collection<E> collection;
	protected Iterator<E> iterator;	
	
	/**
	 * @param collection
	 */
	public CollectionIteration(Collection<E> collection) {
		super();
		this.collection = collection;
		iterator = collection.iterator();
	}
	

	public boolean hasNext() throws X {
		return iterator.hasNext();
	}

	public E next() throws X {
		return iterator.next();
	}

	public void remove() throws X {
		throw new UnsupportedOperationException("Remove not supported on CollectionIteration");		
	}
	

}
