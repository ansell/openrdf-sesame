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

import java.util.NoSuchElementException;

/**
 * An Iteration that limits the amount of elements that it returns from an
 * underlying Iteration to a fixed amount. This class returns the first
 * <tt>limit</tt> elements from the underlying Iteration and drops the rest.
 */
public class LimitIteration<E, X extends Exception> extends IterationWrapper<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The amount of elements to return.
	 */
	private final long limit;

	/**
	 * The number of elements that have been returned so far.
	 */
	private long returnCount;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new LimitIteration.
	 * 
	 * @param iter
	 *        The underlying Iteration, must not be <tt>null</tt>.
	 * @param limit
	 *        The number of query answers to return, must be &gt;= 0.
	 */
	public LimitIteration(Iteration<? extends E, X> iter, long limit) {
		super(iter);

		assert iter != null;
		assert limit >= 0;

		this.limit = limit;
		this.returnCount = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean hasNext()
		throws X
	{
		return returnCount < limit && super.hasNext();
	}

	@Override
	public E next()
		throws X
	{
		if (returnCount >= limit) {
			throw new NoSuchElementException("limit reached");
		}

		returnCount++;
		return super.next();
	}
}
