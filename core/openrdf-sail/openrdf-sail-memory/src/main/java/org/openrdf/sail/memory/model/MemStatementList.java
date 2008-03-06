/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.util.Arrays;

/**
 * A dedicated data structure for storing MemStatement objects, offering
 * operations optimized for their use in the memory Sail.
 */
public class MemStatementList {
	
	/*-----------*
	 * Variables *
	 *-----------*/
	
	private MemStatement[] _statements;
	private int _size;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemStatementList.
	 */
	public MemStatementList() {
		this(4);
	}

	public MemStatementList(int capacity) {
		_statements = new MemStatement[capacity];
		_size = 0;
	}

	public MemStatementList(MemStatementList other) {
		this(other._size);
		addAll(other);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public int size() {
		return _size;
	}

	public boolean isEmpty() {
		return _size == 0;
	}

	public MemStatement get(int index) {
		if (index < 0 || index >= _size) {
			if (index < 0) {
				throw new IndexOutOfBoundsException("index < 0");
			} else {
				throw new IndexOutOfBoundsException("index >= size");
			}
		}

		return _statements[index];
	}

	public void add(MemStatement st) {
		if (_size == _statements.length) {
			// Grow array
			_growArray( (_size==0) ? 1 : 2*_size );
		}

		_statements[_size] = st;
		++_size;
	}

	public void addAll(MemStatementList other) {
		if (_size + other._size >= _statements.length) {
			// Grow array
			_growArray(_size + other._size);
		}

		System.arraycopy(other._statements, 0, _statements, _size, other._size);
		_size += other._size;
	}

	public void remove(int index) {
		if (index < 0 || index >= _size) {
			if (index < 0) {
				throw new IndexOutOfBoundsException("index < 0");
			} else {
				throw new IndexOutOfBoundsException("index >= size");
			}
		}

		if (index == _size - 1) {
			// Last statement in array
			_statements[index] = null;
			--_size;
		}
		else {
			// Not last statement in array, move last
			// statement over the one at [index]
			--_size;
			_statements[index] = _statements[_size];
			_statements[_size] = null;
		}
	}

	public void remove(MemStatement st) {
		for (int i = 0; i < _size; ++i) {
			if (_statements[i] == st) {
				remove(i);
				return;
			}
		}
	}

	public void clear() {
		Arrays.fill(_statements, 0, _size, null);
		_size = 0;
	}

	private void _growArray(int size) {
		MemStatement[] newArray = new MemStatement[size];
		System.arraycopy(_statements, 0, newArray, 0, _size);
		_statements = newArray;
	}
}
