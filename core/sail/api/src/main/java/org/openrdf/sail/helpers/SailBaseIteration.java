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
package org.openrdf.sail.helpers;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;

/**
 * An iteration extension that keeps a reference to the SailConnectionBase from
 * which it originates and signals when it is closed.
 * 
 * @author jeen
 */
class SailBaseIteration<T, E extends Exception> extends IterationWrapper<T, E> {

	private final SailConnectionBase connection;

	/**
	 * Creates a new memory-store specific iteration object.
	 * 
	 * @param lock
	 *        a query lock
	 * @param iter
	 *        the wrapped iteration over sail objects.
	 * @param connection
	 *        the connection from which this iteration originates.
	 */
	public SailBaseIteration(CloseableIteration<? extends T, ? extends E> iter, SailConnectionBase connection)
	{
		super(iter);
		this.connection = connection;
	}

	@Override
	public boolean hasNext()
		throws E
	{
		if (super.hasNext()) {
			return true;
		}
		else {
			// auto-close when exhausted
			close();
			return false;
		}
	}

	@Override
	protected void handleClose()
		throws E
	{
		super.handleClose();
		connection.iterationClosed(this);
	}

	@Deprecated
	protected void forceClose()
		throws E
	{
		close();
	}
}
