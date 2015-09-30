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
package org.openrdf.sail.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.sail.SailException;

/**
 * A SearchIndex is a one-stop-shop abstraction of a Lucene index. It takes care
 * of proper synchronization of IndexReaders, IndexWriters and IndexSearchers in
 * a way that is suitable for a LuceneSail.
 * 
 * @see LuceneSail
 */
public interface SearchIndex {
	void initialize(Properties parameters)
		throws Exception;

	/**
	 * To be removed from interface, prefer {@link evaluate(SearchQueryEvaluator query)}.
	 */
	@Deprecated
	Collection<BindingSet> evaluate(QuerySpec query)
		throws SailException;

	Collection<BindingSet> evaluate(SearchQueryEvaluator query)
		throws SailException;

	void beginReading()
		throws IOException;

	void endReading()
		throws IOException;

	void shutDown()
		throws IOException;


	/**
	 * Returns whether the provided literal is accepted by the LuceneIndex to be
	 * indexed. It for instance does not make much since to index xsd:float.
	 * 
	 * @param literal
	 *        the literal to be accepted
	 * @return true if the given literal will be indexed by this LuceneIndex
	 */
	boolean accept(Literal literal);

	/**
	 * Returns true if the given property contains a geometry.
	 */
	boolean isGeoField(String propertyName);

	/**
	 * Begins a transaction.
	 */
	void begin()
		throws IOException;

	/**
	 * Commits any changes done to the LuceneIndex since the last commit. The
	 * semantics is synchronous to SailConnection.commit(), i.e. the LuceneIndex
	 * should be committed/rollbacked whenever the LuceneSailConnection is
	 * committed/rollbacked.
	 */
	void commit()
		throws IOException;

	void rollback()
		throws IOException;

	/**
	 * Indexes the specified Statement.
	 * This should be called from within a begin-commit-rollback block.
	 */
	void addStatement(Statement statement) throws IOException;

	/**
	 * Removes the specified Statement from the indexes.
	 * This should be called from within a begin-commit-rollback block.
	 */
	void removeStatement(Statement statement) throws IOException;

	/**
	 * Add many statements at the same time, remove many statements at the same
	 * time. Ordering by resource has to be done inside this method. The passed
	 * added/removed sets are disjunct, no statement can be in both.
	 * This should be called from within a begin-commit-rollback block.
	 * 
	 * @param added
	 *        all added statements, can have multiple subjects
	 * @param removed
	 *        all removed statements, can have multiple subjects
	 */
	void addRemoveStatements(Collection<Statement> added, Collection<Statement> removed)
		throws IOException;

	/**
	 * This should be called from within a begin-commit-rollback block.
	 * @param contexts
	 * @param sail
	 *        - the underlying native sail where to read the missing triples from
	 *        after deletion
	 * @throws SailException
	 */
	void clearContexts(Resource... contexts)
		throws IOException;

	/**
	 * Add a complete Lucene Document based on these statements. Do not search
	 * for an existing document with the same subject id. (assume the existing
	 * document was deleted).
	 * This should be called from within a begin-commit-rollback block.
	 * 
	 * @param statements
	 *        the statements that make up the resource
	 * @throws IOException
	 */
	void addDocuments(Resource subject, List<Statement> statements)
		throws IOException;

	/**
	 * Clears the indexes.
	 */
	void clear()
		throws IOException;
}
