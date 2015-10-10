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

package info.aduna.iterator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.openrdf.util.iterators.EmptyIterator;
import org.openrdf.util.iterators.Iterators;

/**
 * @author MJAHale
 */
public class UnionIterator<E> extends LookAheadIterator<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Iterator<? extends Iterable<? extends E>> argIter;

	private volatile Iterator<? extends E> currentIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new UnionIterator that returns the bag union of the results of
	 * a number of Iterators.
	 * 
	 * @param args
	 *        The Iterators containing the elements to iterate over.
	 */
	public UnionIterator(Iterable<? extends E>... args) {
		this(Arrays.asList(args));
	}

	public UnionIterator(Iterable<? extends Iterable<? extends E>> args) {
		argIter = args.iterator();

		// Initialize with empty iteration so that var is never null
		currentIter = new EmptyIterator<E>();
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	@Override
	protected E getNextElement()
	{
		if (currentIter.hasNext()) {
			return currentIter.next();
		}

		// Current Iterator exhausted, continue with the next one
		Iterators.closeSilently(currentIter);

		if (argIter.hasNext()) {
			currentIter = argIter.next().iterator();
		}
		else {
			// All elements have been returned
			return null;
		}

		return getNextElement();
	}

	@Override
	protected void handleClose()
		throws IOException
	{
		// Close this iteration, this will prevent lookAhead() from calling
		// getNextElement() again
		super.handleClose();

		Iterators.close(currentIter);
	}
}
