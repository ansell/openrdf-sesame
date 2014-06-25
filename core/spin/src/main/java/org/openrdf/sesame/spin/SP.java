package org.openrdf.sesame.spin;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for SP.
 * 
 * @see <a href="http:spinrdf.org/sp.html">SP - SPARQL Syntax<a>
 * 
 *      Tools used SP to get this file started
 * 
 *      grep "rdf:ID=" sp |cut -f 2 -d "=" > sed
 *      's/^\([a-z][a-zA-Z_]*\)\(.*)/\1_\2\3/' SP.java > SP.java2 gsed -e
 *      's/^\([a-z][a-zA-Z_]*\)\(.*\)/\U\1\E\2/' SP.java2
 */
public class SP {

	public static final String NAMESPACE = "http:spinrdf.org/sp#";

	/**
	 * Recommended prefix for the SP namespace: "sp"
	 */
	public static final String PREFIX = "sp";

	public static final URI ARG_5;

	public static final URI ARG;

	public static final URI PATH_1;

	public static final URI SYSTEM_PROPERTY;

	public static final URI ARG_1;

	public static final URI DEFAULT;

	public static final URI OBJECT;

	public static final URI GRAPH_NAMENODE;

	public static final URI VAR_NAME;

	public static final URI NAMED;

	public static final URI AS;

	public static final URI DISTINCT;

	public static final URI PATH_2;

	public static final URI VARIABLE;

	public static final URI ARG_4;

	public static final URI ORDER_BY;

	public static final URI INSERT_PATTERN;

	public static final URI MOD_MIN;

	public static final URI FROM;

	public static final URI OFFSET;

	public static final URI ALL;

	public static final URI DELETE_PATTERN;

	public static final URI EXPRESSION;

	public static final URI SUBJECT;

	public static final URI PREDICATE;

	public static final URI ELEMENTS;

	public static final URI NODE;

	public static final URI FROM_NAMED;

	public static final URI ARG_2;

	public static final URI MOD_MAX;

	public static final URI PATH;

	public static final URI TEXT;

	public static final URI RESULT_VARIABLES;

	public static final URI WHERE;

	public static final URI DOCUMENT;

	public static final URI SERVICE_URI;

	public static final URI WITH;

	public static final URI INTO;

	public static final URI SUB_PATH;

	public static final URI REDUCED;

	public static final URI ARG_3;

	public static final URI USING_NAMED;

	public static final URI RESULT_NODES;

	public static final URI TEMPLATES;

	public static final URI USING;

	public static final URI LIMIT;

	public static final URI GRAPH_IRI;

	public static final URI GROUP_BY;

	public static final URI QUERY;

	public static final URI HAVING;

	public static final URI SILENT;

	public static final URI SEQ_PATH;

	public static final URI DESCRIBE;

	public static final URI CLEAR;

	public static final URI MINUS;

	public static final URI FILTER;

	public static final URI SELECT;

	public static final URI SERVICE;

	public static final URI DELETE_WHERE;

	public static final URI INSERT_DATA;

	public static final URI DROP;

	public static final URI NOT_EXISTS;

	public static final URI REVERSE_LINK_PATH;

	public static final URI COMMAND;

	public static final URI NAMED_GRAPH;

	public static final URI CREATE;

	public static final URI MOD_PATH;

	public static final URI ASK;

	public static final URI CONSTRUCT;

	public static final URI REVERSE_PATH;

	public static final URI COUNT;

	public static final URI ALT_PATH;

	public static final URI OPTIONAL;

	public static final URI MIN;

	public static final URI DELETE;

	public static final URI SUB_QUERY;

	public static final URI ELEMENT_LIST;

	public static final URI BIND;

	public static final URI LET;

	public static final URI TUPLE;

	public static final URI TRIPLE_PATH;

	public static final URI AVG;

	public static final URI MODIFY;

	public static final URI INSERT;

	public static final URI MAX;

	public static final URI TRIPLE_TEMPLATE;

	public static final URI DESC;

	public static final URI DELETE_DATA;

	public static final URI UPDATE;

	public static final URI LOAD;

	public static final URI TRIPLE;

	public static final URI ELEMENT;

	public static final URI TRIPLE_PATTERN;

	public static final URI ELEMENT_GROUP;

	public static final URI UNION;

	public static final URI AGGREGATION;

	public static final URI SUM;

