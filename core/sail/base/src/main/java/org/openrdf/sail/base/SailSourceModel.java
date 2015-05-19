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
package org.openrdf.sail.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.IsolationLevels;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.AbstractModel;
import org.openrdf.model.impl.EmptyModel;
import org.openrdf.model.impl.FilteredModel;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.sail.SailException;

/**
 * A {@link Model} that keeps the {@link Statement}s in an {@link SailSource}.
 * 
 * @author James Leigh
 */
public class SailSourceModel extends AbstractModel {

	private final class StatementIterator implements Iterator<Statement> {

		final CloseableIteration<? extends Statement, SailException> stmts;

		Statement last;

		StatementIterator(CloseableIteration<? extends Statement, SailException> closeableIteration) {
			this.stmts = closeableIteration;
		}

		public boolean hasNext() {
			try {
				if (stmts.hasNext())
					return true;
				stmts.close();
				return false;
			}
			catch (SailException e) {
				throw new ModelException(e);
			}
		}

		public Statement next() {
			try {
				last = stmts.next();
				if (last == null) {
					stmts.close();
				}
				return last;
			}
			catch (SailException e) {
				throw new ModelException(e);
			}
		}

		public void remove() {
			if (last == null)
				throw new IllegalStateException("next() not yet called");
			SailSourceModel.this.remove(last);
			last = null;
		}
	}

	final SailSource source;

	SailDataset dataset;

	SailSink sink;

	private long size;

	private final IsolationLevels level = IsolationLevels.NONE;

	public SailSourceModel(SailStore store) {
		this(store.getExplicitSailSource(IsolationLevels.NONE));
	}

	public SailSourceModel(SailSource source) {
		this.source = source;
	}

	@Override
	public void closeIterator(Iterator<?> iter) {
		super.closeIterator(iter);
		if (iter instanceof StatementIterator) {
			try {
				((StatementIterator)iter).stmts.close();
			}
			catch (SailException e) {
				throw new ModelException(e);
			}
		}
	}

	@Override
	public String toString() {
		Iterator<Statement> it = iterator();
		try {
			if (!it.hasNext()) {
				return "[]";
			}

			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for (int i = 0; i < 100; i++) {
				Statement e = it.next();
				sb.append(e == this ? "(this Collection)" : e);
				if (!it.hasNext())
					return sb.append(']').toString();
				sb.append(',').append(' ');
			}
			return sb.toString();
		}
		finally {
			closeIterator(it);
		}
	}

