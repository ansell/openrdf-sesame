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
package org.openrdf.rio.datatypes;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
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

	private static final URI VIRTRDF_GEOMETRY = ValueFactoryImpl.getInstance().createURI(
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
	public boolean isRecognizedDatatype(URI datatypeUri) {
		if (datatypeUri == null) {
			throw new NullPointerException("Datatype URI cannot be null");
		}

		return VIRTRDF_GEOMETRY.equals(datatypeUri);
	}

	@Override
	public boolean verifyDatatype(String literalValue, URI datatypeUri)
		throws LiteralUtilException
	{
		if (isRecognizedDatatype(datatypeUri)) {
			return verifyDatatypeInternal(literalValue, datatypeUri);
		}

		throw new LiteralUtilException("Could not verify Virtuoso Geometry literal");
	}

	@Override
	public Literal normalizeDatatype(String literalValue, URI datatypeUri, ValueFactory valueFactory)
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

	private boolean verifyDatatypeInternal(String literalValue, URI datatypeUri)
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
