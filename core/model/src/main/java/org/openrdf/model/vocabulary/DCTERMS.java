package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary constants for the Dublin Core Terms
 * 
 * @see http://dublincore.org/documents/dcmi-terms/
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class DCTERMS {

	/**
	 * Dublin Core Terms namespace: http://purl.org/dc/terms/
	 */
	public static final String NAMESPACE = "http://purl.org/dc/terms/";

	/**
	 * Recommend prefix for the Dublin Core Terms namespace: "dcterms"
	 */
	public static final String PREFIX = "dcterms";

	// ----------------------------------------
	// Properties common to Dublin Core Elements set
	// ----------------------------------------

	/**
	 * http://purl.org/dc/terms/contributor
	 */
	public static final URI CONTRIBUTOR;

	/**
	 * http://purl.org/dc/terms/coverage
	 */
	public static final URI COVERAGE;

	/**
	 * http://purl.org/dc/terms/creator
	 */
	public static final URI CREATOR;

	/**
	 * http://purl.org/dc/terms/date
	 */
	public static final URI DATE;

	/**
	 * http://purl.org/dc/terms/description
	 */
	public static final URI DESCRIPTION;

	/**
	 * http://purl.org/dc/terms/format
	 */
	public static final URI FORMAT;

	/**
	 * http://purl.org/dc/terms/identifier
	 */
	public static final URI IDENTIFIER;

	/**
	 * http://purl.org/dc/terms/language
	 */
	public static final URI LANGUAGE;

	/**
	 * http://purl.org/dc/terms/publisher
	 */
	public static final URI PUBLISHER;

	/**
	 * http://purl.org/dc/terms/relation
	 */
	public static final URI RELATION;

	/**
	 * http://purl.org/dc/terms/rights
	 */
	public static final URI RIGHTS;

	/**
	 * http://purl.org/dc/terms/source
	 */
	public static final URI SOURCE;

	/**
	 * http://purl.org/dc/terms/subject
	 */
	public static final URI SUBJECT;

	/**
	 * http://purl.org/dc/terms/title
	 */
	public static final URI TITLE;

	/**
	 * http://purl.org/dc/terms/type
	 */
	public static final URI TYPE;

	// ----------------------------------------
	// Properties unique to Dublin Core Terms set
	// ----------------------------------------

	/**
	 * http://purl.org/dc/terms/abstract
	 */
	public static final URI ABSTRACT;

	/**
	 * http://purl.org/dc/terms/accessRights
	 */
	public static final URI ACCESS_RIGHTS;

	/**
	 * http://purl.org/dc/terms/accrualMethod
	 */
	public static final URI ACCRUAL_METHOD;

	/**
	 * http://purl.org/dc/terms/accrualPeriodicity
	 */
	public static final URI ACCRUAL_PERIODICITY;

	/**
	 * http://purl.org/dc/terms/ accrualPolicy
	 */
	public static final URI ACCRUAL_POLICY;

	/**
	 * http://purl.org/dc/terms/alternative
	 */
	public static final URI ALTERNATIVE;

	/**
	 * http://purl.org/dc/terms/audience
	 */
	public static final URI AUDIENCE;

	/**
	 * http://purl.org/dc/terms/available
	 */
	public static final URI AVAILABLE;

	/**
	 * http://purl.org/dc/terms/bibliographicCitation
	 */
	public static final URI BIBLIOGRAPHIC_CITATION;

	/**
	 * http://purl.org/dc/terms/conformsTo
	 */
	public static final URI CONFORMS_TO;

	/**
	 * http://purl.org/dc/terms/created
	 */
	public static final URI CREATED;

	/**
	 * http://purl.org/dc/terms/dateAccepted
	 */
	public static final URI DATE_ACCEPTED;

	/**
	 * http://purl.org/dc/terms/dateCopyrighted
	 */
	public static final URI DATE_COPYRIGHTED;

	/**
	 * http://purl.org/dc/terms/dateSubmitted
	 */
	public static final URI DATE_SUBMITTED;

	/**
	 * http://purl.org/dc/terms/educationLevel
	 */
	public static final URI EDUCATION_LEVEL;

	/**
	 * http://purl.org/dc/terms/extent
	 */
	public static final URI EXTENT;

	/**
	 * http://purl.org/dc/terms/hasFormat
	 */
	public static final URI HAS_FORMAT;

	/**
	 * http://purl.org/dc/terms/hasPart
	 */
	public static final URI HAS_PART;

	/**
	 * http://purl.org/dc/terms/hasVersion
	 */
	public static final URI HAS_VERSION;

	/**
	 * http://purl.org/dc/terms/instructionalMethod
	 */
	public static final URI INSTRUCTIONAL_METHOD;

	/**
	 * http://purl.org/dc/terms/isFormatOf
	 */
	public static final URI IS_FORMAT_OF;

	/**
	 * http://purl.org/dc/terms/isPartOf
	 */
	public static final URI IS_PART_OF;

	/**
	 * http://purl.org/dc/terms/isReferencedBy
	 */
	public static final URI IS_REFERENCED_BY;

	/**
	 * http://purl.org/dc/terms/isReplacedBy
	 */
	public static final URI IS_REPLACED_BY;

	/**
	 * http://purl.org/dc/terms/isRequiredBy
	 */
	public static final URI IS_REQUIRED_BY;

	/**
	 * http://purl.org/dc/terms/issued
	 */
	public static final URI ISSUED;

	/**
	 * http://purl.org/dc/terms/isVersionOf
	 */
	public static final URI IS_VERSION_OF;

	/**
	 * http://purl.org/dc/terms/license
	 */
	public static final URI LICENSE;

	/**
	 * http://purl.org/dc/terms/mediator
	 */
	public static final URI MEDIATOR;

	/**
	 * http://purl.org/dc/terms/medium
	 */
	public static final URI MEDIUM;

	/**
	 * http://purl.org/dc/terms/modified
	 */
	public static final URI MODIFIED;

	/**
	 * http://purl.org/dc/terms/provenance
	 */
	public static final URI PROVENANCE;

	/**
	 * http://purl.org/dc/terms/references
	 */
	public static final URI REFERENCES;

	/**
	 * http://purl.org/dc/terms/replaces
	 */
	public static final URI REPLACES;

	/**
	 * http://purl.org/dc/terms/requires
	 */
	public static final URI REQUIRES;

	/**
	 * http://purl.org/dc/terms/rightsHolder
	 */
	public static final URI RIGHTS_HOLDER;

	/**
	 * http://purl.org/dc/terms/spatial
	 */
	public static final URI SPATIAL;

	/**
	 * http://purl.org/dc/terms/tableOfContents
	 */
	public static final URI TABLE_OF_CONTENTS;

	/**
	 * http://purl.org/dc/terms/temporal
	 */
	public static final URI TEMPORAL;

	/**
	 * http://purl.org/dc/terms/valid
	 */
	public static final URI VALID;

	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		// Properties common to Dublin Core Elements
		CONTRIBUTOR = f.createURI(NAMESPACE, "contributor");
		COVERAGE = f.createURI(NAMESPACE, "coverage");
		CREATOR = f.createURI(NAMESPACE, "creator");
		DATE = f.createURI(NAMESPACE, "date");
		DESCRIPTION = f.createURI(NAMESPACE, "description");
		FORMAT = f.createURI(NAMESPACE, "format");
		IDENTIFIER = f.createURI(NAMESPACE, "identifier");
		LANGUAGE = f.createURI(NAMESPACE, "language");
		PUBLISHER = f.createURI(NAMESPACE, "publisher");
		RELATION = f.createURI(NAMESPACE, "relation");
		RIGHTS = f.createURI(NAMESPACE, "rights");
		SOURCE = f.createURI(NAMESPACE, "source");
		SUBJECT = f.createURI(NAMESPACE, "subject");
		TITLE = f.createURI(NAMESPACE, "title");
		TYPE = f.createURI(NAMESPACE, "type");

		// Properties unique to Dublin Core Terms
		ABSTRACT = f.createURI(NAMESPACE, "abstract");
		ACCESS_RIGHTS = f.createURI(NAMESPACE, "accessRights");
		ACCRUAL_METHOD = f.createURI(NAMESPACE, "accuralMethod");
		ACCRUAL_PERIODICITY = f.createURI(NAMESPACE, "accrualPeriodicity");
		ACCRUAL_POLICY = f.createURI(NAMESPACE, "accrualPolicy");
		ALTERNATIVE = f.createURI(NAMESPACE, "alternative");
		AUDIENCE = f.createURI(NAMESPACE, "audience");
		AVAILABLE = f.createURI(NAMESPACE, "available");
		BIBLIOGRAPHIC_CITATION = f.createURI(NAMESPACE, "bibliographicCitation");
		CONFORMS_TO = f.createURI(NAMESPACE, "conformsTo");
		CREATED = f.createURI(NAMESPACE, "created");
		DATE_ACCEPTED = f.createURI(NAMESPACE, "dateAccepted");
		DATE_COPYRIGHTED = f.createURI(NAMESPACE, "dateCopyrighted");
		DATE_SUBMITTED = f.createURI(NAMESPACE, "dateSubmitted");
		EDUCATION_LEVEL = f.createURI(NAMESPACE, "educationLevel");
		EXTENT = f.createURI(NAMESPACE, "extent");
		HAS_FORMAT = f.createURI(NAMESPACE, "hasFormat");
		HAS_PART = f.createURI(NAMESPACE, "hasPart");
		HAS_VERSION = f.createURI(NAMESPACE, "hasVersion");
		INSTRUCTIONAL_METHOD = f.createURI(NAMESPACE, "instructionalMethod");
		IS_FORMAT_OF = f.createURI(NAMESPACE, "isFormatOf");
		IS_PART_OF = f.createURI(NAMESPACE, "isPartOf");
		IS_REFERENCED_BY = f.createURI(NAMESPACE, "isReferencedBy");
		IS_REPLACED_BY = f.createURI(NAMESPACE, "isReplacedBy");
		IS_REQUIRED_BY = f.createURI(NAMESPACE, "isRequiredBy");
		IS_VERSION_OF = f.createURI(NAMESPACE, "isVersionOf");
		ISSUED = f.createURI(NAMESPACE, "issued");
		LICENSE = f.createURI(NAMESPACE, "license");
		MEDIATOR = f.createURI(NAMESPACE, "mediator");
		MEDIUM = f.createURI(NAMESPACE, "medium");
		MODIFIED = f.createURI(NAMESPACE, "modified");
		PROVENANCE = f.createURI(NAMESPACE, "provenance");
		REFERENCES = f.createURI(NAMESPACE, "references");
		REPLACES = f.createURI(NAMESPACE, "replaces");
		REQUIRES = f.createURI(NAMESPACE, "requires");
		RIGHTS_HOLDER = f.createURI(NAMESPACE, "rightsHolder");
		SPATIAL = f.createURI(NAMESPACE, "spatial");
		TABLE_OF_CONTENTS = f.createURI(NAMESPACE, "tableOfContents");
		TEMPORAL = f.createURI(NAMESPACE, "temporal");
		VALID = f.createURI(NAMESPACE, "valid");
	}
}
