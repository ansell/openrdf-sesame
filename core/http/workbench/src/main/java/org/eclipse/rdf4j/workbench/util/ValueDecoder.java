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

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.workbench.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decodes strings into values for {@link WorkbenchRequst}.
 */
class ValueDecoder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValueDecoder.class);

	private final ValueFactory factory;

	private final Repository repository;

	/**
	 * Creates an instance of ValueDecoder.
	 * 
	 * @param repository
	 *        to get namespaces from
	 * @param factory
	 *        to generate values
	 */
	protected ValueDecoder(Repository repository, ValueFactory factory) {
		this.repository = repository;
		this.factory = factory;
	}

	/**
	 * Decode the given string into a {@link org.eclipse.rdf4j.model.Value}.
	 * 
	 * @param string
	 *        representation of an RDF value
	 * @return the parsed value, or null if the string is null, empty, only
	 *         whitespace, or {@link java.lang.String#equals(Object)} "null".
	 * @throws BadRequestException
	 *         if a problem occurs during parsing
	 */
	protected Value decodeValue(String string)
		throws BadRequestException
	{
		Value result = null;
		try {
			if (string != null) {
				String value = string.trim();
				if (!value.isEmpty() && !"null".equals(value)) {
					if (value.startsWith("_:")) {
						String label = value.substring("_:".length());
						result = factory.createBNode(label);
					}
					else {
						if (value.charAt(0) == '<' && value.endsWith(">")) {
							result = factory.createIRI(value.substring(1, value.length() - 1));
						}
						else {
							if (value.charAt(0) == '"') {
								result = parseLiteral(value);
							}
							else {
								result = parseURI(value);
							}
						}
					}
				}
			}
		}
		catch (Exception exc) {
			LOGGER.warn(exc.toString(), exc);
			throw new BadRequestException("Malformed value: " + string, exc);
		}
		return result;
	}

	private Value parseURI(String value)
		throws RepositoryException, BadRequestException
	{
		String prefix = value.substring(0, value.indexOf(':'));
		String localPart = value.substring(prefix.length() + 1);
		String namespace = getNamespace(prefix);
		if (namespace == null) {
			throw new BadRequestException("Undefined prefix: " + value);
		}
		return factory.createIRI(namespace, localPart);
	}

	private Value parseLiteral(String value)
		throws BadRequestException
	{
		String label = value.substring(1, value.lastIndexOf('"'));
		Value result;
		if (value.length() == (label.length() + 2)) {
			result = factory.createLiteral(label);
		}
		else {
			String rest = value.substring(label.length() + 2);
			if (rest.startsWith("^^")) {
				Value datatype = decodeValue(rest.substring(2));
				if (datatype instanceof IRI) {
					result = factory.createLiteral(label, (IRI)datatype);
				}
				else {
					throw new BadRequestException("Malformed datatype: " + value);
				}
			}
			else if (rest.charAt(0) == '@') {
				result = factory.createLiteral(label, rest.substring(1));
			}
			else {
				throw new BadRequestException("Malformed language tag or datatype: " + value);
			}
		}
		return result;
	}

	private String getNamespace(String prefix)
		throws RepositoryException
	{
		RepositoryConnection con = repository.getConnection();
		try {
			return con.getNamespace(prefix);
		}
		finally {
			con.close();
		}
	}

}
