/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.aduna.collections.iterators.Iterators;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * @author Arjohn Kampman
 */
public class ModelUtil {

	/**
	 * Compares two models, defined by two statement collections, and returns
	 * <tt>true</tt> if they are equal. Models are equal if they contain the same
	 * set of statements. Blank node IDs are not relevant for model equality,
	 * they are mapped from one model to the other by using the attached
	 * properties.
	 */
	public static boolean equals(Iterable<? extends Statement> model1, Iterable<? extends Statement> model2) {
		// Filter duplicates
		Set<Statement> set1 = new LinkedHashSet<Statement>();
		Iterators.addAll(model1.iterator(), set1);

		Set<Statement> set2 = new LinkedHashSet<Statement>();
		Iterators.addAll(model2.iterator(), set2);

		return equals(set1, set2);
	}

	/**
	 * Compares two models, defined by two statement collections, and returns
	 * <tt>true</tt> if they are equal. Models are equal if they contain the same
	 * set of statements. Blank node IDs are not relevant for model equality,
	 * they are mapped from one model to the other by using the attached
	 * properties.
	 */
	public static boolean equals(Set<? extends Statement> model1, Set<? extends Statement> model2) {
		// Compare the number of statements in both sets
		if (model1.size() != model2.size()) {
			return false;
		}

		return isSubsetInternal(model1, model2);
	}

	/**
	 * Compares two models, defined by two statement collections, and returns
	 * <tt>true</tt> if the first model is a subset of the second model.
	 */
	public static boolean isSubset(Iterable<? extends Statement> model1, Iterable<? extends Statement> model2)
	{
		// Filter duplicates
		Set<Statement> set1 = new LinkedHashSet<Statement>();
		Iterators.addAll(model1.iterator(), set1);

		Set<Statement> set2 = new LinkedHashSet<Statement>();
		Iterators.addAll(model2.iterator(), set2);

		return isSubset(set1, set2);
	}

	/**
	 * Compares two models, defined by two statement collections, and returns
	 * <tt>true</tt> if the first model is a subset of the second model.
	 */
	public static boolean isSubset(Set<? extends Statement> model1, Set<? extends Statement> model2) {
		// Compare the number of statements in both sets
		if (model1.size() > model2.size()) {
			return false;
		}

		return isSubsetInternal(model1, model2);
	}

	private static boolean isSubsetInternal(Set<? extends Statement> model1, Set<? extends Statement> model2) {
		// try to create a full blank node mapping
		return matchModels(model1, model2);
	}

	private static boolean matchModels(Set<? extends Statement> model1, Set<? extends Statement> model2) {
		// Compare statements without blank nodes first, save the rest for later
		List<Statement> model1BNodes = new ArrayList<Statement>(model1.size());

		for (Statement st : model1) {
			if (st.getSubject() instanceof BNode || st.getObject() instanceof BNode) {
				model1BNodes.add(st);
			}
			else {
				if (!model2.contains(st)) {
					return false;
				}
			}
		}

		return matchModels(model1BNodes, model2, new HashMap<BNode, BNode>(), 0);
	}

	/**
	 * A recursive method for finding a complete mapping between blank nodes in
	 * model1 and blank nodes in model2. The algorithm does a depth-first search
	 * trying to establish a mapping for each blank node occurring in model1.
	 * 
	 * @param model1
	 * @param model2
	 * @param bNodeMapping
	 * @param idx
	 * @return true if a complete mapping has been found, false otherwise.
	 */
	private static boolean matchModels(List<? extends Statement> model1, Iterable<? extends Statement> model2,
			Map<BNode, BNode> bNodeMapping, int idx)
	{
		boolean result = false;

		if (idx < model1.size()) {
			Statement st1 = model1.get(idx);

			List<Statement> matchingStats = findMatchingStatements(st1, model2, bNodeMapping);

			for (Statement st2 : matchingStats) {
				// Map bNodes in st1 to bNodes in st2
				Map<BNode, BNode> newBNodeMapping = new HashMap<BNode, BNode>(bNodeMapping);

				if (st1.getSubject() instanceof BNode && st2.getSubject() instanceof BNode) {
					newBNodeMapping.put((BNode)st1.getSubject(), (BNode)st2.getSubject());
				}

				if (st1.getObject() instanceof BNode && st2.getObject() instanceof BNode) {
					newBNodeMapping.put((BNode)st1.getObject(), (BNode)st2.getObject());
				}

				// FIXME: this recursive implementation has a high risk of
				// triggering a stack overflow

				// Enter recursion
				result = matchModels(model1, model2, newBNodeMapping, idx + 1);

				if (result == true) {
					// models match, look no further
					break;
				}
			}
		}
		else {
			// All statements have been mapped successfully
			result = true;
		}

		return result;
	}

