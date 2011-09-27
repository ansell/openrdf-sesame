/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Service;

/**
 * Convenience class to create string representations for different query nodes,
 * e.g. for create a valid SPARQL query for the inner expression
 * 
 * @author Andreas Schwarte
 */
public class QueryStringUtil {

	
	/**
	 * Construct a select query string for the service expression, inserts bindings
	 * from the specified bindingset.
	 * 
	 * @param serviceExpr
	 * @param freeVars
	 * @param bindings
	 * @return
	 */
	public static String selectQueryString(Service service, Set<String> freeVars, BindingSet bindings) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(service.getComputedPrefixString());
		sb.append("SELECT");
		for (String var : freeVars)
			sb.append(" ?").append(var);
		sb.append(" WHERE { ");
		
		// insert bindings
		String serviceExpression = insertBindings(service.getServiceExpressionString(), service.getServiceVars(), bindings);
				
		sb.append(serviceExpression);
		sb.append(" }");
		return sb.toString();
	}
	
	
	/**
	 * Construct a ask query string for the service expression, inserts bindings
	 * from the specified bindingset.
	 * 
	 * @param service
	 * @param bindings
	 * @return
	 */
	public static String askQueryString(Service service, BindingSet bindings) {
		
		StringBuilder sb = new StringBuilder();
		sb.append(service.getComputedPrefixString());
		sb.append("ASK {");
	
		// insert bindings
		String serviceExpression = insertBindings(service.getServiceExpressionString(), service.getServiceVars(), bindings);
		
		sb.append(serviceExpression);
		sb.append(" }");
		return sb.toString();
	}
	
	/**
	 * Build the prefix string for the specified declarations
	 * 
	 * @param prefixDeclarations
	 * @return
	 */
	public static String getPrefixString(Map<String, String> prefixDeclarations) {
		StringBuilder sb = new StringBuilder();
		appendPrefixDeclarations(sb, prefixDeclarations);
		return sb.toString();
	}
	
	/**
	 * Append the declarations to the prefix
	 * 
	 * @param sb
	 * @param prefixDeclarations
	 */
	private static void appendPrefixDeclarations(StringBuilder sb, Map<String, String> prefixDeclarations) {
		if (prefixDeclarations==null)
			return;
		
		for (String prefix : prefixDeclarations.keySet()) {
			String uri = prefixDeclarations.get(prefix);
			sb.append("PREFIX ").append(prefix).append(":").append(" <").append(uri).append("> ");
		}
	}
	
	
	/**
	 * Insert bindings into the service expression, see {@link #insertBinding(String, Binding)}.
	 * 
	 * @param serviceExpression
	 * @param freeVars
	 * @param bindings
	 * @return
	 */
	private static String insertBindings(String serviceExpression, Set<String> serviceVars, BindingSet bindings) {
		for (String bindingName : bindings.getBindingNames()) {
			if (!(serviceVars.contains(bindingName)))
				continue;
			serviceExpression = insertBinding(serviceExpression, bindings.getBinding(bindingName));
		}
		return serviceExpression;
	}
	
	
	/**
	 * Insert a string representation of the binding's value to the service
	 * expression, at the place of the variable
	 * 
	 * 1. URI: <http://myUri>
	 * 2. Literal: "myLiteral"^^<dataType>
	 * 
	 * @param sb
	 * @param value
	 * @return
	 */
	private static String insertBinding(String serviceExpression, Binding b) {

		Value value = b.getValue();
		if (value instanceof URI)
			return insertURI(serviceExpression, b.getName(), (URI)value);
		if (value instanceof Literal)
			return insertLiteral(serviceExpression, b.getName(), (Literal)value);

		// XXX check for other types ? BNode ?
		throw new RuntimeException("Type not supported: " + value.getClass().getCanonicalName());
	}
	
	
	/**
	 * Replace occurrences of ?varName with the uri, i.e. <uri.stringValue>.
	 * 
	 * @param serviceExpression
	 * @param varName
	 * @param uri
	 * @return
	 */
	private static String insertURI(String serviceExpression, String varName, URI uri) {
		return serviceExpression.replace("?"+varName, "<"+uri.stringValue()+">");
	}

	
	/**
	 * Replace occurrences of ?varName with the literal
	 * 
	 * @param serviceExpression
	 * @param varName
	 * @param lit
	 * @return
	 */
	private static String insertLiteral(String serviceExpression, String varName, Literal lit) {
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		sb.append(lit.getLabel().replace("\"", "\\\""));
		sb.append('"');

		if (lit.getLanguage() != null) {
			sb.append('@');
			sb.append(lit.getLanguage());
		}

		if (lit.getDatatype() != null) {
			sb.append("^^<");
			sb.append(lit.getDatatype().stringValue());
			sb.append('>');
		}
		return serviceExpression.replace("?"+varName, sb.toString());
	}	
}