	public static final URI ORDER_BY_CONDITION;

	public static final URI ASC;

	public static final URI SYSTEM_CLASS;

	public static final URI VARIABLE_PREDICATE;

	public static final URI QUERY_PREDICATE;

	public static final URI PATH_PREDICATE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PATH = factory.createURI(SP.NAMESPACE, "Path");
		SYSTEM_CLASS = factory.createURI(SP.NAMESPACE, "SystemClass");
		ASC = factory.createURI(SP.NAMESPACE, "Asc");
		ORDER_BY_CONDITION = factory
				.createURI(SP.NAMESPACE, "OrderByCondition");
		SUM = factory.createURI(SP.NAMESPACE, "Sum");
		AGGREGATION = factory.createURI(SP.NAMESPACE, "Aggregation");
		UNION = factory.createURI(SP.NAMESPACE, "Union");
		ELEMENT_GROUP = factory.createURI(SP.NAMESPACE, "ElementGroup");
		TRIPLE_PATTERN = factory.createURI(SP.NAMESPACE, "TriplePattern");
		ELEMENT = factory.createURI(SP.NAMESPACE, "Element");
		TRIPLE = factory.createURI(SP.NAMESPACE, "Triple");
		LOAD = factory.createURI(SP.NAMESPACE, "Load");
		UPDATE = factory.createURI(SP.NAMESPACE, "Update");
		DELETE_DATA = factory.createURI(SP.NAMESPACE, "DeleteData");
		DESC = factory.createURI(SP.NAMESPACE, "Desc");
		TRIPLE_TEMPLATE = factory.createURI(SP.NAMESPACE, "TripleTemplate");
		MAX = factory.createURI(SP.NAMESPACE, "Max");
		INSERT = factory.createURI(SP.NAMESPACE, "Insert");
		MODIFY = factory.createURI(SP.NAMESPACE, "Modify");
		AVG = factory.createURI(SP.NAMESPACE, "Avg");
		TRIPLE_PATH = factory.createURI(SP.NAMESPACE, "TriplePath");
		TUPLE = factory.createURI(SP.NAMESPACE, "Tuple");
		LET = factory.createURI(SP.NAMESPACE, "Let");
		BIND = factory.createURI(SP.NAMESPACE, "Bind");
		ELEMENT_LIST = factory.createURI(SP.NAMESPACE, "ElementList");
		SUB_QUERY = factory.createURI(SP.NAMESPACE, "SubQuery");
		DELETE = factory.createURI(SP.NAMESPACE, "Delete");
		MIN = factory.createURI(SP.NAMESPACE, "Min");
		OPTIONAL = factory.createURI(SP.NAMESPACE, "Optional");
		ALT_PATH = factory.createURI(SP.NAMESPACE, "AltPath");
		COUNT = factory.createURI(SP.NAMESPACE, "Count");
		REVERSE_PATH = factory.createURI(SP.NAMESPACE, "ReversePath");
		CONSTRUCT = factory.createURI(SP.NAMESPACE, "Construct");
		QUERY = factory.createURI(SP.NAMESPACE, "Query");
		VARIABLE = factory.createURI(SP.NAMESPACE, "Variable");
		ASK = factory.createURI(SP.NAMESPACE, "Ask");
		MOD_PATH = factory.createURI(SP.NAMESPACE, "ModPath");
		CREATE = factory.createURI(SP.NAMESPACE, "Create");
		NAMED_GRAPH = factory.createURI(SP.NAMESPACE, "NamedGraph");
		COMMAND = factory.createURI(SP.NAMESPACE, "Command");
		REVERSE_LINK_PATH = factory.createURI(SP.NAMESPACE, "ReverseLinkPath");
		NOT_EXISTS = factory.createURI(SP.NAMESPACE, "NotExists");
		DROP = factory.createURI(SP.NAMESPACE, "Drop");
		INSERT_DATA = factory.createURI(SP.NAMESPACE, "InsertData");
		DELETE_WHERE = factory.createURI(SP.NAMESPACE, "DeleteWhere");
		SERVICE = factory.createURI(SP.NAMESPACE, "Service");
		SELECT = factory.createURI(SP.NAMESPACE, "Select");
		FILTER = factory.createURI(SP.NAMESPACE, "Filter");
		MINUS = factory.createURI(SP.NAMESPACE, "Minus");
		CLEAR = factory.createURI(SP.NAMESPACE, "Clear");
		DESCRIBE = factory.createURI(SP.NAMESPACE, "Describe");
		SEQ_PATH = factory.createURI(SP.NAMESPACE, "SeqPath");
		ARG_5 = factory.createURI(SP.NAMESPACE, "arg5");
		ARG = factory.createURI(SP.NAMESPACE, "arg");
		PATH_1 = factory.createURI(SP.NAMESPACE, "path1");
		SYSTEM_PROPERTY = factory.createURI(SP.NAMESPACE, "systemProperty");
		ARG_1 = factory.createURI(SP.NAMESPACE, "arg1");
		DEFAULT = factory.createURI(SP.NAMESPACE, "default");
		OBJECT = factory.createURI(SP.NAMESPACE, "object");
		GRAPH_NAMENODE = factory.createURI(SP.NAMESPACE, "graphNameNode");
		VAR_NAME = factory.createURI(SP.NAMESPACE, "varName");
		NAMED = factory.createURI(SP.NAMESPACE, "named");
		AS = factory.createURI(SP.NAMESPACE, "as");
		DISTINCT = factory.createURI(SP.NAMESPACE, "distinct");
		PATH_2 = factory.createURI(SP.NAMESPACE, "path2");
		ORDER_BY = factory.createURI(SP.NAMESPACE, "orderBy");
		VARIABLE_PREDICATE = factory.createURI(SP.NAMESPACE, "variable");
		ARG_4 = factory.createURI(SP.NAMESPACE, "arg4");
		SILENT = factory.createURI(SP.NAMESPACE, "silent");
		HAVING = factory.createURI(SP.NAMESPACE, "having");
		QUERY_PREDICATE = factory.createURI(SP.NAMESPACE, "query");
		GROUP_BY = factory.createURI(SP.NAMESPACE, "groupBy");
		GRAPH_IRI = factory.createURI(SP.NAMESPACE, "graphIRI");
		LIMIT = factory.createURI(SP.NAMESPACE, "limit");
		USING = factory.createURI(SP.NAMESPACE, "using");
		TEMPLATES = factory.createURI(SP.NAMESPACE, "templates");
		RESULT_NODES = factory.createURI(SP.NAMESPACE, "resultNodes");
		USING_NAMED = factory.createURI(SP.NAMESPACE, "usingNamed");
		ARG_3 = factory.createURI(SP.NAMESPACE, "arg3");
		REDUCED = factory.createURI(SP.NAMESPACE, "reduced");
		SUB_PATH = factory.createURI(SP.NAMESPACE, "subPath");
		INTO = factory.createURI(SP.NAMESPACE, "into");
		WITH = factory.createURI(SP.NAMESPACE, "with");
		SERVICE_URI = factory.createURI(SP.NAMESPACE, "serviceURI");
		DOCUMENT = factory.createURI(SP.NAMESPACE, "document");
		WHERE = factory.createURI(SP.NAMESPACE, "where");
		RESULT_VARIABLES = factory.createURI(SP.NAMESPACE, "resultVariables");
		TEXT = factory.createURI(SP.NAMESPACE, "text");
		PATH_PREDICATE = factory.createURI(SP.NAMESPACE, "path");
		MOD_MAX = factory.createURI(SP.NAMESPACE, "modMax");
		PREDICATE = factory.createURI(SP.NAMESPACE, "predicate");
		ELEMENTS = factory.createURI(SP.NAMESPACE, "elements");
		NODE = factory.createURI(SP.NAMESPACE, "node");
		FROM_NAMED = factory.createURI(SP.NAMESPACE, "fromNamed");
		ARG_2 = factory.createURI(SP.NAMESPACE, "arg2");
		SUBJECT = factory.createURI(SP.NAMESPACE, "subject");
		EXPRESSION = factory.createURI(SP.NAMESPACE, "expression");
		DELETE_PATTERN = factory.createURI(SP.NAMESPACE, "deletePattern");
		ALL = factory.createURI(SP.NAMESPACE, "all");
		OFFSET = factory.createURI(SP.NAMESPACE, "offset");
		FROM = factory.createURI(SP.NAMESPACE, "from");
		MOD_MIN = factory.createURI(SP.NAMESPACE, "modMin");
		INSERT_PATTERN = factory.createURI(SP.NAMESPACE, "insertPattern");
	}
}
