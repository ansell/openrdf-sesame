/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * An {@link Iteration} that can be closed to free resources that it is holding.
 * CloseableIterations automatically free their resources when exhausted. If not
 * read until exhaustion or if you want to make sure the iteration is properly
 * closed, any code using the iterator should be placed in a try-catch-finally
 * block, closing the iteration in the finally, e.g.:
 * 
 * <pre>
 * CloseableIteration&lt;Object, Exception&gt; iter = ...
 * try {
 *    // read objects from the iterator
 * }
 * catch(Exception e) {
 *   // process the exception that can be thrown while processing.
 * }
 * finally {
 *    iter.close();
 * }
 * </pre>
 */
public interface CloseableIteration<E, X extends Exception> extends Iteration<E, X> {

	/**
	 * Closes this iteration, freeing any resources that it is holding. If the
	 * iteration has already been closed then invoking this method has no effect.
	 */
	public void close()
		throws X;
	
	/**
	 * Adds all elements in the Iteration to the supplied collection. The
	 * Iteration is fully consumed and closed by this method.
	 * 
	 * @since 2.7.0
	 * @param collection
	 *        a {@link java.util.Collection} class to which the Iteration
	 *        elements are to be added.
	 * @return the collection object containing all Iteration elements.
	 * @throws X
	 */
	<C extends Collection<? super E>> C addTo(C collection)
		throws X;

	/**
	 * Adds all elements in the Iteration to a new {@link java.util.List}. The
	 * Iteration is fully consumed and closed by this method.
	 * 
	 * @since 2.7.0
	 * @return a {@link java.util.List} containing all query result elements.
	 * @throws X
	 */
	List<E> asList()
		throws X;

	/**
	 * Adds all elements in the Iteration to a new {@link java.util.Set}. The
	 * Iteration is fully consumed and closed by this method.
	 * 
	 * @since 2.7.0
	 * @return a {@link java.util.Set} containing all query result elements.
	 * @throws X
	 */
	Set<E> asSet()
		throws X;

}
