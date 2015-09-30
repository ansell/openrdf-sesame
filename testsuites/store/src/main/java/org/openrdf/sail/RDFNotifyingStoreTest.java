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
package org.openrdf.sail;

import static org.junit.Assert.*;

import org.junit.Test;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * A JUnit test for testing Sail implementations that store RDF data. This is
 * purely a test for data storage and retrieval which assumes that no
 * inferencing or whatsoever is performed. This is an abstract class that should
 * be extended for specific Sail implementations.
 */
public abstract class RDFNotifyingStoreTest extends RDFStoreTest implements SailChangedListener {

	/*-----------*
	 * Variables *
	 *-----------*/

	private int removeEventCount;

	private int addEventCount;

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets an instance of the Sail that should be tested. The returned
	 * repository should already have been initialized.
	 * 
	 * @return an initialized Sail.
	 * @throws SailException
	 *         If the initialization of the repository failed.
	 */
	@Override
	protected abstract NotifyingSail createSail()
		throws SailException;

	@Override
	public void setUp()
		throws Exception
	{
		super.setUp();

		// set self as listener
		((NotifyingSail) sail).addSailChangedListener(this);

	}

	@Test
	public void testNotifyingRemoveAndClear()
		throws Exception
	{
		// Add some data to the repository
		con.begin();
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context1);
		con.commit();

		// Test removal of statements
		con.begin();
		con.removeStatements(painting, RDF.TYPE, RDFS.CLASS);
		con.commit();

		assertEquals("Repository should contain 4 statements in total", 4, countAllElements());

		assertEquals("Named context should contain 3 statements", 3, countContext1Elements());

		assertEquals("Statement (Painting, type, Class) should no longer be in the repository", 0,
				countQueryResults("select 1 from {ex:Painting} rdf:type {rdfs:Class}"));

		con.begin();
		con.removeStatements(null, null, null, context1);
		con.commit();

		assertEquals("Repository should contain 1 statement in total", 1, countAllElements());

		assertEquals("Named context should be empty", 0, countContext1Elements());

		con.begin();
		con.clear();
		con.commit();

		assertEquals("Repository should no longer contain any statements", 0, countAllElements());

		// test if event listener works properly.
		assertEquals("There should have been 1 event in which statements were added", 1, addEventCount);

		assertEquals("There should have been 3 events in which statements were removed", 3, removeEventCount);
	}

	@Override
	public void sailChanged(SailChangedEvent event) {
		if (event.statementsAdded()) {
			addEventCount++;
		}
		if (event.statementsRemoved()) {
			removeEventCount++;
		}
	}
}
