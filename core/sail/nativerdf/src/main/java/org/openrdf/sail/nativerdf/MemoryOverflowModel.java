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
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.FileUtil;

import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.AbstractModel;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.FilteredModel;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.sail.SailException;
import org.openrdf.sail.base.SailStore;

/**
 * Model implementation that stores in a {@link TreeModel} until more than
 * 10KB statements are added and the estimated memory usage is more than the
 * amount of free memory available. Once the threshold is cross this
 * implementation seamlessly changes to a disk based {@link SailSourceModel}.
 * 
 * @author James Leigh
 * 
 */
abstract class MemoryOverflowModel extends AbstractModel {
	private static final long serialVersionUID = 4119844228099208169L;
	private static final Runtime RUNTIME = Runtime.getRuntime();
	private static final int LARGE_BLOCK = 10000;
	final Logger logger = LoggerFactory.getLogger(MemoryOverflowModel.class);
	private TreeModel memory;
	transient File dataDir;
	transient SailStore store;
	transient SailSourceModel disk;
	private long baseline = 0;
	private long maxBlockSize = 0;

	public MemoryOverflowModel() {
		memory = new TreeModel();
	}

	public MemoryOverflowModel(Model model) {
		this(model.getNamespaces());
		addAll(model);
	}

	public MemoryOverflowModel(Set<Namespace> namespaces,
			Collection<? extends Statement> c) {
		this(namespaces);
		addAll(c);
	}

	public MemoryOverflowModel(Set<Namespace> namespaces) {
		memory = new TreeModel(namespaces);
	}

	@Override
	public synchronized void closeIterator(Iterator<?> iter) {
		super.closeIterator(iter);
		if (disk != null) {
			disk.closeIterator(iter);
		}
	}

	public synchronized Set<Namespace> getNamespaces() {
		return memory.getNamespaces();
	}

	public synchronized Optional<Namespace> getNamespace(String prefix) {
		return memory.getNamespace(prefix);
	}

	public synchronized Namespace setNamespace(String prefix, String name) {
		return memory.setNamespace(prefix, name);
	}

	public void setNamespace(Namespace namespace) {
		memory.setNamespace(namespace);
	}

	public synchronized Optional<Namespace> removeNamespace(String prefix) {
		return memory.removeNamespace(prefix);
	}

	public boolean contains(Resource subj, IRI pred, Value obj,
			Resource... contexts) {
		return getDelegate().contains(subj, pred, obj, contexts);
	}

	public boolean add(Resource subj, IRI pred, Value obj, Resource... contexts) {
		checkMemoryOverflow();
		return getDelegate().add(subj, pred, obj, contexts);
	}

	public boolean remove(Resource subj, IRI pred, Value obj,
			Resource... contexts) {
		return getDelegate().remove(subj, pred, obj, contexts);
	}

	public int size() {
		return getDelegate().size();
	}

	public Iterator<Statement> iterator() {
		return getDelegate().iterator();
	}

	public boolean clear(Resource... contexts) {
		return getDelegate().clear(contexts);
	}

	public Model filter(final Resource subj, final IRI pred, final Value obj,
			final Resource... contexts) {
		return new FilteredModel(this, subj, pred, obj, contexts) {
			private static final long serialVersionUID = -475666402618133101L;

			@Override
			public int size() {
				return getDelegate().filter(subj, pred, obj, contexts).size();
			}

			@Override
			public Iterator<Statement> iterator() {
				return getDelegate().filter(subj, pred, obj, contexts)
						.iterator();
			}

			@Override
			protected void removeFilteredTermIteration(
					Iterator<Statement> iter, Resource subj, IRI pred,
					Value obj, Resource... contexts) {
				MemoryOverflowModel.this.removeTermIteration(iter, subj, pred, obj, contexts);
			}
		};
	}

	@Override
	public synchronized void removeTermIteration(Iterator<Statement> iter,
			Resource subj, IRI pred, Value obj, Resource... contexts) {
		if (disk == null) {
			memory.removeTermIteration(iter, subj, pred, obj, contexts);
		} else {
			disk.removeTermIteration(iter, subj, pred, obj, contexts);
		}
	}

	protected abstract SailStore createSailStore(File dataDir) throws IOException,
	SailException;

	synchronized Model getDelegate() {
		if (disk == null)
			return memory;
		return disk;
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		// Write out any hidden serialization magic
		s.defaultWriteObject();
		// Write in size
		Model delegate = getDelegate();
		s.writeInt(delegate.size());
		// Write in all elements
		for (Statement st : delegate) {
			Resource subj = st.getSubject();
			IRI pred = st.getPredicate();
			Value obj = st.getObject();
			Resource ctx = st.getContext();
			s.writeObject(new ContextStatementImpl(subj, pred, obj, ctx));
		}
	}

	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		// Read in any hidden serialization magic
		s.defaultReadObject();
		// Read in size
		int size = s.readInt();
		// Read in all elements
		for (int i = 0; i < size; i++) {
			add((Statement) s.readObject());
		}
	}

	private synchronized void checkMemoryOverflow() {
		if (disk == null) {
			int size = size();
			if (size >= LARGE_BLOCK && size % LARGE_BLOCK == 0) {
				long totalMemory = RUNTIME.totalMemory();
				long freeMemory = RUNTIME.freeMemory();
				long used = totalMemory - freeMemory;
				if (baseline > 0) {
					long blockSize = used - baseline;
					if (blockSize > maxBlockSize) {
						maxBlockSize = blockSize;
					}
					if (freeMemory < size / LARGE_BLOCK * maxBlockSize) {
						// may not be enough free memory for another
						overflowToDisk();
					}
				}
				baseline = used;
			}
		}
	}

	private synchronized void overflowToDisk() {
		try {
			assert disk == null;
			dataDir = createTempDir("model");
			store = createSailStore(dataDir);
			disk = new SailSourceModel(store) {

				@Override
				protected void finalize() throws Throwable {
					if (disk == this) {
						try {
							store.close();
						} catch (SailException e) {
							logger.error(e.toString(), e);
						} finally {
							FileUtil.deltree(dataDir);
							dataDir = null;
							store = null;
							disk = null;
						}
					}
					super.finalize();
				}
			};
			disk.addAll(memory);
			memory = new TreeModel(memory.getNamespaces());
		} catch (IOException e) {
			logger.error(e.toString(), e);
		} catch (SailException e) {
			logger.error(e.toString(), e);
		}
	}

	private File createTempDir(String name) throws IOException {
		String tmpDirStr = System.getProperty("java.io.tmpdir");
		if (tmpDirStr != null) {
			File tmpDir = new File(tmpDirStr);
			if (!tmpDir.exists()) {
				tmpDir.mkdirs();
			}
		}
		File tmp = File.createTempFile(name, "");
		tmp.delete();
		tmp.mkdir();
		return tmp;
	}

}
