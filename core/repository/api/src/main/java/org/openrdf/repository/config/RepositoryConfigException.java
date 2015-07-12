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
package org.openrdf.repository.config;

import org.openrdf.OpenRDFException;

/**
 * Exception indicating a repository configuration problem.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryConfigException extends OpenRDFException {

	private static final long serialVersionUID = -6643040675968955429L;

	public RepositoryConfigException() {
		super();
	}

	public RepositoryConfigException(String message) {
		super(message);
	}

	public RepositoryConfigException(Throwable t) {
		super(t);
	}

	public RepositoryConfigException(String message, Throwable t) {
		super(message, t);
	}
}
