/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.helpers;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * An RDFHandler that adds RDF data to a repository.
 */
public class RDFInserter extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The transaction to use for the add operations.
	 */
	private SailConnection _con;
	
	private ValueFactory _valueFactory;

	/**
	 * Flag indicating whether the context specified for this RDFInserter should
	 * be enforced upon all reported statements.
	 */
	private boolean _enforceContext;

	/**
	 * The context to add the statements to; <tt>null</tt> to indicate the null
	 * context. This context value is used when _enforceContext is set to true.
	 */
	private Resource _context;

	/**
	 * Flag indicating whether blank node IDs should be preserved.
	 */
	private boolean _preserveBNodeIDs;

	/**
	 * Map that stores namespaces that are reported during the evaluation of the
	 * query. Key is the namespace prefix, value is the namespace name.
	 */
	private Map<String, String> _namespaceMap;

	/**
	 * Map used to keep track of which blank node IDs have been mapped to which
	 * BNode object in case _preserveBNodeIDs is false.
	 */
	private Map<String, BNode> _bNodesMap;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFInserter object that preserves bnode IDs and that does
	 * not enforce any context upon statements that are reported to it.
	 * 
	 * @param txn
	 *        The transaction to use for the add operations.
	 */
	public RDFInserter(SailConnection txn, ValueFactory valueFactory) {
		_con = txn;
		_valueFactory = valueFactory;
		_enforceContext = false;
		_preserveBNodeIDs = true;
		_namespaceMap = new HashMap<String, String>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the connection object that is used by this RDFInserter for the
	 * removal operations.
	 */
	public SailConnection getConnection() {
		return _con;
	}

	/**
	 * Sets whether this RDFInserter should preserve blank node IDs.
	 * 
	 * @param preserveBNodeIDs
	 *        The new value for this flag.
	 */
	public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
		_preserveBNodeIDs = preserveBNodeIDs;
	}

	/**
	 * Checks whether this RDFInserter preserves blank node IDs.
	 */
	public boolean preservesBNodeIDs() {
		return _preserveBNodeIDs;
	}

	/**
	 * Enforces the supplied context upon all statements that are reported to
	 * this RDFInserter.
	 * 
	 * @param context
	 *        A Resource identifying the context, or <tt>null</tt> for the null
	 *        context.
	 */
	public void enforceContext(Resource context) {
		_context = context;
		_enforceContext = true;
	}

	/**
	 * Checks whether this RDFInserter enforces its context upon all statements
	 * that are reported to it.
	 * 
	 * @return <tt>true</tt> if it enforces its context, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean enforcesContext() {
		return _enforceContext;
	}

	/**
	 * Gets the context identifier that this RDFInserter enforces upon all
	 * statements that are reported to it (in case <tt>enforcesContext()</tt>
	 * returns <tt>true</tt>).
	 * 
	 * @return A Resource identifying the context, or <tt>null</tt> if the null
	 *         context is enforced.
	 */
	public Resource getContext() {
		return _context;
	}

	// implements RDFHandler.startRDF()
	public void startRDF()
		throws RDFHandlerException
	{
		if (!_preserveBNodeIDs) {
			_bNodesMap = new HashMap<String, BNode>();
		}
	}

	// implements RDFHandler.endRDF()
	public void endRDF()
		throws RDFHandlerException
	{
		for (Map.Entry<String, String> entry : _namespaceMap.entrySet()) {
			String prefix = entry.getKey();
			String name = entry.getValue();

			try {
				_con.setNamespace(prefix, name);
			}
			catch (SailException e) {
				throw new RDFHandlerException(e);
			}
		}

		_namespaceMap = null;
		_bNodesMap = null;
	}

	// implements RDFHandler.handleNamespace(...)
	public void handleNamespace(String prefix, String name) {
		// don't replace earlier declarations
		if (prefix != null && prefix.trim().length() > 0 && !_namespaceMap.containsKey(prefix)) {
			_namespaceMap.put(prefix, name);
		}
	}

	// implements RDFHandler.handleStatement(...)
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();
		Resource ctxt = _enforceContext ? _context : st.getContext();
		
		if (!_preserveBNodeIDs) {
			if (subj instanceof BNode) {
				subj = _mapBNode((BNode)subj);
			}

			if (obj instanceof BNode) {
				obj = _mapBNode((BNode)obj);
			}
			
			if (!_enforceContext && ctxt instanceof BNode) {
				ctxt = _mapBNode((BNode)ctxt);
			}
		}

		try {
			_con.addStatement(subj, pred, obj, ctxt);
		}
		catch (SailException e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * Maps the supplied BNode, which comes from the data, to a new BNode object.
	 * Consecutive calls with equal BNode objects returns the same object
	 * everytime.
	 */
	private BNode _mapBNode(BNode bNode) {
		BNode result = _bNodesMap.get(bNode.getID());

		if (result == null) {
			result = _valueFactory.createBNode();
			_bNodesMap.put(bNode.getID(), result);
		}

		return result;
	}
}
