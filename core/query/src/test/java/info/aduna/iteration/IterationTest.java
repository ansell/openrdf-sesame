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
package info.aduna.iteration;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public abstract class IterationTest {

	protected static final List<String> stringList1 = Arrays.asList("1", "2", "3", "4", "5", "1", "2", "3",
			"4", "5");

	protected static final List<String> stringList2 = Arrays.asList("4", "5", "6", "7", "8");

	protected static CloseableIteration<String, Exception> createStringList1Iteration() {
		return new CloseableIteratorIteration<String, Exception>(stringList1.iterator());
	}

	protected static CloseableIteration<String, Exception> createStringList2Iteration() {
		return new CloseableIteratorIteration<String, Exception>(stringList2.iterator());
	}

	protected abstract Iteration<String, Exception> createTestIteration()
		throws Exception;

	protected abstract int getTestIterationSize();

	@Test
	public void testFullIteration()
		throws Exception
	{
		Iteration<String, Exception> iter = createTestIteration();
		int count = 0;

		while (iter.hasNext()) {
			iter.next();
			count++;
		}

		assertEquals("test iteration contains incorrect number of elements", getTestIterationSize(), count);
	}
}
