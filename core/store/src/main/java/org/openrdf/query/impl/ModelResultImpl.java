/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.impl;

import org.openrdf.model.Statement;
import org.openrdf.query.Cursor;
import org.openrdf.repository.ModelResult;



/**
 *
 * @author James Leigh
 */
public class ModelResultImpl extends GraphResultImpl implements ModelResult {

	public ModelResultImpl(Cursor<? extends Statement> delegate) {
		super(delegate);
	}

}
