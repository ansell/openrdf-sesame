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
package info.aduna.iterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;


/**
 *
 * @author Mark
 */
public class CloseableIterationIterator<E> implements Iterator<E>, Closeable {
	private final Iteration<? extends E, ? extends RuntimeException> iteration;

	public CloseableIterationIterator(Iteration<? extends E, ? extends RuntimeException> iteration) {
		this.iteration = iteration;
	}

	@Override
	public boolean hasNext() {
		return iteration.hasNext();
	}

	@Override
	public E next() {
		return iteration.next();
	}

	@Override
	public void close()
		throws IOException
	{
		Iterations.closeCloseable(iteration);
	}
}
