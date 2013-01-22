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
package org.openrdf.sail.nativerdf.btree;

/**
 * A RecordComparator that compares values with eachother by comparing all
 * of their bytes.
 * 
 * @author Arjohn Kampman
 */
public class DefaultRecordComparator implements RecordComparator {

	// implements RecordComparator.compareBTreeValues()
	public int compareBTreeValues(byte[] key, byte[] data, int offset, int length) {
		int result = 0;
		for (int i = 0; result == 0 && i < length; i++) {
			result = (key[i] & 0xff) - (data[offset + i] & 0xff);
		}
		return result;
	}
}
