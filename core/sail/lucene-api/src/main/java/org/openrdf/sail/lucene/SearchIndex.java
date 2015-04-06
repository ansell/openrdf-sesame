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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * A SearchIndex is a one-stop-shop abstraction of a Lucene index. It takes care
 * of proper synchronization of IndexReaders, IndexWriters and IndexSearchers in
 * a way that is suitable for a LuceneSail.
 * 
 * @see LuceneSail
 */
public interface SearchIndex {
	/**
	 * The name of the Document field holding the document identifier. This
	 * consists of the Resource identifier (URI or BNodeID) and the Context ID
	 * (the format is "resourceId|contextId")
	 */
	public static final String ID_FIELD_NAME = "id";

	/**
	 * The name of the Document field holding the Resource identifier. The value
	 * stored in this field is either a URI or a BNode ID.
	 */
	public static final String URI_FIELD_NAME = "uri";

	/**
	 * The name of the Document field that holds multiple text values of a
	 * Resource. The field is called "text", as it contains all text, but was
	 * called "ALL" during the discussion. For each statement-literal of the
	 * resource, the object literal is stored in a field using the
	 * predicate-literal and additionally in a TEXT_FIELD_NAME-literal field. The
	 * reasons are given in the documentation of
	 * {@link #addProperty(String, String, Document)}
	 */
	public static final String TEXT_FIELD_NAME = "text";

	/**
	 * The name of the Document field holding the context identifer(s).
	 */
	public static final String CONTEXT_FIELD_NAME = "context";

	/**
	 * the null context
	 */
	public static final String CONTEXT_NULL = "null";

	/**
	 * String used to prefix BNode IDs with so that we can distinguish BNode
	 * fields from URI fields in Documents. The prefix is chosen so that it is
	 * invalid as a (part of a) URI scheme.
	 */
	public static final String BNODE_ID_PREFIX = "!";

	void initialize(Properties parameters);

	Collection<BindingSet> evaluate(QuerySpec query);

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
	 * Add many statements at the same time, remove many statements at the same
	 * time. Ordering by resource has to be done inside this method. The passed
	 * added/removed sets are disjunct, no statement can be in both
	 * 
	 * @param added
	 *        all added statements, can have multiple subjects
	 * @param removed
	 *        all removed statements, can have multiple subjects
	 */
	void addRemoveStatements(Collection<Statement> added, Collection<Statement> removed)
		throws Exception;

	/**
	 * @param contexts
	 * @param sail
	 *        - the underlying native sail where to read the missing triples from
	 *        after deletion
	 * @throws SailException
	 */
	void clearContexts(Resource[] contexts, Sail sail)
		throws IOException, SailException;

	/**
	 * Add a complete Lucene Document based on these statements. Do not search
	 * for an existing document with the same subject id. (assume the existing
	 * document was deleted)
	 * 
	 * @param statements
	 *        the statements that make up the resource
	 * @throws IOException
	 */
	void addDocuments(Resource subject, List<Statement> statements)
		throws IOException;

	/**
	 * 
	 */
	void clear()
		throws IOException;
}
