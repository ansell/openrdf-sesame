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
package info.aduna.webapp.util;


/**
 * A parameter consisting of a key and a value, which are both strings.
 */
public class Parameter {

	private final String key;
	private final String value;
	
	public Parameter(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Parameter) {
			Parameter other = (Parameter)obj;
			return
				key.equals(other.getKey()) &&
				value.equals(other.getValue());
		}
		
		return false;
	}
	
	public int hashCode() {
		return key.hashCode();
	}
	
	public String toString() {
		if (value == null) {
			return key;
		}
		else {
			return key + "="  + value;
		}
	}
}
