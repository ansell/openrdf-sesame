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
import org.openrdf.query.parser.ParsedOperation;

/**
 * Class to represent a SPIN template.
 */
public class Template {

	private final URI uri;

	private ParsedOperation parsedOp;

	public Template(URI uri) {
		this.uri = uri;
	}

	public void setParsedOperation(ParsedOperation op) {
		this.parsedOp = op;
	}

	public ParsedOperation getParsedOperation() {
		return parsedOp;
	}
}
