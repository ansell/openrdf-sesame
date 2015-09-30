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
package org.openrdf.rio.helpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.util.GraphUtil;
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

	private final Graph bufferedStatements;

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
		this.bufferedStatements = new GraphImpl();
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
		for (Resource context : contexts) {
			Set<Resource> subjects = GraphUtil.getSubjects(bufferedStatements, null, null, context);
			for (Resource subject : subjects) {
				Set<IRI> processedPredicates = new HashSet<IRI>();

				// give rdf:type preference over other predicates.
				Iterator<Statement> typeStatements = bufferedStatements.match(subject, RDF.TYPE, null, context);
				while (typeStatements.hasNext()) {
					Statement typeStatement = typeStatements.next();
					super.handleStatement(typeStatement);
				}

				processedPredicates.add(RDF.TYPE);

				// retrieve other statement from this context with the same
				// subject, and output them grouped by predicate
				Iterator<Statement> subjectStatements = bufferedStatements.match(subject, null, null, context);
				while (subjectStatements.hasNext()) {
					Statement subjectStatement = subjectStatements.next();
					IRI predicate = subjectStatement.getPredicate();
					if (!processedPredicates.contains(predicate)) {
						Iterator<Statement> toWrite = bufferedStatements.match(subject, predicate, null, context);
						while (toWrite.hasNext()) {
							Statement toWriteSt = toWrite.next();
							super.handleStatement(toWriteSt);
						}
						processedPredicates.add(predicate);
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
