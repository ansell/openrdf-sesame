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

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class OffsetIterationTest extends CloseableIterationTest {

	protected static OffsetIteration<String, Exception> createOffsetIteration(int offset) {
		return new OffsetIteration<String, Exception>(createStringList1Iteration(), offset);
	}

	@Override
	protected CloseableIteration<String, Exception> createTestIteration()
	{
		return createOffsetIteration(5);
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
		for (int offset = 0; offset < stringList1.size(); offset++) {
			Iteration<String, Exception> iter = createOffsetIteration(offset);
			List<String> resultList = Iterations.asList(iter);
			List<String> expectedList = stringList1.subList(offset, stringList1.size());
			assertEquals("test failed for offset: " + offset, expectedList, resultList);
		}
	}

	@Test
	public void testOutOfRangeOffset()
		throws Exception
	{
		Iteration<String, Exception> iter = createOffsetIteration(2 * stringList1.size());
		List<String> resultList = Iterations.asList(iter);
		assertEquals(Collections.emptyList(), resultList);
	}
}
