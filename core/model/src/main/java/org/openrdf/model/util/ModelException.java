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
package org.openrdf.model.util;

import org.openrdf.model.Model;
import org.openrdf.model.Value;

/**
 * An exception thrown by {@link Model} and {@link org.openrdf.model.util.Models Models} when specific
 * conditions are not met.
 * 
 * @author Arjohn Kampman
 */
public class ModelException extends RuntimeException {

	private static final long serialVersionUID = 3886967415616842867L;

	public ModelException(Value value) {
		this("Unexpected object term: " + value);
	}

	public ModelException(Value v1, Value v2) {
		this(buildMessage(v1, v2));
	}

	public ModelException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelException(String message) {
		super(message);
	}

	public ModelException(Throwable cause) {
		super(cause);
	}

	private static String buildMessage(Value v1, Value v2) {
		StringBuilder sb = new StringBuilder();
		if (!v1.toString().equals(v2.toString())) {
			sb.append("Object is both ");
			sb.append(v1.toString());
			sb.append(" and ");
			sb.append(v2.toString());
		} else if (!v1.getClass().getName().equals(v2.getClass().getName())) {
			sb.append("Object is both ");
			sb.append("a ");
			sb.append(v1.getClass().getName());
			sb.append(" and a ");
			sb.append(v2.getClass().getName());
		} else {
			sb.append("Object is ");
			sb.append(v1);
			sb.append(" twice!? (store maybe corrupt)");
		}
		return sb.toString();
	}
}
