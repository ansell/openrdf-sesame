/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import info.aduna.iteration.Iterations;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * Utility methods for comparing sets of statements (graphs) with each other.
 * The supplied comparison operations map bnodes in the two supplied models on
 * to each other and thus define a graph isomorphism.
 * 
 * @author jeen
 * @author Arjohn Kampman
 */
public class RepositoryUtil {

	/**
	 * Compares the models in the default contexts of the two supplied
	 * repositories and returns true if they are equal. Models are equal if they
	 * contain the same set of statements. bNodes IDs are not relevant for model
	 * equality, they are mapped from one model to the other by using the
	 * attached properties. Note that the method pulls the entire default context
	 * of both repositories into main memory. Use with caution.
	 */
	public static boolean modelsEqual(Repository rep1, Repository rep2)
		throws RepositoryException
	{
		// Fetch statements from rep1 and rep2
		Collection<Statement> model1 = new HashSet<Statement>();
		Collection<Statement> model2 = new HashSet<Statement>();

		RepositoryConnection con1 = rep1.getConnection();
		try {
			Iterations.addAll(con1.getStatements(null, null, null, true), model1);
		}
		finally {
			con1.close();
		}

		RepositoryConnection con2 = rep2.getConnection();
		try {
			Iterations.addAll(con2.getStatements(null, null, null, true), model2);
		}
		finally {
			con2.close();
		}

		return modelsEqual(model1, model2);
	}

	/**
	 * Compares the two query results by converting them to graphs and returns
	 * true if they are equal. QueryResults are equal if they contain the same
	 * set of BindingSet and have the headers. bNodes IDs are not relevant for
	 * equality, they are mapped from one model to the other by using the
	 * attached properties. Note that the method consumes both query results
	 * fully.
	 * 
	 * @throws QueryEvaluationException
	 */
	public static boolean modelsEqual(TupleQueryResult result1, TupleQueryResult result2)
		throws QueryEvaluationException
	{
		Collection<? extends Statement> graph1 = asGraph(result1);
		Collection<? extends Statement> graph2 = asGraph(result2);

		return modelsEqual(graph1, graph2);
	}

	/**
	 * Compares two models, defined by two statement collections, and returns
	 * true if they are equal. Models are equal if they contain the same set of
	 * statements. Blank node IDs are not relevant for model equality, they are
	 * mapped from one model to the other by using the attached properties.
	 */
	public static boolean modelsEqual(Collection<? extends Statement> model1,
			Collection<? extends Statement> model2)
	{
		// Compare the number of statements in both sets
		if (model1.size() != model2.size()) {
			return false;
		}

		// Create working copies
		List<Statement> copy1 = new LinkedList<Statement>(model1);
		List<Statement> copy2 = new LinkedList<Statement>(model2);

		return isSubsetInternal(copy1, copy2);
	}

	public static boolean modelsEqual(GraphQueryResult result1, GraphQueryResult result2)
		throws QueryEvaluationException
	{
		Collection<? extends Statement> graph1 = asGraph(result1);
		Collection<? extends Statement> graph2 = asGraph(result2);

		return modelsEqual(graph1, graph2);
	}

	/**
	 * Compares the models of the default context of two repositories and returns
	 * true if rep1 is a subset of rep2. Note that the method pulls the entire
	 * default context of both repositories into main memory. Use with caution.
	 */
	public static boolean isSubset(Repository rep1, Repository rep2)
		throws RepositoryException
	{
		RepositoryConnection con1 = rep1.getConnection();
		Collection<Statement> model1 = new HashSet<Statement>();
		Iterations.addAll(con1.getStatements(null, null, null, true), model1);
		con1.close();

		RepositoryConnection con2 = rep2.getConnection();
		Collection<Statement> model2 = new HashSet<Statement>();
		Iterations.addAll(con2.getStatements(null, null, null, true), model2);
		con2.close();

		return isSubset(model1, model2);
	}

	/**
	 * Compares two models, defined by two statement collections, and returns
	 * true if the first model is a subset of the second model.
	 */
	public static boolean isSubset(Collection<? extends Statement> model1,
			Collection<? extends Statement> model2)
	{
		// Compare the number of statements in both sets
		if (model1.size() > model2.size()) {
			return false;
		}

		// Create working copies
		List<Statement> copy1 = new LinkedList<Statement>(model1);
		List<Statement> copy2 = new LinkedList<Statement>(model2);

		return isSubsetInternal(copy1, copy2);
	}

