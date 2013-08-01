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
package org.openrdf;

/**
 * Enumeration of Transaction Isolation levels supported by Sesame. Note that
 * Sesame stores are not required to support all levels, consult the
 * documentatation for the specific SAIL implementation you are using to find
 * out which levels are supported.
 * 
 * @author Jeen Broekstra
 * @since 2.7.4
 */
public enum TransactionIsolation {
	/**
	 * the lowest isolation level, a transaction may see uncommitted data from
	 * concurrent transactions (so called 'dirty reads').
	 */
	READ_UNCOMMITTED,
	/**
	 * transactions are guaranteed to only see data that is committed, any
	 * uncommitted writes from concurrent transactions do not influence the
	 * result.
	 */
	READ_COMMITTED,

	/**
	 * transactions are guaranteed to get the exact same result on the same query
	 * (assuming no write in that same transaction has taken place between the
	 * reads).
	 */
	REPEATABLE_READ

}
