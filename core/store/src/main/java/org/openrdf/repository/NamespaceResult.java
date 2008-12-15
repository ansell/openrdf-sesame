/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.model.Namespace;
import org.openrdf.query.Cursor;

/**
 * @author James Leigh
 */
public class NamespaceResult extends RepositoryResult<Namespace> {

	public NamespaceResult(Cursor<? extends Namespace> delegate) {
		super(delegate);
	}

}
