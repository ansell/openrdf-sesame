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
package org.eclipse.rdf4j.model.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.rdf4j.OpenRDFUtil;
import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.util.iterators.FilterIterator;

/**
 * Basic implementation of Graph.
 * 
 * @deprecated since release 2.7.0. Use a {@link org.eclipse.rdf4j.model.Model}
 *             implementation (e.g. {@link TreeModel} or {@link LinkedHashModel}
 *             instead.
 * @author Arjohn Kampman
 */
@Deprecated
public class GraphImpl extends AbstractCollection<Statement> implements Graph {

	private static final long serialVersionUID = -5307095904382050478L;

	protected LinkedList<Statement> statements;

	transient protected ValueFactory valueFactory;

	public GraphImpl(ValueFactory valueFactory) {
		super();
		statements = new LinkedList<Statement>();
		setValueFactory(valueFactory);
	}

	public GraphImpl() {
		this(SimpleValueFactory.getInstance());
	}

	public GraphImpl(ValueFactory valueFactory, Collection<? extends Statement> statements) {
		this(valueFactory);
		addAll(statements);
	}

	public GraphImpl(Collection<? extends Statement> statements) {
		this(SimpleValueFactory.getInstance(), statements);
	}

	public ValueFactory getValueFactory() {
		return valueFactory;
	}

	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public Iterator<Statement> iterator() {
		return statements.iterator();
	}

	@Override
	public int size() {
		return statements.size();
	}

	@Override
	public boolean add(Statement st) {
		return statements.add(st);
	}

	public boolean add(Resource subj, IRI pred, Value obj, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);

		boolean graphChanged = false;

		if (contexts.length == 0) {
			graphChanged = add(valueFactory.createStatement(subj, pred, obj));
		}
		else {
			for (Resource context : contexts) {
				graphChanged |= add(valueFactory.createStatement(subj, pred, obj, context));
			}
		}

		return graphChanged;
	}

	public Iterator<Statement> match(Resource subj, IRI pred, Value obj, Resource... contexts) {
		OpenRDFUtil.verifyContextNotNull(contexts);
		return new PatternIterator(iterator(), subj, pred, obj, contexts);
	}

	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		setValueFactory(SimpleValueFactory.getInstance());
	}

	/*-----------------------------*
	 * Inner class PatternIterator *
	 *-----------------------------*/

	private static class PatternIterator extends FilterIterator<Statement> {

		private Resource subj;

		private IRI pred;

		private Value obj;

		private Resource[] contexts;

		public PatternIterator(Iterator<? extends Statement> iter, Resource subj, IRI pred, Value obj,
				Resource... contexts)
		{
			super(iter);
			this.subj = subj;
			this.pred = pred;
			this.obj = obj;
			this.contexts = contexts;
		}

		@Override
		protected boolean accept(Statement st) {
			if (subj != null && !subj.equals(st.getSubject())) {
				return false;
			}
			if (pred != null && !pred.equals(st.getPredicate())) {
				return false;
			}
			if (obj != null && !obj.equals(st.getObject())) {
				return false;
			}

			if (contexts.length == 0) {
				// Any context matches
				return true;
			}
			else {
				// Accept if one of the contexts from the pattern matches
				Resource stContext = st.getContext();

				for (Resource context : contexts) {
					if (context == null && stContext == null) {
						return true;
					}
					if (context != null && context.equals(stContext)) {
						return true;
					}
				}

				return false;
			}
		}
	}
}
