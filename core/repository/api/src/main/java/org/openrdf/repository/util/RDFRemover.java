/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.repository.util;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.AbstractRDFHandler;

/**
 * An RDFHandler that removes RDF data from a repository.
 */
public class RDFRemover extends AbstractRDFHandler {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The connection to use for the removal operations.
	 */
	private final RepositoryConnection con;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Flag indicating whether the context specified for this RDFRemover should
	 * be enforced upon all reported statements.
	 */
	private boolean enforceContext;

	/**
	 * The context to remove the statements from; <tt>null</tt> to indicate the
	 * null context. This context value is used when enforceContext is set to
	 * true.
	 */
	private Resource context;

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
		this.con = con;
		this.enforceContext = false;
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
		this.context = context;
		enforceContext = true;
	}

	/**
	 * Checks whether this RDFRemover enforces its context upon all statements
	 * that are reported to it.
	 * 
	 * @return <tt>true</tt> if it enforces its context, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean enforcesContext() {
		return enforceContext;
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
		return context;
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		try {
			if (enforceContext) {
				// Override supplied context info
				con.remove(st.getSubject(), st.getPredicate(), st.getObject(), context);
			}
			else {
				con.remove(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
		catch (RepositoryException e) {
			throw new RDFHandlerException(e);
		}
	}
}
