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
package info.aduna.iteration;

/**
 * Removes consecutive duplicates from the object stream.
 * 
 * @author Arjohn Kampman
 */
public class ReducedIteration<E, X extends Exception> extends FilterIteration<E, X> {

	private E previousObject;

	public ReducedIteration(Iteration<? extends E, ? extends X> delegate) {
		super(delegate);
	}

	@Override
	protected boolean accept(E nextObject) {
		if (nextObject.equals(previousObject)) {
			return false;
		}
		else {
			previousObject = nextObject;
			return true;
		}
	}
}
