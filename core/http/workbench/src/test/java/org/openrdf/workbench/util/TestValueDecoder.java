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
package org.openrdf.workbench.util;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.workbench.exceptions.BadRequestException;

/**
 * @author dale
 */
public class TestValueDecoder {

	private ValueDecoder decoder;

	private ValueFactory factory;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		factory = new ValueFactoryImpl();

		// Build a mock repository instance that provides 'decoder' with all
		// it would ever ask for a connection with an associated prefix-namespace
		// mapping.
		RepositoryConnection connection = mock(RepositoryConnection.class);
		when(connection.getNamespace(RDFS.PREFIX)).thenReturn(RDFS.NAMESPACE);
		when(connection.getNamespace(XMLSchema.PREFIX)).thenReturn(XMLSchema.NAMESPACE);
		Repository repository = mock(Repository.class);
		when(repository.getConnection()).thenReturn(connection);
		decoder = new ValueDecoder(repository, factory);
	}

	@Test
	public final void testQnamePropertyValue()
		throws BadRequestException
	{
		Value value = decoder.decodeValue("rdfs:label");
		assertThat(value, is(instanceOf(IRI.class)));
		assertThat((IRI)value, is(equalTo(RDFS.LABEL)));
	}

	@Test
	public final void testPlainStringLiteral()
		throws BadRequestException
	{
		Value value = decoder.decodeValue("\"plain string\"");
		assertThat(value, is(instanceOf(Literal.class)));
		assertThat((Literal)value, is(equalTo(factory.createLiteral("plain string"))));
	}

	@Test
	public final void testUnexpectedLiteralAttribute()
		throws BadRequestException
	{
		try {
			decoder.decodeValue("\"datatype oops\"^rdfs:label");
			fail("Expected BadRequestException.");
		}
		catch (BadRequestException bre) {
			Throwable rootCause = bre.getRootCause();
			assertThat(rootCause, is(instanceOf(BadRequestException.class)));
			assertThat(rootCause.getMessage(), startsWith("Malformed language tag or datatype: "));
		}
	}

	@Test
	public final void testLiteralWithQNameType()
		throws BadRequestException
	{
		Value value = decoder.decodeValue("\"1\"^^xsd:int");
		assertThat(value, is(instanceOf(Literal.class)));
		assertThat((Literal)value, is(equalTo(factory.createLiteral(1))));
	}

	@Test
	public final void testLiteralWithURIType()
		throws BadRequestException
	{
		Value value = decoder.decodeValue("\"1\"^^<" + XMLSchema.INT + ">");
		assertThat(value, is(instanceOf(Literal.class)));
		assertThat((Literal)value, is(equalTo(factory.createLiteral(1))));
	}

	@Test
	public final void testLanguageLiteral()
		throws BadRequestException
	{
		Value value = decoder.decodeValue("\"color\"@en-US");
		assertThat(value, is(instanceOf(Literal.class)));
		assertThat((Literal)value, is(equalTo(factory.createLiteral("color", "en-US"))));
	}
}
