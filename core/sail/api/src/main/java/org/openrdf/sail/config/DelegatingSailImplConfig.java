/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;


/**
 * @author Arjohn Kampman
 */
public interface DelegatingSailImplConfig extends SailImplConfig {

	public SailImplConfig getDelegate();
}