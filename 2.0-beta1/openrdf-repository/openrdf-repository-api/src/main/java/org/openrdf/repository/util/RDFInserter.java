/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.util;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
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
	 * The connection to use for the add operations.
	 */
	private RepositoryConnection _con;

	/**
	 * Flag indicating whether the contexts specified for this RDFInserter should
	 * be enforced upon all reported statements.
	 */
	private boolean _enforceContext;

	/**
	 * The contexts to add the statements to. This contexts value is used when
	 * _enforceContext is set to true.
	 */
	private Resource[] _contexts;

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
	 * @param con
	 *        The connection to use for the add operations.
	 */
	public RDFInserter(RepositoryConnection con) {
		_con = con;
		_enforceContext = false;
		_preserveBNodeIDs = true;
		_namespaceMap = new HashMap<String, String>();
		_bNodesMap = new HashMap<String, BNode>();
	}

	/*---------*
	 * Methods *
	 *---------*/

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
	 * Enforces the supplied contexts upon all statements that are reported to
	 * this RDFInserter.
	 * 
	 * @param contexts
	 *        the contexts to use, or null to force no context being used.
	 */
	public void enforceContext(Resource... contexts) {
		_contexts = contexts;
		_enforceContext = true;
	}

	/**
	 * Checks whether this RDFInserter enforces its contexts upon all statements
	 * that are reported to it.
	 * 
	 * @return <tt>true</tt> if it enforces its contexts, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean enforcesContext() {
		return _enforceContext;
	}

	/**
	 * Gets the contexts that this RDFInserter enforces upon all statements that
	 * are reported to it (in case <tt>enforcesContext()</tt> returns
	 * <tt>true</tt>).
	 * 
	 * @return A Resource[] identifying the contexts, or <tt>null</tt> if no
	 *         contexts is enforced.
	 */
	public Resource[] getContexts() {
		return _contexts;
	}

	public void endRDF()
		throws RDFHandlerException
	{
		for (Map.Entry<String, String> entry : _namespaceMap.entrySet()) {
			String prefix = entry.getKey();
			String name = entry.getValue();

			try {
				if (_con.getNamespace(prefix) == null) {
					_con.setNamespace(prefix, name);
				}
			}
			catch (RepositoryException e) {
				throw new RDFHandlerException(e);
			}
		}

		_namespaceMap.clear();
		_bNodesMap.clear();
	}

	public void handleNamespace(String prefix, String name) {
		// FIXME: set namespaces directly when they are properly handled wrt
		// rollback
		// don't replace earlier declarations
		if (prefix != null && prefix.trim().length() > 0 && !_namespaceMap.containsKey(prefix)) {
			_namespaceMap.put(prefix, name);
		}
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();
		Resource ctxt = st.getContext();

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
			if (_enforceContext) {
				_con.add(subj, pred, obj, _contexts);
			}
			else {
				_con.add(subj, pred, obj, ctxt);
			}
		}
		catch (RepositoryException e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * Maps the supplied BNode, which comes from the data, to a new BNode object.
	 * Consecutive calls with equal BNode objects returns the same object
	 * everytime.
	 * 
	 * @throws RepositoryException
	 */
	private BNode _mapBNode(BNode bNode) {
		BNode result = _bNodesMap.get(bNode.getID());

		if (result == null) {
			result = _con.getRepository().getValueFactory().createBNode();
			_bNodesMap.put(bNode.getID(), result);
		}

		return result;
	}
}
