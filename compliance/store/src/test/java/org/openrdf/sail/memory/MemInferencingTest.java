/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.sail.InferencingTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;

public class MemInferencingTest extends TestCase {

	public static Test suite()
		throws SailException
	{
		Sail sailStack = new ForwardChainingRDFSInferencer(new MemoryStore());
		TestSuite suite = new TestSuite(MemInferencingTest.class.getName());
		InferencingTest.addTests(suite, sailStack);
		return suite;
	}
}
