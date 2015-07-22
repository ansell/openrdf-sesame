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

import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnectionListener;

/**
 * An implementation of the {@link org.openrdf.sail.NotifyingSailConnection}
 * interface that wraps another {@link org.openrdf.sail.NotifyingSailConnection}
 * object and forwards any method calls to the wrapped transaction.
 * 
 * @author Jeen Broekstra
 */
public class NotifyingSailConnectionWrapper extends SailConnectionWrapper implements NotifyingSailConnection {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new {@link NotifyingSailConnectionWrapper} object that wraps the
	 * supplied connection.
	 */
	public NotifyingSailConnectionWrapper(NotifyingSailConnection wrappedCon) {
		super(wrappedCon);
	}

	/*-----------------------*
	 * SailConnectionWrapper *
	 *-----------------------*/

	@Override
	public NotifyingSailConnection getWrappedConnection() {
		return (NotifyingSailConnection)super.getWrappedConnection();
	}

	/*-------------------------*
	 * NotifyingSailConnection *
	 *-------------------------*/

	/**
	 * Adds the given listener to the wrapped connection.
	 */
	@Override
	public void addConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().addConnectionListener(listener);
	}

	/**
	 * Removes the given listener from the wrapped connection.
	 */
	@Override
	public void removeConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().removeConnectionListener(listener);
	}
}
