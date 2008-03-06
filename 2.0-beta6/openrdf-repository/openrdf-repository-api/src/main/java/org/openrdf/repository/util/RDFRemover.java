/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.util;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * An RDFHandler that removes RDF data from a repository.
 */
public class RDFRemover extends RDFHandlerBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The connection to use for the removal operations.
	 */
	private RepositoryConnection _con;

	/**
	 * Flag indicating whether the context specified for this RDFRemover should
	 * be enforced upon all reported statements.
	 */
	private boolean _enforceContext;

	/**
	 * The context to remove the statements from; <tt>null</tt> to indicate the
	 * null context. This context value is used when _enforceContext is set to
	 * true.
	 */
	private Resource _context;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFRemover object that removes the data from the default
	 * context.
	 * 
	 * @param con
	 *        The connection to use for the removal operations.
	 */
	public RDFRemover(RepositoryConnection con) {
		_con = con;
		_enforceContext = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Enforces the supplied context upon all statements that are reported to
	 * this RDFRemover.
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
	 * Checks whether this RDFRemover enforces its context upon all statements
	 * that are reported to it.
	 * 
	 * @return <tt>true</tt> if it enforces its context, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean enforcesContext() {
		return _enforceContext;
	}

	/**
	 * Gets the context identifier that this RDFRemover enforces upon all
	 * statements that are reported to it (in case <tt>enforcesContext()</tt>
	 * returns <tt>true</tt>).
	 * 
	 * @return A Resource identifying the context, or <tt>null</tt> if the null
	 *         context is enforced.
	 */
	public Resource getContext() {
		return _context;
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		try {
			if (_enforceContext) {
				// Override supplied context info
				_con.remove(st.getSubject(), st.getPredicate(), st.getObject(), _context);
			}
			else {
				_con.remove(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
		catch (RepositoryException e) {
			throw new RDFHandlerException(e);
		}
	}
}
