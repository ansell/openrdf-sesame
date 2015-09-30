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
package org.openrdf.model.vocabulary;

import org.openrdf.model.Namespace;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Constants for OWL primitives and for the OWL namespace.
 * 
 * @see <a href="http://www.w3.org/TR/owl-ref/">OWL Web Ontology Language
 *      Reference</a>
 */
public class OWL {

	/** The OWL namespace: http://www.w3.org/2002/07/owl# */
	public static final String NAMESPACE = "http://www.w3.org/2002/07/owl#";

	/**
	 * Recommended prefix for the OWL namespace: "owl"
	 */
	public static final String PREFIX = "owl";

	/**
	 * An immutable {@link Namespace} constant that represents the OWL namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	// OWL Lite

	/** http://www.w3.org/2002/07/owl#Class */
	public final static IRI CLASS;

	/** http://www.w3.org/2002/07/owl#Individual */
	public final static IRI INDIVIDUAL;

	/** http://www.w3.org/2002/07/owl#Thing */
	public static final IRI THING;

	/** http://www.w3.org/2002/07/owl#Nothing */
	public static final IRI NOTHING;

	/** http://www.w3.org/2002/07/owl#equivalentClass */
	public final static IRI EQUIVALENTCLASS;

	/** http://www.w3.org/2002/07/owl#equivalentProperty */
	public final static IRI EQUIVALENTPROPERTY;

	/** http://www.w3.org/2002/07/owl#sameAs */
	public final static IRI SAMEAS;

	/** http://www.w3.org/2002/07/owl#differentFrom */
	public final static IRI DIFFERENTFROM;

	/** http://www.w3.org/2002/07/owl#AllDifferent */
	public final static IRI ALLDIFFERENT;

	/** http://www.w3.org/2002/07/owl#distinctMembers */
	public final static IRI DISTINCTMEMBERS;

	/** http://www.w3.org/2002/07/owl#ObjectProperty */
	public final static IRI OBJECTPROPERTY;

	/** http://www.w3.org/2002/07/owl#DatatypeProperty */
	public final static IRI DATATYPEPROPERTY;

	/** http://www.w3.org/2002/07/owl#inverseOf */
	public final static IRI INVERSEOF;

	/** http://www.w3.org/2002/07/owl#TransitiveProperty */
	public final static IRI TRANSITIVEPROPERTY;

	/** http://www.w3.org/2002/07/owl#SymmetricProperty */
	public final static IRI SYMMETRICPROPERTY;

	/** http://www.w3.org/2002/07/owl#FunctionalProperty */
	public final static IRI FUNCTIONALPROPERTY;

	/** http://www.w3.org/2002/07/owl#InverseFunctionalProperty */
	public final static IRI INVERSEFUNCTIONALPROPERTY;

	/** http://www.w3.org/2002/07/owl#Restriction */
	public final static IRI RESTRICTION;

	/** http://www.w3.org/2002/07/owl#onProperty */
	public final static IRI ONPROPERTY;

	/** http://www.w3.org/2002/07/owl#allValuesFrom */
	public final static IRI ALLVALUESFROM;

	/** http://www.w3.org/2002/07/owl#someValuesFrom */
	public final static IRI SOMEVALUESFROM;

	/** http://www.w3.org/2002/07/owl#minCardinality */
	public final static IRI MINCARDINALITY;

	/** http://www.w3.org/2002/07/owl#maxCardinality */
	public final static IRI MAXCARDINALITY;

	/** http://www.w3.org/2002/07/owl#cardinality */
	public final static IRI CARDINALITY;

	/** http://www.w3.org/2002/07/owl#Ontology */
	public final static IRI ONTOLOGY;

	/** http://www.w3.org/2002/07/owl#imports */
	public final static IRI IMPORTS;

	/** http://www.w3.org/2002/07/owl#intersectionOf */
	public final static IRI INTERSECTIONOF;

	/** http://www.w3.org/2002/07/owl#versionInfo */
	public final static IRI VERSIONINFO;

	/** http://www.w3.org/2002/07/owl#versionIRI */
	public final static IRI VERSIONIRI;

	/** http://www.w3.org/2002/07/owl#priorVersion */
	public final static IRI PRIORVERSION;

	/** http://www.w3.org/2002/07/owl#backwardCompatibleWith */
	public final static IRI BACKWARDCOMPATIBLEWITH;

	/** http://www.w3.org/2002/07/owl#incompatibleWith */
	public final static IRI INCOMPATIBLEWITH;

	/** http://www.w3.org/2002/07/owl#DeprecatedClass */
	public final static IRI DEPRECATEDCLASS;

	/** http://www.w3.org/2002/07/owl#DeprecatedProperty */
	public final static IRI DEPRECATEDPROPERTY;

	/** http://www.w3.org/2002/07/owl#AnnotationProperty */
	public final static IRI ANNOTATIONPROPERTY;

