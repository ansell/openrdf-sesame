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
package org.eclipse.rdf4j.sail.nativerdf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.util.RepositoryUtil;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Integration tests for checking Native Store index consistency.
 * 
 * @author Jeen Broekstra
 */
public class NativeStoreConsistencyTest {

	/*-----------*
	 * Variables *
	 *-----------*/

	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	/*---------*
	 * Methods *
	 *---------*/

	@Test
	public void testSES1867IndexCorruption() throws Exception {
		ValueFactory vf = SimpleValueFactory.getInstance();
		IRI oldContext = vf.createIRI("http://example.org/oldContext");
		IRI newContext = vf.createIRI("http://example.org/newContext");

		File dataDir = tempDir.newFolder("nativestore-consistency");
		
		Repository repo = new SailRepository(new NativeStore(dataDir, "spoc,psoc"));
		repo.initialize();

		RepositoryConnection conn = repo.getConnection();

		// Step1: setup the initial database state
		System.out.println("Preserving initial state ...");
		conn.add(getClass().getResourceAsStream("/nativestore-testdata/SES-1867/initialState.nq"), "",
				RDFFormat.NQUADS);
		System.out.println("Number of statements: " + conn.size());

		// Step 2: in a single transaction remove "oldContext", then add
		// statements to "newContext"
		conn.begin();

		System.out.println("Removing old context");
		conn.remove((Resource)null, (IRI)null, (Value)null, oldContext);

		System.out.println("Adding updated context");
		conn.add(getClass().getResourceAsStream("/nativestore-testdata/SES-1867/newTriples.nt"), "",
				RDFFormat.NTRIPLES, newContext);
		conn.commit();

		// Step 3: check whether oldContext is actually empty
		List<Statement> stmts = Iterations.asList(conn.getStatements(null, null, null, false, oldContext));
		System.out.println("Not deleted statements: " + stmts.size());

		conn.close();
		repo.shutDown();

		// Step 4: check the repository size with SPOC only
		new File(dataDir, "triples.prop").delete(); // delete triples.prop to
																	// update index usage
		repo = new SailRepository(new NativeStore(dataDir, "spoc"));
		repo.initialize();
		conn = repo.getConnection();
		System.out.println("Repository size with SPOC index only: " + conn.size());
		Model spocStatements = Iterations.addAll(conn.getStatements(null, null, null, false),
				new LinkedHashModel());
		conn.close();
		repo.shutDown();

		// Step 5: check the repository size with PSOC only
		new File(dataDir, "triples.prop").delete(); // delete triples.prop to
																	// update index usage
		repo = new SailRepository(new NativeStore(dataDir, "psoc"));
		repo.initialize();
		conn = repo.getConnection();
		System.out.println("Repository size with PSOC index only: " + conn.size());
		Model psocStatements = Iterations.addAll(conn.getStatements(null, null, null, false),
				new LinkedHashModel());
		conn.close();
		repo.shutDown();

		// Step 6: computing the differences of the contents of the indices
		System.out.println("Computing differences of sets...");

		Collection<? extends Statement> differenceA = RepositoryUtil.difference(spocStatements, psocStatements);
		Collection<? extends Statement> differenceB = RepositoryUtil.difference(psocStatements, spocStatements);

		System.out.println("Difference SPOC MINUS PSOC: " + differenceA.size());
		System.out.println("Difference PSOC MINUS SPOC: " + differenceB.size());

		System.out.println("Different statements in SPOC MINUS PSOC (Mind the contexts):");
		for (Statement st : differenceA) {
			System.out.println("  * " + st);
		}

		System.out.println("Different statements in PSOC MINUS SPOC (Mind the contexts):");
		for (Statement st : differenceB) {
			System.out.println("  * " + st);
		}
		
		assertEquals(0, differenceA.size());
		assertEquals(0, differenceB.size());
	}

}
