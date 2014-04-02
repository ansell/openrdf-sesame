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
package org.openrdf.sail.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andriy.nikolov
 */
public class MapOfListMaps<Index1Type, Index2Type, DataType> {

	private final HashMap<Index1Type, Map<Index2Type, List<DataType>>> data;

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
		return new ArrayList<DataType>(0);
	}

	public Map<Index2Type, List<DataType>> get(Index1Type key1) {
		Map<Index2Type, List<DataType>> intermediateMap = data.get(key1);
		if (intermediateMap != null) {
			return intermediateMap;
		}
		else {
			return new HashMap<Index2Type, List<DataType>>();
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

}