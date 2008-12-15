/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.results.impl;

import org.openrdf.model.Statement;
import org.openrdf.results.Cursor;
import org.openrdf.results.ModelResult;



/**
 *
 * @author James Leigh
 */
public class ModelResultImpl extends GraphResultImpl implements ModelResult {

	public ModelResultImpl(Cursor<? extends Statement> delegate) {
		super(delegate);
	}

}
