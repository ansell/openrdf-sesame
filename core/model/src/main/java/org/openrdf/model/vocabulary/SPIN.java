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
public class SPIN {

	/**
	 * http://spinrdf.org/spin An RDF Schema that can be used to attach
	 * constraints and rules to RDFS classes, and to encapsulate reusable SPARQL
	 * queries into functions and templates.
	 */
	public static final String NAMESPACE = "http://spinrdf.org/spin#";

	public static final String PREFIX = "spin";

	/**
	 * http://spinrdf.org/spin#Function Metaclass for functions that can be used
	 * in SPARQL expressions (e.g. FILTER or BIND). The function themselves are
	 * classes that are instances of this metaclass. Function calls are instances
	 * of the function classes, with property values for the arguments.
	 */
	public static URI FUNCTION_CLASS;

	/**
	 * http://spinrdf.org/spin#Module An abstract building block of a SPARQL
	 * system. A Module can take Arguments as input and applies them on an input
	 * RDF Graph. The Arguments should be declared as spin:constraints.
	 */
	public static URI MODULE_CLASS;

	/**
	 * http://spinrdf.org/spin#body The body of a Function or Template. This
	 * points to a Query instance. For Functions, this is limited to either ASK
	 * or SELECT type queries. If the body is the ASK function then the return
	 * value is xsd:boolean. Otherwise, the SELECT query must have a single
	 * return variable. The first binding of this SELECT query will be returned
	 * as result of the function call.
	 */
	public static URI BODY_PROPERTY;

	/**
	 * http://spinrdf.org/spin#TableDataProvider An abstraction of objects that
	 * can produce tabular data. This serves as a base class of
	 * spin:SelectTemplate, because SELECT queries can produce tables with
	 * columns for each result variable. However, other types of
	 * TableDataProviders are conceivable by other frameworks, and this class may
	 * prove as a useful shared foundation. TableDataProviders can link to
	 * definitions of columns via spin:column, and these definitions can inform
	 * rendering engines.
	 */
	public static URI TABLE_DATA_PROVIDER_CLASS;

	public static URI CONSTRUCT_TEMPLATE_CLASS;

	/**
	 * http://spinrdf.org/spin#Template The metaclass of SPIN templates.
	 * Templates are classes that are instances of this class. A template
	 * represents a reusable SPARQL query or update request that can be
	 * parameterized with arguments. Templates can be instantiated in places
	 * where normally a SPARQL query or update request is used, in particular as
	 * spin:rules and spin:constraints.
	 */
	public static URI TEMPLATE_CLASS;

	/**
	 * http://spinrdf.org/spin#Rule Groups together the kinds of SPARQL commands
	 * that can appear as SPIN rules and constructors: CONSTRUCT, DELETE WHERE
	 * and DELETE/INSERT. This class is never to be instantiated directly.
	 */
	public static URI RULE_CLASS;

	/**
	 * http://spinrdf.org/spin#AskTemplate A SPIN template that wraps an ASK
	 * query.
	 */
	public static URI ASK_TEMPLATE_CLASS;

	/**
	 * http://spinrdf.org/spin#UpdateTemplate A SPIN template that has an UPDATE
	 * command as its body.
	 */
	public static URI UPDATE_TEMPLATE_CLASS;

	/**
	 * http://spinrdf.org/spin#RuleProperty The metaclass of spin:rule and its
	 * subproperties. spin:RuleProperties can have additional metadata attached
	 * to them.
	 */
	public static URI RULE_PROPERTY_CLASS;

	/**
	 * http://spinrdf.org/spin#ConstraintViolation An object that can be created
	 * by spin:constraints to provide information about a constraint violation.
	 */
	public static URI CONSTRAINT_VIOLATION_CLASS;

	/**
	 * http://spinrdf.org/spin#Modules An "artificial" parent class for all
	 * Functions and Templates.
	 */
	public static URI MODULES_CLASS;

	/**
	 * http://spinrdf.org/spin#SelectTemplate A SPIN template that wraps a SELECT
	 * query.
	 */
	public static URI SELECT_TEMPLATE_CLASS;

