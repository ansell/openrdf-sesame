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
import java.util.concurrent.atomic.AtomicReference;

/**
 * An Iteration that contains exactly one element.
 */
public class SingletonIteration<E, X extends Exception> extends AbstractCloseableIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final AtomicReference<E> value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new EmptyIteration.
	 */
	public SingletonIteration(E value) {
		this.value = new AtomicReference<E>(value);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean hasNext() {
		return value.get() != null;
	}

	public E next() {
		E result = value.getAndSet(null);
		if (result == null) {
			throw new NoSuchElementException();
		}
		return result;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		value.set(null);
	}
}
