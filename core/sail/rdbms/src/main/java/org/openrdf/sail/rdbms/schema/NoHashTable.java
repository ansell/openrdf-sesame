/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

/**
 * 
 * @author James Leigh
 */
public class NoHashTable extends HashTable {

	public NoHashTable() {
		super(null);
	}

	@Override
	public void close() {
	}

	@Override
	public boolean expungeRemovedStatements(int count, String condition) {
		return false;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void initialize() {
	}

	@Override
	public void insert(long id, long hash) {
	}

	@Override
	public void optimize() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
