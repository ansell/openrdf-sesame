/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.dataset.exceptions;

import org.openrdf.store.StoreException;

/**
 * If a query was trying to used a remote dataset, but the sail is not
 * configured to accept it.
 * 
 * @author James Leigh
 */
public class SailClosedException extends StoreException {

	private static final long serialVersionUID = 503699048360232725L;

}
