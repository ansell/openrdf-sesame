/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
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
		if(intermediateMap!=null) {
			List<DataType> tmp = intermediateMap.get(key2);
			if(tmp!=null)
				return tmp;
		}
		return new ArrayList<DataType>(0);
	}
	
	public Map<Index2Type, List<DataType>> get(Index1Type key1) {
		Map<Index2Type, List<DataType>> intermediateMap = data.get(key1);
		if(intermediateMap!=null) {
			return intermediateMap;
		} else {
			return new HashMap<Index2Type, List<DataType>>();
		}
	}
	
	public void add(Index1Type key1, Index2Type key2, DataType value) {
		Map<Index2Type, List<DataType>> intermediateMap = data.get(key1);
		List<DataType> tmpList;
		
		if(intermediateMap==null) {
			intermediateMap = new HashMap<Index2Type, List<DataType>>();
			data.put(key1, intermediateMap);
		}
		
		tmpList = intermediateMap.get(key2);

		if(tmpList==null) {
			tmpList = new ArrayList<DataType>();
			intermediateMap.put(key2, tmpList);
		}
		
		tmpList.add(value);
	}

}