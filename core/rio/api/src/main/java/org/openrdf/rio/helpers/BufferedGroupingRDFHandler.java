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
package org.openrdf.rio.helpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.Models;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * An {@link RDFHandlerWrapper} that buffers statements internally and passes
 * them to underlying handlers grouped by context, then subject, then predicate.
 * 
 * @author Jeen Broekstra
 */
public class BufferedGroupingRDFHandler extends RDFHandlerWrapper {

	/**
	 * Default buffer size. Buffer size is expressed in number of RDF statements.
	 * The default is set to 1024.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 1024;

	private final int bufferSize;

	private final Model bufferedStatements;

	private final Set<Resource> contexts;

	private final Object bufferLock = new Object();

	/**
	 * Creates a new BufferedGroupedWriter that wraps the supplied handlers,
	 * using the default buffer size.
	 * 
	 * @param handlers
	 *        one or more wrapped RDFHandlers
	 */
	public BufferedGroupingRDFHandler(RDFHandler... handlers) {
		this(DEFAULT_BUFFER_SIZE, handlers);
	}

	/**
	 * Creates a new BufferedGroupedWriter that wraps the supplied handlers,
	 * using the supplied buffer size.
	 * 
	 * @param bufferSize
	 *        size of the buffer expressed in number of RDF statements
	 * @param handlers
	 *        one or more wrapped RDFHandlers
	 */
	public BufferedGroupingRDFHandler(int bufferSize, RDFHandler... handlers) {
		super(handlers);
		this.bufferSize = bufferSize;
		this.bufferedStatements = new LinkedHashModel();
		this.contexts = new HashSet<Resource>();
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		synchronized (bufferLock) {
			bufferedStatements.add(st);
			contexts.add(st.getContext());

			if (bufferedStatements.size() >= this.bufferSize) {
				processBuffer();
			}
		}
	}

	/*
	 * not synchronized, assumes calling method has obtained a lock on bufferLock
	 */
	private void processBuffer()
		throws RDFHandlerException
	{
		// primary grouping per context.
		for (final Resource context : contexts) {
			for (Resource subject : bufferedStatements.filter(null, null, null, context).subjects()) {
				// give rdf:type preference over other predicates.
				for (Statement nextSt : bufferedStatements.filter(subject, RDF.TYPE, null, context)) {
					super.handleStatement(nextSt);
				}

				// retrieve other statement from this context with the same
				// subject, and output them grouped by predicate
				for (URI nextPredicate : bufferedStatements.filter(subject, null, null, context).stream().filter(
						s -> !s.getPredicate().equals(RDF.TYPE)).map(s -> s.getPredicate()).distinct().collect(
						Collectors.toSet()))
				{
					for (Statement nextSt : bufferedStatements.filter(subject, nextPredicate, null, context)) {
						super.handleStatement(nextSt);
					}
				}
			}
		}
		bufferedStatements.clear();
		contexts.clear();
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		synchronized (bufferLock) {
			processBuffer();
		}
		super.endRDF();
	}
}
