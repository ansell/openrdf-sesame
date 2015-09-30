/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
