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
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @since 2.7.3
 * @version 1.4.0
 */
public class SP {

	/**
	 * http://spinrdf.org/sp An RDF Schema to syntactically represent SPARQL
	 * queries (including SPARQL UPDATE) as RDF triples.
	 */
	public static final String NAMESPACE = "http://spinrdf.org/sp#";

	public static final String PREFIX = "sp";

	/**
	 * http://spinrdf.org/sp#Path The base class of SPARQL property path
	 * expressions. Paths are used by sp:TriplePath triple paths.
	 */
	public static URI PATH_CLASS;

	/**
	 * http://spinrdf.org/sp#SystemClass An "artificial" root class that groups
	 * all SP classes. This makes them look much less overwhelming in UI tools.
	 * Typical end users don't need to see those classes anyway.
	 */
	public static URI SYSTEM_CLASS;

	/**
	 * http://spinrdf.org/sp#Asc Marker to indicate ascending order.
	 */
	public static URI ASC_CLASS;

	/**
	 * http://spinrdf.org/sp#OrderByCondition An abstract base class for
	 * ascending or descending order conditions. Instances of this class
	 * (typically bnodes) must have a value for expression to point to the actual
	 * values.
	 */
	public static URI ORDER_BY_CONDITION_CLASS;

	/**
	 * http://spinrdf.org/sp#Sum Represents sum aggregations, e.g. SELECT
	 * SUM(?varName)...
	 */
	public static URI SUM_CLASS;

	/**
	 * http://spinrdf.org/sp#Aggregation Base class of aggregation types (not
	 * part of the SPARQL 1.0 standard but supported by ARQ and other engines).
	 */
	public static URI AGGREGATION_CLASS;

	/**
	 * http://spinrdf.org/sp#Union A UNION group.
	 */
	public static URI UNION_CLASS;

	/**
	 * http://spinrdf.org/sp#ElementGroup Abstract base class of group patterns.
	 */
	public static URI ELEMENT_GROUP_CLASS;

	/**
	 * http://spinrdf.org/sp#TriplePattern A triple pattern used in the body of a
	 * query.
	 */
	public static URI TRIPLE_PATTERN_CLASS;

	/**
	 * http://spinrdf.org/sp#Element An abstract base class for all pattern
	 * elements.
	 */
	public static URI ELEMENT_CLASS;

	/**
	 * http://spinrdf.org/sp#Triple A base class for TriplePattern and
	 * TripleTemplate. This basically specifies that subject, predicate and
	 * object must be present.
	 */
	public static URI TRIPLE_CLASS;

	/**
	 * http://spinrdf.org/sp#Load A LOAD Update operation. The document to load
	 * is specified using sp:document, and the (optional) target graph using
	 * sp:into.
	 */
	public static URI LOAD_CLASS;

	/**
	 * http://spinrdf.org/sp#Update Abstract base class to group the various
	 * SPARQL UPDATE commands.
	 */
	public static URI UPDATE_CLASS;

	/**
	 * http://spinrdf.org/sp#DeleteData An Update operation to delete specific
	 * triples. The graph triples are represented using sp:data, which points to
	 * an rdf:List of sp:Triples or sp:NamedGraphs.
	 */
	public static URI DELETE_DATA_CLASS;

	/**
	 * http://spinrdf.org/sp#Desc Marker to indicate descending order.
	 */
	public static URI DESC_CLASS;

	/**
	 * http://spinrdf.org/sp#TripleTemplate A prototypical triple used as
	 * template in the head of a Construct query. May contain variables.
	 */
	public static URI TRIPLE_TEMPLATE_CLASS;

	/**
	 * http://spinrdf.org/sp#Max Represents MAX aggregations.
	 */
	public static URI MAX_CLASS;

	/**
	 * http://spinrdf.org/sp#Insert Deprecated - use sp:Modify instead.
	 * Represents a INSERT INTO (part of SPARQL UPDATE language). The graph IRIs
	 * are stored in sp:graphIRI. The template patterns to delete are stored in
	 * sp:insertPattern. The WHERE clause is represented using sp:where.
	 */
	@Deprecated
	public static URI INSERT_CLASS;