	public synchronized int size() {
		if (size < 0) {
			try {
				SailIteration<? extends Statement> iter = dataset().get(null, null, null);
				try {
					while (iter.hasNext()) {
						iter.next();
						size++;
					}
				}
				finally {
					iter.close();
				}
			}
			catch (SailException e) {
				throw new ModelException(e);
			}
		}
		if (size > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		else {
			return (int)size;
		}
	}

	public Set<Namespace> getNamespaces() {
		Set<Namespace> set = new LinkedHashSet<Namespace>();
		try {
			SailIteration<? extends Namespace> spaces = dataset().getNamespaces();
			try {
				while (spaces.hasNext()) {
					set.add(spaces.next());
				}
			}
			finally {
				spaces.close();
			}
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
		return set;
	}

	public Namespace getNamespace(String prefix) {
		try {
			String name = dataset().getNamespace(prefix);
			return new NamespaceImpl(prefix, name);
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	public Namespace setNamespace(String prefix, String name) {
		try {
			sink().setNamespace(prefix, name);
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
		return new NamespaceImpl(prefix, name);
	}

	public void setNamespace(Namespace namespace) {
		setNamespace(namespace.getPrefix(), namespace.getName());
	}

	public Namespace removeNamespace(String prefix) {
		Namespace ret = getNamespace(prefix);
		try {
			sink().removeNamespace(prefix);
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
		return ret;
	}

	public boolean contains(Resource subj, URI pred, Value obj, Resource... contexts) {
		try {
			if (!isEmptyOrResourcePresent(contexts))
				return false;
			return contains(dataset(), subj, pred, obj, contexts);
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	public synchronized boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
		if (subj == null || pred == null || obj == null)
			throw new UnsupportedOperationException("Incomplete statement");
		try {
			if (contains(dataset, subj, pred, obj, contexts))
				return false;
			if (size >= 0) {
				size++;
			}
			if (contexts == null || contexts.length == 0) {
				sink().approve(subj, pred, obj, null);
			}
			else {
				for (Resource ctx : contexts) {
					sink().approve(subj, pred, obj, ctx);
				}
			}
			return true;
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	public synchronized boolean clear(Resource... contexts) {
		try {
			if (contains(null, null, null, contexts)) {
				sink().clear(contexts);
				size = -1;
				return true;
			}
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
		return false;
	}

	public synchronized boolean remove(Resource subj, URI pred, Value obj, Resource... contexts) {
		try {
			if (contains(subj, pred, obj, contexts)) {
				size = -1;
				SailIteration<? extends Statement> stmts = dataset().get(subj, pred, obj, contexts);
				try {
					while (stmts.hasNext()) {
						Statement st = stmts.next();
						sink().deprecate(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
					}
				}
				finally {
					stmts.close();
				}
				return true;
			}
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
		return false;
	}

	@Override
	public Iterator<Statement> iterator() {
		try {
			return new StatementIterator(dataset().get(null, null, null));
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	public Model filter(final Resource subj, final URI pred, final Value obj, final Resource... contexts) {
		if (!isEmptyOrResourcePresent(contexts))
			return new EmptyModel(this);
		return new FilteredModel(this, subj, pred, obj, contexts) {

			@Override
			public int size() {
				if (subj == null && pred == null && obj == null) {
					try {
						SailIteration<? extends Statement> iter = dataset().get(null, null, null);
						try {
							long size = 0;
							while (iter.hasNext()) {
								iter.next();
								if (size++ >= Integer.MAX_VALUE) {
									return Integer.MAX_VALUE;
								}
							}
							return (int)size;
						}
						finally {
							iter.close();
						}
					}
					catch (SailException e) {
						throw new ModelException(e);
					}
				}
				return super.size();
			}

			@Override
			public Iterator<Statement> iterator() {
				try {
					return new StatementIterator(dataset().get(subj, pred, obj, contexts));
				}
				catch (SailException e) {
					throw new ModelException(e);
				}
			}

			@Override
			protected void removeFilteredTermIteration(Iterator<Statement> iter, Resource subj, URI pred,
					Value obj, Resource... contexts)
			{
				SailSourceModel.this.removeTermIteration(iter, subj, pred, obj, contexts);
			}
		};
	}

	@Override
	public synchronized void removeTermIteration(Iterator<Statement> iter, Resource subj, URI pred, Value obj,
			Resource... contexts)
	{
		try {
			SailIteration<? extends Statement> stmts = dataset().get(subj, pred, obj, contexts);
			try {
				while (stmts.hasNext()) {
					Statement st = stmts.next();
					sink().deprecate(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
				}
			}
			finally {
				stmts.close();
			}
			size = -1;
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	private SailSink sink() throws SailException {
		if (sink == null) {
			sink = source.sink(level);
		}
		return sink;
	}

	private SailDataset dataset() throws SailException {
		if (sink != null) {
			try {
				sink.flush();
			} finally {
				sink.close();
				sink = null;
			}
			if (dataset != null) {
				dataset.close();
				dataset = null;
			}
		}
		if (dataset == null) {
			dataset = source.dataset(level);
		}
		return dataset;
	}

	private boolean contains(SailDataset dataset, Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		if (dataset == null) {
			return false;
		}
		SailIteration<? extends Statement> stmts = dataset.get(subj, pred, obj, contexts);
		try {
			return stmts.hasNext();
		}
		finally {
			stmts.close();
		}
	}

	private boolean isEmptyOrResourcePresent(Value[] contexts) {
		if (contexts instanceof Resource[])
			return true;
		if (contexts == null)
			return true;
		if (contexts.length == 0)
			return true;
		Resource[] result = new Resource[contexts.length];
		for (int i = 0; i < result.length; i++) {
			if (contexts[i] == null || contexts[i] instanceof Resource)
				return true;
		}
		return false;
	}

	Resource[] cast(Value[] contexts) {
		if (contexts instanceof Resource[])
			return (Resource[])contexts;
		if (contexts == null)
			return new Resource[] { null };
		if (contexts.length == 0)
			return new Resource[0];
		Resource[] result = new Resource[contexts.length];
		for (int i = 0; i < result.length; i++) {
			if (contexts[i] == null || contexts[i] instanceof Resource) {
				result[i] = (Resource)contexts[i];
			}
			else {
				List<Resource> list = new ArrayList<Resource>();
				for (Value v : contexts) {
					if (v == null || v instanceof Resource) {
						list.add((Resource)v);
					}
				}
				return list.toArray(new Resource[list.size()]);
			}
		}
		return result;
	}

}
