/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author James Leigh
 */
public class PrefixHashSet {

	private int length = Integer.MAX_VALUE;

	private Map<String, List<String>> index = new HashMap<String, List<String>>();

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
				index.put(key, entry = new ArrayList<String>());
			}
			entry.add(value.substring(length));
		}
	}

	public boolean match(String value) {
		if (value.length() < length)
			return false;
		String key = value.substring(0, length);
		List<String> entry = index.get(key);
		if (entry == null)
			return false;
		String tail = value.substring(length);
		for (String prefix : entry) {
			if (tail.startsWith(prefix))
				return true;
		}
		return false;
	}
}
