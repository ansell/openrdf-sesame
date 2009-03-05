/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

/**
 * An interface for Sails that can be stacked on top of other Sails.
 */
public interface StackableSail extends Sail {

	/**
	 * Sets the base Sail that this Sail will work on top of. This method will be
	 * called before the initialize() method is called.
	 */
	public void setDelegate(Sail baseSail);

	/**
	 * Gets the base Sail that this Sail works on top of.
	 */
	public Sail getDelegate();
}
