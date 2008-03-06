/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.util.Iterator;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.ForwardChainingRDFSInferencer;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.InferencerConnectionWrapper;
import org.openrdf.sail.inferencer.RDFSRules;

/**
 * @author jeen
 */
public class NativeStoreRDFSInferencer extends ForwardChainingRDFSInferencer {

	private Graph _newThisIteration;

	private Graph _newStatements;

	public NativeStoreRDFSInferencer() {
		super();
	}

	public NativeStoreRDFSInferencer(Sail baseSail) {
		super(baseSail);
	}
	
	protected void _prepareIteration() {
		super._prepareIteration();
		_newThisIteration = _newStatements;
		_newStatements = new GraphImpl();
	}

	public boolean hasNewStatements() {
		return _newStatements != null && !_newStatements.isEmpty();
	}

	public SailConnection getConnection()
		throws SailException
	{
		try {
			InferencerConnection con = (InferencerConnection)super.getConnection();
			return new NativeStoreRDFSInferencerConnection(con);
		}
		catch (ClassCastException e) {
			throw new SailException(e);
		}
	}

	protected int _applyRule(InferencerConnection con, int rule)
		throws SailException
	{
		int result = 0;
		InferencerConnection mt = (InferencerConnection)con;

		switch (rule) {
			case RDFSRules.Rdf1:
				result = _applyRuleRdf1(mt);
				break;
			case RDFSRules.Rdfs2_1:
				result = _applyRuleRdfs2_1(mt);
				break;
			case RDFSRules.Rdfs2_2:
				result = _applyRuleRdfs2_2(mt);
				break;
			case RDFSRules.Rdfs3_1:
				result = _applyRuleRdfs3_1(mt);
				break;
			case RDFSRules.Rdfs3_2:
				result = _applyRuleRdfs3_2(mt);
				break;
			case RDFSRules.Rdfs4a:
				result = _applyRuleRdfs4a(mt);
				break;
			case RDFSRules.Rdfs4b:
				result = _applyRuleRdfs4b(mt);
				break;
			case RDFSRules.Rdfs5_1:
				result = _applyRuleRdfs5_1(mt);
				break;
			case RDFSRules.Rdfs5_2:
				result = _applyRuleRdfs5_2(mt);
				break;
			case RDFSRules.Rdfs6:
				result = _applyRuleRdfs6(mt);
				break;
			case RDFSRules.Rdfs7_1:
				result = _applyRuleRdfs7_1(mt);
				break;
			case RDFSRules.Rdfs7_2:
				result = _applyRuleRdfs7_2(mt);
				break;
			case RDFSRules.Rdfs8:
				result = _applyRuleRdfs8(mt);
				break;
			case RDFSRules.Rdfs9_1:
				result = _applyRuleRdfs9_1(mt);
				break;
			case RDFSRules.Rdfs9_2:
				result = _applyRuleRdfs9_2(mt);
				break;
			case RDFSRules.Rdfs10:
				result = _applyRuleRdfs10(mt);
				break;
			case RDFSRules.Rdfs11_1:
				result = _applyRuleRdfs11_1(mt);
				break;
			case RDFSRules.Rdfs11_2:
				result = _applyRuleRdfs11_2(mt);
				break;
			case RDFSRules.Rdfs12:
				result = _applyRuleRdfs12(mt);
				break;
			case RDFSRules.Rdfs13:
				result = _applyRuleRdfs13(mt);
				break;
			case RDFSRules.RX1:
				result = _applyRuleX1(mt);
				break;
			default:
				// FIXME throw exception here?
				break;
		}
		// ThreadLog.trace("Rule " + RDFSRules.RULENAMES[rule] + " inferred " +
		// result + " new triples.");
		return result;
	}

