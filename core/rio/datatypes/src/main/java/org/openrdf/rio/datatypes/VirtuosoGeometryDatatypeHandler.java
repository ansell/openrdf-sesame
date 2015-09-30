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
package org.openrdf.rio.datatypes;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.LiteralUtilException;
import org.openrdf.rio.DatatypeHandler;

/**
 * An implementation of a datatype handler that can process Virtuoso Geometry
 * datatypes.
 * 
 * @author Peter Ansell
 * @since 2.7.1
 */
public class VirtuosoGeometryDatatypeHandler implements DatatypeHandler {

	private static final IRI VIRTRDF_GEOMETRY = SimpleValueFactory.getInstance().createIRI(
			"http://www.openlinksw.com/schemas/virtrdf#", "Geometry");

	private static final String POINT_START = "POINT(";

	private static final String POINT_END = ")";

	private static final String POINT_SEPERATOR = " ";

	/**
	 * Default constructor.
	 */
	public VirtuosoGeometryDatatypeHandler() {
	}

	@Override
	public boolean isRecognizedDatatype(IRI datatypeUri) {
		if (datatypeUri == null) {
			throw new NullPointerException("Datatype URI cannot be null");
		}

		return VIRTRDF_GEOMETRY.equals(datatypeUri);
	}

	@Override
	public boolean verifyDatatype(String literalValue, IRI datatypeUri)
		throws LiteralUtilException
	{
		if (isRecognizedDatatype(datatypeUri)) {
			return verifyDatatypeInternal(literalValue, datatypeUri);
		}

		throw new LiteralUtilException("Could not verify Virtuoso Geometry literal");
	}

	@Override
	public Literal normalizeDatatype(String literalValue, IRI datatypeUri, ValueFactory valueFactory)
		throws LiteralUtilException
	{
		if (isRecognizedDatatype(datatypeUri) && verifyDatatypeInternal(literalValue, datatypeUri)) {
			// TODO: Implement normalization
			return valueFactory.createLiteral(literalValue, datatypeUri);
		}

		throw new LiteralUtilException("Could not normalise Virtuoso Geometry literal");
	}

	@Override
	public String getKey() {
		return DatatypeHandler.VIRTUOSOGEOMETRY;
	}

	private boolean verifyDatatypeInternal(String literalValue, IRI datatypeUri)
		throws LiteralUtilException
	{
		if (literalValue == null) {
			throw new NullPointerException("Literal value cannot be null");
		}

		if (VIRTRDF_GEOMETRY.equals(datatypeUri)) {
			if (!literalValue.startsWith(POINT_START)) {
				return false;
			}
			if (!literalValue.endsWith(POINT_END)) {
				return false;
			}

			String valueString = literalValue.substring(POINT_START.length(),
					literalValue.length() - POINT_END.length());

			String[] split = valueString.split(POINT_SEPERATOR);

			if (split.length != 2) {
				return false;
			}

			try {
				// Verify that both parts of the point reference are valid doubles
				Double.parseDouble(split[0]);
				Double.parseDouble(split[1]);
			}
			catch (NumberFormatException e) {
				return false;
			}

			return true;
		}

		throw new LiteralUtilException("Did not recognise datatype");
	}
}
