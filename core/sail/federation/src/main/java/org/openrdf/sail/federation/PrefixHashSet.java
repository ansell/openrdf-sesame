/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
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
