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
package org.openrdf.sail.memory.model;

/**
 * A type-safe enumeration for read modes for MemStatementIterator.
 *
 * @see MemStatementIterator
 */
public enum ReadMode  {
	
	/**
	 * Constant indicating that only committed statements should be read.
	 * Statements that have been added but not yet committed will be skipped.
	 */
	COMMITTED, 
	
	/**
	 * Constant indicating that statements should be treated as if the currently
	 * active transaction has been committed. Statements that have been
	 * scheduled for removal will be skipped.
	 */
	TRANSACTION, 
	
	/**
	 * Constant indicating that statements should be read no matter what their
	 * transaction status is.
	 */
	RAW;
}