	private static List<Statement> findMatchingStatements(Statement st, Iterable<? extends Statement> model,
			Map<BNode, BNode> bNodeMapping)
	{
		List<Statement> result = new ArrayList<Statement>();

		for (Statement modelSt : model) {
			if (statementsMatch(st, modelSt, bNodeMapping)) {
				// All components possibly match
				result.add(modelSt);
			}
		}

		return result;
	}

	private static boolean statementsMatch(Statement st1, Statement st2, Map<BNode, BNode> bNodeMapping) {
		URI pred1 = st1.getPredicate();
		URI pred2 = st2.getPredicate();

		if (!pred1.equals(pred2)) {
			// predicates don't match
			return false;
		}

		Resource subj1 = st1.getSubject();
		Resource subj2 = st2.getSubject();

		if (subj1 instanceof BNode && subj2 instanceof BNode) {
			BNode mappedBNode = bNodeMapping.get(subj1);

			if (mappedBNode != null) {
				// bNode 'subj1' was already mapped to some other bNode
				if (!subj2.equals(mappedBNode)) {
					// 'subj1' and 'subj2' do not match
					return false;
				}
			}
			else {
				// 'subj1' was not yet mapped. we need to check if 'subj2' is a
				// possible mapping candidate
				if (bNodeMapping.containsValue(subj2)) {
					// 'subj2' is already mapped to some other value.
					return false;
				}
			}
		}
		else {
			// subjects are not (both) bNodes
			if (!subj1.equals(subj2)) {
				return false;
			}
		}

		Value obj1 = st1.getObject();
		Value obj2 = st2.getObject();

		if (obj1 instanceof BNode && obj2 instanceof BNode) {
			BNode mappedBNode = bNodeMapping.get(obj1);

			if (mappedBNode != null) {
				// bNode 'obj1' was already mapped to some other bNode
				if (!obj2.equals(mappedBNode)) {
					// 'obj1' and 'obj2' do not match
					return false;
				}
			}
			else {
				// 'obj1' was not yet mapped. we need to check if 'obj2' is a
				// possible mapping candidate
				if (bNodeMapping.containsValue(obj2)) {
					// 'obj2' is already mapped to some other value.
					return false;
				}
			}
		}
		else {
			// objects are not (both) bNodes
			if (!obj1.equals(obj2)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Gets the subject of the statement(s) with the specified predicate and
	 * object from the specified contexts in the supplied model. The combination
	 * of predicate, object and contexts must match at least one statement. In
	 * case more than one statement matches -- for example statements from
	 * multiple contexts -- all these statements should have the same subject. A
	 * {@link ModelUtilException} is thrown if these conditions are not met. See
	 * {@link Model#match(Resource, URI, Value, Resource[])} for a description of
	 * the parameter values.
	 * 
	 * @return The subject of the matched statement(s).
	 * @throws ModelUtilException
	 *         If the statements matched by the specified parameters do not have
	 *         exactly one unique subject.
	 */
	public static Resource getUniqueSubject(Model model, URI pred, Value obj, Resource... contexts)
		throws ModelUtilException
	{
		Set<Resource> subjects = model.filter(null, pred, obj, contexts).subjects();

		if (subjects.size() == 1) {
			return subjects.iterator().next();
		}
		else if (subjects.isEmpty()) {
			throw new ModelUtilException("Missing property: " + pred);
		}
		else {
			throw new ModelUtilException("Multiple " + pred + " properties found");
		}
	}

	/**
	 * Utility method that casts the return value of
	 * {@link #getUniqueSubject(Model, URI, Value, Resource[])} to a URI, or
	 * throws a ModelUtilException if that value is not a URI.
	 * 
	 * @return The subject of the matched statement(s).
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getUniqueSubject(Model, URI, Value, Resource[])} or if its
	 *         return value is not a URI.
	 */
	public static URI getUniqueSubjectURI(Model model, URI pred, Value obj, Resource... contexts)
		throws ModelUtilException
	{
		Resource subject = getUniqueSubject(model, pred, obj, contexts);

		if (subject instanceof URI) {
			return (URI)subject;
		}
		else {
			throw new ModelUtilException("Expected URI for subject " + subject);
		}
	}

	/**
	 * Gets the subject of the statement(s) with the specified predicate and
	 * object from the specified contexts in the supplied model. If the
	 * combination of predicate, object and contexts matches one or more
	 * statements, all these statements should have the same subject. A
	 * {@link RepositoryConfigException} is thrown if this is not the case. See
	 * {@link Model#match(Resource, URI, Value, Resource[])} for a description of
	 * the parameter values.
	 * 
	 * @return The subject of the matched statement(s), or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelUtilException
	 *         If the statements matched by the specified parameters have more
	 *         than one unique subject.
	 */
	public static Resource getOptionalSubject(Model model, URI pred, Value obj, Resource... contexts)
		throws ModelUtilException
	{
		Set<Resource> subjects = model.filter(null, pred, obj, contexts).subjects();

		if (subjects.isEmpty()) {
			return null;
		}
		else if (subjects.size() == 1) {
			return subjects.iterator().next();
		}
		else {
			throw new ModelUtilException("Multiple " + pred + " properties found");
		}
	}

	/**
	 * Utility method that casts the return value of
	 * {@link #getOptionalSubject(Model, URI, Value, Resource[])} to a URI, or
	 * throws a ModelUtilException if that value is not a URI.
	 * 
	 * @return The subject of the matched statement(s), or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getOptionalSubject(Model, URI, Value, Resource[])} or if
	 *         its return value is not a URI.
	 */
	public static URI getOptionalSubjectURI(Model model, URI pred, Value obj, Resource... contexts)
		throws ModelUtilException
	{
		Resource subject = getOptionalSubject(model, pred, obj, contexts);

		if (subject instanceof URI) {
			return (URI)subject;
		}
		else {
			throw new ModelUtilException("Expected URI for subject " + subject);
		}
	}

	/**
	 * Gets the object of the statement(s) with the specified subject and
	 * predicate from the specified contexts in the supplied model. The
	 * combination of subject, predicate and contexts must match at least one
	 * statement. In case more than one statement matches -- for example
	 * statements from multiple contexts -- all these statements should have the
	 * same object. A {@link ModelUtilException} is thrown if these conditions
	 * are not met. See {@link Model#match(Resource, URI, Value, Resource[])} for
	 * a description of the parameter values.
	 * 
	 * @return The object of the matched statement(s).
	 * @throws ModelUtilException
	 *         If the statements matched by the specified parameters do not have
	 *         exactly one unique object.
	 */
	public static Value getUniqueObject(Model model, Resource subj, URI pred, Resource... contexts)
		throws ModelUtilException
	{
		Set<Value> objects = model.filter(subj, pred, null, contexts).objects();

		if (objects.size() == 1) {
			return objects.iterator().next();
		}
		else if (objects.isEmpty()) {
			throw new ModelUtilException("Missing property: " + pred);
		}
		else {
			throw new ModelUtilException("Multiple " + pred + " properties found");
		}
	}

	/**
	 * Adds the specified statement and makes sure that no other statements are
	 * present in the Model with the same subject and predicate. When contexts
	 * are specified, the (subj, pred) pair will occur exactly once in each
	 * context, else the (subj, pred) pair will occur exactly once in the entire
	 * Model.
	 */
	public static void setUniqueObject(Model model, Resource subj, URI pred, Value obj, Resource... contexts) {
		model.remove(subj, pred, null, contexts);
		model.add(subj, pred, obj, contexts);
	}

	/**
	 * Utility method that casts the return value of
	 * {@link #getUniqueObject(Model, Resource, URI, Resource[])} to a Resource,
	 * or throws a ModelUtilException if that value is not a Resource.
	 * 
	 * @return The object of the matched statement(s).
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getUniqueObject(Model, Resource, URI, Resource[])} or if
	 *         its return value is not a Resource.
	 */
	public static Resource getUniqueObjectResource(Model model, Resource subj, URI pred)
		throws ModelUtilException
	{
		Value obj = getUniqueObject(model, subj, pred);

		if (obj instanceof Resource) {
			return (Resource)obj;
		}
		else {
			throw new ModelUtilException("Expected URI or blank node for property " + pred);
		}
	}

	/**
	 * Utility method that casts the return value of
	 * {@link #getUniqueObject(Model, Resource, URI, Resource[])} to a URI, or
	 * throws a ModelUtilException if that value is not a URI.
	 * 
	 * @return The object of the matched statement(s).
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getUniqueObject(Model, Resource, URI, Resource[])} or if
	 *         its return value is not a URI.
	 */
	public static URI getUniqueObjectURI(Model model, Resource subj, URI pred)
		throws ModelUtilException
	{
		Value obj = getUniqueObject(model, subj, pred);

		if (obj instanceof URI) {
			return (URI)obj;
		}
		else {
			throw new ModelUtilException("Expected URI for property " + pred);
		}
	}

	/**
	 * Utility method that casts the return value of
	 * {@link #getUniqueObject(Model, Resource, URI, Resource[])} to a Literal,
	 * or throws a ModelUtilException if that value is not a Literal.
	 * 
	 * @return The object of the matched statement(s).
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getUniqueObject(Model, Resource, URI, Resource[])} or if
	 *         its return value is not a Literal.
	 */
	public static Literal getUniqueObjectLiteral(Model model, Resource subj, URI pred)
		throws ModelUtilException
	{
		Value obj = getUniqueObject(model, subj, pred);

		if (obj instanceof Literal) {
			return (Literal)obj;
		}
		else {
			throw new ModelUtilException("Expected literal for property " + pred);
		}
	}

	/**
	 * Gets the object of the statement(s) with the specified subject and
	 * predicate from the specified contexts in the supplied model. If the
	 * combination of subject, predicate and contexts matches one or more
	 * statements, all these statements should have the same object. A
	 * {@link RepositoryConfigException} is thrown if this is not the case. See
	 * {@link Model#match(Resource, URI, Value, Resource[])} for a description of
	 * the parameter values.
	 * 
	 * @return The object of the matched statement(s), or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelUtilException
	 *         If the statements matched by the specified parameters have more
	 *         than one unique object.
	 */
	public static Value getOptionalObject(Model model, Resource subj, URI pred, Resource... contexts)
		throws ModelUtilException
	{
		return model.filter(subj, pred, null, contexts).value();
	}

	/**
	 * Utility method that casts the return value of
	 * {@link #getOptionalObject(Model, Resource, URI, Resource[])} to a
	 * Resource, or throws a ModelUtilException if that value is not a Resource.
	 * 
	 * @return The object of the matched statement(s), or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getOptionalObject(Model, Resource, URI, Resource[])} or if
	 *         its return value is not a Resource.
	 */
	public static Resource getOptionalObjectResource(Model model, Resource subj, URI pred)
		throws ModelUtilException
	{
		return model.filter(subj, pred, null).resource();
	}

	/**
	 * Utility method that casts the return value of
	 * {@link #getOptionalObject(Model, Resource, URI, Resource[])} to a URI, or
	 * throws a ModelUtilException if that value is not a URI.
	 * 
	 * @return The object of the matched statement(s), or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getOptionalObject(Model, Resource, URI, Resource[])} or if
	 *         its return value is not a URI.
	 */
	public static URI getOptionalObjectURI(Model model, Resource subj, URI pred)
		throws ModelUtilException
	{
		return model.filter(subj, pred, null).uri();
	}

	/**
	 * Utility method that casts the return value of
	 * {@link #getOptionalObject(Model, Resource, URI, Resource[])} to a Literal,
	 * or throws a ModelUtilException if that value is not a Literal.
	 * 
	 * @return The object of the matched statement(s), or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getOptionalObject(Model, Resource, URI, Resource[])} or if
	 *         its return value is not a Literal.
	 */
	public static Literal getOptionalObjectLiteral(Model model, Resource subj, URI pred)
		throws ModelUtilException
	{
		return model.filter(subj, pred, null).literal();
	}

	/**
	 * Utility method that returns the value of
	 * {@link #getOptionalObject(Model, Resource, URI, Resource[])}'s stringValue.
	 * 
	 * @return The stirngValue of the matched statement's object, or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelUtilException
	 *         If such an exception is thrown by
	 *         {@link #getOptionalObject(Model, Resource, URI, Resource[])}.
	 */
	public static String getOptionalObjectStringValue(Model model, Resource subj, URI pred)
		throws ModelUtilException
	{
		return model.filter(subj, pred, null).stringValue();
	}
}