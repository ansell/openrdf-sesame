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
package org.openrdf.sail.inferencer.fc;

import info.aduna.iteration.CloseableIteration;
import info.aduna.text.ASCIIUtil;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.InferencerConnection;

/**
 * Forward-chaining RDF Schema inferencer, using the rules from the <a
 * href="http://www.w3.org/TR/2004/REC-rdf-mt-20040210/">RDF Semantics
 * Recommendation (10 February 2004)</a>. This inferencer can be used to add RDF
 * Schema semantics to any Sail that returns {@link InferencerConnection}s from
 * their {@link Sail#getConnection()} method.
 */
class ForwardChainingRDFSInferencerConnection extends AbstractForwardChainingInferencerConnection
{
	private static final Resource[] NO_CONTEXT = {};

	private final ForwardChainingRDFSInferencer inferencer;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Model newThisIteration;

	/**
	 * Flags indicating which rules should be evaluated.
	 */
	private boolean[] checkRule = new boolean[RDFSRules.RULECOUNT];

	/**
	 * Flags indicating which rules should be evaluated next iteration.
	 */
	private boolean[] checkRuleNextIter = new boolean[RDFSRules.RULECOUNT];

	/**
	 * The number of inferred statements per rule.
	 */
	private int[] ruleCount = new int[RDFSRules.RULECOUNT];

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ForwardChainingRDFSInferencerConnection(ForwardChainingRDFSInferencer sail, InferencerConnection con) {
		super(sail, con);
		this.inferencer = sail;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Called by base sail
	@Override
	protected Model createModel() {
		return new TreeModel();
	}

	@Override
	protected void resetInferred()
		throws SailException
	{
		Resource axiomContext = inferencer.getAxiomContext();
		if(axiomContext != null) {
			// optimised reset
			List<Resource> contexts = new ArrayList<Resource>();
			CloseableIteration<? extends Resource, SailException> iter = getContextIDs();
			while (iter.hasNext()) {
				Resource ctx = iter.next();
				if (!axiomContext.equals(ctx)) {
					contexts.add(ctx);
				}
			}
			clearInferred(contexts.toArray(new Resource[contexts.size()]));
		}
		else {
			super.resetInferred();
		}
	}

	/**
	 * Adds all basic set of axiom statements from which the complete set can be
	 * inferred to the underlying Sail.
	 */
	@Override
	protected void addAxiomStatements()
		throws SailException
	{
		logger.debug("Inserting axiom statements");

		Resource axiomContext = inferencer.getAxiomContext();
		Resource[] contexts = (axiomContext != null) ? new Resource[] {axiomContext} : NO_CONTEXT;

		// RDF axiomatic triples (from RDF Semantics, section 3.1):

		addInferredStatement(RDF.TYPE, RDF.TYPE, RDF.PROPERTY, contexts);
		addInferredStatement(RDF.SUBJECT, RDF.TYPE, RDF.PROPERTY, contexts);
		addInferredStatement(RDF.PREDICATE, RDF.TYPE, RDF.PROPERTY, contexts);
		addInferredStatement(RDF.OBJECT, RDF.TYPE, RDF.PROPERTY, contexts);

		addInferredStatement(RDF.FIRST, RDF.TYPE, RDF.PROPERTY, contexts);
		addInferredStatement(RDF.REST, RDF.TYPE, RDF.PROPERTY, contexts);
		addInferredStatement(RDF.VALUE, RDF.TYPE, RDF.PROPERTY, contexts);

		addInferredStatement(RDF.NIL, RDF.TYPE, RDF.LIST, contexts);

		// RDFS axiomatic triples (from RDF Semantics, section 4.1):

		addInferredStatement(RDF.TYPE, RDFS.DOMAIN, RDFS.RESOURCE, contexts);
		addInferredStatement(RDFS.DOMAIN, RDFS.DOMAIN, RDF.PROPERTY, contexts);
		addInferredStatement(RDFS.RANGE, RDFS.DOMAIN, RDF.PROPERTY, contexts);
		addInferredStatement(RDFS.SUBPROPERTYOF, RDFS.DOMAIN, RDF.PROPERTY, contexts);
		addInferredStatement(RDFS.SUBCLASSOF, RDFS.DOMAIN, RDFS.CLASS, contexts);
		addInferredStatement(RDF.SUBJECT, RDFS.DOMAIN, RDF.STATEMENT, contexts);
		addInferredStatement(RDF.PREDICATE, RDFS.DOMAIN, RDF.STATEMENT, contexts);
		addInferredStatement(RDF.OBJECT, RDFS.DOMAIN, RDF.STATEMENT, contexts);
		addInferredStatement(RDFS.MEMBER, RDFS.DOMAIN, RDFS.RESOURCE, contexts);
		addInferredStatement(RDF.FIRST, RDFS.DOMAIN, RDF.LIST, contexts);
		addInferredStatement(RDF.REST, RDFS.DOMAIN, RDF.LIST, contexts);
		addInferredStatement(RDFS.SEEALSO, RDFS.DOMAIN, RDFS.RESOURCE, contexts);
		addInferredStatement(RDFS.ISDEFINEDBY, RDFS.DOMAIN, RDFS.RESOURCE, contexts);
		addInferredStatement(RDFS.COMMENT, RDFS.DOMAIN, RDFS.RESOURCE, contexts);
		addInferredStatement(RDFS.LABEL, RDFS.DOMAIN, RDFS.RESOURCE, contexts);
		addInferredStatement(RDF.VALUE, RDFS.DOMAIN, RDFS.RESOURCE, contexts);

		addInferredStatement(RDF.TYPE, RDFS.RANGE, RDFS.CLASS, contexts);
		addInferredStatement(RDFS.DOMAIN, RDFS.RANGE, RDFS.CLASS, contexts);
		addInferredStatement(RDFS.RANGE, RDFS.RANGE, RDFS.CLASS, contexts);
		addInferredStatement(RDFS.SUBPROPERTYOF, RDFS.RANGE, RDF.PROPERTY, contexts);
		addInferredStatement(RDFS.SUBCLASSOF, RDFS.RANGE, RDFS.CLASS, contexts);
		addInferredStatement(RDF.SUBJECT, RDFS.RANGE, RDFS.RESOURCE, contexts);
		addInferredStatement(RDF.PREDICATE, RDFS.RANGE, RDFS.RESOURCE, contexts);
		addInferredStatement(RDF.OBJECT, RDFS.RANGE, RDFS.RESOURCE, contexts);
		addInferredStatement(RDFS.MEMBER, RDFS.RANGE, RDFS.RESOURCE, contexts);
		addInferredStatement(RDF.FIRST, RDFS.RANGE, RDFS.RESOURCE, contexts);
		addInferredStatement(RDF.REST, RDFS.RANGE, RDF.LIST, contexts);
		addInferredStatement(RDFS.SEEALSO, RDFS.RANGE, RDFS.RESOURCE, contexts);
		addInferredStatement(RDFS.ISDEFINEDBY, RDFS.RANGE, RDFS.RESOURCE, contexts);
		addInferredStatement(RDFS.COMMENT, RDFS.RANGE, RDFS.LITERAL, contexts);
		addInferredStatement(RDFS.LABEL, RDFS.RANGE, RDFS.LITERAL, contexts);
		addInferredStatement(RDF.VALUE, RDFS.RANGE, RDFS.RESOURCE, contexts);

		addInferredStatement(RDF.ALT, RDFS.SUBCLASSOF, RDFS.CONTAINER, contexts);
		addInferredStatement(RDF.BAG, RDFS.SUBCLASSOF, RDFS.CONTAINER, contexts);
		addInferredStatement(RDF.SEQ, RDFS.SUBCLASSOF, RDFS.CONTAINER, contexts);
		addInferredStatement(RDFS.CONTAINERMEMBERSHIPPROPERTY, RDFS.SUBCLASSOF, RDF.PROPERTY, contexts);

		addInferredStatement(RDFS.ISDEFINEDBY, RDFS.SUBPROPERTYOF, RDFS.SEEALSO, contexts);

		addInferredStatement(RDF.XMLLITERAL, RDF.TYPE, RDFS.DATATYPE, contexts);
		addInferredStatement(RDF.XMLLITERAL, RDFS.SUBCLASSOF, RDFS.LITERAL, contexts);
		addInferredStatement(RDFS.DATATYPE, RDFS.SUBCLASSOF, RDFS.CLASS, contexts);
	}

	@Override
	protected void doInferencing()
		throws SailException
	{
		// All rules need to be checked:
		for (int i = 0; i < RDFSRules.RULECOUNT; i++) {
			ruleCount[i] = 0;
			checkRuleNextIter[i] = true;
		}

		super.doInferencing();

		// Print some statistics
		logger.debug("---RdfMTInferencer statistics:---");
		logger.debug("total statements inferred = " + totalInferred);
		for (int i = 0; i < RDFSRules.RULECOUNT; i++) {
			logger.debug("rule " + RDFSRules.RULENAMES[i] + ":\t#inferred=" + ruleCount[i]);
		}
		logger.debug("---end of statistics:---");
	}

	@Override
	protected int applyRules(Model iteration) throws SailException
	{
		newThisIteration = iteration;
		int nofInferred = 0;
		nofInferred += applyRule(RDFSRules.Rdf1);
		nofInferred += applyRule(RDFSRules.Rdfs2_1);
		nofInferred += applyRule(RDFSRules.Rdfs2_2);
		nofInferred += applyRule(RDFSRules.Rdfs3_1);
		nofInferred += applyRule(RDFSRules.Rdfs3_2);
		nofInferred += applyRule(RDFSRules.Rdfs4a);
		nofInferred += applyRule(RDFSRules.Rdfs4b);
		nofInferred += applyRule(RDFSRules.Rdfs5_1);
		nofInferred += applyRule(RDFSRules.Rdfs5_2);
		nofInferred += applyRule(RDFSRules.Rdfs6);
		nofInferred += applyRule(RDFSRules.Rdfs7_1);
		nofInferred += applyRule(RDFSRules.Rdfs7_2);
		nofInferred += applyRule(RDFSRules.Rdfs8);
		nofInferred += applyRule(RDFSRules.Rdfs9_1);
		nofInferred += applyRule(RDFSRules.Rdfs9_2);
		nofInferred += applyRule(RDFSRules.Rdfs10);
		nofInferred += applyRule(RDFSRules.Rdfs11_1);
		nofInferred += applyRule(RDFSRules.Rdfs11_2);
		nofInferred += applyRule(RDFSRules.Rdfs12);
		nofInferred += applyRule(RDFSRules.Rdfs13);
		nofInferred += applyRule(RDFSRules.RX1);
		newThisIteration = null;
		return nofInferred;
	}

	@Override
	protected Model prepareIteration() {
		for (int i = 0; i < RDFSRules.RULECOUNT; i++) {
			checkRule[i] = checkRuleNextIter[i];

			// reset for next iteration:
			checkRuleNextIter[i] = false;
		}
		return super.prepareIteration();
	}

	protected void updateTriggers(int ruleNo, int nofInferred) {
		if (nofInferred > 0) {
			ruleCount[ruleNo] += nofInferred;

			// Check which rules are triggered by this one.
			boolean[] triggers = RDFSRules.TRIGGERS[ruleNo];

			for (int i = 0; i < RDFSRules.RULECOUNT; i++) {
				if (triggers[i] == true) {
					checkRuleNextIter[i] = true;
				}
			}
		}
	}

	protected int applyRule(int rule)
		throws SailException
	{
		if (!checkRule[rule]) {
			return 0;
		}
		int nofInferred = 0;

		nofInferred = applyRuleInternal(rule);

		updateTriggers(rule, nofInferred);

		return nofInferred;
	}

	protected int applyRuleInternal(int rule)
		throws SailException
	{
		int result = 0;

		switch (rule) {
			case RDFSRules.Rdf1:
				result = applyRuleRdf1();
				break;
			case RDFSRules.Rdfs2_1:
				result = applyRuleRdfs2_1();
				break;
			case RDFSRules.Rdfs2_2:
				result = applyRuleRdfs2_2();
				break;
			case RDFSRules.Rdfs3_1:
				result = applyRuleRdfs3_1();
				break;
			case RDFSRules.Rdfs3_2:
				result = applyRuleRdfs3_2();
				break;
			case RDFSRules.Rdfs4a:
				result = applyRuleRdfs4a();
				break;
			case RDFSRules.Rdfs4b:
				result = applyRuleRdfs4b();
				break;
			case RDFSRules.Rdfs5_1:
				result = applyRuleRdfs5_1();
				break;
			case RDFSRules.Rdfs5_2:
				result = applyRuleRdfs5_2();
				break;
			case RDFSRules.Rdfs6:
				result = applyRuleRdfs6();
				break;
			case RDFSRules.Rdfs7_1:
				result = applyRuleRdfs7_1();
				break;
			case RDFSRules.Rdfs7_2:
				result = applyRuleRdfs7_2();
				break;
			case RDFSRules.Rdfs8:
				result = applyRuleRdfs8();
				break;
			case RDFSRules.Rdfs9_1:
				result = applyRuleRdfs9_1();
				break;
			case RDFSRules.Rdfs9_2:
				result = applyRuleRdfs9_2();
				break;
			case RDFSRules.Rdfs10:
				result = applyRuleRdfs10();
				break;
			case RDFSRules.Rdfs11_1:
				result = applyRuleRdfs11_1();
				break;
			case RDFSRules.Rdfs11_2:
				result = applyRuleRdfs11_2();
				break;
			case RDFSRules.Rdfs12:
				result = applyRuleRdfs12();
				break;
			case RDFSRules.Rdfs13:
				result = applyRuleRdfs13();
				break;
			case RDFSRules.RX1:
				result = applyRuleX1();
				break;
			default:
				throw new AssertionError("Unexpected rule: "+rule);
		}
		// ThreadLog.trace("Rule " + RDFSRules.RULENAMES[rule] + " inferred " +
		// result + " new triples.");
		return result;
	}

	// xxx aaa yyy --> aaa rdf:type rdf:Property
	private int applyRuleRdf1()
		throws SailException
	{
		int nofInferred = 0;

		Model iter = newThisIteration.filter(null, null, null);

		for(Statement st : iter) {
			boolean added = addInferredStatement(st.getPredicate(), RDF.TYPE, RDF.PROPERTY, inferredContext(st));

			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	// xxx aaa yyy (nt) && aaa rdfs:domain zzz (t1) --> xxx rdf:type zzz
	private int applyRuleRdfs2_1()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, null, null);

		for(Statement nt : ntIter) {
			Resource xxx = nt.getSubject();
			URI aaa = nt.getPredicate();

			CloseableIteration<? extends Statement, SailException> t1Iter;
			t1Iter = getWrappedConnection().getStatements(aaa, RDFS.DOMAIN, null, true);

			while (t1Iter.hasNext()) {
				Statement t1 = t1Iter.next();

				Value zzz = t1.getObject();
				if (zzz instanceof Resource) {
					boolean added = addInferredStatement(xxx, RDF.TYPE, zzz, inferredContext(nt));
					if (added) {
						nofInferred++;
					}
				}
			}
			t1Iter.close();
		}

		return nofInferred;
	}

	// aaa rdfs:domain zzz (nt) && xxx aaa yyy (t1) --> xxx rdf:type zzz
	private int applyRuleRdfs2_2()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDFS.DOMAIN, null);

