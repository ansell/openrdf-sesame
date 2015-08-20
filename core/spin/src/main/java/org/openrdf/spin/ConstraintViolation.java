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

import java.io.Serializable;

/**
 * Class to store SPIN constraint violation RDF statements.
 */
public class ConstraintViolation implements Serializable {
	private static final long serialVersionUID = 3699022598761641221L;

	private final String message;
	private final String root;
	private final String path;
	private final String value;
	private final ConstraintViolationLevel level;

	public ConstraintViolation(String message, String root, String path, String value, ConstraintViolationLevel level) {
		this.message = message;
		this.root = root;
		this.path = path;
		this.value = value;
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public String getRoot() {
		return root;
	}

	public String getPath() {
		return path;
	}

	public String getValue() {
		return value;
	}

	public ConstraintViolationLevel getLevel() {
		return level;
	}
}