	/**
	 * http://spinrdf.org/sp#Modify Represents a MODIFY (part of SPARQL UPDATE
	 * language). The graph IRIs are stored in sp:graphIRI. The template patterns
	 * are stored in sp:deletePattern and sp:insertPattern. The WHERE clause is
	 * represented using sp:where.
	 */
	public static URI MODIFY_CLASS;

	/**
	 * http://spinrdf.org/sp#Insert Deprecated - use sp:Modify instead.
	 * Represents a INSERT INTO (part of SPARQL UPDATE language). The graph IRIs
	 * are stored in sp:graphIRI. The template patterns to delete are stored in
	 * sp:insertPattern. The WHERE clause is represented using sp:where.
	 */
	@Deprecated
	public static URI Insert;

	/**
	 * http://spinrdf.org/sp#Avg Represents AVG aggregations.
	 */
	public static URI AVG_CLASS;

	/**
	 * http://spinrdf.org/sp#TriplePath Similar to a TriplePattern, but with a
	 * path expression as its predicate. For example, this can be used to express
	 * transitive sub-class relationships (?subClass rdfs:subClassOf*
	 * ?superClass).
	 */
	public static URI TRIPLE_PATH_CLASS;

	/**
	 * http://spinrdf.org/sp#Tuple Abstract base class for things that have
	 * subject and object.
	 */
	public static URI TUPLE_CLASS;

	/**
	 * http://spinrdf.org/sp#Let Deprecated: use sp:Bind instead. A variable
	 * assignment (LET (?<varName> := <expression>)). Not part of the SPARQL 1.0
	 * standard, but (for example) ARQ.
	 */
	@Deprecated
	public static URI LET_CLASS;

	/**
	 * http://spinrdf.org/sp#Bind A BIND element.
	 */
	public static URI BIND_CLASS;

	/**
	 * http://spinrdf.org/sp#Let Deprecated: use sp:Bind instead. A variable
	 * assignment (LET (?<varName> := <expression>)). Not part of the SPARQL 1.0
	 * standard, but (for example) ARQ.
	 */
	@Deprecated
	public static URI Let;

	/**
	 * http://spinrdf.org/sp#ElementList A list of Elements. This class is never
	 * instantiated directly as SPIN will use plain rdf:Lists to store element
	 * lists.
	 */
	public static URI ELEMENT_LIST_CLASS;

	/**
	 * http://spinrdf.org/sp#SubQuery A nested SELECT query inside of an element
	 * list. The query is stored in sp:query.
	 */
	public static URI SUB_QUERY_CLASS;

	/**
	 * http://spinrdf.org/sp#Delete Deprecated - use sp:Modify instead.
	 * Represents a DELETE FROM (part of SPARQL UPDATE language). The graph IRIs
	 * are stored in sp:graphIRI. The template patterns to delete are stored in
	 * sp:deletePattern. The WHERE clause is represented using sp:where.
	 */
	@Deprecated
	public static URI DELETE_CLASS;

	/**
	 * http://spinrdf.org/sp#Delete Deprecated - use sp:Modify instead.
	 * Represents a DELETE FROM (part of SPARQL UPDATE language). The graph IRIs
	 * are stored in sp:graphIRI. The template patterns to delete are stored in
	 * sp:deletePattern. The WHERE clause is represented using sp:where.
	 */
	@Deprecated
	public static URI Delete;

	/**
	 * http://spinrdf.org/sp#Min Represents MIN aggregations.
	 */
	public static URI MIN_CLASS;

	/**
	 * http://spinrdf.org/sp#Optional An optional element in a query.
	 */
	public static URI OPTIONAL_CLASS;

	/**
	 * http://spinrdf.org/sp#AltPath An alternative path with the union of
	 * sp:path1 and sp:path2.
	 */
	public static URI ALT_PATH_CLASS;

	/**
	 * http://spinrdf.org/sp#Count Counts the number of times a variable is used.
	 * The variable is stored in the variable property. This might be left blank
	 * to indicate COUNT(*).
	 */
	public static URI COUNT_CLASS;

	/**
	 * http://spinrdf.org/sp#ReversePath A path with reversed direction.
	 */
	public static URI REVERSE_PATH_CLASS;

