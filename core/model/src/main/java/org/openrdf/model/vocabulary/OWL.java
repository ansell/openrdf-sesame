/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for OWL primitives and for the OWL namespace.
 * 
 * @see http://www.w3.org/TR/owl-ref/
 */
public class OWL {

	/** The OWL namespace: http://www.w3.org/2002/07/owl# */
	public static final String NAMESPACE = "http://www.w3.org/2002/07/owl#";

	/** 
	 * Recommended prefix for the OWL namespace: "owl"
	 */
	public static final String PREFIX = "owl";
	
	// OWL Lite

	/** http://www.w3.org/2002/07/owl#Class */
	public final static URI CLASS;

	/** http://www.w3.org/2002/07/owl#Individual */
	public final static URI INDIVIDUAL;
	
	/** http://www.w3.org/2002/07/owl#Thing */
	public static final URI THING;
	
	/** http://www.w3.org/2002/07/owl#Nothing */
	public static final URI NOTHING;

	/** http://www.w3.org/2002/07/owl#equivalentClass */
	public final static URI EQUIVALENTCLASS;

	/** http://www.w3.org/2002/07/owl#equivalentProperty */
	public final static URI EQUIVALENTPROPERTY;

	/** http://www.w3.org/2002/07/owl#sameAs */
	public final static URI SAMEAS;

	/** http://www.w3.org/2002/07/owl#differentFrom */
	public final static URI DIFFERENTFROM;

	/** http://www.w3.org/2002/07/owl#AllDifferent */
	public final static URI ALLDIFFERENT;

	/** http://www.w3.org/2002/07/owl#distinctMembers */
	public final static URI DISTINCTMEMBERS;

	/** http://www.w3.org/2002/07/owl#ObjectProperty */
	public final static URI OBJECTPROPERTY;

	/** http://www.w3.org/2002/07/owl#DatatypeProperty */
	public final static URI DATATYPEPROPERTY;

	/** http://www.w3.org/2002/07/owl#inverseOf */
	public final static URI INVERSEOF;

	/** http://www.w3.org/2002/07/owl#TransitiveProperty */
	public final static URI TRANSITIVEPROPERTY;

	/** http://www.w3.org/2002/07/owl#SymmetricProperty */
	public final static URI SYMMETRICPROPERTY;

	/** http://www.w3.org/2002/07/owl#FunctionalProperty */
	public final static URI FUNCTIONALPROPERTY;

	/** http://www.w3.org/2002/07/owl#InverseFunctionalProperty */
	public final static URI INVERSEFUNCTIONALPROPERTY;

	/** http://www.w3.org/2002/07/owl#Restriction */
	public final static URI RESTRICTION;

	/** http://www.w3.org/2002/07/owl#onProperty */
	public final static URI ONPROPERTY;

	/** http://www.w3.org/2002/07/owl#allValuesFrom */
	public final static URI ALLVALUESFROM;

	/** http://www.w3.org/2002/07/owl#someValuesFrom */
	public final static URI SOMEVALUESFROM;

	/** http://www.w3.org/2002/07/owl#minCardinality */
	public final static URI MINCARDINALITY;

	/** http://www.w3.org/2002/07/owl#maxCardinality */
	public final static URI MAXCARDINALITY;

	/** http://www.w3.org/2002/07/owl#cardinality */
	public final static URI CARDINALITY;

	/** http://www.w3.org/2002/07/owl#Ontology */
	public final static URI ONTOLOGY;

	/** http://www.w3.org/2002/07/owl#imports */
	public final static URI IMPORTS;

	/** http://www.w3.org/2002/07/owl#intersectionOf */
	public final static URI INTERSECTIONOF;

	/** http://www.w3.org/2002/07/owl#versionInfo */
	public final static URI VERSIONINFO;

	/** http://www.w3.org/2002/07/owl#priorVersion */
	public final static URI PRIORVERSION;

	/** http://www.w3.org/2002/07/owl#backwardCompatibleWith */
	public final static URI BACKWARDCOMPATIBLEWITH;

	/** http://www.w3.org/2002/07/owl#incompatibleWith */
	public final static URI INCOMPATIBLEWITH;

	/** http://www.w3.org/2002/07/owl#DeprecatedClass */
	public final static URI DEPRECATEDCLASS;

	/** http://www.w3.org/2002/07/owl#DeprecatedProperty */
	public final static URI DEPRECATEDPROPERTY;

	/** http://www.w3.org/2002/07/owl#AnnotationProperty */
	public final static URI ANNOTATIONPROPERTY;

	/** http://www.w3.org/2002/07/owl#OntologyProperty */
	public final static URI ONTOLOGYPROPERTY;

