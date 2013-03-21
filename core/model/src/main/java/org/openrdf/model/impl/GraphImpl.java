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
package org.openrdf.model.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.openrdf.util.iterators.FilterIterator;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Basic implementation of Graph.
 * 
 * @deprecated since release 2.7.0. Use a {@link org.openrdf.model.Model}
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
		this(new ValueFactoryImpl());
	}

	public GraphImpl(ValueFactory valueFactory, Collection<? extends Statement> statements) {
		this(valueFactory);
		addAll(statements);
	}

	public GraphImpl(Collection<? extends Statement> statements) {
		this(new ValueFactoryImpl(), statements);
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

	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
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

	public Iterator<Statement> match(Resource subj, URI pred, Value obj, Resource... contexts) {
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
		setValueFactory(new ValueFactoryImpl());
	}

	/*-----------------------------*
	 * Inner class PatternIterator *
	 *-----------------------------*/

	private static class PatternIterator extends FilterIterator<Statement> {

		private Resource subj;

		private URI pred;

		private Value obj;

		private Resource[] contexts;

		public PatternIterator(Iterator<? extends Statement> iter, Resource subj, URI pred, Value obj,
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
