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
package org.openrdf;

/**
 * General superclass of all unchecked exceptions that parts of OpenRDF Sesame
 * can throw.
 * 
 * @author jeen
 */
public abstract class OpenRDFException extends RuntimeException {

	private static final long serialVersionUID = 8913366826930181397L;

	public OpenRDFException() {
		super();
	}

	public OpenRDFException(String msg) {
		super(msg);
	}

	public OpenRDFException(Throwable t) {
		super(t);
	}

	public OpenRDFException(String msg, Throwable t) {
		super(msg, t);
	}
}
