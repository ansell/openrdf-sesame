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
package org.openrdf.repository.sail;

import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailException;

/**
 * @author Herko ter Horst
 */
class SailCloseableIteration<E> extends ExceptionConvertingIteration<E, RepositoryException> {

	public SailCloseableIteration(Iteration<? extends E, ? extends SailException> iter) {
		super(iter);
	}

	@Override
	protected RepositoryException convert(Exception e)
	{
		if (e instanceof SailException) {
			return new RepositoryException(e);
		}
		else if (e instanceof RuntimeException) {
			throw (RuntimeException)e;
		}
		else if (e == null) {
			throw new IllegalArgumentException("e must not be null");
		}
		else {
			throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
		}
	}
}
