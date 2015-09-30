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
package org.eclipse.rdf4j.sail.federation;

import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;

import java.util.Arrays;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.federation.Federation;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SPARQLBuilderTest {

	@Parameters(name = "{index}({0})-{2}:{3}")
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ "StatementPattern", "SELECT * WHERE {?s ?p ?o}", "", "" },
				{ "Join", "SELECT * WHERE {?s ?p ?o; <urn:test:pred> ?obj}", "", "" },
				{ "Distinct", "SELECT DISTINCT ?s WHERE {?s ?p ?o; <urn:test:pred> ?obj}", "", "" },
				{ "Optional", "SELECT * WHERE {?s ?p ?o . OPTIONAL { ?s <urn:test:pred> ?obj}}", "", "" },
				{
						"Filter",
						"SELECT ?s WHERE {?s ?p ?o; <urn:test:pred> ?obj FILTER (str(?obj) = \"urn:test:obj\")}",
						"",
						"" },
				{
						"Bindings",
						"SELECT * WHERE {?s ?p ?o . OPTIONAL { ?s <urn:test:pred> ?obj}}",
						"s",
						"urn:test:subj" } });
	}

	private RepositoryConnection con;

	private ValueFactory valueFactory;

	private final String pattern, prefix, namespace;

	public SPARQLBuilderTest(String name, String pattern, String prefix, String namespace) {
		super();
		assert !name.isEmpty();
		this.pattern = pattern;
		this.prefix = prefix;
		this.namespace = namespace;
	}

	@Before
	public void setUp()
		throws Exception
	{
		Federation federation = new Federation();
		federation.addMember(new SailRepository(new MemoryStore()));
		Repository repository = new SailRepository(federation);
		repository.initialize();
		con = repository.getConnection();
		valueFactory = con.getValueFactory();
		IRI subj = valueFactory.createIRI("urn:test:subj");
		IRI pred = valueFactory.createIRI("urn:test:pred");
		IRI obj = valueFactory.createIRI("urn:test:obj");
		con.add(subj, pred, obj);
	}

	@Test
	public void test()
		throws OpenRDFException
	{ // NOPMD
		// Thrown exceptions are the only failure path.
		TupleQuery tupleQuery = con.prepareTupleQuery(SPARQL, pattern);
		if (!(prefix.isEmpty() || namespace.isEmpty())) {
			tupleQuery.setBinding(prefix, valueFactory.createIRI(namespace));
		}
		tupleQuery.evaluate().close();
	}
}
