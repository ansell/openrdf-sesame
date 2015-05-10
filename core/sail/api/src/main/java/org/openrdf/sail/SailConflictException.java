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
package org.openrdf.sail;

import org.openrdf.IsolationLevels;

/**
 * An exception thrown by some methods in Sail to indicate that a requested
 * isolation level could not be fulfilled. Such as when multiple conflicting
 * {@link IsolationLevels#SERIALIZABLE} transactions overlap in time.
 */
public class SailConflictException extends SailException {

	private static final long serialVersionUID = 8616609700552763681L;

	public SailConflictException() {
		super();
	}

	public SailConflictException(String msg) {
		super(msg);
	}

	public SailConflictException(String msg, Throwable t) {
		super(msg, t);
	}

	public SailConflictException(Throwable t) {
		super(t);
	}

}
