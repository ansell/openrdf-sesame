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
package org.openrdf.rio;

/**
 * A listener interface for listening to the parser's progress.
 */
public interface ParseLocationListener {

	/**
	 * Signals an update of a parser's progress, indicated by a line and column
	 * number. Both line and column number start with value 1 for the first line
	 * or column.
	 *
	 * @param lineNo
	 *        The line number, or -1 if none is available.
	 * @param columnNo
	 *        The column number, or -1 if none is available.
	 */
	public void parseLocationUpdate(long lineNo, long columnNo);

}
