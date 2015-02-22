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
package org.openrdf.rio.helpers;

import java.util.Iterator;

/**
 * Helper class for working with Strings as sequences of Unicode code points.
 * 
 * @author Jeen Broekstra
 * @since 2.8.0
 * @see CodePointIterator
 */
public class CodePointSequence implements Iterable<Integer> {

	private final String charSequence;

	public CodePointSequence(String charSequence) {
		this.charSequence = charSequence;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new CodePointIterator(charSequence);
	}
	
	public int length() {
		return charSequence.codePointCount(0, charSequence.length());
	}

}
