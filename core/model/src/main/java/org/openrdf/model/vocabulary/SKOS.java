package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the <a href="http://www.w3.org/2004/02/skos/">Simple
 * Knowledge Organization System (SKOS)</a>.
 * 
 * @see http://www.w3.org/TR/skos-reference/
 * @author Jeen Broekstra
 */
public class SKOS {

	/**
	 * The SKOS namespace: http://www.w3.org/2004/02/skos/core#
	 */
	public static final String NAMESPACE = "http://www.w3.org/2004/02/skos/core#";

	/**
	 * The recommended prefix for the SKOS namespace: "skos"
	 */
	public static final String PREFIX = "skos";

	/* classes */

	/**
	 * The skos:Concept class
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#concepts
	 */
	public static final URI CONCEPT;

	/**
	 * The skos:ConceptScheme class
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#schemes
	 */
	public static final URI CONCEPT_SCHEME;

	/**
	 * The skos:Collection class
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#collections
	 */
	public static final URI COLLECTION;

	/**
	 * The skos:OrderedCollection class
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#collections
	 */
	public static final URI ORDERED_COLLECTION;

	/* lexical labels */

	/**
	 * The skos:prefLabel lexical label property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#labels
	 */
	public static final URI PREF_LABEL;

	/**
	 * The skos:altLabel lexical label property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#labels
	 */
	public static final URI ALT_LABEL;

	/**
	 * The skos:hiddenLabel lexical label property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#labels
	 */
	public static final URI HIDDEN_LABEL;

	/* Concept Scheme properties */

	/**
	 * The skos:inScheme relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#schemes
	 */
	public static final URI IN_SCHEME;

	/**
	 * The skos:hasTopConcept relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#schemes
	 */
	public static final URI HAS_TOP_CONCEPT;

	/**
	 * The skos:topConceptOf relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#schemes
	 */
	public static final URI TOP_CONCEPT_OF;

	/* collection properties */

	/**
	 * The skos:member relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#collections
	 */
	public static final URI MEMBER;
	
	/**
	 * The skos:memberList relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#collections
	 */
	public static final URI MEMBER_LIST;
	

	/* notation properties */

	/**
	 * The skos:notation property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#notations
	 */
	public static final URI NOTATION;

	/* documentation properties */

	/**
	 * The skos:changeNote property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#notes
	 */
	public static final URI CHANGE_NOTE;

	/**
	 * The skos:definition property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#notes
	 */
	public static final URI DEFINITION;

	/**
	 * The skos:editorialNote property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#notes
	 */
	public static final URI EDITORIAL_NOTE;

	/**
	 * The skos:example property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#notes
	 */

	public static final URI EXAMPLE;

	/**
	 * The skos:historyNote property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#notes
	 */
	public static final URI HISTORY_NOTE;

	/**
	 * The skos:note property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#notes
	 */
	public static final URI NOTE;

	/**
	 * The skos:scopeNote property.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#notes
	 */
	public static final URI SCOPE_NOTE;

	/* semantic relations */

	/**
	 * The skos:broader relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#semantic-relations
	 */
	public static final URI BROADER;

	/**
	 * The skos:broaderTransitive relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#semantic-relations
	 */
	public static final URI BROADER_TRANSITIVE;

	/**
	 * The skos:narrower relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#semantic-relations
	 */
	public static final URI NARROWER;
	
	/**
	 * The skos:narrowerTransitive relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#semantic-relations
	 */
	public static final URI NARROWER_TRANSITIVE;
	
	/**
	 * The skos:related relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#semantic-relations
	 */
	public static final URI RELATED;
	
	/**
	 * The skos:semanticRelation relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#semantic-relations
	 */
	public static final URI SEMANTIC_RELATION;
	
	/* mapping properties */
	
	/**
	 * The skos:broadMatch relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#mapping
	 */
	public static final URI BROAD_MATCH;
	
	/**
	 * The skos:closeMatch relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#mapping
	 */
	public static final URI CLOSE_MATCH;

	/**
	 * The skos:exactMatch relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#mapping
	 */
	public static final URI EXACT_MATCH;
	
	/**
	 * The skos:mappingRelation relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#mapping
	 */
	public static final URI MAPPING_RELATION;
	
	/**
	 * The skos:narrowMatch relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#mapping
	 */
	public static final URI NARROW_MATCH;
	
	/**
	 * The skos:relatedMatch relation.
	 * 
	 * @see http://www.w3.org/TR/skos-reference/#mapping
	 */
	public static final URI RELATED_MATCH;
	
	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		CONCEPT = f.createURI(NAMESPACE, "Concept");
		CONCEPT_SCHEME = f.createURI(NAMESPACE, "ConceptScheme");
		COLLECTION = f.createURI(NAMESPACE, "Collection");
		ORDERED_COLLECTION = f.createURI(NAMESPACE, "OrderedCollection");

		PREF_LABEL = f.createURI(NAMESPACE, "prefLabel");
		ALT_LABEL = f.createURI(NAMESPACE, "altLabel");

		BROADER = f.createURI(NAMESPACE, "broader");
		NARROWER = f.createURI(NAMESPACE, "narrower");

		HAS_TOP_CONCEPT = f.createURI(NAMESPACE, "hasTopConcept");
		MEMBER = f.createURI(NAMESPACE, "member");
		
		HIDDEN_LABEL = f.createURI(NAMESPACE, "hiddenLabel");
		
		IN_SCHEME = f.createURI(NAMESPACE, "inScheme");
		
		TOP_CONCEPT_OF = f.createURI(NAMESPACE, "topConceptOf");
		
		MEMBER_LIST = f.createURI(NAMESPACE, "memberList");
		NOTATION = f.createURI(NAMESPACE, "notation");
		CHANGE_NOTE = f.createURI(NAMESPACE, "changeNote");
		DEFINITION = f.createURI(NAMESPACE, "definition");
		EDITORIAL_NOTE = f.createURI(NAMESPACE, "editorialNote");
		EXAMPLE = f.createURI(NAMESPACE, "example");
		HISTORY_NOTE = f.createURI(NAMESPACE, "historyNote");
		NOTE = f.createURI(NAMESPACE, "note");
		SCOPE_NOTE = f.createURI(NAMESPACE, "scopeNote");
		BROADER_TRANSITIVE = f.createURI(NAMESPACE, "broaderTransitive");
		NARROWER_TRANSITIVE = f.createURI(NAMESPACE, "narrowerTransitive");
		RELATED = f.createURI(NAMESPACE, "related");
		SEMANTIC_RELATION = f.createURI(NAMESPACE, "semanticRelation");
		BROAD_MATCH = f.createURI(NAMESPACE, "broadMatch");
		CLOSE_MATCH = f.createURI(NAMESPACE, "closeMatch");
		EXACT_MATCH = f.createURI(NAMESPACE, "exactMatch");
		MAPPING_RELATION = f.createURI(NAMESPACE, "mappingRelation");
		NARROW_MATCH = f.createURI(NAMESPACE, "narrowMatch");
		RELATED_MATCH = f.createURI(NAMESPACE, "relatedMatch");
		
	}
}