	/**
	 * http://spinrdf.org/sp#Construct A CONSTRUCT-type query that can be used to
	 * construct new triples from template triples (head) that use variable
	 * bindings from the match patterns (body).
	 */
	public static URI CONSTRUCT_CLASS;

	/**
	 * http://spinrdf.org/sp#Query Abstract base class of the various types of
	 * supported queries. Common to all types of queries is that they can have a
	 * body ("WHERE clause").
	 */
	public static URI QUERY_CLASS;

	/**
	 * http://spinrdf.org/sp#Variable A variable mentioned in a Triple or
	 * expression. Variables are often blank nodes with the variable name stored
	 * in ts:name. Variables can also be supplied with a URI in which case the
	 * system will attempt to reuse the same variable instance across multiple
	 * query definitions.
	 */
	public static URI VARIABLE_CLASS;

	/**
	 * http://spinrdf.org/sp#Ask An ASK query that returns true if the condition
	 * in the body is met by at least one result set.
	 */
	public static URI ASK_CLASS;

	/**
	 * http://spinrdf.org/sp#ModPath A modified path such as rdfs:subClassOf*.
	 */
	public static URI MOD_PATH_CLASS;

	/**
	 * http://spinrdf.org/sp#Create An Update operation that creates a new empty
	 * graph with a name specified by sp:graphIRI. May have sp:silent set to
	 * true.
	 */
	public static URI CREATE_CLASS;

	/**
	 * http://spinrdf.org/sp#NamedGraph A named Graph element such as GRAPH <uri>
	 * {...}.
	 */
	public static URI NAMED_GRAPH_CLASS;

	/**
	 * http://spinrdf.org/sp#Command A shared superclass for sp:Query and
	 * sp:Update that can be used to specify that the range of property can be
	 * either one.
	 */
	public static URI COMMAND_CLASS;

	public static URI REVERSE_LINK_PATH_CLASS;

	/**
	 * http://spinrdf.org/sp#NotExists A NOT EXISTS element group (ARQ only).
	 */
	public static URI NOT_EXISTS_CLASS;

	/**
	 * http://spinrdf.org/sp#Drop An Update operation that removes a specified
	 * graph from the Graph Store. Must specify the graph using sp:graphIRI, or
	 * sp:default, sp:named or sp:all. May have the SILENT flag, encoded using
	 * sp:silent.
	 */
	public static URI DROP_CLASS;

	/**
	 * http://spinrdf.org/sp#InsertData An Update operation to insert specific
	 * triples. The graph triples are represented using sp:data, which points to
	 * an rdf:List of sp:Triples or sp:NamedGraphs.
	 */
	public static URI INSERT_DATA_CLASS;

	/**
	 * http://spinrdf.org/sp#DeleteWhere An Update operation where the triples
	 * matched by the WHERE clause (sp:where) will be the triples deleted.
	 */
	public static URI DELETE_WHERE_CLASS;

	/**
	 * http://spinrdf.org/sp#Service A SERVICE call that matches a nested
	 * sub-pattern against a SPARQL end point specified by a URI.
	 */
	public static URI SERVICE_CLASS;

	/**
	 * http://spinrdf.org/sp#Select A SELECT-type query that returns variable
	 * bindings as its result.
	 */
	public static URI SELECT_CLASS;

	/**
	 * http://spinrdf.org/sp#Filter A constraint element that evaluates a given
	 * expression to true or false.
	 */
	public static URI FILTER_CLASS;

	/**
	 * http://spinrdf.org/sp#Minus A MINUS element group.
	 */
	public static URI MINUS_CLASS;

	/**
	 * http://spinrdf.org/sp#Clear An Update operation that removes all triples
	 * from a specified graph. Must specify the graph using sp:graphIRI, or
	 * sp:default, sp:named or sp:all. May have the SILENT flag, encoded using
	 * sp:silent.
	 */
	public static URI CLEAR_CLASS;

	/**
	 * http://spinrdf.org/sp#Describe A DESCRIBE-type Query.
	 */
	public static URI DESCRIBE_CLASS;

	/**
	 * http://spinrdf.org/sp#SeqPath A sequence of multiple paths.
	 */
	public static URI SEQ_PATH_CLASS;

