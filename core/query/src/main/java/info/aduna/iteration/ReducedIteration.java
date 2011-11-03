/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

/**
 * Removes consecutive duplicates from the object stream.
 * 
 * @author Arjohn Kampman
 */
public class ReducedIteration<E, X extends Exception> extends FilterIteration<E, X> {

	private E previousObject;

	public ReducedIteration(Iteration<? extends E, ? extends X> delegate) {
		super(delegate);
	}

	@Override
	protected boolean accept(E nextObject) {
		if (nextObject.equals(previousObject)) {
			return false;
		}
		else {
			previousObject = nextObject;
			return true;
		}
	}
}
