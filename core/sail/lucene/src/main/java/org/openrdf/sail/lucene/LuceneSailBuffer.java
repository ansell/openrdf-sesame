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
package org.openrdf.sail.lucene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;

/**
 * A buffer collecting all transaction operations (triples that need to be
 * added, removed, clear operations) so that they can be executed at once during
 * commit.
 * 
 * @author sauermann
 * @author andriy.nikolov
 */
public class LuceneSailBuffer {

	private static class ContextAwareStatementImpl implements Statement {

		private static final long serialVersionUID = -2976244503679342649L;

		private Statement delegate;

		public ContextAwareStatementImpl(Statement delegate) {
			if (delegate == null)
				throw new RuntimeException("Trying to add/remove a null statement");
			this.delegate = delegate;
		}

		@Override
		public Resource getSubject() {
			return delegate.getSubject();
		}

		@Override
		public IRI getPredicate() {
			return delegate.getPredicate();
		}

		@Override
		public Value getObject() {
			return delegate.getObject();
		}

		@Override
		public Resource getContext() {
			return delegate.getContext();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj instanceof Statement) {
				Statement other = (Statement)obj;

				return this.delegate.equals(other)
						&& ((this.getContext() == null && other.getContext() == null) || (this.getContext() != null
								&& other.getContext() != null && this.getContext().equals(other.getContext())));
			}
			return false;
		}

		@Override
		public int hashCode() {
			return delegate.hashCode() + ((getContext() == null) ? 0 : 29791 * getContext().hashCode());
		}

		@Override
		public String toString() {
			return delegate.toString();
		}

	}

	public static class Operation {

	}

	public static class AddRemoveOperation extends Operation {

		HashSet<Statement> added = new HashSet<Statement>();

		HashSet<Statement> removed = new HashSet<Statement>();

		public void add(Statement s) {
			if (!removed.remove(s))
				added.add(s);
		}

		public void remove(Statement s) {
			if (!added.remove(s))
				removed.add(s);
		}

		/**
		 * @return Returns the added.
		 */
		public HashSet<Statement> getAdded() {
			return added;
		}

		/**
		 * @return Returns the removed.
		 */
		public HashSet<Statement> getRemoved() {
			return removed;
		}

	}

	public static class ClearContextOperation extends Operation {

		Resource[] contexts;

		public ClearContextOperation(Resource[] contexts) {
			this.contexts = contexts;
		}

		/**
		 * @return Returns the contexts.
		 */
		public Resource[] getContexts() {
			return contexts;
		}

	}

	public static class ClearOperation extends Operation {

	}

	private ArrayList<Operation> operations = new ArrayList<Operation>();

	/**
	 * Add this statement to the buffer
	 * 
	 * @param s
	 *        the statement
	 */
	public synchronized void add(Statement s) {
		// check if the last operation was adding/Removing triples
		Operation o = (operations.isEmpty()) ? null : operations.get(operations.size() - 1);
		if ((o == null) || !(o instanceof AddRemoveOperation)) {
			o = new AddRemoveOperation();
			operations.add(o);
		}
		AddRemoveOperation aro = (AddRemoveOperation)o;
		aro.add(new ContextAwareStatementImpl(s));
	}

	/**
	 * Remove this statement to the buffer
	 * 
	 * @param s
	 *        the statement
	 */
	public synchronized void remove(Statement s) {
		// check if the last operation was adding/Removing triples
		Operation o = (operations.isEmpty()) ? null : operations.get(operations.size() - 1);
		if ((o == null) || !(o instanceof AddRemoveOperation)) {
			o = new AddRemoveOperation();
			operations.add(o);
		}
		AddRemoveOperation aro = (AddRemoveOperation)o;
		aro.remove(new ContextAwareStatementImpl(s));
	}

	public synchronized void clear(Resource[] contexts) {
		if ((contexts == null) || (contexts.length == 0))
			operations.add(new ClearOperation());
		else
			operations.add(new ClearContextOperation(contexts));
	}

	/**
	 * Iterator over the operations
	 * 
	 */
	public synchronized Iterator<Operation> operationsIterator() {
		return operations.iterator();
	}

	/**
	 * the list of operations. You must not change it
	 * 
	 */
	public synchronized List<Operation> operations() {
		return operations;
	}

	/**
	 * Optimize will remove any changes that are done before a clear()
	 */
	public void optimize() {
		for (int i = operations.size() - 1; i >= 0; i--) {
			Operation o = operations.get(i);
			if (o instanceof ClearOperation) {
				// remove everything before
				// is is now the size of the operations to be removed
				while (i > 0) {
					operations.remove(i);
					i--;
				}
				return;
			}
		}
	}

	/**
	 * reset the buffer, empty the operations list
	 */
	public void reset() {
		operations.clear();
	}

}