	/** http://www.w3.org/2002/07/owl#OntologyProperty */
	public final static IRI ONTOLOGYPROPERTY;

	// OWL DL and OWL Full

	/** http://www.w3.org/2002/07/owl#oneOf */
	public final static IRI ONEOF;

	/** http://www.w3.org/2002/07/owl#hasValue */
	public final static IRI HASVALUE;

	/** http://www.w3.org/2002/07/owl#disjointWith */
	public final static IRI DISJOINTWITH;

	/** http://www.w3.org/2002/07/owl#unionOf */
	public final static IRI UNIONOF;

	/** http://www.w3.org/2002/07/owl#complementOf */
	public final static IRI COMPLEMENTOF;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		CLASS = factory.createIRI(OWL.NAMESPACE, "Class");
		INDIVIDUAL = factory.createIRI(OWL.NAMESPACE, "Individual");

		THING = factory.createIRI(OWL.NAMESPACE, "Thing");
		NOTHING = factory.createIRI(NAMESPACE, "Nothing");

		EQUIVALENTCLASS = factory.createIRI(OWL.NAMESPACE, "equivalentClass");
		EQUIVALENTPROPERTY = factory.createIRI(OWL.NAMESPACE, "equivalentProperty");
		SAMEAS = factory.createIRI(OWL.NAMESPACE, "sameAs");
		DIFFERENTFROM = factory.createIRI(OWL.NAMESPACE, "differentFrom");
		ALLDIFFERENT = factory.createIRI(OWL.NAMESPACE, "AllDifferent");

		DISTINCTMEMBERS = factory.createIRI(OWL.NAMESPACE, "distinctMembers");

		OBJECTPROPERTY = factory.createIRI(OWL.NAMESPACE, "ObjectProperty");

		DATATYPEPROPERTY = factory.createIRI(OWL.NAMESPACE, "DatatypeProperty");

		INVERSEOF = factory.createIRI(OWL.NAMESPACE, "inverseOf");

		TRANSITIVEPROPERTY = factory.createIRI(OWL.NAMESPACE, "TransitiveProperty");

		SYMMETRICPROPERTY = factory.createIRI(OWL.NAMESPACE, "SymmetricProperty");

		FUNCTIONALPROPERTY = factory.createIRI(OWL.NAMESPACE, "FunctionalProperty");

		INVERSEFUNCTIONALPROPERTY = factory.createIRI(OWL.NAMESPACE, "InverseFunctionalProperty");

		RESTRICTION = factory.createIRI(OWL.NAMESPACE, "Restriction");

		ONPROPERTY = factory.createIRI(OWL.NAMESPACE, "onProperty");

		ALLVALUESFROM = factory.createIRI(OWL.NAMESPACE, "allValuesFrom");

		SOMEVALUESFROM = factory.createIRI(OWL.NAMESPACE, "someValuesFrom");

		MINCARDINALITY = factory.createIRI(OWL.NAMESPACE, "minCardinality");

		MAXCARDINALITY = factory.createIRI(OWL.NAMESPACE, "maxCardinality");

		CARDINALITY = factory.createIRI(OWL.NAMESPACE, "cardinality");

		ONTOLOGY = factory.createIRI(OWL.NAMESPACE, "Ontology");

		IMPORTS = factory.createIRI(OWL.NAMESPACE, "imports");

		INTERSECTIONOF = factory.createIRI(OWL.NAMESPACE, "intersectionOf");

		VERSIONINFO = factory.createIRI(OWL.NAMESPACE, "versionInfo");

		VERSIONIRI = factory.createIRI(OWL.NAMESPACE, "versionIRI");

		PRIORVERSION = factory.createIRI(OWL.NAMESPACE, "priorVersion");

		BACKWARDCOMPATIBLEWITH = factory.createIRI(OWL.NAMESPACE, "backwardCompatibleWith");

		INCOMPATIBLEWITH = factory.createIRI(OWL.NAMESPACE, "incompatibleWith");

		DEPRECATEDCLASS = factory.createIRI(OWL.NAMESPACE, "DeprecatedClass");

		DEPRECATEDPROPERTY = factory.createIRI(OWL.NAMESPACE, "DeprecatedProperty");

		ANNOTATIONPROPERTY = factory.createIRI(OWL.NAMESPACE, "AnnotationProperty");

		ONTOLOGYPROPERTY = factory.createIRI(OWL.NAMESPACE, "OntologyProperty");

		// OWL DL and OWL Full

		ONEOF = factory.createIRI(OWL.NAMESPACE, "oneOf");

		HASVALUE = factory.createIRI(OWL.NAMESPACE, "hasValue");

		DISJOINTWITH = factory.createIRI(OWL.NAMESPACE, "disjointWith");

		UNIONOF = factory.createIRI(OWL.NAMESPACE, "unionOf");

		COMPLEMENTOF = factory.createIRI(OWL.NAMESPACE, "complementOf");

	}
}
