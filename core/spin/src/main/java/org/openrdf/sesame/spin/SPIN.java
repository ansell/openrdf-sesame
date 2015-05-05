package org.openrdf.sesame.spin;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class SPIN {
	public static final String NAMESPACE = "http:spinrdf.org/spin#";

	/**
	 * Recommended prefix for the SPIN namespace: "spin"
	 */
	public static final String PREFIX = "spin";

	public final static URI MODULE_CLASS;

	public final static URI FUNCTION_CLASS;

	public final static URI BODY_PROPERTY;

	public final static URI CONSTRUCT_TEMPLATE_CLASS;

	public final static URI TEMPLATE_CLASS;

	public final static URI RULE_CLASS;

	public final static URI ASK_TEMPLATE_CLASS;

	public final static URI RULE_PROPERTY_CLASS;

	public final static URI UPDATE_TEMPLATE_CLASS;

	public final static URI MODULES_CLASS;

	public final static URI CONSTRAINT_VIOLATION_CLASS;

	public final static URI SELECT_TEMPLATE_CLASS;

	public final static URI LIBRARY_ONTOLOGY_CLASS;

	public final static URI MAGIC_PROPERTY_CLASS;

	public final static URI UPDATE_PROPERTY;

	public final static URI COMMAND_PROPERTY;

	public final static URI RETURN_TYPE_PROPERTY;

	public final static URI SYSTEM_PROPERTY_PROPERTY;

	public final static URI VIOLATION_ROOT_PROPERTY;

	public final static URI SYMBOL_PROPERTY;

	public final static URI PRIVATE_PROPERTY;

	public final static URI NEXT_RULE_PROPERTY_PROPERTY;

	public final static URI LABEL_TEMPLATE_PROPERTY;

	public final static URI CONSTRUCTOR_PROPERTY;

	public final static URI VIOLATION_PATH_PROPERTY;

	public final static URI ABSTRACT_PROPERTY;

	public final static URI CONSTRAINT_PROPERTY;

	public final static URI QUERY_PROPERTY;

	public final static URI FIX_PROPERTY;

	public final static URI VIOLATION_SOURCE_PROPERTY;

	public final static URI THIS_UNBOUND_PROPERTY;

	public final static URI RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		FUNCTION_CLASS = factory.createURI(SPIN.NAMESPACE, "Function");
		MODULE_CLASS = factory.createURI(SPIN.NAMESPACE, "Module");
		BODY_PROPERTY = factory.createURI(SPIN.NAMESPACE, "body");
		CONSTRUCT_TEMPLATE_CLASS = factory.createURI(SPIN.NAMESPACE,
				"ConstructTemplate");
		TEMPLATE_CLASS = factory.createURI(SPIN.NAMESPACE, "Template");
		RULE_CLASS = factory.createURI(SPIN.NAMESPACE, "Rule");
		ASK_TEMPLATE_CLASS = factory.createURI(SPIN.NAMESPACE, "AskTemplate");
		RULE_PROPERTY_CLASS = factory.createURI(SPIN.NAMESPACE, "RuleProperty");
		UPDATE_TEMPLATE_CLASS = factory.createURI(SPIN.NAMESPACE,
				"UpdateTemplate");
		MODULES_CLASS = factory.createURI(SPIN.NAMESPACE, "Modules");
		CONSTRAINT_VIOLATION_CLASS = factory.createURI(SPIN.NAMESPACE,
				"ConstraintViolation");
		SELECT_TEMPLATE_CLASS = factory.createURI(SPIN.NAMESPACE,
				"SelectTemplate");
		LIBRARY_ONTOLOGY_CLASS = factory.createURI(SPIN.NAMESPACE,
				"LibraryOntology");
		MAGIC_PROPERTY_CLASS = factory.createURI(SPIN.NAMESPACE,
				"MagicProperty");
		UPDATE_PROPERTY = factory.createURI(SPIN.NAMESPACE, "update");
		COMMAND_PROPERTY = factory.createURI(SPIN.NAMESPACE, "command");
		RETURN_TYPE_PROPERTY = factory.createURI(SPIN.NAMESPACE, "returnType");
		SYSTEM_PROPERTY_PROPERTY = factory.createURI(SPIN.NAMESPACE,
				"systemProperty");
		SYMBOL_PROPERTY = factory.createURI(SPIN.NAMESPACE, "symbol");
		VIOLATION_ROOT_PROPERTY = factory.createURI(SPIN.NAMESPACE,
				"violationRoot");
		PRIVATE_PROPERTY = factory.createURI(SPIN.NAMESPACE, "private");
		NEXT_RULE_PROPERTY_PROPERTY = factory.createURI(SPIN.NAMESPACE,
				"nextRuleProperty");
		LABEL_TEMPLATE_PROPERTY = factory.createURI(SPIN.NAMESPACE,
				"labelTemplate");
		CONSTRUCTOR_PROPERTY = factory.createURI(SPIN.NAMESPACE, "constructor");
		VIOLATION_PATH_PROPERTY = factory.createURI(SPIN.NAMESPACE,
				"violationPath");
		ABSTRACT_PROPERTY = factory.createURI(SPIN.NAMESPACE, "abstract");
		CONSTRAINT_PROPERTY = factory.createURI(SPIN.NAMESPACE, "constraint");
		QUERY_PROPERTY = factory.createURI(SPIN.NAMESPACE, "query");
		FIX_PROPERTY = factory.createURI(SPIN.NAMESPACE, "fix");
		VIOLATION_SOURCE_PROPERTY = factory.createURI(SPIN.NAMESPACE,
				"violationSource");
		THIS_UNBOUND_PROPERTY = factory
				.createURI(SPIN.NAMESPACE, "thisUnbound");
		RULE_PROPERTY_MAX_ITERATION_COUNT_PROPERTY = factory.createURI(
				SPIN.NAMESPACE, "rulePropertyMaxIterationCount");
	}
}