	/**
	 * http://spinrdf.org/spin#Column Provides metadata about a column in the
	 * result set of a (SPARQL) query, for example of the body queries of SPIN
	 * templates. Columns can define human-readable labels that serve as column
	 * titles, using rdfs:label.
	 */
	public static URI COLUMN_CLASS;

	/**
	 * http://spinrdf.org/spin#LibraryOntology A marker class that can be
	 * attached to base URIs (ontologies) to instruct SPIN engines that this
	 * ontology only contains a library of SPIN declarations. Library Ontologies
	 * should be ignored by SPIN inference engines even if they have been
	 * imported by a domain model. For example, a SPIN version of OWL RL may
	 * contain all the OWL RL axioms, attached to owl:Thing, but nothing else.
	 * However, when executed, these axioms should not be executed over
	 * themselves, because we don't want the system to reason about the SPIN
	 * triples to speed up things.
	 */
	public static URI LIBRARY_ONTOLOGY_CLASS;

	public static URI MAGIC_PROPERTY_CLASS;

	/**
	 * http://spinrdf.org/spin#update Can be used to point from any resource to
	 * an Update.
	 */
	public static URI UPDATE_PROPERTY;

	/**
	 * http://spinrdf.org/spin#command Can be used to link a resource with a
	 * SPARQL query or update request (sp:Command).
	 */
	public static URI COMMAND_PROPERTY;

	/**
	 * http://spinrdf.org/spin#returnType The return type of a Function, e.g.
	 * xsd:string.
	 */
	public static URI RETURN_TYPE_PROPERTY;

	/**
	 * http://spinrdf.org/spin#systemProperty An "abstract" base property that
	 * groups together those system properties that the user will hardly ever
	 * need to see in property trees. This property may be dropped in future
	 * versions of this ontology - right now it's mainly here for convenience.
	 */
	public static URI SYSTEM_PROPERTY_PROPERTY;

	/**
	 * http://spinrdf.org/spin#column Can link a TableDataProvider (esp.
	 * SelectTemplate) with one or more columns that provide metadata for
	 * rendering purposes. Columns can be sorted by their spin:columnIndex (which
	 * must align with the ordering of variables in the SELECT query starting
	 * with 0). Not all result variables of the underlying query need to have a
	 * matching spin:Column.
	 */
	public static URI COLUMN_PROPERTY;

	/**
	 * http://spinrdf.org/spin#symbol The symbol of a function, e.g. "=" for the
	 * eq function.
	 */
	public static URI SYMBOL_PROPERTY;

	/**
	 * http://spinrdf.org/spin#violationRoot The root resource of the violation
	 * (often ?this in the constraint body).
	 */
	public static URI VIOLATION_ROOT_PROPERTY;

	/**
	 * http://spinrdf.org/spin#columnType The datatype or resource type of a
	 * spin:Column. For example this is useful as metadata to inform a rendering
	 * engine that numeric columns (e.g. xsd:float) need to be right-aligned.
	 */
	public static URI COLUMN_TYPE_PROPERTY;

	/**
	 * http://spinrdf.org/spin#nextRuleProperty Can be used to link two
	 * sub-properties of spin:rule (or spin:rule itself) to instruct the SPIN
	 * engine to execute one set of rules before another one. The values of the
	 * subject property will be executed before those of the object property.
	 */
	public static URI NEXT_RULE_PROPERTY_PROPERTY;

	/**
	 * http://spinrdf.org/spin#private Can be set to true to indicate that a SPIN
	 * function is only meant to be used as a helper of other functions, but not
	 * directly. Among others, this allows user interfaces to filter out private
	 * functions. Furthermore, it tells potential users of this function that
	 * they should avoid using this function, as it may not be stable.
	 */
	public static URI PRIVATE_PROPERTY;

	/**
	 * http://spinrdf.org/spin#labelTemplate A template string for displaying
	 * instantiations of a module in human-readable form. The template may
	 * contain the argument variable names in curly braces to support
	 * substitution. For example, "The number of values of the {?arg1} property."
	 */
	public static URI LABEL_TEMPLATE_PROPERTY;

