/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.model.Statement;
import org.openrdf.query.Cursor;



/**
 *
 * @author James Leigh
 */
public class ModelResult extends RepositoryResult<Statement> {

	public ModelResult(Cursor<? extends Statement> delegate) {
		super(delegate);
	}

}
