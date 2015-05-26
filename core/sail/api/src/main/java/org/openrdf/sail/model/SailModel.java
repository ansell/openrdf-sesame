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
package org.openrdf.sail.model;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;
import info.aduna.iterator.CloseableIterationIterator;

import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.AbstractModel;
import org.openrdf.model.impl.FilteredModel;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.sail.AdvancedSailConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;


/**
 *
 * @author Mark
 */
public class SailModel extends AbstractModel {
	private static final long serialVersionUID = -2104886971549374410L;

	private transient SailConnection conn;
	private boolean includeInferred;

	public SailModel(SailConnection conn, boolean includeInferred) {
		this.conn = conn;
		this.includeInferred = includeInferred;
	}

	public void setConnection(SailConnection conn) {
		this.conn = conn;
	}

	@Override
	public Set<Namespace> getNamespaces() {
		try {
			return Iterations.asSet(conn.getNamespaces());
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	@Override
	public Namespace getNamespace(String prefix) {
		try {
			String name = conn.getNamespace(prefix);
			return (name != null) ? new NamespaceImpl(prefix, name) : null;
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	@Override
	public Namespace setNamespace(String prefix, String name) {
		try {
			conn.setNamespace(prefix, name);
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
		return new NamespaceImpl(prefix, name);
	}

	@Override
	public void setNamespace(Namespace namespace) {
		try {
			conn.setNamespace(namespace.getPrefix(), namespace.getName());
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	@Override
	public Namespace removeNamespace(String prefix) {
		Namespace namespace = getNamespace(prefix);
		if(namespace != null) {
			try {
				conn.removeNamespace(prefix);
			}
			catch (SailException e) {
				throw new ModelException(e);
			}
		}
		return namespace;
	}

	@Override
	public boolean contains(Resource subj, URI pred, Value obj, Resource... contexts) {
		try {
			if(conn instanceof AdvancedSailConnection) {
				return ((AdvancedSailConnection)conn).hasStatement(subj, pred, obj, includeInferred, contexts);
			}
			else {
				CloseableIteration<? extends Statement,SailException> iter = conn.getStatements(subj, pred, obj, includeInferred, contexts);
				try {
					return iter.hasNext();
				}
				finally {
					iter.close();
				}
			}
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	@Override
	public boolean add(Resource subj, URI pred, Value obj, Resource... contexts) {
		boolean exists = contains(subj, pred, obj, contexts);
		if(!exists) {
			try {
				conn.addStatement(subj, pred, obj, contexts);
			}
			catch (SailException e) {
				throw new ModelException(e);
			}
		}
		return !exists;
	}

	@Override
	public boolean remove(Resource subj, URI pred, Value obj, Resource... contexts) {
		boolean exists = contains(subj, pred, obj, contexts);
		if(exists) {
			try {
				conn.removeStatements(subj, pred, obj, contexts);
			}
			catch (SailException e) {
				throw new ModelException(e);
			}
		}
		return exists;
	}

	@Override
	public Model filter(Resource subj, URI pred, Value obj, Resource... contexts) {
		return new FilteredModel(this, subj, pred, obj, contexts) {
			private static final long serialVersionUID = -3834026632361358191L;

			@Override
			public Iterator<Statement> iterator() {
				return SailModel.this.iterator(subj, pred, obj, contexts);
			}

			@Override
			protected void removeFilteredTermIteration(Iterator<Statement> iter, Resource subj, URI pred,
					Value obj, Resource... contexts)
			{
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void removeTermIteration(Iterator<Statement> iter, Resource subj, URI pred, Value obj,
			Resource... contexts)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Statement> iterator() {
		return iterator(null, null, null);
	}

	private Iterator<Statement> iterator(Resource subj, URI pred, Value obj, Resource... contexts) {
		try {
			Iteration<? extends Statement,?> iter = conn.getStatements(subj, pred, obj, includeInferred, contexts);
			return new CloseableIterationIterator<Statement>(new ExceptionConvertingIteration<Statement,ModelException>(iter)
			{
				@Override
				protected ModelException convert(Exception e) {
					throw new ModelException(e);
				}
			});
		}
		catch (SailException e) {
			throw new ModelException(e);
		}
	}

	@Override
	protected void closeIterator(Iterator<?> iter) {
		if (iter instanceof Closeable) {
			try
			{
				((Closeable)iter).close();
			}
			catch(IOException ioe)
			{
				throw new ModelException(ioe);
			}
		}
		else {
			super.closeIterator(iter);
		}
	}

	@Override
	public int size() {
		if(!includeInferred) {
			try {
				return (int) conn.size();
			}
			catch(SailException e) {
				throw new ModelException(e);
			}
		}
		else {
			Iterator<Statement> iter = iterator();
			try {
				int size = 0;
				while (iter.hasNext()) {
					size++;
					iter.next();
				}
				return size;
			}
			finally {
				closeIterator(iter);
			}
		}
	}
}
