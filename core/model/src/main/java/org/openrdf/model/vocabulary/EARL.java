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
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

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

	public final static URI ASSERTOR;

	public final static URI ASSERTION;

	public final static URI ASSERTEDBY;

	public final static URI SUBJECT;

	public final static URI TEST;

	public final static URI TEST_SUBJECT;

	public final static URI RESULT;

	public final static URI MODE;

	public final static URI TESTRESULT;

	public final static URI OUTCOME;

	public final static URI SOFTWARE;

	// Outcome values

	public final static URI PASS;

	public final static URI FAIL;

	public final static URI CANNOTTELL;

	public final static URI NOTAPPLICABLE;

	public final static URI NOTTESTED;

	// Test modes

	public final static URI MANUAL;

	public final static URI AUTOMATIC;

	public final static URI SEMIAUTOMATIC;

	public final static URI NOTAVAILABLE;

	public final static URI HEURISTIC;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		ASSERTOR = factory.createURI(EARL.NAMESPACE, "Assertor");
		ASSERTION = factory.createURI(EARL.NAMESPACE, "Assertion");
		ASSERTEDBY = factory.createURI(EARL.NAMESPACE, "assertedBy");
		SUBJECT = factory.createURI(EARL.NAMESPACE, "subject");
		TEST = factory.createURI(EARL.NAMESPACE, "test");
		TEST_SUBJECT = factory.createURI(EARL.NAMESPACE, "TestSubject");
		RESULT = factory.createURI(EARL.NAMESPACE, "result");
		MODE = factory.createURI(EARL.NAMESPACE, "mode");
		TESTRESULT = factory.createURI(EARL.NAMESPACE, "TestResult");
		OUTCOME = factory.createURI(EARL.NAMESPACE, "outcome");
		SOFTWARE = factory.createURI(EARL.NAMESPACE, "Software");

		// Outcome values

		PASS = factory.createURI(EARL.NAMESPACE, "pass");
		FAIL = factory.createURI(EARL.NAMESPACE, "fail");
		CANNOTTELL = factory.createURI(EARL.NAMESPACE, "cannotTell");
		NOTAPPLICABLE = factory.createURI(EARL.NAMESPACE, "notApplicable");
		NOTTESTED = factory.createURI(EARL.NAMESPACE, "notTested");

		// Test modes
		MANUAL = factory.createURI(EARL.NAMESPACE, "manual");
		AUTOMATIC = factory.createURI(EARL.NAMESPACE, "automatic");
		SEMIAUTOMATIC = factory.createURI(EARL.NAMESPACE, "semiAutomatic");
		NOTAVAILABLE = factory.createURI(EARL.NAMESPACE, "notAvailable");
		HEURISTIC = factory.createURI(EARL.NAMESPACE, "heuristic");
	}
}
