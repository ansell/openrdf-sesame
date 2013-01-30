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

import java.io.Serializable;

/**
 * A type-safe enumeration for transaction status information on
 * MemStatements.
 */
public enum TxnStatus implements Serializable {

	/**
	 * Constant indicating that a statement has not been affected by a
	 * transaction.
	 */
	NEUTRAL,

	/**
	 * Constant indicating that a statement has been newly added as part of a
	 * transaction, but has not yet been committed. Such statements should not
	 * be queried to prevent 'dirty reads'.
	 */
	NEW,

	/**
	 * Constant indicating that an existing statement has been deprecated and
	 * should be removed upon commit.
	 */
	DEPRECATED,
	
	/**
	 * Constant indicating that an existing inferred statement has been added
	 * explicitly as part of a transaction and that it should be marked as such
	 * upon commit.
	 */
	EXPLICIT,
	
	/**
	 * Constant indicating that an existing explicit statement has been removed
	 * as part of a transaction, but that it can still be inferred from the
	 * other statements.
	 */
	INFERRED,

	/**
	 * Constant indicating that a statement was added and then removed in a
	 * single transaction. The statement should be removed upon commit.
	 */
	ZOMBIE;
}
