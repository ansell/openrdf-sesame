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
package org.openrdf.sail;


/**
 * A connection to an RDF Sail object. A SailConnection is active from the
 * moment it is created until it is closed. Care should be taken to properly
 * close SailConnections as they might block concurrent queries and/or updates
 * on the Sail while active, depending on the Sail-implementation that is being
 * used.
 * 
 * @author James Leigh
 */
public interface NotifyingSailConnection extends SailConnection {

	/**
	 * Registers a SailConnection listener with this SailConnection. The listener
	 * should be notified of any statements that are added or removed as part of
	 * this SailConnection.
	 * 
	 * @param listener
	 *        A SailConnectionListener.
	 */
	public void addConnectionListener(SailConnectionListener listener);

	/**
	 * Deregisters a SailConnection listener with this SailConnection.
	 * 
	 * @param listener
	 *        A SailConnectionListener.
	 */
	public void removeConnectionListener(SailConnectionListener listener);

}
