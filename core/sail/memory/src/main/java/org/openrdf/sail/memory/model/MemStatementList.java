/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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

	private volatile MemStatement[] statements;

	private volatile int size;

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
		statements = new MemStatement[capacity];
		size = 0;
	}

	public MemStatementList(MemStatementList other) {
		this(other.size);
		addAll(other);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public MemStatement get(int index) {
		assert index >= 0 : "index < 0";
		assert index < size : "index >= size";

		return statements[index];
	}

	public void add(MemStatement st) {
		if (size == statements.length) {
			// Grow array
			growArray((size == 0) ? 1 : 2 * size);
		}

		statements[size] = st;
		++size;
	}

	public void addAll(MemStatementList other) {
		if (size + other.size >= statements.length) {
			// Grow array
			growArray(size + other.size);
		}

		System.arraycopy(other.statements, 0, statements, size, other.size);
		size += other.size;
	}

	public void remove(int index) {
		assert index >= 0 : "index < 0";
		assert index < size : "index >= size";

		if (index == size - 1) {
			// Last statement in array
			statements[index] = null;
			--size;
		}
		else {
			// Not last statement in array, move last
			// statement over the one at [index]
			--size;
			statements[index] = statements[size];
			statements[size] = null;
		}
	}

	public void remove(MemStatement st) {
		for (int i = 0; i < size; ++i) {
			if (statements[i] == st) {
				remove(i);
				return;
			}
		}
	}

	public void clear() {
		Arrays.fill(statements, 0, size, null);
		size = 0;
	}

	public void cleanSnapshots(int currentSnapshot) {
		int i = size - 1;

		// remove all deprecated statements from the end of the list
		for (; i >= 0; i--) {
			if (statements[i].getTillSnapshot() <= currentSnapshot) {
				--size;
				statements[i] = null;
			}
			else {
				i--;
				break;
			}
		}

		// remove all deprecated statements that are not at the end of the list
		for (; i >= 0; i--) {
			if (statements[i].getTillSnapshot() <= currentSnapshot) {
				// replace statement with last statement in the list
				--size;
				statements[i] = statements[size];
				statements[size] = null;
			}
		}
	}

	private void growArray(int newSize) {
		MemStatement[] newArray = new MemStatement[newSize];
		System.arraycopy(statements, 0, newArray, 0, size);
		statements = newArray;
	}
}
