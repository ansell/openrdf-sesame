/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

/**
 * RDF(S) Inferencer for in-memory repositories using the rules from the RDF
 * Semantics Recommendation (10 February 2004). See
 * http://www.w3.org/TR/2004/REC-rdf-mt-20040210/
 */
public abstract class ForwardChainingRDFSInferencer extends SailWrapper {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Flags indicating which rules should be evaluated.
	 */
	private boolean[] _checkRule = new boolean[RDFSRules.RULECOUNT];

	/**
	 * Flags indicating which rules should be evaluated next iteration.
	 */
	private boolean[] _checkRuleNextIter = new boolean[RDFSRules.RULECOUNT];

	private int _totalInferred = 0;

	/**
	 * The number of inferred statements per rule.
	 */
	private int[] _ruleCount = new int[RDFSRules.RULECOUNT];

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ForwardChainingRDFSInferencer() {
		super();
	}

	public ForwardChainingRDFSInferencer(Sail baseSail) {
		super(baseSail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Adds axiom statements to the underlying Sail.
	 */
	public void initialize()
		throws SailException
	{
		super.initialize();

		_addAxiomStatements();
	}

	public void doInferencing(ForwardChainingInferencerConnection con)
		throws SailException
	{
		if (!hasNewStatements()) {
			// There's nothing to do
			return;
		}

		// initialize some vars
		_totalInferred = 0;
		int iteration = 0;
		int nofInferred = 1;

		// All rules need to be checked:
		for (int i = 0; i < RDFSRules.RULECOUNT; i++) {
			_ruleCount[i] = 0;
			_checkRuleNextIter[i] = true;
		}

		while (hasNewStatements()) {
			iteration++;
			logger.debug("starting iteration " + iteration);
			_prepareIteration();

			nofInferred = 0;
			nofInferred += applyRule(con, RDFSRules.Rdf1);
			nofInferred += applyRule(con, RDFSRules.Rdfs2_1);
			nofInferred += applyRule(con, RDFSRules.Rdfs2_2);
			nofInferred += applyRule(con, RDFSRules.Rdfs3_1);
			nofInferred += applyRule(con, RDFSRules.Rdfs3_2);
			nofInferred += applyRule(con, RDFSRules.Rdfs4a);
			nofInferred += applyRule(con, RDFSRules.Rdfs4b);
			nofInferred += applyRule(con, RDFSRules.Rdfs5_1);
			nofInferred += applyRule(con, RDFSRules.Rdfs5_2);
			nofInferred += applyRule(con, RDFSRules.Rdfs6);
			nofInferred += applyRule(con, RDFSRules.Rdfs7_1);
			nofInferred += applyRule(con, RDFSRules.Rdfs7_2);
			nofInferred += applyRule(con, RDFSRules.Rdfs8);
			nofInferred += applyRule(con, RDFSRules.Rdfs9_1);
			nofInferred += applyRule(con, RDFSRules.Rdfs9_2);
			nofInferred += applyRule(con, RDFSRules.Rdfs10);
			nofInferred += applyRule(con, RDFSRules.Rdfs11_1);
			nofInferred += applyRule(con, RDFSRules.Rdfs11_2);
			nofInferred += applyRule(con, RDFSRules.Rdfs12);
			nofInferred += applyRule(con, RDFSRules.Rdfs13);
			nofInferred += applyRule(con, RDFSRules.RX1);

			logger.debug("iteration " + iteration + " done; inferred " + nofInferred + " new statements");
			_totalInferred += nofInferred;
		}

		// Print some statistics
		logger.debug("---RdfMTInferencer statistics:---");
		logger.debug("total statements inferred = " + _totalInferred);
		for (int i = 0; i < RDFSRules.RULECOUNT; i++) {
			logger.debug("rule " + RDFSRules.RULENAMES[i] + ":\t#inferred=" + _ruleCount[i]);
		}
		logger.debug("---end of statistics:---");
	}

	protected void _prepareIteration() {
		for (int i = 0; i < RDFSRules.RULECOUNT; i++) {
			_checkRule[i] = _checkRuleNextIter[i];

			// reset for next iteration:
			_checkRuleNextIter[i] = false;
		}
	}

	protected void _updateTriggers(int ruleNo, int nofInferred) {
		if (nofInferred > 0) {
			_ruleCount[ruleNo] += nofInferred;

			// Check which rules are triggered by this one.
			boolean[] triggers = RDFSRules.TRIGGERS[ruleNo];

			for (int i = 0; i < RDFSRules.RULECOUNT; i++) {
				if (triggers[i] == true) {
					_checkRuleNextIter[i] = true;
				}
			}
		}
	}

	public int applyRule(ForwardChainingInferencerConnection con, int rule)
		throws SailException
	{
		if (!_checkRule[rule]) {
			return 0;
		}
		int nofInferred = 0;

		nofInferred = _applyRule(con, rule);

		_updateTriggers(rule, nofInferred);

		return nofInferred;
	}

	protected abstract int _applyRule(ForwardChainingInferencerConnection con, int rule)
		throws SailException;

	public abstract boolean hasNewStatements();

	private void _addAxiomStatements()
		throws SailException
	{
		ForwardChainingInferencerConnection con = (ForwardChainingInferencerConnection)getConnection();
		_addAxiomStatements(con);
		con.commit();
		con.close();
	}

	/**
	 * Adds all basic set of axiom statements from which the complete set can be
	 * inferred to the underlying Sail.
	 */
	protected void _addAxiomStatements(ForwardChainingInferencerConnection con)
		throws SailException
	{
		logger.debug("Inserting axiom statements");

		// RDF axiomatic triples (from RDF Semantics, section 3.1):

		con._addInferredStatement(RDF.TYPE, RDF.TYPE, RDF.PROPERTY);
		con._addInferredStatement(RDF.SUBJECT, RDF.TYPE, RDF.PROPERTY);
		con._addInferredStatement(RDF.PREDICATE, RDF.TYPE, RDF.PROPERTY);
		con._addInferredStatement(RDF.OBJECT, RDF.TYPE, RDF.PROPERTY);

		con._addInferredStatement(RDF.FIRST, RDF.TYPE, RDF.PROPERTY);
		con._addInferredStatement(RDF.REST, RDF.TYPE, RDF.PROPERTY);
		con._addInferredStatement(RDF.VALUE, RDF.TYPE, RDF.PROPERTY);

		con._addInferredStatement(RDF.NIL, RDF.TYPE, RDF.LIST);

		// RDFS axiomatic triples (from RDF Semantics, section 4.1):

		con._addInferredStatement(RDF.TYPE, RDFS.DOMAIN, RDFS.RESOURCE);
		con._addInferredStatement(RDFS.DOMAIN, RDFS.DOMAIN, RDF.PROPERTY);
		con._addInferredStatement(RDFS.RANGE, RDFS.DOMAIN, RDF.PROPERTY);
		con._addInferredStatement(RDFS.SUBPROPERTYOF, RDFS.DOMAIN, RDF.PROPERTY);
		con._addInferredStatement(RDFS.SUBCLASSOF, RDFS.DOMAIN, RDFS.CLASS);
		con._addInferredStatement(RDF.SUBJECT, RDFS.DOMAIN, RDF.STATEMENT);
		con._addInferredStatement(RDF.PREDICATE, RDFS.DOMAIN, RDF.STATEMENT);
		con._addInferredStatement(RDF.OBJECT, RDFS.DOMAIN, RDF.STATEMENT);
		con._addInferredStatement(RDFS.MEMBER, RDFS.DOMAIN, RDFS.RESOURCE);
		con._addInferredStatement(RDF.FIRST, RDFS.DOMAIN, RDF.LIST);
		con._addInferredStatement(RDF.REST, RDFS.DOMAIN, RDF.LIST);
		con._addInferredStatement(RDFS.SEEALSO, RDFS.DOMAIN, RDFS.RESOURCE);
		con._addInferredStatement(RDFS.ISDEFINEDBY, RDFS.DOMAIN, RDFS.RESOURCE);
		con._addInferredStatement(RDFS.COMMENT, RDFS.DOMAIN, RDFS.RESOURCE);
		con._addInferredStatement(RDFS.LABEL, RDFS.DOMAIN, RDFS.RESOURCE);
		con._addInferredStatement(RDF.VALUE, RDFS.DOMAIN, RDFS.RESOURCE);

		con._addInferredStatement(RDF.TYPE, RDFS.RANGE, RDFS.CLASS);
		con._addInferredStatement(RDFS.DOMAIN, RDFS.RANGE, RDFS.CLASS);
		con._addInferredStatement(RDFS.RANGE, RDFS.RANGE, RDFS.CLASS);
		con._addInferredStatement(RDFS.SUBPROPERTYOF, RDFS.RANGE, RDF.PROPERTY);
		con._addInferredStatement(RDFS.SUBCLASSOF, RDFS.RANGE, RDFS.CLASS);
		con._addInferredStatement(RDF.SUBJECT, RDFS.RANGE, RDFS.RESOURCE);
		con._addInferredStatement(RDF.PREDICATE, RDFS.RANGE, RDFS.RESOURCE);
		con._addInferredStatement(RDF.OBJECT, RDFS.RANGE, RDFS.RESOURCE);
		con._addInferredStatement(RDFS.MEMBER, RDFS.RANGE, RDFS.RESOURCE);
		con._addInferredStatement(RDF.FIRST, RDFS.RANGE, RDFS.RESOURCE);
		con._addInferredStatement(RDF.REST, RDFS.RANGE, RDF.LIST);
		con._addInferredStatement(RDFS.SEEALSO, RDFS.RANGE, RDFS.RESOURCE);
		con._addInferredStatement(RDFS.ISDEFINEDBY, RDFS.RANGE, RDFS.RESOURCE);
		con._addInferredStatement(RDFS.COMMENT, RDFS.RANGE, RDFS.LITERAL);
		con._addInferredStatement(RDFS.LABEL, RDFS.RANGE, RDFS.LITERAL);
		con._addInferredStatement(RDF.VALUE, RDFS.RANGE, RDFS.RESOURCE);

		con._addInferredStatement(RDF.ALT, RDFS.SUBCLASSOF, RDFS.CONTAINER);
		con._addInferredStatement(RDF.BAG, RDFS.SUBCLASSOF, RDFS.CONTAINER);
		con._addInferredStatement(RDF.SEQ, RDFS.SUBCLASSOF, RDFS.CONTAINER);
		con._addInferredStatement(RDFS.CONTAINERMEMBERSHIPPROPERTY, RDFS.SUBCLASSOF, RDF.PROPERTY);

		con._addInferredStatement(RDFS.ISDEFINEDBY, RDFS.SUBPROPERTYOF, RDFS.SEEALSO);

		con._addInferredStatement(RDF.XMLLITERAL, RDF.TYPE, RDFS.DATATYPE);
		con._addInferredStatement(RDF.XMLLITERAL, RDFS.SUBCLASSOF, RDFS.LITERAL);
		con._addInferredStatement(RDFS.DATATYPE, RDFS.SUBCLASSOF, RDFS.CLASS);
	}

	/*-------------------------------------------------*
	 * Inner class ForwardChainingInferencerConnection *
	 *-------------------------------------------------*/

	protected abstract class ForwardChainingInferencerConnection extends InferencerConnectionWrapper {

		public ForwardChainingInferencerConnection(InferencerConnection con) {
			super(con);
		}

		/**
		 * Adds an inferred statement with the specified subject, predicate and
		 * object to the null context of the underlying Sail.
		 */
		protected abstract boolean _addInferredStatement(Resource subject, URI predicate, Value object)
			throws SailException;

		/**
		 * Removes all inferred statements from the underlying Sail.
		 */
		public abstract void _clearInferred()
			throws SailException;
	} // end inner class ForwardChainingInferencerTransaction
}
