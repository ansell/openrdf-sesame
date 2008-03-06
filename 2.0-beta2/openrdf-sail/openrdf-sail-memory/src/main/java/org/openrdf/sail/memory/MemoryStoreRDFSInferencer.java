/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.ForwardChainingRDFSInferencer;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.RDFSRules;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemStatementIterator;
import org.openrdf.sail.memory.model.MemStatementList;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;


public class MemoryStoreRDFSInferencer extends ForwardChainingRDFSInferencer {
	final Logger logger = LoggerFactory.getLogger(this.getClass()); 

	private MemStatementList _newThisIteration;
	private MemStatementList _newStatements;

	public MemoryStoreRDFSInferencer() {
		super();
	}

	public MemoryStoreRDFSInferencer(Sail baseSail) {
		super(baseSail);
	}

	protected void _prepareIteration() {
		super._prepareIteration();
		_newThisIteration = _newStatements;
		_newStatements = new MemStatementList();
	}

	public boolean hasNewStatements() {
		return _newStatements != null && !_newStatements.isEmpty();
	}

	public SailConnection getConnection()
		throws SailException
	{
		try {
			InferencerConnection con =
					(InferencerConnection)super.getConnection();
			return new MemoryStoreRDFSInferencerConnection(con);
		}
		catch (ClassCastException e) {
			throw new SailException(e);
		}
	}

	protected int _applyRule(ForwardChainingInferencerConnection con, int rule)
		throws SailException
	{
		int result = 0;
		MemoryStoreRDFSInferencerConnection mt = (MemoryStoreRDFSInferencerConnection)con;

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
//		ThreadLog.trace("Rule " + RDFSRules.RULENAMES[rule] + " inferred " + result + " new triples.");
		return result;
	}

	/* rdf1.
	 * xxx aaa yyy --> aaa rdf:type rdf:Property
	 */
	private int _applyRuleRdf1(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, null, null);

		while (iter.hasNext()) {
			Statement st = iter.next();

			boolean added = con._addInferredStatement(st.getPredicate(), RDF.TYPE, RDF.PROPERTY);

			if (added) {
				nofInferred++;
			}
		}
		iter.close();

