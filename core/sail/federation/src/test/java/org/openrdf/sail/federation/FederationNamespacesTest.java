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
package org.openrdf.sail.federation;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

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
public class FederationNamespacesTest extends TestCase {

	private static String PREFIX = "a";

	@Test
	public void testTwoMatchingNamespaces()
		throws RepositoryException, RDFParseException, IOException
	{
		RepositoryConnection con = createFederationWithMemberNamespaces("a", "a");
		try {
			assertThat(con.getNamespace("a"), is(equalTo("http://test/a#")));
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
			assertThat(con.getNamespace("a"), is(equalTo("http://test/a#")));
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
			assertThat(con.getNamespace("a"), is(nullValue()));
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
			federation.addMember(createMember(Integer.toString(i), PREFIX, "http://test/" + paths[i] + "#"));
		}
		SailRepository repo = new SailRepository(federation);
		repo.initialize();
		return repo.getConnection();
	}

	private Repository createMember(String memberID, String prefix, String name)
		throws RepositoryException, RDFParseException, IOException
	{
		SailRepository member = new SailRepository(new MemoryStore());
		member.initialize();
		SailRepositoryConnection con = member.getConnection();
		try {
			con.setNamespace(prefix, name);
		}
		finally {
			con.close();
		}
		return member;
	}
}