	/**
	 * Compares two models defined by the default context of two repositories and
	 * returns the difference between the first and the second model (that is,
	 * all statements that are present in rep1 but not in rep2). Blank node IDs
	 * are not relevant for model equality, they are mapped from one model to the
	 * other by using the attached properties. Note that the method pulls the
	 * entire default context of both repositories into main memory. Use with
	 * caution.
	 * 
	 * @return The collection of statements that is the difference between rep1
	 *         and rep2.
	 */
	public static Collection<? extends Statement> difference(Repository rep1, Repository rep2)
		throws RepositoryException
	{
		RepositoryConnection con1 = rep1.getConnection();
		Collection<Statement> model1 = new ArrayList<Statement>();
		Iterations.addAll(con1.getStatements(null, null, null, false), model1);
		con1.close();

		RepositoryConnection con2 = rep2.getConnection();
		Collection<Statement> model2 = new ArrayList<Statement>();
		Iterations.addAll(con2.getStatements(null, null, null, false), model2);
		con2.close();

		return difference(model1, model2);
	}

	/**
	 * Compares two models, defined by two statement collections, and returns the
	 * difference between the first and the second model (that is, all statements
	 * that are present in model1 but not in model2). Blank node IDs are not
	 * relevant for model equality, they are mapped from one model to the other
	 * by using the attached properties.
	 * 
	 * @return The collection of statements that is the difference between model1
	 *         and model2.
	 */
	public static Collection<? extends Statement> difference(Collection<? extends Statement> model1,
			Collection<? extends Statement> model2)
	{
		Collection<Statement> result = new ArrayList<Statement>();

		// Create working copies
		List<Statement> copy1 = new LinkedList<Statement>(model1);
		List<Statement> copy2 = new LinkedList<Statement>(model2);

		// Compare statements that don't contain bNodes
		Iterator<Statement> iter1 = copy1.iterator();
		while (iter1.hasNext()) {
			Statement st = iter1.next();

			if (st.getSubject() instanceof BNode || st.getObject() instanceof BNode) {
				// One or more of the statement's components is a bNode,
				// these statements are handled later
				continue;
			}

			// Try to remove the statement from model2
			boolean removed = copy2.remove(st);
			if (!removed) {
				// statement was not present in model2 and is part of the difference
				result.add(st);
			}
			iter1.remove();
		}

		HashMap<BNode, BNode> bNodeMapping = new HashMap<BNode, BNode>();

		// create a bNode mapping (if possible) and determine if under this
		// mapping the two working copies of the models are equal
		
		mapBlankNodes(copy1, copy2, bNodeMapping, 0);

		for (Statement st1 : copy1) {
			boolean foundMatch = false;

			for (Statement st2 : copy2) {
				if (statementsMatch(st1, st2, bNodeMapping)) {
					// Found a matching statement
					foundMatch = true;
					break;
				}
			}

			if (!foundMatch) {
				// No statement matching st1 was found in model2, st1 is part of
				// the difference.
				result.add(st1);
			}
		}
		return result;
	}

