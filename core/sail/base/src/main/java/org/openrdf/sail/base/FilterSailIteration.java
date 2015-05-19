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
package org.openrdf.sail.base;

import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.sail.SailException;

/**
 * A {@link SailIteration} that wraps another Iteration, applying a filter on the
 * objects that are returned. Subclasses must implement the <tt>accept</tt>
 * method to indicate which objects should be returned.
 * 
 * @author James Leigh
 */
public abstract class FilterSailIteration<E> extends FilterIteration<E, SailException> implements
		SailIteration<E>
{

	public FilterSailIteration(Iteration<? extends E, ? extends SailException> iter) {
		super(iter);
	}

}