	/*
	 * rdf1. xxx aaa yyy --> aaa rdf:type rdf:Property
	 */
	private int _applyRuleRdf1(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> iter = _newThisIteration.match(null, null, null);

		while (iter.hasNext()) {
			Statement st = iter.next();

			boolean added = con.addInferredStatement(st.getPredicate(), RDF.TYPE, RDF.PROPERTY);

			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs2. 2_1. xxx aaa yyy && (nt) aaa rdfs:domain zzz --> (t1) xxx rdf:type
	 * zzz (t2)
	 */
	private int _applyRuleRdfs2_1(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, null, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource xxx = nt.getSubject();
			URI aaa = nt.getPredicate();

		CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(aaa, RDFS.DOMAIN, null, true);
			
			while (t1Iter.hasNext()) {
				Statement t1 = t1Iter.next();

				Value zzz = t1.getObject();
				if (zzz instanceof Resource) {
					boolean added = con.addInferredStatement(xxx, RDF.TYPE, zzz);
					if (added) {
						nofInferred++;
					}
				}
			}
			t1Iter.close();
		}

		return nofInferred;
	}

	/*
	 * rdfs2. 2_2. aaa rdfs:domain zzz && (nt) xxx aaa yyy --> (t1) xxx rdf:type
	 * zzz (t2)
	 */
	private int _applyRuleRdfs2_2(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, RDFS.DOMAIN, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value zzz = nt.getObject();

			if (aaa instanceof URI && zzz instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(null, (URI)aaa, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();
					boolean added = con.addInferredStatement(xxx, RDF.TYPE, zzz);
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs3. 3_1. xxx aaa uuu && (nt) aaa rdfs:range zzz --> (t1) uuu rdf:type
	 * zzz (t2)
	 */
	private int _applyRuleRdfs3_1(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, null, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			URI aaa = nt.getPredicate();
			Value uuu = nt.getObject();

			if (uuu instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(aaa, RDFS.RANGE, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value zzz = t1.getObject();
					if (zzz instanceof Resource) {
						boolean added = con.addInferredStatement((Resource)uuu, RDF.TYPE, zzz);
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

	/*
	 * rdfs3. 3_2. aaa rdfs:range zzz && (nt) xxx aaa uuu --> (t1) uuu rdf:type
	 * zzz (t2)
	 */
	private int _applyRuleRdfs3_2(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, RDFS.RANGE, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value zzz = nt.getObject();

			if (aaa instanceof URI && zzz instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(null, (URI)aaa, null, true);
				
				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value uuu = t1.getObject();
					if (uuu instanceof Resource) {
						boolean added = con.addInferredStatement((Resource)uuu, RDF.TYPE, zzz);
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

	/*
	 * rdfs4a. xxx aaa yyy --> xxx rdf:type rdfs:Resource
	 */
	private int _applyRuleRdfs4a(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> iter = _newThisIteration.match(null, null, null);

		while (iter.hasNext()) {
			Statement st = iter.next();

			boolean added = con.addInferredStatement(st.getSubject(), RDF.TYPE, RDFS.RESOURCE);
			if (added) {
				nofInferred++;
			}
		}
		
		return nofInferred;
	}

	/*
	 * rdfs4b. xxx aaa uuu --> uuu rdf:type rdfs:Resource
	 */
	private int _applyRuleRdfs4b(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		
		Iterator<Statement> iter = _newThisIteration.match(null, null, null);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Value uuu = st.getObject();
			if (uuu instanceof Resource) {
				boolean added = con.addInferredStatement((Resource)uuu, RDF.TYPE, RDFS.RESOURCE);
				if (added) {
					nofInferred++;
				}
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs5. 5_1. aaa rdfs:subPropertyOf bbb && (nt) bbb rdfs:subPropertyOf ccc
	 * --> (t1) aaa rdfs:subPropertyOf ccc (t2)
	 */
	private int _applyRuleRdfs5_1(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, RDFS.SUBPROPERTYOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value bbb = nt.getObject();

			if (bbb instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements((Resource)bbb, RDFS.SUBPROPERTYOF, null, true);
				
				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value ccc = t1.getObject();
					if (ccc instanceof Resource) {
						boolean added = con.addInferredStatement(aaa, RDFS.SUBPROPERTYOF, ccc);
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

	/*
	 * rdfs5. 5_2. bbb rdfs:subPropertyOf ccc && (nt) aaa rdfs:subPropertyOf bbb
	 * --> (t1) aaa rdfs:subPropertyOf ccc (t2)
	 */
	private int _applyRuleRdfs5_2(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, RDFS.SUBPROPERTYOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource bbb = nt.getSubject();
			Value ccc = nt.getObject();

			if (ccc instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(null,	RDFS.SUBPROPERTYOF, bbb, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource aaa = t1.getSubject();
					boolean added = con.addInferredStatement(aaa, RDFS.SUBPROPERTYOF, ccc);
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs6. xxx rdf:type rdf:Property --> xxx rdfs:subPropertyOf xxx
	 * reflexivity of rdfs:subPropertyOf
	 */
	private int _applyRuleRdfs6(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> iter = _newThisIteration.match(null, RDF.TYPE, RDF.PROPERTY);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();
			boolean added = con.addInferredStatement(xxx, RDFS.SUBPROPERTYOF, xxx);
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs7. 7_1. xxx aaa yyy && (nt) aaa rdfs:subPropertyOf bbb --> (t1) xxx
	 * bbb yyy (t2)
	 */
	private int _applyRuleRdfs7_1(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, null, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource xxx = nt.getSubject();
			URI aaa = nt.getPredicate();
			Value yyy = nt.getObject();

			CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(aaa, RDFS.SUBPROPERTYOF, null, true);

			while (t1Iter.hasNext()) {
				Statement t1 = t1Iter.next();

				Value bbb = t1.getObject();
				if (bbb instanceof URI) {
					boolean added = con.addInferredStatement(xxx, (URI)bbb, yyy);
					if (added) {
						nofInferred++;
					}
				}
			}
			t1Iter.close();
		}

		return nofInferred;
	}

	/*
	 * rdfs7. 7_2. aaa rdfs:subPropertyOf bbb && (nt) xxx aaa yyy --> (t1) xxx
	 * bbb yyy (t2)
	 */
	private int _applyRuleRdfs7_2(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null,	RDFS.SUBPROPERTYOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value bbb = nt.getObject();

			if (aaa instanceof URI && bbb instanceof URI) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(null, (URI)aaa, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();
					Value yyy = t1.getObject();

					boolean added = con.addInferredStatement(xxx, (URI)bbb, yyy);
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs8. xxx rdf:type rdfs:Class --> xxx rdfs:subClassOf rdfs:Resource
	 */
	private int _applyRuleRdfs8(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> iter = _newThisIteration.match(null, RDF.TYPE, RDFS.CLASS);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();

			boolean added = con.addInferredStatement(xxx, RDFS.SUBCLASSOF, RDFS.RESOURCE);
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs9. 9_1. xxx rdfs:subClassOf yyy && (nt) aaa rdf:type xxx --> (t1) aaa
	 * rdf:type yyy (t2)
	 */
	private int _applyRuleRdfs9_1(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, RDFS.SUBCLASSOF,
				null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource xxx = nt.getSubject();
			Value yyy = nt.getObject();

			if (yyy instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(null, RDF.TYPE,
						xxx, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource aaa = t1.getSubject();

					boolean added = con.addInferredStatement(aaa, RDF.TYPE, yyy);
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs9. 9_2. aaa rdf:type xxx && (nt) xxx rdfs:subClassOf yyy --> (t1) aaa
	 * rdf:type yyy (t2)
	 */
	private int _applyRuleRdfs9_2(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, RDF.TYPE, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value xxx = nt.getObject();

			if (xxx instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements((Resource)xxx,
						RDFS.SUBCLASSOF, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value yyy = t1.getObject();

					if (yyy instanceof Resource) {
						boolean added = con.addInferredStatement(aaa, RDF.TYPE, yyy);
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

	/*
	 * rdfs10. xxx rdf:type rdfs:Class --> xxx rdfs:subClassOf xxx reflexivity of
	 * rdfs:subClassOf
	 */
	private int _applyRuleRdfs10(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> iter = _newThisIteration.match(null, RDF.TYPE, RDFS.CLASS);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();

			boolean added = con.addInferredStatement(xxx, RDFS.SUBCLASSOF, xxx);
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs11. 11_1. xxx rdfs:subClassOf yyy && (nt) yyy rdfs:subClassOf zzz -->
	 * (t1) xxx rdfs:subClassOf zzz (t2)
	 */
	private int _applyRuleRdfs11_1(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, RDFS.SUBCLASSOF,
				null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource xxx = nt.getSubject();
			Value yyy = nt.getObject();

			if (yyy instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements((Resource)yyy,
						RDFS.SUBCLASSOF, null, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value zzz = t1.getObject();

					if (zzz instanceof Resource) {
						boolean added = con.addInferredStatement(xxx, RDFS.SUBCLASSOF, zzz);
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

	/*
	 * rdfs11. 11_2. yyy rdfs:subClassOf zzz && (nt) xxx rdfs:subClassOf yyy -->
	 * (t1) xxx rdfs:subClassOf zzz (t2)
	 */
	private int _applyRuleRdfs11_2(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> ntIter = _newThisIteration.match(null, RDFS.SUBCLASSOF,
				null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource yyy = nt.getSubject();
			Value zzz = nt.getObject();

			if (zzz instanceof Resource) {
				CloseableIteration<? extends Statement, SailException> t1Iter = con.getStatements(null,
						RDFS.SUBCLASSOF, yyy, true);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();

					boolean added = con.addInferredStatement(xxx, RDFS.SUBCLASSOF, zzz);
					if (added) {
						nofInferred++;
					}
				}
				t1Iter.close();
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs12. xxx rdf:type rdfs:ContainerMembershipProperty --> xxx
	 * rdfs:subPropertyOf rdfs:member
	 */
	private int _applyRuleRdfs12(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> iter = _newThisIteration.match(null, RDF.TYPE,
				RDFS.CONTAINERMEMBERSHIPPROPERTY);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();

			boolean added = con.addInferredStatement(xxx, RDFS.SUBPROPERTYOF, RDFS.MEMBER);
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	/*
	 * rdfs13. xxx rdf:type rdfs:Datatype --> xxx rdfs:subClassOf rdfs:Literal
	 */
	private int _applyRuleRdfs13(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> iter = _newThisIteration.match(null, RDF.TYPE,
				RDFS.DATATYPE);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();

			boolean added = con.addInferredStatement(xxx, RDFS.SUBCLASSOF, RDFS.LITERAL);
			if (added) {
				nofInferred++;
			}
		}

		return nofInferred;
	}

	/*
	 * X1. xxx rdf:_* yyy --> rdf:_* rdf:type rdfs:ContainerMembershipProperty
	 * This is an extra rule for list membership properties (_1, _2, _3, ...).
	 * The RDF MT does not specificy a production for this.
	 */
	private int _applyRuleX1(InferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		Iterator<Statement> iter = _newThisIteration.match(null, null, null);

		while (iter.hasNext()) {
			Statement st = iter.next();

			URI predNode = st.getPredicate();

			if (predNode.toString().startsWith(RDF.NAMESPACE + "_")) {
				boolean added = con.addInferredStatement(predNode, RDF.TYPE, RDFS.CONTAINERMEMBERSHIPPROPERTY);
				if (added) {
					nofInferred++;
				}
			}
		}

		return nofInferred;
	}
	
	private class NativeStoreRDFSInferencerConnection
	extends InferencerConnectionWrapper
	implements SailConnectionListener
{
	private boolean _statementsRemoved;

	public NativeStoreRDFSInferencerConnection(InferencerConnection con) {
		super(con);
		con.addConnectionListener(this);
	}
	

	public void commit()
		throws SailException
	{
		super.commit();
		if (_statementsRemoved) {
			logger.debug("statements removed, starting inferencing from scratch");
			clearInferred();
			_addAxiomStatements(this);

			_newStatements.clear();
			CloseableIteration<? extends Statement, SailException> stIter = getWrappedConnection().getStatements(null, null, null, true);

			while (stIter.hasNext()) {
				_newStatements.add( stIter.next() );
			}
			stIter.close();
		}

		doInferencing(this);
		super.commit();
	}

	public void statementAdded(Statement st) {
		if (_newStatements == null) {
			_newStatements = new GraphImpl();
		}
		_newStatements.add( st );
	}

	public void statementRemoved(Statement st) {
		_statementsRemoved = true;
	}
} // end inner class MemoryStoreRDFSInferencerTransaction

}