	/**
	 * http://spinrdf.org/sp#arg5 The fifth argument of a function call. Further
	 * arguments are not common in SPARQL, therefore no sp:arg6, etc are defined
	 * here. However, they can be created if needed.
	 */
	public static URI ARG5_PROPERTY;

	/**
	 * http://spinrdf.org/sp#arg Abstract superproperty for the enumerated arg1,
	 * arg2 etc.
	 */
	public static URI ARG_PROPERTY;

	/**
	 * http://spinrdf.org/sp#path1 The first child path of a property path. Used
	 * by sp:AltPath and sp:SeqPath.
	 */
	public static URI PATH1_PROPERTY;

	/**
	 * http://spinrdf.org/sp#systemProperty An abstract base proprerty that
	 * groups together the SP system properties. Users typically don't need to
	 * see them anyway.
	 */
	public static URI SYSTEM_PROPERTY;

	/**
	 * http://spinrdf.org/sp#arg1 The first argument of a function call.
	 */
	public static URI ARG1_PROPERTY;

	/**
	 * http://spinrdf.org/sp#default Used in DROP and CLEAR.
	 */
	public static URI DEFAULT_PROPERTY;

	/**
	 * http://spinrdf.org/sp#object An RDF Node or Variable describing the object
	 * of a triple.
	 */
	public static URI OBJECT_PROPERTY;

	/**
	 * http://spinrdf.org/sp#graphNameNode The name (URI or Variable) of a
	 * NamedGraph.
	 */
	public static URI GRAPH_NAME_NODE_PROPERTY;

	/**
	 * http://spinrdf.org/sp#varName The name of a Variable.
	 */
	public static URI VAR_NAME_PROPERTY;

	/**
	 * http://spinrdf.org/sp#named Used in DROP and CLEAR.
	 */
	public static URI NAMED_PROPERTY;

	/**
	 * http://spinrdf.org/sp#as Points to a Variable used in an AS statement such
	 * as COUNT aggregates.
	 */
	public static URI AS_PROPERTY;

	/**
	 * http://spinrdf.org/sp#distinct A marker property to indicate that a Select
	 * query is of type SELECT DISTINCT.
	 */
	public static URI DISTINCT_PROPERTY;

	/**
	 * http://spinrdf.org/sp#path2 The second child path of a property path. Used
	 * by sp:AltPath and sp:SeqPath.
	 */
	public static URI PATH2_PROPERTY;

	/**
	 * http://spinrdf.org/sp#orderBy Links a query with an ORDER BY clause where
	 * the values are rdf:List containing OrderByConditions or expressions. While
	 * the domain of this property is sp:Query, only Describe and Select queries
	 * can have values of it.
	 */
	public static URI ORDER_BY_PROPERTY;

	/**
	 * http://spinrdf.org/sp#variable The variable of a Bind element.
	 */
	public static URI VARIABLE_PROPERTY;

	/**
	 * http://spinrdf.org/sp#arg4 The forth argument of a function call.
	 */
	public static URI ARG4_PROPERTY;

	public static URI SILENT_PROPERTY;

	/**
	 * http://spinrdf.org/sp#having Points from a SELECT query to a list of
	 * HAVING expressions.
	 */
	public static URI HAVING_PROPERTY;

	/**
	 * http://spinrdf.org/sp#query Links a SubQuery resource with the nested
	 * Query.
	 */
	public static URI QUERY_PROPERTY;

	/**
	 * http://spinrdf.org/sp#groupBy Points from a Query to the list of GROUP BY
	 * expressions.
	 */
	public static URI GROUP_BY_PROPERTY;

	/**
	 * http://spinrdf.org/sp#graphIRI Points to graph names (IRIs) in various
	 * sp:Update operations.
	 */
	public static URI GRAPH_IRI_PROPERTY;

	/**
	 * http://spinrdf.org/sp#limit The LIMIT solution modifier of a Query.
	 */
	public static URI LIMIT_PROPERTY;

	public static URI USING_PROPERTY;

	/**
	 * http://spinrdf.org/sp#templates Points to a list of TripleTemplates that
	 * form the head of a Construct query.
	 */
	public static URI TEMPLATES_PROPERTY;

