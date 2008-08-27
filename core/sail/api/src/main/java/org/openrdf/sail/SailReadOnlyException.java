/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import org.openrdf.StoreException;

/**
 * Indicates that the current write operation did not succeed because the SAIL
 * cannot be written to, it can only be read from.
 * 
 * @author James Leigh
 */
public class SailReadOnlyException extends StoreException {
	private static final long serialVersionUID = 2439801771913652923L;

	public SailReadOnlyException(String msg) {
		super(msg);
	}

}
