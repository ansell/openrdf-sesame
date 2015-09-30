/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.memory.model;

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