	/**
	 * http://spinrdf.org/sp#resultNodes Contains the result nodes (URI resources
	 * or Variables) of a Describe query.
	 */
	public static URI RESULT_NODES_PROPERTY;

	public static URI USING_NAMED_PROPERTY;

	/**
	 * http://spinrdf.org/sp#arg3 The third argument of a function call.
	 */
	public static URI ARG3_PROPERTY;

	/**
	 * http://spinrdf.org/sp#reduced A property with true to indicate that a
	 * Select query has a REDUCED flag.
	 */
	public static URI REDUCED_PROPERTY;

	/**
	 * http://spinrdf.org/sp#subPath The child path of a property path
	 * expression. This is used by ReversePath and ModPath.
	 */
	public static URI SUB_PATH_PROPERTY;

	/**
	 * http://spinrdf.org/sp#into The (optional) target of a LOAD Update
	 * operation.
	 */
	public static URI INTO_PROPERTY;

	public static URI WITH_PROPERTY;

	/**
	 * http://spinrdf.org/sp#serviceURI Used by sp:Service to specify the URI of
	 * the SPARQL end point to invoke. Must point to a URI node.
	 */
	public static URI SERVICE_URI_PROPERTY;

	/**
	 * http://spinrdf.org/sp#document The URI of the document to load using a
	 * LOAD Update operation.
	 */
	public static URI DOCUMENT_PROPERTY;

	/**
	 * http://spinrdf.org/sp#where The WHERE clause of a Query.
	 */
	public static URI WHERE_PROPERTY;

	/**
	 * http://spinrdf.org/sp#resultVariables An rdf:List of variables that are
	 * returned by a Select query.
	 */
	public static URI RESULT_VARIABLES_PROPERTY;

	/**
	 * http://spinrdf.org/sp#text Can be attached to sp:Queries to store a
	 * textual representation of the query. This can be useful for tools that do
	 * not have a complete SPIN Syntax parser available.
	 */
	public static URI TEXT_PROPERTY;

	/**
	 * http://spinrdf.org/sp#path Points from a TriplePath to its path.
	 */
	public static URI PATH_PROPERTY;

	public static URI MOD_MAX_PROPERTY;

	/**
	 * http://spinrdf.org/sp#predicate A resource or Variable describing the
	 * predicate of a triple.
	 */
	public static URI PREDICATE_PROPERTY;

	/**
	 * http://spinrdf.org/sp#elements Points to an ElementList, for example in an
	 * Optional element.
	 */
	public static URI ELEMENTS_PROPERTY;

	public static URI NODE_PROPERTY;

	/**
	 * http://spinrdf.org/sp#fromNamed Specifies a named RDF Dataset used by a
	 * Query (FROM NAMED syntax in SPARQL). Values of this property must be URI
	 * resources.
	 */
	public static URI FROM_NAMED_PROPERTY;

	/**
	 * http://spinrdf.org/sp#arg2 The second argument of a function call.
	 */
	public static URI ARG2_PROPERTY;

	/**
	 * http://spinrdf.org/sp#subject A resource or Variable describing the
	 * subject of a triple.
	 */
	public static URI SUBJECT_PROPERTY;

	/**
	 * http://spinrdf.org/sp#expression Points to an expression, for example in a
	 * Filter or Assignment.
	 */
	public static URI EXPRESSION_PROPERTY;

	/**
	 * http://spinrdf.org/sp#deletePattern Points to a list of sp:TripleTemplates
	 * and sp:NamedGraphs in a modify operation.
	 */
	public static URI DELETE_PATTERN_PROPERTY;

	/**
	 * http://spinrdf.org/sp#all Used in DROP and CLEAR.
	 */
	public static URI ALL_PROPERTY;

	/**
	 * http://spinrdf.org/sp#offset The OFFSET solution modifier of a Query.
	 */
	public static URI OFFSET_PROPERTY;

	/**
	 * http://spinrdf.org/sp#from Specifies an RDF Dataset used by a Query (FROM
	 * syntax in SPARQL). Values of this property must be URI resources.
	 */
	public static URI FROM_PROPERTY;

	public static URI MOD_MIN_PROPERTY;

	/**
	 * http://spinrdf.org/sp#insertPattern Points to a list of sp:TripleTemplates
	 * or sp:NamedGraphs in a modify command.
	 */
	public static URI INSERT_PATTERN_PROPERTY;

