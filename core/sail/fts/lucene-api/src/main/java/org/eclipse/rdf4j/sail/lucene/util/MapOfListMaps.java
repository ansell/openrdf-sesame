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
package org.eclipse.rdf4j.sail.lucene.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andriy.nikolov
 */
public class MapOfListMaps<Index1Type, Index2Type, DataType> {

	private final Map<Index1Type, Map<Index2Type, List<DataType>>> data;

	/**
	 * 
	 */
	public MapOfListMaps() {
		data = new HashMap<Index1Type, Map<Index2Type, List<DataType>>>();
	}

	public List<DataType> get(Index1Type key1, Index2Type key2) {
		Map<Index2Type, List<DataType>> intermediateMap = data.get(key1);
		if (intermediateMap != null) {
			List<DataType> tmp = intermediateMap.get(key2);
			if (tmp != null)
				return tmp;
		}
		return Collections.emptyList();
	}

	public Map<Index2Type, List<DataType>> get(Index1Type key1) {
		Map<Index2Type, List<DataType>> intermediateMap = data.get(key1);
		if (intermediateMap != null) {
			return intermediateMap;
		}
		else {
			return Collections.emptyMap();
		}
	}

	public void add(Index1Type key1, Index2Type key2, DataType value) {
		Map<Index2Type, List<DataType>> intermediateMap = data.get(key1);
		List<DataType> tmpList;

		if (intermediateMap == null) {
			intermediateMap = new HashMap<Index2Type, List<DataType>>();
			data.put(key1, intermediateMap);
		}

		tmpList = intermediateMap.get(key2);

		if (tmpList == null) {
			tmpList = new ArrayList<DataType>();
			intermediateMap.put(key2, tmpList);
		}

		tmpList.add(value);
	}

	@Override
	public String toString() {
		return data.toString();
	}
}