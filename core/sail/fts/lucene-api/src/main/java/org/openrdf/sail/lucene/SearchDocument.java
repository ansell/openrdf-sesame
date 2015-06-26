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

import java.util.List;
import java.util.Set;

import com.spatial4j.core.shape.Shape;

public interface SearchDocument
{
	String getId();
	String getResource();
	String getContext();
	Set<String> getPropertyNames();
	/**
	 * Adds/creates a new property with the given name.
	 */
	void addProperty(String name);
	/**
	 * Adds a value to the property with the given name.
	 */
	void addProperty(String name, String value);
	void addGeoProperty(String name, String value, Shape shape);
	/**
	 * Checks whether a field occurs with a specified value in a Document.
	 */
	boolean hasProperty(String name, String value);
	List<String> getProperty(String name);
}