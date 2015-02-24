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
package org.openrdf.query.algebra.evaluation.iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

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
public class OrderIteratorTest {
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

	@Test
	public void testFirstHasNext() throws Exception {
		order.hasNext();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	@Test
	public void testHasNext() throws Exception {
		order.hasNext();
		order.next();
		order.hasNext();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	@Test
	public void testFirstNext() throws Exception {
		order.next();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	@Test
	public void testNext() throws Exception {
		order.next();
		order.next();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemove() throws Exception {
		order.remove();
	}

	@Test
	public void testSorting() throws Exception {
		List<BindingSet> sorted = new ArrayList<BindingSet>(list);
		Collections.sort(sorted, cmp);
		for (BindingSet b : sorted) {
			assertEquals(b, order.next());
		}
		assertFalse(order.hasNext());
	}

	@Before
	public void setUp() throws Exception {
		list = Arrays.asList(b3, b5, b2, b1, b4, b2);
		cmp = new SizeComparator();
		iteration = new IterationStub();
		iteration.setIterator(list.iterator());
		order = new OrderIterator(iteration, cmp);
	}

}
