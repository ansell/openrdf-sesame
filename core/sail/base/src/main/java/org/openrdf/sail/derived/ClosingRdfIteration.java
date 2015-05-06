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
package org.openrdf.sail.derived;

import java.util.Iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.sail.SailException;

/**
 * @author James Leigh
 */
public class ClosingRdfIteration<E> extends ClosingIteration<E, SailException> implements RdfIteration<E> {

	public static <E> ClosingRdfIteration<E> close(CloseableIteration<? extends E, SailException> iter,
			RdfClosable... closes)
	{
		return new ClosingRdfIteration<E>(iter, closes);
	}

	public static <E> ClosingRdfIteration<E> close(Iterator<? extends E> iter,
			RdfClosable... closes)
	{
		return new ClosingRdfIteration<E>(new CloseableIteratorIteration<E, SailException>(iter), closes);
	}

	/**
	 * Creates a new LockingIteration.
	 * 
	 * @param lock
	 *        The lock to release when the itererator is closed, must not be
	 *        <tt>null</tt>.
	 * @param iter
	 *        The underlying Iteration, must not be <tt>null</tt>.
	 */
	protected ClosingRdfIteration(CloseableIteration<? extends E, SailException> iter, RdfClosable... closes) {
		super(iter, closes);
	}

	@Override
	protected void handleSailException(SailException e)
		throws SailException
	{
		throw e;
	}
}