		return nofInferred;
	}

	/* rdfs2.
	 * 2_1. xxx aaa yyy &&           (nt)
	 *      aaa rdfs:domain zzz -->  (t1)
	 *      xxx rdf:type zzz         (t2)
	 */
	private int _applyRuleRdfs2_1(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, null, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource xxx = nt.getSubject();
			URI aaa = nt.getPredicate();

			CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(aaa, RDFS.DOMAIN, null);

			while (t1Iter.hasNext()) {
				Statement t1 = t1Iter.next();

				Value zzz = t1.getObject();
				if (zzz instanceof Resource) {
					boolean added = con._addInferredStatement(xxx, RDF.TYPE, zzz);
					if (added) {
						nofInferred++;
					}
				}
			}

			t1Iter.close();
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs2.
	 * 2_2. aaa rdfs:domain zzz &&  (nt)
	 *     xxx aaa yyy -->         (t1)
	 *     xxx rdf:type zzz        (t2)
	 */
	private int _applyRuleRdfs2_2(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDFS.DOMAIN, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value zzz = nt.getObject();

			if (aaa instanceof URI && zzz instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(null, (URI)aaa, null);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();
					boolean added = con._addInferredStatement(xxx, RDF.TYPE, zzz);
					if (added) {
						nofInferred++;
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs3.
	 * 3_1. xxx aaa uuu &&          (nt)
	 *     aaa rdfs:range zzz -->  (t1)
	 *     uuu rdf:type zzz        (t2)
	 */
	private int _applyRuleRdfs3_1(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, null, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			URI aaa = nt.getPredicate();
			Value uuu = nt.getObject();

			if (uuu instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(aaa, RDFS.RANGE, null);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value zzz = t1.getObject();
					if (zzz instanceof Resource) {
						boolean added = con._addInferredStatement((Resource)uuu, RDF.TYPE, zzz);
						if (added) {
							nofInferred++;
						}
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs3.
	 * 3_2. aaa rdfs:range zzz &&  (nt)
	 *     xxx aaa uuu -->        (t1)
	 *     uuu rdf:type zzz       (t2)
	 */
	private int _applyRuleRdfs3_2(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDFS.RANGE, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value zzz = nt.getObject();

			if (aaa instanceof URI && zzz instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(null, (URI)aaa, null);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value uuu = t1.getObject();
					if (uuu instanceof Resource) {
						boolean added = con._addInferredStatement((Resource)uuu, RDF.TYPE, zzz);
						if (added) {
							nofInferred++;
						}
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;

	}

	/* rdfs4a.
	 * xxx aaa yyy --> xxx rdf:type rdfs:Resource
	 */
	private int _applyRuleRdfs4a(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, null, null);

		while (iter.hasNext()) {
			Statement st = iter.next();

			boolean added = con._addInferredStatement(st.getSubject(), RDF.TYPE, RDFS.RESOURCE);
			if (added) {
				nofInferred++;
			}
		}

		iter.close();

		return nofInferred;
	}

	/* rdfs4b.
	 * xxx aaa uuu --> uuu rdf:type rdfs:Resource
	 */
	private int _applyRuleRdfs4b(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, null, null);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Value uuu = st.getObject();
			if (uuu instanceof Resource) {
				boolean added = con._addInferredStatement((Resource)uuu, RDF.TYPE, RDFS.RESOURCE);
				if (added) {
					nofInferred++;
				}
			}
		}

		iter.close();

		return nofInferred;
	}

	/* rdfs5.
	 * 5_1. aaa rdfs:subPropertyOf bbb &&   (nt)
	 *     bbb rdfs:subPropertyOf ccc -->  (t1)
	 *     aaa rdfs:subPropertyOf ccc      (t2)
	 */
	private int _applyRuleRdfs5_1(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDFS.SUBPROPERTYOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value bbb = nt.getObject();

			if (bbb instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements((Resource)bbb, RDFS.SUBPROPERTYOF, null);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value ccc = t1.getObject();
					if (ccc instanceof Resource) {
						boolean added = con._addInferredStatement(aaa, RDFS.SUBPROPERTYOF, ccc);
						if (added) {
							nofInferred++;
						}
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs5.
	 * 5_2. bbb rdfs:subPropertyOf ccc && (nt)
	 *     aaa rdfs:subPropertyOf bbb -->  (t1)
	 *     aaa rdfs:subPropertyOf ccc      (t2)
	 */
	private int _applyRuleRdfs5_2(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDFS.SUBPROPERTYOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource bbb = nt.getSubject();
			Value ccc = nt.getObject();

			if (ccc instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(null, RDFS.SUBPROPERTYOF, bbb);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource aaa = t1.getSubject();
					boolean added = con._addInferredStatement(aaa, RDFS.SUBPROPERTYOF, ccc);
					if (added) {
						nofInferred++;
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs6.
	 * xxx rdf:type rdf:Property --> xxx rdfs:subPropertyOf xxx
	 * reflexivity of rdfs:subPropertyOf
	 */
	private int _applyRuleRdfs6(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, RDF.TYPE, RDF.PROPERTY);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();
			boolean added = con._addInferredStatement(xxx, RDFS.SUBPROPERTYOF, xxx);
			if (added) {
				nofInferred++;
			}
		}

		iter.close();

		return nofInferred;
	}

	/* rdfs7.
	 * 7_1. xxx aaa yyy &&                  (nt)
	 *     aaa rdfs:subPropertyOf bbb -->  (t1)
	 *     xxx bbb yyy                     (t2)
	 */
	private int _applyRuleRdfs7_1(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, null, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource xxx = nt.getSubject();
			URI aaa = nt.getPredicate();
			Value yyy = nt.getObject();

			CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(aaa, RDFS.SUBPROPERTYOF, null);

			while (t1Iter.hasNext()) {
				Statement t1 = t1Iter.next();

				Value bbb = t1.getObject();
				if (bbb instanceof URI) {
					boolean added = con._addInferredStatement(xxx, (URI)bbb, yyy);
					if (added) {
						nofInferred++;
					}
				}
			}

			t1Iter.close();
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs7.
	 * 7_2. aaa rdfs:subPropertyOf bbb &&  (nt)
	 *     xxx aaa yyy -->                (t1)
	 *     xxx bbb yyy                    (t2)
	 */
	private int _applyRuleRdfs7_2(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDFS.SUBPROPERTYOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value bbb = nt.getObject();

			if (aaa instanceof URI && bbb instanceof URI) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(null, (URI)aaa, null);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();
					Value yyy = t1.getObject();

					boolean added = con._addInferredStatement(xxx, (URI)bbb, yyy);
					if (added) {
						nofInferred++;
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs8.
	 *  xxx rdf:type rdfs:Class --> xxx rdfs:subClassOf rdfs:Resource */
	private int _applyRuleRdfs8(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, RDF.TYPE, RDFS.CLASS);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();

			boolean added = con._addInferredStatement(xxx, RDFS.SUBCLASSOF, RDFS.RESOURCE);
			if (added) {
				nofInferred++;
			}
		}

		iter.close();

		return nofInferred;
	}

	/* rdfs9.
	 * 9_1. xxx rdfs:subClassOf yyy &&  (nt)
	 *     aaa rdf:type xxx -->        (t1)
	 *     aaa rdf:type yyy            (t2)
	 */
	private int _applyRuleRdfs9_1(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDFS.SUBCLASSOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource xxx = nt.getSubject();
			Value yyy = nt.getObject();

			if (yyy instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(null, RDF.TYPE, xxx);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource aaa = t1.getSubject();

					boolean added = con._addInferredStatement(aaa, RDF.TYPE, yyy);
					if (added) {
						nofInferred++;
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs9.
	 * 9_2. aaa rdf:type xxx &&          (nt)
	 *     xxx rdfs:subClassOf yyy -->  (t1)
	 *     aaa rdf:type yyy             (t2)
	 */
	private int _applyRuleRdfs9_2(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDF.TYPE, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource aaa = nt.getSubject();
			Value xxx = nt.getObject();

			if (xxx instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements((Resource)xxx, RDFS.SUBCLASSOF, null);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value yyy = t1.getObject();

					if (yyy instanceof Resource) {
						boolean added = con._addInferredStatement(aaa, RDF.TYPE, yyy);
						if (added) {
							nofInferred++;
						}
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs10.
	 * xxx rdf:type rdfs:Class --> xxx rdfs:subClassOf xxx
	 * reflexivity of rdfs:subClassOf
	 */
	private int _applyRuleRdfs10(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, RDF.TYPE, RDFS.CLASS);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();

			boolean added = con._addInferredStatement(xxx, RDFS.SUBCLASSOF, xxx);
			if (added) {
				nofInferred++;
			}
		}

		iter.close();

		return nofInferred;
	}

	/* rdfs11.
	 * 11_1. xxx rdfs:subClassOf yyy &&  (nt)
	 *     yyy rdfs:subClassOf zzz -->  (t1)
	 *     xxx rdfs:subClassOf zzz      (t2)
	 */
	private int _applyRuleRdfs11_1(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDFS.SUBCLASSOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource xxx = nt.getSubject();
			Value yyy = nt.getObject();

			if (yyy instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements((Resource)yyy, RDFS.SUBCLASSOF, null);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Value zzz = t1.getObject();

					if (zzz instanceof Resource) {
						boolean added = con._addInferredStatement(xxx, RDFS.SUBCLASSOF, zzz);
						if (added) {
							nofInferred++;
						}
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs11.
	 * 11_2. yyy rdfs:subClassOf zzz &&  (nt)
	 *     xxx rdfs:subClassOf yyy -->  (t1)
	 *     xxx rdfs:subClassOf zzz      (t2)
	 */
	private int _applyRuleRdfs11_2(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> ntIter = _iterateNewStatements(null, RDFS.SUBCLASSOF, null);

		while (ntIter.hasNext()) {
			Statement nt = ntIter.next();

			Resource yyy = nt.getSubject();
			Value zzz = nt.getObject();

			if (zzz instanceof Resource) {
				CloseableIteration<MemStatement, SailException> t1Iter = _iterateAllStatements(null, RDFS.SUBCLASSOF, yyy);

				while (t1Iter.hasNext()) {
					Statement t1 = t1Iter.next();

					Resource xxx = t1.getSubject();

					boolean added = con._addInferredStatement(xxx, RDFS.SUBCLASSOF, zzz);
					if (added) {
						nofInferred++;
					}
				}

				t1Iter.close();
			}
		}

		ntIter.close();

		return nofInferred;
	}

	/* rdfs12.
	 * xxx rdf:type rdfs:ContainerMembershipProperty -->
	 *     xxx rdfs:subPropertyOf rdfs:member
	 */
	private int _applyRuleRdfs12(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, RDF.TYPE, RDFS.CONTAINERMEMBERSHIPPROPERTY);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();

			boolean added = con._addInferredStatement(xxx, RDFS.SUBPROPERTYOF, RDFS.MEMBER);
			if (added) {
				nofInferred++;
			}
		}

		iter.close();

		return nofInferred;
	}

	/* rdfs13.
	 * xxx rdf:type rdfs:Datatype --> xxx rdfs:subClassOf rdfs:Literal
	 */
	private int _applyRuleRdfs13(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, RDF.TYPE, RDFS.DATATYPE);

		while (iter.hasNext()) {
			Statement st = iter.next();

			Resource xxx = st.getSubject();

			boolean added = con._addInferredStatement(xxx, RDFS.SUBCLASSOF, RDFS.LITERAL);
			if (added) {
				nofInferred++;
			}
		}

		iter.close();

		return nofInferred;
	}

	/* X1. xxx rdf:_* yyy -->
	 *     rdf:_* rdf:type rdfs:ContainerMembershipProperty
	 *
	 * This is an extra rule for list membership properties (_1, _2, _3,
	 * ...). The RDF MT does not specificy a production for this.
	 */
	private int _applyRuleX1(MemoryStoreRDFSInferencerConnection con)
		throws SailException
	{
		int nofInferred = 0;

		CloseableIteration<MemStatement, SailException> iter = _iterateNewStatements(null, null, null);

		while (iter.hasNext()) {
			Statement st = iter.next();

			URI predNode = st.getPredicate();

			if (predNode.toString().startsWith(RDF.NAMESPACE + "_")) {
				boolean added = con._addInferredStatement(predNode, RDF.TYPE, RDFS.CONTAINERMEMBERSHIPPROPERTY);
				if (added) {
					nofInferred++;
				}
			}
		}

		iter.close();

		return nofInferred;
	}

	private CloseableIteration<MemStatement, SailException> _iterateNewStatements(
		Resource subj, URI pred, Value obj)
	{
		// Map supplied values to values used in the memory store
		MemValueFactory factory = (MemValueFactory)getValueFactory();

		MemResource memSubj = null;
		if (subj != null) {
			memSubj = factory.getMemResource(subj);
		}

		MemURI memPred = null;
		if (pred != null) {
			memPred = factory.getMemURI(pred);
		}

		MemValue memObj = null;
		if (obj != null) {
			memObj = factory.getMemValue(obj);
		}

		return new MemStatementIterator<SailException>(_newThisIteration,
				memSubj, memPred, memObj, false, ReadMode.TRANSACTION);
	}

	private CloseableIteration<MemStatement, SailException> _iterateAllStatements(
		Resource subj, URI pred, Value obj)
	{
		MemoryStore store = (MemoryStore)getBaseSail();
		return store.createStatementIterator(SailException.class,
				subj, pred, obj, false, ReadMode.TRANSACTION);
	}

	/*-------------------------------------------------*
	 * Inner class MemoryStoreRDFSInferencerConnection *
	 *-------------------------------------------------*/

	private class MemoryStoreRDFSInferencerConnection
		extends ForwardChainingInferencerConnection
		implements SailConnectionListener
	{
		private boolean _statementsRemoved;

		public MemoryStoreRDFSInferencerConnection(InferencerConnection con) {
			super(con);
			con.addConnectionListener(this);
		}

		public void commit()
			throws SailException
		{
			if (_statementsRemoved) {
				logger.debug("statements removed, starting inferencing from scratch");
				_clearInferred();
				_addAxiomStatements(this);

				_newStatements.clear();
				CloseableIteration<MemStatement, SailException> stIter = _iterateAllStatements(null, null, null);

				while (stIter.hasNext()) {
					_newStatements.add( stIter.next() );
				}
			}

			doInferencing(this);

			super.commit();
		}

		protected boolean _addInferredStatement(Resource subj, URI pred, Value obj)
			throws SailException
		{
			MemoryStoreConnection mt = (MemoryStoreConnection)getWrappedConnection();
			return mt.addInferredStatement(subj, pred, obj, null);
		}

		public void _clearInferred()
			throws SailException
		{
			((MemoryStoreConnection)getWrappedConnection()).clearInferred();
		}

		public void statementAdded(Statement st) {
			if (_newStatements == null) {
				_newStatements = new MemStatementList();
			}
			_newStatements.add( (MemStatement)st );
		}

		public void statementRemoved(Statement st) {
			_statementsRemoved = true;
		}
	} // end inner class MemoryStoreRDFSInferencerTransaction
}
