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
 * An interface for an RDF Storage And Inference Layer. RDF Sails can store RDF
 * statements and evaluate queries over them. Statements can be stored in named
 * contexts or in the null context. Contexts can be used to group sets of
 * statements that logically belong together, for example because they come from
 * the same source. Both URIs and bnodes can be used as context identifiers.
 * 
 * @author James Leigh
 */
public interface NotifyingSail extends Sail {

	/**
	 * Opens a connection on the Sail which can be used to query and update data.
	 * Depending on how the implementation handles concurrent access, a call to
	 * this method might block when there is another open connection on this
	 * Sail.
	 * 
	 * @throws SailException
	 *         If no transaction could be started, for example because the Sail
	 *         is not writable.
	 */
	public NotifyingSailConnection getConnection()
		throws SailException;

	/**
	 * Adds the specified SailChangedListener to receive events when the data in
	 * this Sail object changes.
	 */
	public void addSailChangedListener(SailChangedListener listener);

	/**
	 * Removes the specified SailChangedListener so that it no longer receives
	 * events from this Sail object.
	 */
	public void removeSailChangedListener(SailChangedListener listener);
}