	public static final URI VALUES_CLASS;
	public static final URI BINDINGS_PROPERTY;
	public static final URI VAR_NAMES_PROPERTY;
	public static final URI UNDEF;

	public static final URI GROUP_CONCAT_CLASS;
	public static final URI SAMPLE_CLASS;

	// "The SPIN RDF Syntax provides standard URIs for the built-in functions and operators of the SPARQL language.
	// For example, sp:gt represents the > operator."
	public static final URI ADD;
	public static final URI SUB;
	public static final URI MUL;
	public static final URI DIVIDE;
	public static final URI EQ;
	public static final URI NE;
	public static final URI LT;
	public static final URI LE;
	public static final URI GE;
	public static final URI GT;
	public static final URI NOT;
	public static final URI EXISTS;
	public static final URI NOT_EXISTS;
	public static final URI IS_IRI;
	public static final URI IS_URI;
	public static final URI IS_BLANK;
	public static final URI IS_LITERAL;
	public static final URI IS_NUMERIC;
	public static final URI STR;
	public static final URI LANG;
	public static final URI DATATYPE;
	public static final URI IRI;
	public static final URI URI;
	public static final URI BNODE;
	public static final URI REGEX;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PATH_CLASS = factory.createURI(NAMESPACE, "Path");
		SYSTEM_CLASS = factory.createURI(NAMESPACE, "SystemClass");
		ASC_CLASS = factory.createURI(NAMESPACE, "Asc");
		ORDER_BY_CONDITION_CLASS = factory.createURI(NAMESPACE, "OrderByCondition");
		SUM_CLASS = factory.createURI(NAMESPACE, "Sum");
		AGGREGATION_CLASS = factory.createURI(NAMESPACE, "Aggregation");
		UNION_CLASS = factory.createURI(NAMESPACE, "Union");
		ELEMENT_GROUP_CLASS = factory.createURI(NAMESPACE, "ElementGroup");
		TRIPLE_PATTERN_CLASS = factory.createURI(NAMESPACE, "TriplePattern");
		ELEMENT_CLASS = factory.createURI(NAMESPACE, "Element");
		TRIPLE_CLASS = factory.createURI(NAMESPACE, "Triple");
		LOAD_CLASS = factory.createURI(NAMESPACE, "Load");
		UPDATE_CLASS = factory.createURI(NAMESPACE, "Update");
		DELETE_DATA_CLASS = factory.createURI(NAMESPACE, "DeleteData");
		DESC_CLASS = factory.createURI(NAMESPACE, "Desc");
		TRIPLE_TEMPLATE_CLASS = factory.createURI(NAMESPACE, "TripleTemplate");
		MAX_CLASS = factory.createURI(NAMESPACE, "Max");
		INSERT_CLASS = factory.createURI(NAMESPACE, "Insert");
		MODIFY_CLASS = factory.createURI(NAMESPACE, "Modify");
		AVG_CLASS = factory.createURI(NAMESPACE, "Avg");
		TRIPLE_PATH_CLASS = factory.createURI(NAMESPACE, "TriplePath");
		TUPLE_CLASS = factory.createURI(NAMESPACE, "Tuple");
		LET_CLASS = factory.createURI(NAMESPACE, "Let");
		BIND_CLASS = factory.createURI(NAMESPACE, "Bind");
		ELEMENT_LIST_CLASS = factory.createURI(NAMESPACE, "ElementList");
		SUB_QUERY_CLASS = factory.createURI(NAMESPACE, "SubQuery");
		DELETE_CLASS = factory.createURI(NAMESPACE, "Delete");
		MIN_CLASS = factory.createURI(NAMESPACE, "Min");
		OPTIONAL_CLASS = factory.createURI(NAMESPACE, "Optional");
		ALT_PATH_CLASS = factory.createURI(NAMESPACE, "AltPath");
		COUNT_CLASS = factory.createURI(NAMESPACE, "Count");
		REVERSE_PATH_CLASS = factory.createURI(NAMESPACE, "ReversePath");
		CONSTRUCT_CLASS = factory.createURI(NAMESPACE, "Construct");
		QUERY_CLASS = factory.createURI(NAMESPACE, "Query");
		VARIABLE_CLASS = factory.createURI(NAMESPACE, "Variable");
		ASK_CLASS = factory.createURI(NAMESPACE, "Ask");
		MOD_PATH_CLASS = factory.createURI(NAMESPACE, "ModPath");
		CREATE_CLASS = factory.createURI(NAMESPACE, "Create");
		NAMED_GRAPH_CLASS = factory.createURI(NAMESPACE, "NamedGraph");
		COMMAND_CLASS = factory.createURI(NAMESPACE, "Command");
		REVERSE_LINK_PATH_CLASS = factory.createURI(NAMESPACE, "ReverseLinkPath");
		NOT_EXISTS_CLASS = factory.createURI(NAMESPACE, "NotExists");
		DROP_CLASS = factory.createURI(NAMESPACE, "Drop");
		INSERT_DATA_CLASS = factory.createURI(NAMESPACE, "InsertData");
		DELETE_WHERE_CLASS = factory.createURI(NAMESPACE, "DeleteWhere");
		SERVICE_CLASS = factory.createURI(NAMESPACE, "Service");
		SELECT_CLASS = factory.createURI(NAMESPACE, "Select");
		FILTER_CLASS = factory.createURI(NAMESPACE, "Filter");
		MINUS_CLASS = factory.createURI(NAMESPACE, "Minus");
		CLEAR_CLASS = factory.createURI(NAMESPACE, "Clear");
		DESCRIBE_CLASS = factory.createURI(NAMESPACE, "Describe");
		SEQ_PATH_CLASS = factory.createURI(NAMESPACE, "SeqPath");
		ARG5_PROPERTY = factory.createURI(NAMESPACE, "arg5");
		ARG_PROPERTY = factory.createURI(NAMESPACE, "arg");
		PATH1_PROPERTY = factory.createURI(NAMESPACE, "path1");
		SYSTEM_PROPERTY = factory.createURI(NAMESPACE, "systemProperty");
		ARG1_PROPERTY = factory.createURI(NAMESPACE, "arg1");
		DEFAULT_PROPERTY = factory.createURI(NAMESPACE, "default");
		OBJECT_PROPERTY = factory.createURI(NAMESPACE, "object");
		GRAPH_NAME_NODE_PROPERTY = factory.createURI(NAMESPACE, "graphNameNode");
		VAR_NAME_PROPERTY = factory.createURI(NAMESPACE, "varName");
		NAMED_PROPERTY = factory.createURI(NAMESPACE, "named");
		AS_PROPERTY = factory.createURI(NAMESPACE, "as");
		DISTINCT_PROPERTY = factory.createURI(NAMESPACE, "distinct");
		PATH2_PROPERTY = factory.createURI(NAMESPACE, "path2");
		ORDER_BY_PROPERTY = factory.createURI(NAMESPACE, "orderBy");
		VARIABLE_PROPERTY = factory.createURI(NAMESPACE, "variable");
		ARG4_PROPERTY = factory.createURI(NAMESPACE, "arg4");
		SILENT_PROPERTY = factory.createURI(NAMESPACE, "silent");
		HAVING_PROPERTY = factory.createURI(NAMESPACE, "having");
		QUERY_PROPERTY = factory.createURI(NAMESPACE, "query");
		GROUP_BY_PROPERTY = factory.createURI(NAMESPACE, "groupBy");
		GRAPH_IRI_PROPERTY = factory.createURI(NAMESPACE, "graphIRI");
		LIMIT_PROPERTY = factory.createURI(NAMESPACE, "limit");
		USING_PROPERTY = factory.createURI(NAMESPACE, "using");
		TEMPLATES_PROPERTY = factory.createURI(NAMESPACE, "templates");
		RESULT_NODES_PROPERTY = factory.createURI(NAMESPACE, "resultNodes");
		USING_NAMED_PROPERTY = factory.createURI(NAMESPACE, "usingNamed");
		ARG3_PROPERTY = factory.createURI(NAMESPACE, "arg3");
		REDUCED_PROPERTY = factory.createURI(NAMESPACE, "reduced");
		SUB_PATH_PROPERTY = factory.createURI(NAMESPACE, "subPath");
		INTO_PROPERTY = factory.createURI(NAMESPACE, "into");
		WITH_PROPERTY = factory.createURI(NAMESPACE, "with");
		SERVICE_URI_PROPERTY = factory.createURI(NAMESPACE, "serviceURI");
		DOCUMENT_PROPERTY = factory.createURI(NAMESPACE, "document");
		WHERE_PROPERTY = factory.createURI(NAMESPACE, "where");
		RESULT_VARIABLES_PROPERTY = factory.createURI(NAMESPACE, "resultVariables");
		TEXT_PROPERTY = factory.createURI(NAMESPACE, "text");
		PATH_PROPERTY = factory.createURI(NAMESPACE, "path");
		MOD_MAX_PROPERTY = factory.createURI(NAMESPACE, "modMax");
		PREDICATE_PROPERTY = factory.createURI(NAMESPACE, "predicate");
		ELEMENTS_PROPERTY = factory.createURI(NAMESPACE, "elements");
		NODE_PROPERTY = factory.createURI(NAMESPACE, "node");
		FROM_NAMED_PROPERTY = factory.createURI(NAMESPACE, "fromNamed");
		ARG2_PROPERTY = factory.createURI(NAMESPACE, "arg2");
		SUBJECT_PROPERTY = factory.createURI(NAMESPACE, "subject");
		EXPRESSION_PROPERTY = factory.createURI(NAMESPACE, "expression");
		DELETE_PATTERN_PROPERTY = factory.createURI(NAMESPACE, "deletePattern");
		ALL_PROPERTY = factory.createURI(NAMESPACE, "all");
		OFFSET_PROPERTY = factory.createURI(NAMESPACE, "offset");
		FROM_PROPERTY = factory.createURI(NAMESPACE, "from");
		MOD_MIN_PROPERTY = factory.createURI(NAMESPACE, "modMin");
		INSERT_PATTERN_PROPERTY = factory.createURI(NAMESPACE, "insertPattern");

