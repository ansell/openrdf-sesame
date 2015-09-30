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
package org.eclipse.rdf4j.common.iteration;

import static org.junit.Assert.assertTrue;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.SingletonIteration;
import org.eclipse.rdf4j.common.iteration.UnionIteration;
import org.junit.Test;

public class UnionIterationTest extends CloseableIterationTest {

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return new UnionIteration<String, Exception>(createStringList1Iteration(), createStringList2Iteration());
	}

	@Override
	protected int getTestIterationSize()
	{
		return stringList1.size() + stringList2.size();
	}

	@Test
	public void testArgumentsClosed()
		throws Exception
	{
		SingletonIteration<String, Exception> iter1 = new SingletonIteration<String, Exception>("1");
		SingletonIteration<String, Exception> iter2 = new SingletonIteration<String, Exception>("2");
		SingletonIteration<String, Exception> iter3 = new SingletonIteration<String, Exception>("3");
		UnionIteration<String, Exception> unionIter = new UnionIteration<String, Exception>(iter1, iter2, iter3);

		unionIter.next();
		unionIter.close();

		assertTrue("iter1 should have been closed", iter1.isClosed());
		assertTrue("iter2 should have been closed", iter2.isClosed());
		assertTrue("iter3 should have been closed", iter3.isClosed());
	}
}
