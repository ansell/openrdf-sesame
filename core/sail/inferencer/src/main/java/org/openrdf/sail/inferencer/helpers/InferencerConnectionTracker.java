/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.helpers;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.helpers.SailConnectionTracker;
import org.openrdf.sail.inferencer.InferencerConnection;

/**
 * Takes care of closing of active connections and a grace period for active
 * connections during shutdown of the store.
 * 
 * @author Herko ter Horst
 * @author jeen
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class InferencerConnectionTracker extends SailConnectionTracker {

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected InferencerConnection wrapConnection(SailConnection connection) {
		assert connection != null : "connection must not be null";
		assert connection instanceof InferencerConnection : "connection should be an InferencerConnection, is: "
				+ connection.getClass();
		return new TrackingInferencerConnection((InferencerConnection)connection, this);
	}
}
