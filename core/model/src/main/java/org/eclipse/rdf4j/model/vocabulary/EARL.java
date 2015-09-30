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
package org.eclipse.rdf4j.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Constants for EARL primitives and for the EARL namespace.
 */
public class EARL {

	/**
	 * The EARL namespace: http://www.w3.org/ns/earl#
	 */
	public static final String NAMESPACE = "http://www.w3.org/ns/earl#";

	/**
	 * The recommended prefix for the EARL namespace: "earl"
	 */
	public static final String PREFIX = "earl";

	public final static IRI ASSERTOR;

	public final static IRI ASSERTION;

	public final static IRI ASSERTEDBY;

	public final static IRI SUBJECT;

	public final static IRI TEST;

	public final static IRI TEST_SUBJECT;

	public final static IRI RESULT;

	public final static IRI MODE;

	public final static IRI TESTRESULT;

	public final static IRI OUTCOME;

	public final static IRI SOFTWARE;

	// Outcome values

	public final static IRI PASS;

	public final static IRI FAIL;

	public final static IRI CANNOTTELL;

	public final static IRI NOTAPPLICABLE;

	public final static IRI NOTTESTED;

	// Test modes

	public final static IRI MANUAL;

	public final static IRI AUTOMATIC;

	public final static IRI SEMIAUTOMATIC;

	public final static IRI NOTAVAILABLE;

	public final static IRI HEURISTIC;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		ASSERTOR = factory.createIRI(EARL.NAMESPACE, "Assertor");
		ASSERTION = factory.createIRI(EARL.NAMESPACE, "Assertion");
		ASSERTEDBY = factory.createIRI(EARL.NAMESPACE, "assertedBy");
		SUBJECT = factory.createIRI(EARL.NAMESPACE, "subject");
		TEST = factory.createIRI(EARL.NAMESPACE, "test");
		TEST_SUBJECT = factory.createIRI(EARL.NAMESPACE, "TestSubject");
		RESULT = factory.createIRI(EARL.NAMESPACE, "result");
		MODE = factory.createIRI(EARL.NAMESPACE, "mode");
		TESTRESULT = factory.createIRI(EARL.NAMESPACE, "TestResult");
		OUTCOME = factory.createIRI(EARL.NAMESPACE, "outcome");
		SOFTWARE = factory.createIRI(EARL.NAMESPACE, "Software");

		// Outcome values

		PASS = factory.createIRI(EARL.NAMESPACE, "pass");
		FAIL = factory.createIRI(EARL.NAMESPACE, "fail");
		CANNOTTELL = factory.createIRI(EARL.NAMESPACE, "cannotTell");
		NOTAPPLICABLE = factory.createIRI(EARL.NAMESPACE, "notApplicable");
		NOTTESTED = factory.createIRI(EARL.NAMESPACE, "notTested");

		// Test modes
		MANUAL = factory.createIRI(EARL.NAMESPACE, "manual");
		AUTOMATIC = factory.createIRI(EARL.NAMESPACE, "automatic");
		SEMIAUTOMATIC = factory.createIRI(EARL.NAMESPACE, "semiAutomatic");
		NOTAVAILABLE = factory.createIRI(EARL.NAMESPACE, "notAvailable");
		HEURISTIC = factory.createIRI(EARL.NAMESPACE, "heuristic");
	}
}
