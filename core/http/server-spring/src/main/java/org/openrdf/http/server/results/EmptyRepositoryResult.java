/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.results;

import org.openrdf.query.Cursor;
import org.openrdf.query.algebra.evaluation.cursors.EmptyCursor;
import org.openrdf.repository.RepositoryResult;


/**
 *
 * @author James Leigh
 */
@SuppressWarnings("unchecked")
public class EmptyRepositoryResult<T> extends RepositoryResult<T> {

	public static <T> EmptyRepositoryResult<T> emptyRepositoryResult() {
		return new EmptyRepositoryResult();
	}

	public EmptyRepositoryResult() {
		super((Cursor<? extends T>)EmptyCursor.emptyCursor());
	}

}
