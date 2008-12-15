/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.model.Resource;
import org.openrdf.query.Cursor;


/**
 *
 * @author Jabmes Leigh
 */
public class ContextResult extends RepositoryResult<Resource> {

	public ContextResult(Cursor<? extends Resource> delegate) {
		super(delegate);
	}

}
