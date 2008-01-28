/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

/**
 * An interface for objects that want to be notified when the data in specific
 * Sail objects change.
 */
public interface SailChangedListener {
	
	/**
	 * Notifies the listener of a change to the data of a specific Sail.
	 */
	public void sailChanged(SailChangedEvent event);
}
