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

import java.util.List;

import org.junit.Test;

public class LimitIterationTest extends CloseableIterationTest {

	protected static LimitIteration<String, Exception> createLimitIteration(int limit) {
		return new LimitIteration<String, Exception>(createStringList1Iteration(), limit);
	}

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return createLimitIteration(5);
	}

	@Override
	protected int getTestIterationSize()
	{
		return 5;
	}

	@Test
	public void testInRangeOffset()
		throws Exception
	{
		for (int limit = 0; limit < stringList1.size(); limit++) {
			Iteration<String, Exception> iter = createLimitIteration(limit);
			List<String> resultList = Iterations.asList(iter);
			List<String> expectedList = stringList1.subList(0, limit);
			assertEquals("testInRangeOffset failed for limit: " + limit, expectedList, resultList);
		}
	}

	@Test
	public void testOutOfRangeOffset()
		throws Exception
	{
		Iteration<String, Exception> iter = createLimitIteration(2 * stringList1.size());
		List<String> resultList = Iterations.asList(iter);
		assertEquals(stringList1, resultList);
	}
}
