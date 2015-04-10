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

import java.io.Serializable;
import java.util.Iterator;

import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

class IterationStub extends CloseableIteratorIteration<BindingSet, QueryEvaluationException> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int hasNextCount = 0;

	int nextCount = 0;

	int removeCount = 0;

	@Override
	public void setIterator(Iterator<? extends BindingSet> iter) {
		super.setIterator(iter);
	}

	@Override
	public boolean hasNext()
		throws QueryEvaluationException
	{
		hasNextCount++;
		return super.hasNext();
	}

	@Override
	public BindingSet next()
		throws QueryEvaluationException
	{
		nextCount++;
		return super.next();
	}

	@Override
	public void remove() {
		removeCount++;
	}
}