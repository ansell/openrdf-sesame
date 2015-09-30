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
package org.eclipse.rdf4j.workbench.util;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.workbench.exceptions.BadRequestException;
import org.eclipse.rdf4j.workbench.util.ValueDecoder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
		factory = SimpleValueFactory.getInstance();

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
