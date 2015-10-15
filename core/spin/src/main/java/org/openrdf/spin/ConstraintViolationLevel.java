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
package org.openrdf.spin;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.SPIN;

/**
 * Enum of possible SPIN constraint violation levels.
 */
public enum ConstraintViolationLevel {
	INFO, WARNING, ERROR, FATAL;

	public static ConstraintViolationLevel valueOf(URI levelValue) {
		ConstraintViolationLevel level;
		if (levelValue == null) {
			level = ConstraintViolationLevel.ERROR;
		}
		else if (SPIN.INFO_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.INFO;
		}
		else if (SPIN.WARNING_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.WARNING;
		}
		else if (SPIN.ERROR_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.ERROR;
		}
		else if (SPIN.FATAL_VIOLATION_LEVEL.equals(levelValue)) {
			level = ConstraintViolationLevel.FATAL;
		}
		else {
			level = null;
		}
		return level;
	}
}
