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
package org.openrdf.sail.federation;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import info.aduna.iteration.Iterations;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Tests for correct behavior when members define matching or conflicting
 * prefix/namespace maps.
 *
 * @author Dale Visser
 */
public class FederationNamespacesTest {

	private static String PREFIX = "test";

	private static String EXPECTED_NAME = "http://test/a#";

	private static Namespace EXPECTED_NAMESPACE = new SimpleNamespace(PREFIX, EXPECTED_NAME);

	@Test
	public void testTwoMatchingNamespaces()
		throws RepositoryException, RDFParseException, IOException
	{
		RepositoryConnection con = createFederationWithMemberNamespaces("a", "a");
		try {
			assertThat(con.getNamespace(PREFIX), is(equalTo(EXPECTED_NAME)));
			List<Namespace> asList = Iterations.asList(con.getNamespaces());
			assertThat(asList, hasItem(EXPECTED_NAMESPACE));
		}
		finally {
			con.close();
		}
	}

	@Test
	public void testThreeMatchingNamespaces()
		throws RepositoryException, RDFParseException, IOException
	{
		RepositoryConnection con = createFederationWithMemberNamespaces("a", "a", "a");
		try {
			assertThat(con.getNamespace(PREFIX), is(equalTo(EXPECTED_NAME)));
			List<Namespace> asList = Iterations.asList(con.getNamespaces());
			assertThat(asList, hasItem(EXPECTED_NAMESPACE));
		}
		finally {
			con.close();
		}
	}

	@Test
	public void testTwoMismatchedNamespaces()
		throws RepositoryException, RDFParseException, IOException
	{
		RepositoryConnection con = createFederationWithMemberNamespaces("a", "b");
		try {
			assertThat(con.getNamespace(PREFIX), is(nullValue()));
			List<Namespace> asList = Iterations.asList(con.getNamespaces());
			assertThat(asList, not(hasItem(EXPECTED_NAMESPACE)));
		}
		finally {
			con.close();
		}
	}

	@Test
	public void testThreeMismatchedNamespaces()
		throws RepositoryException, RDFParseException, IOException
	{
		RepositoryConnection con = createFederationWithMemberNamespaces("a", "b", "c");
		try {
			assertThat(con.getNamespace(PREFIX), is(nullValue()));
			List<Namespace> asList = Iterations.asList(con.getNamespaces());
			assertThat(asList, not(hasItem(EXPECTED_NAMESPACE)));
		}
		finally {
			con.close();
		}
	}

	private RepositoryConnection createFederationWithMemberNamespaces(String... paths)
		throws RepositoryException, RDFParseException, IOException
	{
		Federation federation = new Federation();
		for (int i = 0; i < paths.length; i++) {
			federation.addMember(createMember(Integer.toString(i), "http://test/" + paths[i] + "#"));
		}
		SailRepository repo = new SailRepository(federation);
		repo.initialize();
		return repo.getConnection();
	}

	private Repository createMember(String memberID, String name)
		throws RepositoryException, RDFParseException, IOException
	{
		SailRepository member = new SailRepository(new MemoryStore());
		member.initialize();
		SailRepositoryConnection con = member.getConnection();
		try {
			con.setNamespace(PREFIX, name);
		}
		finally {
			con.close();
		}
		return member;
	}
}
