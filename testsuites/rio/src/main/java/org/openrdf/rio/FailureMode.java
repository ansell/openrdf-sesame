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

public enum FailureMode {
	/**
	 * Helper constant to indicate that test failures for negative tests are
	 * being ignored.
	 */
	IGNORE_FAILURE(true),

	/**
	 * Helper constant to indicate that test failures for negative tests are
	 * being recognised.
	 */
	DO_NOT_IGNORE_FAILURE(false);

	private final boolean ignoreFailure;

	FailureMode(boolean ignoreFailure) {
		this.ignoreFailure = ignoreFailure;
	}
	
	public boolean ignoreFailure() {
		return ignoreFailure;
	}
}