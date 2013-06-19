/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package info.aduna.iteration;

import java.util.Arrays;
import java.util.Iterator;

/**
 * An Iteration that returns the bag union of the results of a number of
 * Iterations. 'Bag union' means that the UnionIteration does not filter
 * duplicate objects.
 */
public class UnionIteration<E, X extends Exception> extends LookAheadIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Iterator<? extends Iteration<? extends E, X>> argIter;

	private volatile Iteration<? extends E, X> currentIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new UnionIteration that returns the bag union of the results of
	 * a number of Iterations.
	 * 
	 * @param args
	 *        The Iterations containing the elements to iterate over.
	 */
	public UnionIteration(Iteration<? extends E, X>... args) {
		this(Arrays.asList(args));
	}

	/**
	 * Creates a new UnionIteration that returns the bag union of the results of
	 * a number of Iterations.
	 * 
	 * @param args
	 *        The Iterations containing the elements to iterate over.
	 */
	public UnionIteration(Iterable<? extends Iteration<? extends E, X>> args) {
		argIter = args.iterator();

		// Initialize with empty iteration so that var is never null
		currentIter = new EmptyIteration<E, X>();
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected E getNextElement()
		throws X
	{
		if (currentIter.hasNext()) {
			return currentIter.next();
		}

		// Current Iteration exhausted, continue with the next one
		Iterations.closeCloseable(currentIter);

		synchronized (this) {
			if (argIter.hasNext()) {
				currentIter = argIter.next();
			}
			else {
				// All elements have been returned
				return null;
			}
		}

		return getNextElement();
	}

	@Override
	protected void handleClose()
		throws X
	{
		// Close this iteration, this will prevent lookAhead() from calling
		// getNextElement() again
		super.handleClose();

		synchronized (this) {
			while (argIter.hasNext()) {
				Iterations.closeCloseable(argIter.next());
			}
		}

		Iterations.closeCloseable(currentIter);
	}
}
