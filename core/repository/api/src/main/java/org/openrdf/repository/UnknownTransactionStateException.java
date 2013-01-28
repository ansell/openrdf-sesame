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
package org.openrdf.repository;

/**
 * A specific subtype of {@link RepositoryException} that indicates the
 * connection's transaction state can not be determined.
 * 
 * @since 2.7.0.
 * @author Jeen Broekstra
 */
public class UnknownTransactionStateException extends RepositoryException {

	private static final long serialVersionUID = -5938676154783704438L;

	public UnknownTransactionStateException() {
		super();
	}

	public UnknownTransactionStateException(String msg) {
		super(msg);
	}

	public UnknownTransactionStateException(Throwable t) {
		super(t);
	}

	public UnknownTransactionStateException(String msg, Throwable t) {
		super(msg, t);
	}
}