	/**
	 * http://spinrdf.org/spin#violationPath An optional attribute of
	 * ConstraintViolations to provide a path expression from the root resource
	 * to the value that is invalid. If this is a URI then the path represents
	 * the predicate of a subject/predicate combination. Otherwise it should be a
	 * blank node of type sp:Path.
	 */
	public static URI VIOLATION_PATH_PROPERTY;

	/**
	 * http://spinrdf.org/spin#constructor Can be used to attach a "constructor"
	 * to a class. A constructor is a SPARQL CONSTRUCT query or INSERT/DELETE
	 * Update operation that can add initial values to the current instance. At
	 * execution time, the variable ?this is bound to the current instance. Tools
	 * can call constructors of a class and its superclasses when an instance of
	 * a class has been created. Constructors will also be used to initialize
	 * resources that have received a new rdf:type triple as a result of
	 * spin:rules firing.
	 */
	public static URI CONSTRUCTOR_PROPERTY;

	/**
	 * http://spinrdf.org/spin#abstract Can be set to true to indicate that this
	 * module shall not be instantiated. Abstract modules are only there to
	 * organize other modules into hierarchies.
	 */
	public static URI ABSTRACT_PROPERTY;

	/**
	 * http://spinrdf.org/spin#constraint Links a class with constraints on its
	 * instances. The values of this property are "axioms" expressed as CONSTRUCT
	 * or ASK queries where the variable ?this refers to the instances of the
	 * surrounding class. ASK queries must evaluate to false for each member of
	 * this class - returning true means that the instance ?this violates the
	 * constraint. CONSTRUCT queries must create instances of
	 * spin:ConstraintViolation to provide details on the reason for the
	 * violation.
	 */
	public static URI CONSTRAINT_PROPERTY;

	/**
	 * http://spinrdf.org/spin#query Can be used to point from any resource to a
	 * Query.
	 */
	public static URI QUERY_PROPERTY;

	/**
	 * http://spinrdf.org/spin#fix Can be used to link a ConstraintViolation with
	 * one or more UPDATE Templates that would help fix the violation.
	 */
	public static URI FIX_PROPERTY;

	/**
	 * http://spinrdf.org/spin#columnWidth The preferred width of the associated
	 * Column, for display purposes. Values in pixels (rendering engines may
	 * multiply the values depending on resolution).
	 */
	public static URI COLUMN_WIDTH_PROPERTY;

	/**
	 * http://spinrdf.org/spin#violationSource Can be used to link a
	 * spin:ConstraintViolation with the query or template call that caused it.
	 * This property is typically filled in automatically by the constraint
	 * checking engine and does not need to be set manually. However, it can be
	 * useful to learn more about the origin of a violation.
	 */
	public static URI VIOLATION_SOURCE_PROPERTY;

	/**
	 * http://spinrdf.org/spin#columnIndex The index of a column (from left to
	 * right) starting at 0.
	 */
	public static URI COLUMN_INDEX_PROPERTY;

	/**
	 * http://spinrdf.org/spin#thisUnbound Can be set to true for SPIN rules and
	 * constraints that do not require pre-binding the variable ?this with all
	 * members of the associated class. This flag should only be set to true if
	 * the WHERE clause is sufficiently strong to only bind instances of the
	 * associated class, or its subclasses. In those cases, the engine can
	 * greatly improve performance of query execution, because it does not need
	 * to add clauses to narrow down the WHERE clause.
	 */
	public static URI THIS_UNBOUND_PROPERTY;

	/**
	 * http://spinrdf.org/spin#rulePropertyMaxIterationCount Can be attached to
	 * spin:rule (or subclasses thereof) to instruct a SPIN rules engine that it
	 * shall only execute the rules max times. If no value is specified, then the
	 * rules will be executed with no specific limit.
	 */
	public static URI RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY;

	/**
	 * http://spinrdf.org/spin#imports Can be used to link an RDF graph (usually
	 * the instance of owl:Ontology) with a SPIN library to define constraints.
	 * SPIN-aware tools should include the definitions from those libraries for
	 * constraint checking. Using such libraries is a simpler alternative than
	 * explicitly importing them using owl:imports, because it does not force all
	 * the SPIN triples into the RDF model.
	 */
	public static URI IMPORTS_PROPERTY;