	// OWL DL and OWL Full

	/** http://www.w3.org/2002/07/owl#oneOf */
	public final static URI ONEOF;

	/** http://www.w3.org/2002/07/owl#hasValue */
	public final static URI HASVALUE;

	/** http://www.w3.org/2002/07/owl#disjointWith */
	public final static URI DISJOINTWITH;

	/** http://www.w3.org/2002/07/owl#unionOf */
	public final static URI UNIONOF;

	/** http://www.w3.org/2002/07/owl#complementOf */
	public final static URI COMPLEMENTOF;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		
		CLASS = factory.createURI(OWL.NAMESPACE, "Class");
		INDIVIDUAL = factory.createURI(OWL.NAMESPACE, "Individual");
		
		THING = factory.createURI(OWL.NAMESPACE, "Thing");
		NOTHING = factory.createURI(NAMESPACE, "Nothing");
		
		EQUIVALENTCLASS = factory.createURI(OWL.NAMESPACE, "equivalentClass");
		EQUIVALENTPROPERTY = factory.createURI(OWL.NAMESPACE,
				"equivalentProperty");
		SAMEAS = factory.createURI(OWL.NAMESPACE, "sameAs");
		DIFFERENTFROM = factory.createURI(OWL.NAMESPACE, "differentFrom");
		ALLDIFFERENT = factory.createURI(OWL.NAMESPACE, "AllDifferent");

		DISTINCTMEMBERS = factory.createURI(OWL.NAMESPACE, "distinctMembers");

		OBJECTPROPERTY = factory.createURI(OWL.NAMESPACE, "ObjectProperty");

		DATATYPEPROPERTY = factory.createURI(OWL.NAMESPACE, "DatatypeProperty");

		INVERSEOF = factory.createURI(OWL.NAMESPACE, "inverseOf");

		TRANSITIVEPROPERTY = factory.createURI(OWL.NAMESPACE,
				"TransitiveProperty");

		SYMMETRICPROPERTY = factory.createURI(OWL.NAMESPACE, "SymmetricProperty");

		FUNCTIONALPROPERTY = factory.createURI(OWL.NAMESPACE,
				"FunctionalProperty");

		INVERSEFUNCTIONALPROPERTY = factory.createURI(OWL.NAMESPACE,
				"InverseFunctionalProperty");

		RESTRICTION = factory.createURI(OWL.NAMESPACE, "Restriction");

		ONPROPERTY = factory.createURI(OWL.NAMESPACE, "onProperty");

		ALLVALUESFROM = factory.createURI(OWL.NAMESPACE, "allValuesFrom");

		SOMEVALUESFROM = factory.createURI(OWL.NAMESPACE, "someValuesFrom");

		MINCARDINALITY = factory.createURI(OWL.NAMESPACE, "minCardinality");

		MAXCARDINALITY = factory.createURI(OWL.NAMESPACE, "maxCardinality");

		CARDINALITY = factory.createURI(OWL.NAMESPACE, "cardinality");

		ONTOLOGY = factory.createURI(OWL.NAMESPACE, "Ontology");

		IMPORTS = factory.createURI(OWL.NAMESPACE, "imports");

		INTERSECTIONOF = factory.createURI(OWL.NAMESPACE, "intersectionOf");

		VERSIONINFO = factory.createURI(OWL.NAMESPACE, "versionInfo");

		PRIORVERSION = factory.createURI(OWL.NAMESPACE, "priorVersion");

		BACKWARDCOMPATIBLEWITH = factory.createURI(OWL.NAMESPACE,
				"backwardCompatibleWith");

		INCOMPATIBLEWITH = factory.createURI(OWL.NAMESPACE, "incompatibleWith");

		DEPRECATEDCLASS = factory.createURI(OWL.NAMESPACE, "DeprecatedClass");

		DEPRECATEDPROPERTY = factory.createURI(OWL.NAMESPACE,
				"DeprecatedProperty");

		ANNOTATIONPROPERTY = factory.createURI(OWL.NAMESPACE,
				"AnnotationProperty");

		ONTOLOGYPROPERTY = factory.createURI(OWL.NAMESPACE, "OntologyProperty");

		// OWL DL and OWL Full

		ONEOF = factory.createURI(OWL.NAMESPACE, "oneOf");

		HASVALUE = factory.createURI(OWL.NAMESPACE, "hasValue");

		DISJOINTWITH = factory.createURI(OWL.NAMESPACE, "disjointWith");

		UNIONOF = factory.createURI(OWL.NAMESPACE, "unionOf");

		COMPLEMENTOF = factory.createURI(OWL.NAMESPACE, "complementOf");

	}
}
