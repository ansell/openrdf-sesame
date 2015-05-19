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

import info.aduna.iteration.EmptyIteration;

import org.openrdf.sail.SailException;

/**
 * An {@link SailIteration} that does not contain any elements.
 * 
 * @author James Leigh
 */
public class EmptySailIteration<T> extends EmptyIteration<T, SailException> implements SailIteration<T> {

	private static final EmptySailIteration<?> instance = new EmptySailIteration<Object>();

	public static <T> EmptySailIteration<T> emptyIteration() {
		return (EmptySailIteration<T>)instance;
	}

}
