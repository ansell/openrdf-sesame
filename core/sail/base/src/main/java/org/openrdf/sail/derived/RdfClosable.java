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
package org.openrdf.sail.derived;

import org.openrdf.sail.SailException;

/**
 * Common interface to objects that throw {@link SailException} on close.
 * 
 * @author James Leigh
 */
public interface RdfClosable {

	/**
	 * Closes this resource, relinquishing any underlying resources. This method
	 * is invoked automatically on objects managed by the {@code try}
	 * -with-resources statement.
	 * 
	 * @throws SailException
	 *         if this resource cannot be closed
	 */
	void close()
		throws SailException;
}