		for(Statement nt : ntIter) {
			Resource aaa = nt.getSubject();
			Value zzz = nt.getObject();

			if (aaa instanceof URI && zzz instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements(null, (URI)aaa, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();
					boolean added = addInferredStatement(xxx, RDF.TYPE, zzz, inferredContext(nt));
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	// xxx aaa uuu (nt) && aaa rdfs:range zzz (t1) --> uuu rdf:type zzz
	private int applyRuleRdfs3_1()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, null, null);

		for(Statement nt : ntIter) {
			URI aaa = nt.getPredicate();
			Value uuu = nt.getObject();

			if (uuu instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements(aaa, RDFS.RANGE, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value zzz = t1.getObject();
					if (zzz instanceof Resource) {
						boolean added = addInferredStatement((Resource)uuu, RDF.TYPE, zzz, inferredContext(nt));
						if (added) {
							nofInferred++;
						}
					}
				}
				t1Iter.close();
			}
		}
		return nofInferred;
	}

	// aaa rdfs:range zzz (nt) && xxx aaa uuu (t1) --> uuu rdf:type zzz
	private int applyRuleRdfs3_2()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDFS.RANGE, null);

		for(Statement nt : ntIter) {
			Resource aaa = nt.getSubject();
			Value zzz = nt.getObject();

			if (aaa instanceof URI && zzz instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements(null, (URI)aaa, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value uuu = t1.getObject();
					if (uuu instanceof Resource) {
						boolean added = addInferredStatement((Resource)uuu, RDF.TYPE, zzz, inferredContext(nt));
						if (added) {
							nofInferred++;
						}
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;

	}

	// xxx aaa yyy --> xxx rdf:type rdfs:Resource
	private int applyRuleRdfs4a()
		throws SailException
	{
		int nofInferred = 0;

		Model iter = newThisIteration.filter(null, null, null);

		for(Statement st : iter) {
			boolean added = addInferredStatement(st.getSubject(), RDF.TYPE, RDFS.RESOURCE, inferredContext(st));
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	// xxx aaa uuu --> uuu rdf:type rdfs:Resource
	private int applyRuleRdfs4b()
		throws SailException
	{
		int nofInferred = 0;

		Model iter = newThisIteration.filter(null, null, null);

		for(Statement st : iter) {
			Value uuu = st.getObject();
			if (uuu instanceof Resource) {
				boolean added = addInferredStatement((Resource)uuu, RDF.TYPE, RDFS.RESOURCE, inferredContext(st));
				if (added) {
					nofInferred++;
				}
			}
		}

		return nofInferred;
	}

	// aaa rdfs:subPropertyOf bbb (nt) && bbb rdfs:subPropertyOf ccc (t1)
	// --> aaa rdfs:subPropertyOf ccc
	private int applyRuleRdfs5_1()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDFS.SUBPROPERTYOF, null);

		for(Statement nt : ntIter) {
			Resource aaa = nt.getSubject();
			Value bbb = nt.getObject();

			if (bbb instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements((Resource)bbb, RDFS.SUBPROPERTYOF, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value ccc = t1.getObject();
					if (ccc instanceof Resource) {
						boolean added = addInferredStatement(aaa, RDFS.SUBPROPERTYOF, ccc, inferredContext(nt));
						if (added) {
							nofInferred++;
						}
					}
				}
				t1Iter.close();

			}
		}

		return nofInferred;
	}

	// bbb rdfs:subPropertyOf ccc (nt) && aaa rdfs:subPropertyOf bbb (t1)
	// --> aaa rdfs:subPropertyOf ccc
	private int applyRuleRdfs5_2()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDFS.SUBPROPERTYOF, null);

		for(Statement nt : ntIter) {
			Resource bbb = nt.getSubject();
			Value ccc = nt.getObject();

			if (ccc instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements(null, RDFS.SUBPROPERTYOF, bbb, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource aaa = t1.getSubject();
					boolean added = addInferredStatement(aaa, RDFS.SUBPROPERTYOF, ccc, inferredContext(nt));
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	// xxx rdf:type rdf:Property --> xxx rdfs:subPropertyOf xxx
	private int applyRuleRdfs6()
		throws SailException
	{
		int nofInferred = 0;

		Model iter = newThisIteration.filter(null, RDF.TYPE, RDF.PROPERTY);

		for(Statement st : iter) {
			Resource xxx = st.getSubject();
			boolean added = addInferredStatement(xxx, RDFS.SUBPROPERTYOF, xxx, inferredContext(st));
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	// xxx aaa yyy (nt) && aaa rdfs:subPropertyOf bbb (t1) --> xxx bbb yyy
	private int applyRuleRdfs7_1()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, null, null);

		for(Statement nt : ntIter) {
			Resource xxx = nt.getSubject();
			URI aaa = nt.getPredicate();
			Value yyy = nt.getObject();

			CloseableIteration<? extends Statement, SailException> t1Iter;
			t1Iter = getWrappedConnection().getStatements(aaa, RDFS.SUBPROPERTYOF, null, true);

			while (t1Iter.hasNext()) {
				Statement t1 = t1Iter.next();

				Value bbb = t1.getObject();
				if (bbb instanceof URI) {
					boolean added = addInferredStatement(xxx, (URI)bbb, yyy, inferredContext(nt));
					if (added) {
						nofInferred++;
					}
				}
			}
			t1Iter.close();
		}

		return nofInferred;
	}

	// aaa rdfs:subPropertyOf bbb (nt) && xxx aaa yyy (t1) --> xxx bbb yyy
	private int applyRuleRdfs7_2()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDFS.SUBPROPERTYOF, null);

		for(Statement nt : ntIter) {
			Resource aaa = nt.getSubject();
			Value bbb = nt.getObject();

			if (aaa instanceof URI && bbb instanceof URI) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements(null, (URI)aaa, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();
					Value yyy = t1.getObject();

					boolean added = addInferredStatement(xxx, (URI)bbb, yyy, inferredContext(nt));
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	// xxx rdf:type rdfs:Class --> xxx rdfs:subClassOf rdfs:Resource
	private int applyRuleRdfs8()
		throws SailException
	{
		int nofInferred = 0;

		Model iter = newThisIteration.filter(null, RDF.TYPE, RDFS.CLASS);

		for(Statement st : iter) {
			Resource xxx = st.getSubject();

			boolean added = addInferredStatement(xxx, RDFS.SUBCLASSOF, RDFS.RESOURCE, inferredContext(st));
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	// xxx rdfs:subClassOf yyy (nt) && aaa rdf:type xxx (t1) --> aaa rdf:type yyy
	private int applyRuleRdfs9_1()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDFS.SUBCLASSOF, null);

		for(Statement nt : ntIter) {
			Resource xxx = nt.getSubject();
			Value yyy = nt.getObject();

			if (yyy instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements(null, RDF.TYPE, xxx, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource aaa = t1.getSubject();

					boolean added = addInferredStatement(aaa, RDF.TYPE, yyy, inferredContext(nt));
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	// aaa rdf:type xxx (nt) && xxx rdfs:subClassOf yyy (t1) --> aaa rdf:type yyy
	private int applyRuleRdfs9_2()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDF.TYPE, null);

		for(Statement nt : ntIter) {
			Resource aaa = nt.getSubject();
			Value xxx = nt.getObject();

			if (xxx instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements((Resource)xxx, RDFS.SUBCLASSOF, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value yyy = t1.getObject();

					if (yyy instanceof Resource) {
						boolean added = addInferredStatement(aaa, RDF.TYPE, yyy, inferredContext(nt));
						if (added) {
							nofInferred++;
						}
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	// xxx rdf:type rdfs:Class --> xxx rdfs:subClassOf xxx
	private int applyRuleRdfs10()
		throws SailException
	{
		int nofInferred = 0;

		Model iter = newThisIteration.filter(null, RDF.TYPE, RDFS.CLASS);

		for(Statement st : iter) {
			Resource xxx = st.getSubject();

			boolean added = addInferredStatement(xxx, RDFS.SUBCLASSOF, xxx, inferredContext(st));
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	// xxx rdfs:subClassOf yyy (nt) && yyy rdfs:subClassOf zzz (t1)
	// --> xxx rdfs:subClassOf zzz
	private int applyRuleRdfs11_1()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDFS.SUBCLASSOF, null);

		for(Statement nt : ntIter) {
			Resource xxx = nt.getSubject();
			Value yyy = nt.getObject();

			if (yyy instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements((Resource)yyy, RDFS.SUBCLASSOF, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value zzz = t1.getObject();

					if (zzz instanceof Resource) {
						boolean added = addInferredStatement(xxx, RDFS.SUBCLASSOF, zzz, inferredContext(nt));
						if (added) {
							nofInferred++;
						}
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	// yyy rdfs:subClassOf zzz (nt) && xxx rdfs:subClassOf yyy (t1)
	// --> xxx rdfs:subClassOf zzz
	private int applyRuleRdfs11_2()
		throws SailException
	{
		int nofInferred = 0;

		Model ntIter = newThisIteration.filter(null, RDFS.SUBCLASSOF, null);

		for(Statement nt : ntIter) {
			Resource yyy = nt.getSubject();
			Value zzz = nt.getObject();

			if (zzz instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter;
				t1Iter = getWrappedConnection().getStatements(null, RDFS.SUBCLASSOF, yyy, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();

					boolean added = addInferredStatement(xxx, RDFS.SUBCLASSOF, zzz, inferredContext(nt));
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	// xxx rdf:type rdfs:ContainerMembershipProperty
	// --> xxx rdfs:subPropertyOf rdfs:member
	private int applyRuleRdfs12()
		throws SailException
	{
		int nofInferred = 0;

		Model iter = newThisIteration.filter(null, RDF.TYPE, RDFS.CONTAINERMEMBERSHIPPROPERTY);

		for(Statement st : iter) {
			Resource xxx = st.getSubject();

			boolean added = addInferredStatement(xxx, RDFS.SUBPROPERTYOF, RDFS.MEMBER, inferredContext(st));
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	// xxx rdf:type rdfs:Datatype --> xxx rdfs:subClassOf rdfs:Literal
	private int applyRuleRdfs13()
		throws SailException
	{
		int nofInferred = 0;

		Model iter = newThisIteration.filter(null, RDF.TYPE, RDFS.DATATYPE);

		for(Statement st : iter) {
			Resource xxx = st.getSubject();

			boolean added = addInferredStatement(xxx, RDFS.SUBCLASSOF, RDFS.LITERAL, inferredContext(st));
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	// xxx rdf:_* yyy --> rdf:_* rdf:type rdfs:ContainerMembershipProperty
	// This is an extra rule for list membership properties (_1, _2, _3, ...).
	// The RDF MT does not specificy a production for this.
	private int applyRuleX1()
		throws SailException
	{
		int nofInferred = 0;

		String prefix = RDF.NAMESPACE + "_";
		Model iter = newThisIteration.filter(null, null, null);

		for(Statement st : iter) {
			URI predNode = st.getPredicate();
			String predURI = predNode.toString();

			if (predURI.startsWith(prefix) && isValidPredicateNumber(predURI.substring(prefix.length()))) {
				boolean added = addInferredStatement(predNode, RDF.TYPE, RDFS.CONTAINERMEMBERSHIPPROPERTY, inferredContext(st));
				if (added) {
					nofInferred++;
				}
			}
		}

		return nofInferred;
	}

	private Resource[] inferredContext(Statement stmt) {
		return inferencer.isContextPreserved() ? new Resource[] {stmt.getContext()} : NO_CONTEXT;
	}

	/**
	 * Util method for {@link #applyRuleX1}.
	 */
	private boolean isValidPredicateNumber(String str) {
		int strLength = str.length();

		if (strLength == 0) {
			return false;
		}

		for (int i = 0; i < strLength; i++) {
			if (!ASCIIUtil.isNumber(str.charAt(i))) {
				return false;
			}
		}

		// No leading zeros
		if (str.charAt(0) == '0') {
			return false;
		}

		return true;
	}
}
