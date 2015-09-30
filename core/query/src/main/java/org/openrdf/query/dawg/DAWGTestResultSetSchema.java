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
package org.openrdf.query.dawg;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestResultSetSchema {

	public static final String NAMESPACE = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#";

	public static final IRI RESULTSET;

	public static final IRI RESULTVARIABLE;

	public static final IRI SOLUTION;

	public static final IRI BINDING;

	public static final IRI VALUE;

	public static final IRI VARIABLE;

	public static final IRI BOOLEAN;

	public static final Literal TRUE;

	public static final Literal FALSE;

	static {
		ValueFactory vf = SimpleValueFactory.getInstance();
		RESULTSET = vf.createIRI(NAMESPACE, "ResultSet");
		RESULTVARIABLE = vf.createIRI(NAMESPACE, "resultVariable");
		SOLUTION = vf.createIRI(NAMESPACE, "solution");
		BINDING = vf.createIRI(NAMESPACE, "binding");
		VALUE = vf.createIRI(NAMESPACE, "value");
		VARIABLE = vf.createIRI(NAMESPACE, "variable");
		BOOLEAN = vf.createIRI(NAMESPACE, "boolean");
		TRUE = vf.createLiteral(true);
		FALSE = vf.createLiteral(false);
	}
}
