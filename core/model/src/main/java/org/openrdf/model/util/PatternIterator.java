/*
 * Copyright (c) 2012 3 Round Stones Inc., Some rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution. 
 * - Neither the name of the openrdf.org nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package org.openrdf.model.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

/**
 * Excludes an Iterator<Statement> based on a given basic graph pattern.
 */
public class PatternIterator<S extends Statement> implements Iterator<S> {

	private final Iterator<S> filteredIter;
	private final Value subj;
	private final Value pred;
	private final Value obj;
	private final Value[] contexts;
	private S nextElement;
	private boolean nextCalled;

	public PatternIterator(Iterator<S> iter, Value subj, Value pred,
			Value obj, Value... contexts) {
		this.filteredIter = iter;
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
		this.contexts = notNull(contexts);
	}

	public boolean hasNext() {
		findNextElement();

		return nextElement != null;
	}

	public S next() {
		findNextElement();

		S result = nextElement;

		if (result != null) {
			nextElement = null;
			nextCalled = true;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	private void findNextElement() {
		while (nextElement == null && filteredIter.hasNext()) {
			S candidate = filteredIter.next();

			if (accept(candidate)) {
				nextElement = candidate;
			}
		}
	}

	public void remove() {
		if (!nextCalled)
			throw new IllegalStateException();
		filteredIter.remove();
	}

	/**
	 * Tests whether or not the specified object should be returned by this
	 * iterator. All objects from the wrapped iterator pass through this method
	 * in the same order as they are coming from the wrapped iterator.
	 * 
	 * @param object
	 *        The object to be tested.
	 * @return <tt>true</tt> if the object should be returned, <tt>false</tt>
	 *         otherwise.
	 * @throws X
	 */
	protected boolean accept(S st) {
		if (subj != null && !subj.equals(st.getSubject())) {
			return false;
		}
		if (pred != null && !pred.equals(st.getPredicate())) {
			return false;
		}
		if (obj != null && !obj.equals(st.getObject())) {
			return false;
		}
		Resource stContext = st.getContext();
		if (contexts != null && contexts.length == 0) {
			// Any context matches
			return true;
		} else {
			// Accept if one of the contexts from the pattern matches
			for (Value context : notNull(contexts)) {
				if (context == null && stContext == null) {
					return true;
				}
				if (context != null && context.equals(stContext)) {
					return true;
				}
			}

			return false;
		}
	}

	private Value[] notNull(Value[] contexts) {
		if (contexts == null) {
			return new Resource[] { null };
		}
		return contexts;
	}
}
