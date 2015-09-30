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
package org.openrdf.sail.federation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James Leigh
 */
public class PrefixHashSet {

	private int length = Integer.MAX_VALUE; // NOPMD

	private final Map<String, List<String>> index = new HashMap<String, List<String>>();

	public PrefixHashSet(Iterable<String> values) {
		for (String value : values) {
			if (value.length() < length) {
				length = value.length();
			}
		}
		for (String value : values) {
			String key = value.substring(0, length);
			List<String> entry = index.get(key);
			if (entry == null) {
				index.put(key, entry = new ArrayList<String>()); // NOPMD
			}
			entry.add(value.substring(length));
		}
	}

	public boolean match(String value) {
		boolean result = false;
		if (value.length() >= length) {
			String key = value.substring(0, length);
			List<String> entry = index.get(key);
			if (entry != null) {
				result = matchValueToEntry(value, entry);
			}
		}
		return result;
	}

	private boolean matchValueToEntry(String value, List<String> entry) {
		boolean result = false;
		String tail = value.substring(length);
		for (String prefix : entry) {
			if (tail.startsWith(prefix)) {
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return index.toString();
	}
}
