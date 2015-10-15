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
package org.openrdf.sail.spin;

import org.openrdf.sail.SailException;
import org.openrdf.spin.ConstraintViolation;

/**
 * Exception thrown when a SPIN constraint is violated.
 */
public class ConstraintViolationException extends SailException {

	private static final long serialVersionUID = 2208275585538203176L;

	private final ConstraintViolation violation;

	public ConstraintViolationException(ConstraintViolation violation) {
		super(violation.getMessage());
		this.violation = violation;
	}

	public ConstraintViolation getConstraintViolation() {
		return violation;
	}
}
