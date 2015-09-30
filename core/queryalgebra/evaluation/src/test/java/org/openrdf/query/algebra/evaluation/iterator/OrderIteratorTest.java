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
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;


/**
 * 
 * @author james
 * 
 */
public class OrderIteratorTest extends TestCase {
	class IterationStub extends
			CloseableIteratorIteration<BindingSet, QueryEvaluationException> {
		int hasNextCount = 0;

		int nextCount = 0;

		int removeCount = 0;

		@Override
		public void setIterator(Iterator<? extends BindingSet> iter) {
			super.setIterator(iter);
		}

		@Override
		public boolean hasNext() throws QueryEvaluationException {
			hasNextCount++;
			return super.hasNext();
		}

		@Override
		public BindingSet next() throws QueryEvaluationException {
			nextCount++;
			return super.next();
		}

		@Override
		public void remove() {
			removeCount++;
		}
	}

	class SizeComparator implements Comparator<BindingSet> {
		public int compare(BindingSet o1, BindingSet o2) {
			return Integer.valueOf(o1.size()).compareTo(
					Integer.valueOf(o2.size()));
		}
	}

	class BindingSetSize implements BindingSet {

		private static final long serialVersionUID = -7968068342865378845L;

		private final int size;

		public BindingSetSize(int size) {
			super();
			this.size = size;
		}

		public Binding getBinding(String bindingName) {
			throw new UnsupportedOperationException();
		}

		public Set<String> getBindingNames() {
			throw new UnsupportedOperationException();
		}

		public Value getValue(String bindingName) {
			throw new UnsupportedOperationException();
		}

		public boolean hasBinding(String bindingName) {
			throw new UnsupportedOperationException();
		}

		public Iterator<Binding> iterator() {
			throw new UnsupportedOperationException();
		}

		public int size() {
			return size;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "#" + size;
		}
	}

	private IterationStub iteration;

	private OrderIterator order;

	private List<BindingSet> list;

	private BindingSet b1 = new BindingSetSize(1);

	private BindingSet b2 = new BindingSetSize(2);

	private BindingSet b3 = new BindingSetSize(3);

	private BindingSet b4 = new BindingSetSize(4);

	private BindingSet b5 = new BindingSetSize(5);

	private SizeComparator cmp;

	public void testFirstHasNext() throws Exception {
		order.hasNext();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	public void testHasNext() throws Exception {
		order.hasNext();
		order.next();
		order.hasNext();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	public void testFirstNext() throws Exception {
		order.next();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	public void testNext() throws Exception {
		order.next();
		order.next();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	public void testRemove() throws Exception {
		try {
			order.remove();
			fail();
		} catch (UnsupportedOperationException e) {
		}

	}

	public void testSorting() throws Exception {
		List<BindingSet> sorted = new ArrayList<BindingSet>(list);
		Collections.sort(sorted, cmp);
		for (BindingSet b : sorted) {
			assertEquals(b, order.next());
		}
		assertFalse(order.hasNext());
	}

	@Override
	protected void setUp() throws Exception {
		list = Arrays.asList(b3, b5, b2, b1, b4, b2);
		cmp = new SizeComparator();
		iteration = new IterationStub();
		iteration.setIterator(list.iterator());
		order = new OrderIterator(iteration, cmp);
	}

}
