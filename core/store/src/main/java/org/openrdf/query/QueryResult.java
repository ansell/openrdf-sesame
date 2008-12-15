/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.repository.RepositoryResult;

/**
 * Super type of all query result types (TupleQueryResult, GraphQueryResult,
 * etc.).
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
@Deprecated
public interface QueryResult<T> extends RepositoryResult<T> {
}
