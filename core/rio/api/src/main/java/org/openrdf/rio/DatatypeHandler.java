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
package org.openrdf.rio;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtilException;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An interface defining methods related to verification and normalization of
 * typed literals and datatype URIs.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public interface DatatypeHandler {

	/**
	 * Identifier for datatypes defined in the {@link XMLSchema} vocabulary.
	 * 
	 * @since 2.7.0
	 */
	public static final String XMLSCHEMA = "org.openrdf.rio.datatypes.xmlschema";

	/**
	 * Identifier for datatypes defined in the
	 * {@link org.openrdf.model.vocabulary.RDF} vocabulary.
	 * 
	 * @since 2.7.0
	 */
	public static final String RDFDATATYPES = "org.openrdf.rio.datatypes.rdf";

	/**
	 * Identifier for datatypes defined by DBPedia.
	 * 
	 * @see <a
	 *      href="http://mappings.dbpedia.org/index.php/DBpedia_Datatypes">DBPedia
	 *      Datatypes</a>
	 * @since 2.7.1
	 */
	public static final String DBPEDIA = "org.openrdf.rio.datatypes.dbpedia";

	/**
	 * Identifier for datatypes defined in the Virtuoso Geometry vocabulary.
	 * 
	 * @see <a
	 *      href="http://docs.openlinksw.com/virtuoso/rdfsparqlgeospat.html">Virtuoso
	 *      Geospatial</a>
	 * @since 2.7.1
	 */
	public static final String VIRTUOSOGEOMETRY = "org.openrdf.rio.datatypes.virtuosogeometry";

	/**
	 * Checks if the given datatype URI is recognized by this datatype handler.
	 * 
	 * @param datatypeUri
	 *        The datatype URI to check.
	 * @return True if the datatype is syntactically valid and could be used with
	 *         {@link #verifyDatatype(String, URI)} and
	 *         {@link #normalizeDatatype(String, URI, ValueFactory)}.
	 * @since 2.7.0
	 */
	public boolean isRecognizedDatatype(URI datatypeUri);

	/**
	 * Verifies that the datatype URI is valid, including a check on the
	 * structure of the literal value.
	 * <p>
	 * This method must only be called after verifying that
	 * {@link #isRecognizedDatatype(URI)} returns true for the given datatype
	 * URI.
	 * 
	 * @param literalValue
	 *        Literal value matching the given datatype URI.
	 * @param datatypeUri
	 *        A datatype URI that matched with {@link #isRecognizedDatatype(URI)}
	 * @return True if the datatype URI is recognized by this datatype handler,
	 *         and it is verified to be syntactically valid.
	 * @since 2.7.0
	 * @throws LiteralUtilException
	 *         If the datatype was not recognized.
	 */
	public boolean verifyDatatype(String literalValue, URI datatypeUri)
		throws LiteralUtilException;

	/**
	 * Normalize both the datatype URI and the literal value if appropriate, and
	 * use the given value factory to generate a literal matching a literal value
	 * and datatype URI.
	 * <p>
	 * This method must only be called after verifying that
	 * {@link #isRecognizedDatatype(URI)} returns true for the given datatype
	 * URI, and {@link #verifyDatatype(String, URI)} also returns true for the
	 * given datatype URI and literal value.
	 * 
	 * @param literalValue
	 *        Required literal value to use in the normalization process and to
	 *        provide the value for the resulting literal.
	 * @param datatypeUri
	 *        The datatype URI which is to be normalized. This URI is available
	 *        in normalized form from the result using
	 *        {@link Literal#getDatatype()}.
	 * @param valueFactory
	 *        The {@link ValueFactory} to use to create the result literal.
	 * @return A {@link Literal} containing the normalized literal value and
	 *         datatype URI.
	 * @since 2.7.0
	 * @throws LiteralUtilException
	 *         If the datatype URI was not recognized or verified, or the literal
	 *         value could not be normalized due to an error.
	 */
	public Literal normalizeDatatype(String literalValue, URI datatypeUri, ValueFactory valueFactory)
		throws LiteralUtilException;

	/**
	 * A unique key for this datatype handler to identify it in the
	 * DatatypeHandlerRegistry.
	 * 
	 * @return A unique string key.
	 * @since 2.7.0
	 */
	public String getKey();

}
