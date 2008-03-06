/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;


/**
 *
 * @author Herko ter Horst
 */
public class SailCloseableIteration<E> extends ExceptionConvertingIteration<E, RepositoryException> {

	/**
	 * @param iter
	 */
	public SailCloseableIteration(Iteration<? extends E, ? extends SailException> iter) {
		super(iter);
	}

	@Override
	protected RepositoryException convertedException(Exception e) {
		if(e instanceof SailException) {
			return new RepositoryException(e);			
		}
		else if(e instanceof RuntimeException) {
			throw (RuntimeException)e;
		}
		else {
			assert false : "Only SailExceptions and RuntimeExceptions should arrive here.";
			return null;
		}
	}

}