	private static boolean isSubsetInternal(List<Statement> model1, List<Statement> model2) {
		// Compare statements that don't contain bNodes
		Iterator<Statement> iter1 = model1.iterator();
		while (iter1.hasNext()) {
			Statement st = iter1.next();

			if (st.getSubject() instanceof BNode || st.getObject() instanceof BNode) {
				// One or more of the statement's components is a bNode,
				// these statements are handled later
				continue;
			}

			// Try to remove the statement from model2
			boolean removed = model2.remove(st);
			if (removed) {
				iter1.remove();
			}
			else {
				// Statement could not be found in stat2; models are not equal
				return false;
			}
		}

		// Compare the statements that do contain bNodes

		// try to create a full blank node mapping
		HashMap<BNode, BNode> bNodeMapping = new HashMap<BNode, BNode>();
		boolean matchingModels = mapBlankNodes(model1, model2, bNodeMapping, 0);
		if (matchingModels) {
			// if a full mapping has been found, determine if model1 completely
			// matches model2
			return modelsMatch(model1, model2, bNodeMapping);
		}
		else {
			return false;
		}
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
	private static boolean mapBlankNodes(List<Statement> model1, List<Statement> model2,
			Map<BNode, BNode> bNodeMapping, int idx)
	{
		boolean result = false;

		// Find next statement with unmapped bNode(s) in model1,
		// starting from index 'idx'
		Statement st1 = null;
		for (; idx < model1.size(); idx++) {
			st1 = model1.get(idx);

			if (st1.getSubject() instanceof BNode && !bNodeMapping.containsKey(st1.getSubject())
					|| st1.getObject() instanceof BNode && !bNodeMapping.containsKey(st1.getObject()))
			{
				// Found a statement containing an unmapped bNode
				break;
			}
		}

		if (idx < model1.size()) {
			// Found a statement containing an unmapped bNode, find
			// statements in model2 that potentially matches st1
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

				// Enter recursion
				result = mapBlankNodes(model1, model2, newBNodeMapping, idx + 1);

				if (result == true) {
					// models match, look no further
					break;
				}
			}
		}
		else {
			// full mapping found
			result = true;
		}

		return result;
	}

	private static List<Statement> findMatchingStatements(Statement st, List<Statement> model,
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

		if (!(subj1 instanceof BNode)) {
			if (!subj1.equals(subj2)) {
				// subjects are not bNodes and don't match
				return false;
			}
		}
		else { // subj1 instanceof BNode
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
				// possible mapping
				// candidate
				if (bNodeMapping.containsValue(subj2)) {
					// 'subj2' is already mapped to some other value.
					return false;
				}
			}
		}

		Value obj1 = st1.getObject();
		Value obj2 = st2.getObject();

		if (!(obj1 instanceof BNode)) {
			if (!obj1.equals(obj2)) {
				// objects are not bNodes and don't match
				return false;
			}
		}
		else { // obj1 instanceof BNode
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
				// possible mapping
				// candidate
				if (bNodeMapping.containsValue(obj2)) {
					// 'obj2' is already mapped to some other value.
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * determines if model1 is completely matched by model2, given a complete
	 * blank node mapping
	 * 
	 * @param model1
	 * @param model2
	 * @param bNodeMapping
	 * @return true if model1 is completely matched by model2 (i.e. model1 is
	 *         graph-isomorphic to a subgraph of model2), false otherwise.
	 */
	private static boolean modelsMatch(List<Statement> model1, List<Statement> model2,
			Map<BNode, BNode> bNodeMapping)
	{
		for (Statement st1 : model1) {
			boolean foundMatch = false;

			for (Statement st2 : model2) {
				if (statementsMatch(st1, st2, bNodeMapping)) {
					// Found a matching statement
					foundMatch = true;
					break;
				}
			}

			if (!foundMatch) {
				// No statement matching st1 was found in model2
				return false;
			}
		}

		// All statements from model1 have matching statements in model2
		return true;
	}

	/**
	 * Creates a graph representation of the supplied TupleQueryResult, using the
	 * Data Access Working Group Test Result Set RDF Vocabulary
	 * (http://www.w3.org/2001/sw/DataAccess/tests/result-set#). The supplied
	 * TupleQueryResult is fully consumed.
	 */
	public static Graph asGraph(TupleQueryResult tqr)
		throws QueryEvaluationException
	{
		try {
			StatementCollector stCollector = new StatementCollector();
			TupleQueryResultHandler dawgWriter = new DAWGTestResultSetWriter(stCollector);

			QueryResultUtil.report(tqr, dawgWriter);

			return new GraphImpl(stCollector.getStatements());
		}
		catch (TupleQueryResultHandlerException e) {
			// No exceptions expected here, foud a bug?
			throw new RuntimeException(e);
		}
	}

	public static Graph asGraph(GraphQueryResult gqr)
		throws QueryEvaluationException
	{
		try {
			StatementCollector stCollector = new StatementCollector();
			QueryResultUtil.report(gqr, stCollector);
			return new GraphImpl(stCollector.getStatements());
		}
		catch (RDFHandlerException e) {
			// No exceptions expected here, foud a bug?
			throw new RuntimeException(e);
		}
	}
}
