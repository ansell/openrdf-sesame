/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lucene;

import org.openrdf.model.Statement;


/**
 * Specifies a filter, which determines whether a statement should be included in the keyword index
 * when performing complete reindexing. See {@link LuceneSail#registerStatementFilter(IndexableStatementFilter)}.} 
 *
 * @author andriy.nikolov
 */
public interface IndexableStatementFilter {

	public boolean accept(Statement statement);
	
}