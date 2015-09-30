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
package org.eclipse.rdf4j.rio;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.LiteralUtilException;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

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
	public static final String XMLSCHEMA = "org.eclipse.rdf4j.rio.datatypes.xmlschema";

	/**
	 * Identifier for datatypes defined in the
	 * {@link org.eclipse.rdf4j.model.vocabulary.RDF} vocabulary.
	 * 
	 * @since 2.7.0
	 */
	public static final String RDFDATATYPES = "org.eclipse.rdf4j.rio.datatypes.rdf";

	/**
	 * Identifier for datatypes defined by DBPedia.
	 * 
	 * @see <a
	 *      href="http://mappings.dbpedia.org/index.php/DBpedia_Datatypes">DBPedia
	 *      Datatypes</a>
	 * @since 2.7.1
	 */
	public static final String DBPEDIA = "org.eclipse.rdf4j.rio.datatypes.dbpedia";

	/**
	 * Identifier for datatypes defined in the Virtuoso Geometry vocabulary.
	 * 
	 * @see <a
	 *      href="http://docs.openlinksw.com/virtuoso/rdfsparqlgeospat.html">Virtuoso
	 *      Geospatial</a>
	 * @since 2.7.1
	 */
	public static final String VIRTUOSOGEOMETRY = "org.eclipse.rdf4j.rio.datatypes.virtuosogeometry";

	/**
	 * Identifier for datatypes defined in the GeoSPARQL vocabulary.
	 * 
	 * @see <a
	 *      href="http://www.opengeospatial.org/standards/geosparql">GeoSPARQL</a>
	 * @since 2.7.4
	 */
	public static final String GEOSPARQL = "org.eclipse.rdf4j.rio.datatypes.geosparql";

	/**
	 * Checks if the given datatype URI is recognized by this datatype handler.
	 * 
	 * @param datatypeUri
	 *        The datatype URI to check.
	 * @return True if the datatype is syntactically valid and could be used with
	 *         {@link #verifyDatatype(String, IRI)} and
	 *         {@link #normalizeDatatype(String, IRI, ValueFactory)}.
	 * @since 2.7.0
	 */
	public boolean isRecognizedDatatype(IRI datatypeUri);

	/**
	 * Verifies that the datatype URI is valid, including a check on the
	 * structure of the literal value.
	 * <p>
	 * This method must only be called after verifying that
	 * {@link #isRecognizedDatatype(IRI)} returns true for the given datatype
	 * URI.
	 * 
	 * @param literalValue
	 *        Literal value matching the given datatype URI.
	 * @param datatypeUri
	 *        A datatype URI that matched with {@link #isRecognizedDatatype(IRI)}
	 * @return True if the datatype URI is recognized by this datatype handler,
	 *         and it is verified to be syntactically valid.
	 * @since 2.7.0
	 * @throws LiteralUtilException
	 *         If the datatype was not recognized.
	 */
	public boolean verifyDatatype(String literalValue, IRI datatypeUri)
		throws LiteralUtilException;

	/**
	 * Normalize both the datatype URI and the literal value if appropriate, and
	 * use the given value factory to generate a literal matching a literal value
	 * and datatype URI.
	 * <p>
	 * This method must only be called after verifying that
	 * {@link #isRecognizedDatatype(IRI)} returns true for the given datatype
	 * URI, and {@link #verifyDatatype(String, IRI)} also returns true for the
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
	public Literal normalizeDatatype(String literalValue, IRI datatypeUri, ValueFactory valueFactory)
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