		VALUES_CLASS = factory.createURI(NAMESPACE, "Values");
		BINDINGS_PROPERTY = factory.createURI(NAMESPACE, "bindings");
		VAR_NAMES_PROPERTY = factory.createURI(NAMESPACE, "varNames");
		UNDEF = factory.createURI(NAMESPACE, "undef");

		GROUP_CONCAT_CLASS = factory.createURI(NAMESPACE, "GroupConcat");
		SAMPLE_CLASS = factory.createURI(NAMESPACE, "Sample");

		ADD = factory.createURI(NAMESPACE, "add");
		SUB = factory.createURI(NAMESPACE, "sub");
		MUL = factory.createURI(NAMESPACE, "mul");
		DIVIDE = factory.createURI(NAMESPACE, "divide");
		EQ = factory.createURI(NAMESPACE, "eq");
		NE = factory.createURI(NAMESPACE, "ne");
		LT = factory.createURI(NAMESPACE, "lt");
		LE = factory.createURI(NAMESPACE, "le");
		GE = factory.createURI(NAMESPACE, "ge");
		GT = factory.createURI(NAMESPACE, "gt");
		NOT = factory.createURI(NAMESPACE, "not");

		EXISTS = factory.createURI(NAMESPACE, "exists");
		NOT_EXISTS = factory.createURI(NAMESPACE, "notExists");

		IS_IRI = factory.createURI(NAMESPACE, "isIRI");
		IS_URI = factory.createURI(NAMESPACE, "isURI");
		IS_BLANK = factory.createURI(NAMESPACE, "isBlank");
		IS_LITERAL = factory.createURI(NAMESPACE, "isLiteral");
		IS_NUMERIC = factory.createURI(NAMESPACE, "isNumeric");
		STR = factory.createURI(NAMESPACE, "str");
		LANG = factory.createURI(NAMESPACE, "lang");
		DATATYPE = factory.createURI(NAMESPACE, "datatype");
		IRI = factory.createURI(NAMESPACE, "iri");
		URI = factory.createURI(NAMESPACE, "uri");
		BNODE = factory.createURI(NAMESPACE, "bnode");
		REGEX = factory.createURI(NAMESPACE, "regex");
	}
}