	/**
	 * http://spinrdf.org/spin#ConstructTemplates Suggested abstract base class
	 * for all ConstructTemplates.
	 */
	public static URI CONSTRUCT_TEMPLATES_CLASS;

	/**
	 * http://spinrdf.org/spin#Templates Suggested abstract base class for all
	 * Templates.
	 */
	public static URI TEMPLATES_CLASS;

	/**
	 * http://spinrdf.org/spin#eval Evaluates a given SPIN expression or SELECT
	 * or ASK query, and returns its result. The first argument must be the
	 * expression in SPIN RDF syntax. All other arguments must come in pairs:
	 * first a property name, and then a value. These name/value pairs will be
	 * pre-bound variables for the execution of the expression.
	 */
	public static URI EVAL_CLASS;

	/**
	 * http://spinrdf.org/spin#Functions An abstract base class for all defined
	 * functions. This class mainly serves as a shared root so that the various
	 * instances of the Function metaclass are grouped together.
	 */
	public static URI FUNCTIONS_CLASS;

	/**
	 * http://spinrdf.org/spin#AskTemplates Suggested abstract base class for all
	 * AskTemplates.
	 */
	public static URI ASK_TEMPLATES_CLASS;

	/**
	 * http://spinrdf.org/spin#SelectTemplates Suggested abstract base class for
	 * all SelectTemplates.
	 */
	public static URI SELECT_TEMPLATES_CLASS;

	/**
	 * http://spinrdf.org/spin#MagicProperties An abstract superclass that can be
	 * used to group all spin:MagicProperty instances under a single parent
	 * class.
	 */
	public static URI MAGIC_PROPERTIES_CLASS;

	/**
	 * http://spinrdf.org/spin#_this A system variable representing the current
	 * context instance in a rule or constraint.
	 */
	public static URI THIS_CONTEXT_INSTANCE;

	/**
	 * http://spinrdf.org/spin#UpdateTemplates Suggested abstract base class for
	 * all UpdateTemplates.
	 */
	public static URI UPDATE_TEMPLATES_CLASS;

	/**
	 * http://spinrdf.org/spin#rule An inferencing rule attached to a class.
	 * Rules are expressed as CONSTRUCT queries or INSERT/DELETE operations where
	 * the variable ?this will be bound to the current instance of the class.
	 * These inferences can be used to derive new values from existing values at
	 * the instance.
	 */
	public static URI RULE_PROPERTY;

	public static final URI ARG1_INSTANCE;
	public static final URI ARG2_INSTANCE;
	public static final URI ARG3_INSTANCE;
	public static final URI ARG4_INSTANCE;
	public static final URI ARG5_INSTANCE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		FUNCTION_CLASS = factory.createURI(NAMESPACE, "Function");
		MODULE_CLASS = factory.createURI(NAMESPACE, "Module");
		BODY_PROPERTY = factory.createURI(NAMESPACE, "body");
		TABLE_DATA_PROVIDER_CLASS = factory.createURI(NAMESPACE, "TableDataProvider");
		CONSTRUCT_TEMPLATE_CLASS = factory.createURI(NAMESPACE, "ConstructTemplate");
		TEMPLATE_CLASS = factory.createURI(NAMESPACE, "Template");
		RULE_CLASS = factory.createURI(NAMESPACE, "Rule");
		ASK_TEMPLATE_CLASS = factory.createURI(NAMESPACE, "AskTemplate");
		UPDATE_TEMPLATE_CLASS = factory.createURI(NAMESPACE, "UpdateTemplate");
		RULE_PROPERTY_CLASS = factory.createURI(NAMESPACE, "RuleProperty");
		CONSTRAINT_VIOLATION_CLASS = factory.createURI(NAMESPACE, "ConstraintViolation");
		MODULES_CLASS = factory.createURI(NAMESPACE, "Modules");
		SELECT_TEMPLATE_CLASS = factory.createURI(NAMESPACE, "SelectTemplate");
		COLUMN_CLASS = factory.createURI(NAMESPACE, "Column");
		LIBRARY_ONTOLOGY_CLASS = factory.createURI(NAMESPACE, "LibraryOntology");
		MAGIC_PROPERTY_CLASS = factory.createURI(NAMESPACE, "MagicProperty");
		UPDATE_PROPERTY = factory.createURI(NAMESPACE, "update");
		COMMAND_PROPERTY = factory.createURI(NAMESPACE, "command");
		RETURN_TYPE_PROPERTY = factory.createURI(NAMESPACE, "returnType");
		SYSTEM_PROPERTY_PROPERTY = factory.createURI(NAMESPACE, "systemProperty");
		COLUMN_PROPERTY = factory.createURI(NAMESPACE, "column");
		SYMBOL_PROPERTY = factory.createURI(NAMESPACE, "symbol");
		VIOLATION_ROOT_PROPERTY = factory.createURI(NAMESPACE, "violationRoot");
		COLUMN_TYPE_PROPERTY = factory.createURI(NAMESPACE, "columnType");
		NEXT_RULE_PROPERTY_PROPERTY = factory.createURI(NAMESPACE, "nextRuleProperty");
		PRIVATE_PROPERTY = factory.createURI(NAMESPACE, "private");
		LABEL_TEMPLATE_PROPERTY = factory.createURI(NAMESPACE, "labelTemplate");
		VIOLATION_PATH_PROPERTY = factory.createURI(NAMESPACE, "violationPath");
		CONSTRUCTOR_PROPERTY = factory.createURI(NAMESPACE, "constructor");
		ABSTRACT_PROPERTY = factory.createURI(NAMESPACE, "abstract");
		CONSTRAINT_PROPERTY = factory.createURI(NAMESPACE, "constraint");
		QUERY_PROPERTY = factory.createURI(NAMESPACE, "query");
		FIX_PROPERTY = factory.createURI(NAMESPACE, "fix");
		COLUMN_WIDTH_PROPERTY = factory.createURI(NAMESPACE, "columnWidth");
		VIOLATION_SOURCE_PROPERTY = factory.createURI(NAMESPACE, "violationSource");
		COLUMN_INDEX_PROPERTY = factory.createURI(NAMESPACE, "columnIndex");
		THIS_UNBOUND_PROPERTY = factory.createURI(NAMESPACE, "thisUnbound");
		RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY = factory.createURI(NAMESPACE,
				"rulePropertyMaxIterationCount");
		IMPORTS_PROPERTY = factory.createURI(NAMESPACE, "imports");
		CONSTRUCT_TEMPLATES_CLASS = factory.createURI(NAMESPACE, "ConstructTemplates");
		TEMPLATES_CLASS = factory.createURI(NAMESPACE, "Templates");
		EVAL_CLASS = factory.createURI(NAMESPACE, "eval");
		FUNCTIONS_CLASS = factory.createURI(NAMESPACE, "Functions");
		ASK_TEMPLATES_CLASS = factory.createURI(NAMESPACE, "AskTemplates");
		SELECT_TEMPLATES_CLASS = factory.createURI(NAMESPACE, "SelectTemplates");
		MAGIC_PROPERTIES_CLASS = factory.createURI(NAMESPACE, "MagicProperties");
		THIS_CONTEXT_INSTANCE = factory.createURI(NAMESPACE, "_this");
		UPDATE_TEMPLATES_CLASS = factory.createURI(NAMESPACE, "UpdateTemplates");
		RULE_PROPERTY = factory.createURI(NAMESPACE, "rule");

		ARG1_INSTANCE = factory.createURI(NAMESPACE, "_arg1");
		ARG2_INSTANCE = factory.createURI(NAMESPACE, "_arg2");
		ARG3_INSTANCE = factory.createURI(NAMESPACE, "_arg3");
		ARG4_INSTANCE = factory.createURI(NAMESPACE, "_arg4");
		ARG5_INSTANCE = factory.createURI(NAMESPACE, "_arg5");
	}
}
