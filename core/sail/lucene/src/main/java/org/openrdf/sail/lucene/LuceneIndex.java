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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;

/**
 * A LuceneIndex is a one-stop-shop abstraction of a Lucene index. It takes care
 * of proper synchronization of IndexReaders, IndexWriters and IndexSearchers in
 * a way that is suitable for a LuceneSail.
 * 
 * @see LuceneSail
 */
public class LuceneIndex {
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

	private static final List<String> REJECTED_DATATYPES = new ArrayList<String>();

	static {
		REJECTED_DATATYPES.add("http://www.w3.org/2001/XMLSchema#float");
	};

	static {
		// do NOT set this to Integer.MAX_VALUE, because this breaks fuzzy queries
		BooleanQuery.setMaxClauseCount(1024 * 1024);
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The Directory that holds the Lucene index files.
	 */
	private final Directory directory;

	/**
	 * The Analyzer used to tokenize strings and queries.
	 */
	private final Analyzer analyzer;

	private final Analyzer queryAnalyzer;

	/**
	 * The IndexWriter that can be used to alter the index' contents. Created
	 * lazily.
	 */
	private IndexWriter indexWriter;

	/**
	 * This holds IndexReader and IndexSearcher.
	 */
	protected ReaderMonitor currentMonitor;

	/**
	 * keep a lit of old monitors that are still iterating but not closed (open
	 * iterators), will be all closed on shutdown items are removed from list by
	 * ReaderMnitor.endReading() when closing
	 */
	protected final Collection<ReaderMonitor> oldmonitors = new LinkedList<ReaderMonitor>();

	/**
	 * Creates a new LuceneIndex.
	 * 
	 * @param directory
	 *        The Directory in which an index can be found and/or in which index
	 *        files are written.
	 * @param analyzer
	 *        The Analyzer that will be used for tokenizing strings to index and
	 *        queries.
	 * @throws IOException
	 *         When the Directory could not be unlocked.
	 */
	public LuceneIndex(Directory directory, Analyzer analyzer)
		throws IOException
	{
		this.directory = directory;
		this.analyzer = analyzer;
		this.queryAnalyzer = new StandardAnalyzer();

		// get rid of any locks that may have been left by previous (crashed)
		// sessions
		if (IndexWriter.isLocked(directory)) {
			logger.info("unlocking directory {}", directory);
			IndexWriter.unlock(directory);
		}

		// do some initialization for new indices
		if (!DirectoryReader.indexExists(directory)) {
			logger.info("creating new Lucene index in directory {}", directory);
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
			indexWriterConfig.setOpenMode(OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
			writer.close();
		}
	}

	// //////////////////////////////// Setters and getters

	public Directory getDirectory() {
		return directory;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	// //////////////////////////////// Methods for controlled index access
	// For quick'n'easy access to reader, the indexreader is returned directly
	// result LuceneQueryIterators use the more elaborate
	// ReaderMonitor directly to be able to close the reader when they
	// are done.

	public IndexReader getIndexReader()
		throws IOException
	{
		return getIndexSearcher().getIndexReader();
	}

	public IndexSearcher getIndexSearcher()
		throws IOException
	{
		return getCurrentMonitor().getIndexSearcher();
	}

	/**
	 * Current monitor holds instance of IndexReader and IndexSearcher It is used
	 * to keep track of readers
	 */
	public ReaderMonitor getCurrentMonitor() {
		if (currentMonitor == null)
			currentMonitor = new ReaderMonitor(this, directory);
		return currentMonitor;
	}

	public IndexWriter getIndexWriter()
		throws IOException
	{

		if (indexWriter == null) {
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
			indexWriter = new IndexWriter(directory, indexWriterConfig);
		}
		return indexWriter;
	}

	public void shutDown()
		throws IOException
	{
		// try-finally setup ensures that closing of an instance is not skipped
		// when an earlier instance resulted in an IOException
		// FIXME: is there a more elegant way to ensure this?

		// This close oldMonitors which hold InderReader and IndexSeracher
		// Monitor close IndexReader and IndexSearcher
		if (currentMonitor != null) {
			currentMonitor.doClose();
			currentMonitor = null;
		}
		if (oldmonitors.size() > 0) {
			logger.warn(
					"LuceneSail: On shutdown {} IndexReaders were not closed. This is due to non-closed Query Iterators, which must be closed!",
					oldmonitors.size());
		}
		for (ReaderMonitor monitor : oldmonitors) {
			monitor.doClose();
		}
		oldmonitors.clear();

		try {
			if (indexWriter != null) {
				indexWriter.close();
			}
		}
		finally {
			indexWriter = null;
		}
	}

	// //////////////////////////////// Methods for updating the index

	/**
	 * Indexes the specified Statement.
	 */
	public synchronized void addStatement(Statement statement)
		throws IOException
	{
		// determine stuff to store
		Value object = statement.getObject();
		if (!(object instanceof Literal)) {
			return;
		}

		String field = statement.getPredicate().toString();
		String text = ((Literal)object).getLabel();
		String context = getContextID(statement.getContext());
		boolean updated = false;
		IndexWriter writer = null;

		// fetch the Document representing this Resource
		String resourceId = getResourceID(statement.getSubject());
		String contextId = getContextID(statement.getContext());

		String id = formIdString(resourceId, contextId);
		Term idTerm = new Term(ID_FIELD_NAME, id);
		Document document = getDocument(idTerm);

		if (document == null) {
			// there is no such Document: create one now
			document = new Document();
			addID(id, document);
			addResourceID(resourceId, document);
			// add context
			addContext(context, document);

			addProperty(field, text, document);

			// add it to the index
			writer = getIndexWriter();
			writer.addDocument(document);
			updated = true;
		}
		else {
			// update this Document when this triple has not been stored already
			if (!hasProperty(field, text, document)) {
				// create a copy of the old document; updating the retrieved
				// Document instance works ok for stored properties but indexed data
				// gets lots when doing an IndexWriter.updateDocument with it
				Document newDocument = new Document();

				// add all existing fields (including id, uri, context, and text)
				for (Object oldFieldObject : document.getFields()) {
					Field oldField = (Field)oldFieldObject;
					newDocument.add(oldField);
				}

				// add the new triple to the cloned document
				addProperty(field, text, newDocument);

				// update the index with the cloned document
				writer = getIndexWriter();
				writer.updateDocument(idTerm, newDocument);
				updated = true;
			}
		}

		if (updated) {
			// make sure that these updates are visible for new
			// IndexReaders/Searchers
			writer.commit();

			// the old IndexReaders/Searchers are not outdated
			invalidateReaders();
		}
	}

	/**
	 * Returns whether the provided literal is accepted by the LuceneIndex to be
	 * indexed. It for instance does not make much since to index xsd:float.
	 * 
	 * @param literal
	 *        the literal to be accepted
	 * @return true if the given literal will be indexed by this LuceneIndex
	 */
	public boolean accept(Literal literal) {
		// we reject null literals
		if (literal == null)
			return false;

		// we reject literals that are in the list of rejected data types
		if ((literal.getDatatype() != null)
				&& (REJECTED_DATATYPES.contains(literal.getDatatype().stringValue())))
			return false;

		return true;
	}

	/**
	 * Add the "context" value to the doc
	 * 
	 * @param context
	 *        the context or null, if null-context
	 * @param document
	 *        the document
	 * @param ifNotExists
	 *        check if this context exists
	 */
	private void addContext(String context, Document document) {
		if (context != null) {
			document.add(new StoredField(CONTEXT_FIELD_NAME, context));
		}
	}

	/**
	 * Returns the String ID corresponding with the specified Resource. The id
	 * string is either the URI or a bnode prefixed with a "!".
	 */
	private String getResourceID(Resource resource) {
		if (resource instanceof URI) {
			return resource.toString();
		}
		else if (resource instanceof BNode) {
			return BNODE_ID_PREFIX + ((BNode)resource).getID();
		}
		else {
			throw new IllegalArgumentException("Unknown Resource type: " + resource);
		}
	}

	/**
	 * Get the ID for a context. Context can be null, then the "null" string is
	 * returned
	 * 
	 * @param resource
	 *        the context
	 * @return a string
	 */
	private String getContextID(Resource resource) {
		if (resource == null)
			return CONTEXT_NULL;
		else
			return getResourceID(resource);
	}

	/**
	 * Returns a Document representing the specified document ID (combination of
	 * resource and context), or null when no such Document exists yet.
	 */
	private Document getDocument(Term idTerm)
		throws IOException
	{
		IndexReader reader = getIndexReader();
		TermDocs termDocs = reader.termDocs(idTerm);

		try {
			if (termDocs.next()) {
				// return the Document and make sure there are no others
				int docNr = termDocs.doc();
				if (termDocs.next()) {
					throw new RuntimeException("Multiple Documents for resource " + idTerm.text());
				}

				return reader.document(docNr);
			}
			else {
				// no such Document
				return null;
			}
		}
		finally {
			termDocs.close();
		}
	}

	private String formIdString(String resourceId, String contextId) {
		StringBuilder idBuilder = new StringBuilder(resourceId);
		idBuilder.append("|");
		idBuilder.append(contextId);
		return idBuilder.toString();
	}

	private Term formIdTerm(String resourceId, String contextId) {
		return new Term(ID_FIELD_NAME, formIdString(resourceId, contextId));
	}

	/**
	 * Returns a list of Documents representing the specified Resource (empty
	 * when no such Document exists yet). Each document represent a set of
	 * statements with the specified Resource as a subject, which are stored in a
	 * specific context
	 */
	private List<Document> getDocuments(Term uriTerm)
		throws IOException
	{

		List<Document> result = new LinkedList<Document>();

		IndexReader reader = getIndexReader();
		TermDocs termDocs = reader.termDocs(uriTerm);

		try {
			while (termDocs.next()) {
				int docNr = termDocs.doc();
				result.add(reader.document(docNr));
			}
		}
		finally {
			termDocs.close();
		}

		return result;
	}

	/**
	 * Returns a Document representing the specified Resource & Context
	 * combination, or null when no such Document exists yet.
	 */
	public Document getDocument(Resource subject, Resource context)
		throws IOException
	{
		// fetch the Document representing this Resource
		String resourceId = getResourceID(subject);
		String contextId = getContextID(context);
		Term idTerm = formIdTerm(resourceId, contextId);
		return getDocument(idTerm);
	}

	/**
	 * Returns a list of Documents representing the specified Resource (empty
	 * when no such Document exists yet). Each document represent a set of
	 * statements with the specified Resource as a subject, which are stored in a
	 * specific context
	 */
	public List<Document> getDocuments(Resource subject)
		throws IOException
	{
		String resourceId = getResourceID(subject);
		Term uriTerm = new Term(URI_FIELD_NAME, resourceId);
		return getDocuments(uriTerm);
	}

	/**
	 * Checks whether a field occurs with a specified value in a Document.
	 */
	private boolean hasProperty(String fieldName, String value, Document document) {
		IndexableField[] fields = document.getFields(fieldName);
		if (fields != null) {
			for (IndexableField field : fields) {
				if (value.equals(field.stringValue())) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Determines whether the specified field name is a property field name.
	 */
	private boolean isPropertyField(String fieldName) {
		return !ID_FIELD_NAME.equals(fieldName) && !URI_FIELD_NAME.equals(fieldName)
				&& !TEXT_FIELD_NAME.equals(fieldName) && !CONTEXT_FIELD_NAME.equals(fieldName);
	}

	/**
	 * Determines the number of properties stored in a Document.
	 */
	private int numberOfPropertyFields(Document document) {
		// count the properties that are NOT id nor context nor text
		int propsize = 0;
		for (Object o : document.getFields()) {
			Field f = (Field)o;
			if (isPropertyField(f.name()))
				propsize++;
		}
		return propsize;
	}

	/**
	 * Filters the given list of fields, retaining all property fields.
	 */
	public IndexableField[] getPropertyFields(List<IndexableField> fields) {
		List<IndexableField> result = new ArrayList<IndexableField>();
		for (IndexableField field : fields) {
			if (isPropertyField(field.name()))
				result.add(field);
		}
		return result.toArray(new IndexableField[result.size()]);
	}

	/**
	 * Stores and indexes an ID in a Document.
	 */
	private void addID(String id, Document document) {
		document.add(new StringField(ID_FIELD_NAME, id, Field.Store.YES));
	}

	/**
	 * Stores and indexes the resource ID in a Document.
	 */
	private void addResourceID(String resourceId, Document document) {
		document.add(new StoredField(URI_FIELD_NAME, resourceId));
	}

	private String getLiteralPropertyValueAsString(Statement statement) {
		Value object = statement.getObject();
		if (!(object instanceof Literal)) {
			return null;
		}
		return ((Literal)object).getLabel();
	}

	/**
	 * check if the passed statement should be added (is it indexed? is it
	 * stored?) and add it as predicate to the passed document. No checks whether
	 * the predicate was already there.
	 * 
	 * @param statement
	 *        the statement to add
	 * @param document
	 *        the document to add to
	 */
	private void addProperty(Statement statement, Document document) {
		String text = getLiteralPropertyValueAsString(statement);
		if (text == null)
			return;
		String field = statement.getPredicate().toString();
		addProperty(field, text, document);
	}

	/**
	 * Stores and indexes a property in a Document. We don't have to recalculate
	 * the concatenated text: just add another TEXT field and Lucene will take
	 * care of this. Additional advantage: Lucene may be able to handle the
	 * invididual strings in a way that may affect e.g. phrase and proximity
	 * searches (concatenation basically means loss of information). NOTE: The
	 * TEXT_FIELD_NAME has to be stored, see in LuceneSail
	 * 
	 * @see LuceneSail
	 */
	private void addProperty(String predicate, String text, Document document) {
		// store this predicate
		document.add(new TextField(predicate, text, Field.Store.YES));

		// and in TEXT_FIELD_NAME
		document.add(new TextField(TEXT_FIELD_NAME, text, Field.Store.YES));
	}

	/**
	 * invalidate readers, free them if possible (readers that are still open by
	 * a {@link LuceneQueryConnection} will not be closed. Synchronized on
	 * oldmonitors because it manipulates them
	 * 
	 * @throws IOException
	 */
	private void invalidateReaders()
		throws IOException
	{
		synchronized (oldmonitors) {
			// Move current monitor to old monitors and set null
			if (currentMonitor != null)
				// we do NOT close it directly as it may be used by an open result
				// iterator, hence moving it to the
				// list of oldmonitors where it is handled as other older monitors
				oldmonitors.add(currentMonitor);
			currentMonitor = null;

			// close all monitors if possible
			for (Iterator<ReaderMonitor> i = oldmonitors.iterator(); i.hasNext();) {
				ReaderMonitor monitor = i.next();
				if (monitor.closeWhenPossible()) {
					i.remove();
				}
			}

			// check if all readers were closed
			if (oldmonitors.isEmpty()) {
				logger.debug("Deleting unused files from Lucene index");

				// clean up unused files (marked as 'deletable' in Luke Filewalker)
				getIndexWriter().deleteUnusedFiles();

				// logIndexStats();
			}
		}
	}

	private void logIndexStats() {
		try {
			IndexReader reader = null;
			try {
				reader = getIndexReader();

				Document doc;
				int totalFields = 0;

				Set<String> ids = new HashSet<String>();
				String[] idArray;
				int count = 0;
				for (int i = 0; i < reader.maxDoc(); i++) {
					if (reader.isDeleted(i))
						continue;
					doc = reader.document(i);
					totalFields += doc.getFields().size();
					count++;
					idArray = doc.getValues("id");
					for (String id : idArray)
						ids.add(id);

				}

				logger.info("Total documents in the index: " + reader.numDocs()
						+ ", number of deletable documents in the index: " + reader.numDeletedDocs()
						+ ", valid documents: " + count + ", total fields in all documents: " + totalFields
						+ ", average number of fields per document: " + ((double)totalFields) / reader.numDocs());
				logger.info("Distinct ids in the index: " + ids.size());

			}
			finally {
				if (currentMonitor != null) {
					currentMonitor.closeWhenPossible();
					currentMonitor = null;
				}
			}
		}
		catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}

	}

	public synchronized void removeStatement(Statement statement)
		throws IOException
	{
		Value object = statement.getObject();
		if (!(object instanceof Literal)) {
			return;
		}

		IndexWriter writer = null;
		boolean updated = false;

		// fetch the Document representing this Resource
		String resourceId = getResourceID(statement.getSubject());
		String contextId = getContextID(statement.getContext());
		String id = formIdString(resourceId, contextId);
		Term idTerm = new Term(ID_FIELD_NAME, id);

		Document document = getDocument(idTerm);

		if (document != null) {
			// determine the values used in the index for this triple
			String fieldName = statement.getPredicate().toString();
			String text = ((Literal)object).getLabel();

			// see if this triple occurs in this Document
			if (hasProperty(fieldName, text, document)) {
				// if the Document only has one predicate field, we can remove the
				// document
				int nrProperties = numberOfPropertyFields(document);
				if (nrProperties == 0) {
					logger.info("encountered document with zero properties, should have been deleted: {}",
							resourceId);
				}
				else if (nrProperties == 1) {
					writer = getIndexWriter();
					writer.deleteDocuments(idTerm);
					updated = true;
				}
				else {
					// there are more triples encoded in this Document: remove the
					// document and add a new Document without this triple
					Document newDocument = new Document();
					addID(id, newDocument);
					addResourceID(resourceId, newDocument);
					addContext(contextId, newDocument);

					for (Object oldFieldObject : document.getFields()) {
						Field oldField = (Field)oldFieldObject;
						String oldFieldName = oldField.name();
						String oldValue = oldField.stringValue();

						if (isPropertyField(oldFieldName)
								&& !(fieldName.equals(oldFieldName) && text.equals(oldValue)))
						{
							addProperty(oldFieldName, oldValue, newDocument);
						}
					}

					writer = getIndexWriter();
					writer.updateDocument(idTerm, newDocument);
					updated = true;
				}
			}
		}

		if (updated) {
			// make sure that these updates are visible for new
			// IndexReaders/Searchers
			writer.commit();

			// the old IndexReaders/Searchers are not outdated
			invalidateReaders();
		}
	}

	/**
	 * Commits any changes done to the LuceneIndex since the last commit. The
	 * semantics is synchronous to SailConnection.commit(), i.e. the LuceneIndex
	 * should be committed/rollbacked whenever the LuceneSailConnection is
	 * committed/rollbacked.
	 */
	public void commit()
		throws IOException
	{
		// FIXME: implement
	}

	public void rollback()
		throws IOException
	{
		// FIXME: implement
	}

	// //////////////////////////////// Methods for querying the index

	/**
	 * Returns the Resource corresponding with the specified Document number.
	 * Note that all of Lucene's restrictions of using document numbers apply.
	 */
	public Resource getResource(int documentNumber)
		throws IOException
	{
		Document document = getIndexSearcher().doc(documentNumber, Collections.singleton(URI_FIELD_NAME));
		return document == null ? null : getResource(document);
	}

	/**
	 * Returns the Resource corresponding with the specified Document.
	 */
	public Resource getResource(Document document) {
		String idString = document.get(URI_FIELD_NAME);
		return getResource(idString);
	}

	/**
	 * Parses an id-string (a serialized resource) back to a resource Inverse
	 * method of {@link #getResourceID(Resource)}
	 * 
	 * @param idString
	 */
	private Resource getResource(String idString) {
		if (idString.startsWith(BNODE_ID_PREFIX)) {
			return new BNodeImpl(idString.substring(BNODE_ID_PREFIX.length()));
		}
		else {
			return new URIImpl(idString);
		}
	}

	private String getContextID(Document document) {
		return document.get(CONTEXT_FIELD_NAME);
	}

	// /**
	// * Parses an id-string used for a context filed (a serialized resource)
	// back to a resource.
	// * <b>CAN RETURN NULL</b>
	// * Inverse method of {@link #getResourceID(Resource)}
	// * @param idString
	// * @return null if the passed idString was the {@link #CONTEXT_NULL}
	// constant
	// */
	// private Resource getContextResource(String idString) {
	// if (CONTEXT_NULL.equals(idString))
	// return null;
	// else
	// return getResource(idString);
	// }

	/**
	 * Evaluates the given query and returns the results as a TopDocs instance.
	 */
	public TopDocs search(String query)
		throws ParseException, IOException
	{
		return search(getQueryParser(null).parse(query));
	}

	/**
	 * Evaluates the given query only for the given resource.
	 */
	public TopDocs search(Resource resource, Query query)
		throws ParseException, IOException
	{
		// rewrite the query
		TermQuery idQuery = new TermQuery(new Term(URI_FIELD_NAME, getResourceID(resource)));
		BooleanQuery combinedQuery = new BooleanQuery();
		combinedQuery.add(idQuery, Occur.MUST);
		combinedQuery.add(query, Occur.MUST);
		int nDocs = Math.max(getIndexReader().numDocs(), 1);
		TopDocs hits = getIndexSearcher().search(combinedQuery, nDocs);

		// Now this is ok
		// if(hits.totalHits > 1)
		// logger.info("More than one Lucene doc was found with {} == {}",
		// ID_FIELD_NAME, getID(resource));

		return hits;
	}

	/**
	 * Parse the passed query.
	 * 
	 * @param query
	 *        string
	 * @return the parsed query
	 * @throws ParseException
	 *         when the parsing brakes
	 */
	public Query parseQuery(String query, URI propertyURI)
		throws ParseException
	{
		return getQueryParser(propertyURI).parse(query);
	}

	/**
	 * Evaluates the given query and returns the results as a TopDocs instance.
	 */
	public TopDocs search(Query query)
		throws IOException
	{
		int nDocs = Math.max(getIndexReader().numDocs(), 1);
		return getIndexSearcher().search(query, nDocs);
	}

	/**
	 * Gets the score for a particular Resource and query. Returns a value < 0
	 * when the Resource does not match the query.
	 */
	public float getScore(Resource resource, String query, URI propertyURI)
		throws ParseException, IOException
	{
		return getScore(resource, getQueryParser(propertyURI).parse(query));
	}

	/**
	 * Gets the score for a particular Resource and query. Returns a value < 0
	 * when the Resource does not match the query.
	 */
	public float getScore(Resource resource, Query query)
		throws IOException
	{
		// rewrite the query
		TermQuery idQuery = new TermQuery(new Term(URI_FIELD_NAME, getResourceID(resource)));
		BooleanQuery combinedQuery = new BooleanQuery();
		combinedQuery.add(idQuery, Occur.MUST);
		combinedQuery.add(query, Occur.MUST);
		IndexSearcher searcher = getIndexSearcher();

		// fetch the score when the URI matches the original query
		TopDocs docs = searcher.search(combinedQuery, null, 1);
		if (docs.totalHits == 0) {
			return -1f;
		}
		else {
			return docs.scoreDocs[0].score;
		}
	}

	private QueryParser getQueryParser(URI propertyURI) {
		// check out which query parser to use, based on the given property URI
		if (propertyURI == null)
			// if we have no property given, we create a default query parser which
			// has the TEXT_FIELD_NAME as the default field
			return new QueryParser(TEXT_FIELD_NAME, this.queryAnalyzer);
		else
			// otherwise we create a query parser that has the given property as
			// the default field
			return new QueryParser(propertyURI.toString(), this.queryAnalyzer);
	}

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
	public synchronized void addRemoveStatements(Collection<Statement> added, Collection<Statement> removed)
		throws Exception
	{
		// Buffer per resource
		MapOfListMaps<Resource, String, Statement> rsAdded = new MapOfListMaps<Resource, String, Statement>();
		MapOfListMaps<Resource, String, Statement> rsRemoved = new MapOfListMaps<Resource, String, Statement>();

		HashSet<Resource> resources = new HashSet<Resource>();
		for (Statement s : added) {
			rsAdded.add(s.getSubject(), getContextID(s.getContext()), s);
			resources.add(s.getSubject());
		}
		for (Statement s : removed) {
			rsRemoved.add(s.getSubject(), getContextID(s.getContext()), s);
			resources.add(s.getSubject());
		}

		logger.debug("Removing " + removed.size() + " statements, adding " + added.size() + " statements");

		IndexWriter writer = getIndexWriter();

		// for each resource, add/remove
		for (Resource resource : resources) {
			Map<String, List<Statement>> stmtsToRemove = rsRemoved.get(resource);
			Map<String, List<Statement>> stmtsToAdd = rsAdded.get(resource);

			Set<String> contextsToUpdate = new HashSet<String>(stmtsToAdd.keySet());
			contextsToUpdate.addAll(stmtsToRemove.keySet());

			Map<String, Document> docsByContext = new HashMap<String, Document>();
			// is the resource in the store?
			// fetch the Document representing this Resource
			String resourceId = getResourceID(resource);
			Term uriTerm = new Term(URI_FIELD_NAME, resourceId);
			List<Document> documents = getDocuments(uriTerm);

			for (Document doc : documents) {
				docsByContext.put(this.getContextID(doc), doc);
			}

			for (String contextId : contextsToUpdate) {
				String id = formIdString(resourceId, contextId);

				Term idTerm = new Term(ID_FIELD_NAME, id);
				Document document = docsByContext.get(contextId);
				if (document == null) {
					// there are no such Documents: create one now
					document = new Document();
					addID(id, document);
					addResourceID(resourceId, document);
					addContext(contextId, document);
					// add all statements, remember the contexts
					// HashSet<Resource> contextsToAdd = new HashSet<Resource>();
					List<Statement> list = stmtsToAdd.get(contextId);
					if (list != null) {
						for (Statement s : list) {
							addProperty(s, document);
						}
					}

					// add it to the index
					writer.addDocument(document);

					// THERE SHOULD BE NO DELETED TRIPLES ON A NEWLY ADDED RESOURCE
					if (stmtsToRemove.containsKey(contextId))
						logger.info(
								"Statements are marked to be removed that should not be in the store, for resource {} and context {}. Nothing done.",
								resource, contextId);
				}
				else {
					// update the Document

					// create a copy of the old document; updating the retrieved
					// Document instance works ok for stored properties but indexed
					// data
					// gets lots when doing an IndexWriter.updateDocument with it
					Document newDocument = new Document();

					// buffer the removed literal statements
					ListMap<String, String> removedOfResource = null;
					{
						List<Statement> removedStatements = stmtsToRemove.get(contextId);
						if (removedStatements != null && !removedStatements.isEmpty()) {
							removedOfResource = new ListMap<String, String>();
							for (Statement r : removedStatements) {
								if (r.getObject() instanceof Literal) {
									// remove value from both property field and the
									// corresponding text field
									String label = ((Literal)r.getObject()).getLabel();
									removedOfResource.put(r.getPredicate().toString(), label);
									removedOfResource.put(TEXT_FIELD_NAME, label);
								}
							}
						}
					}

					// add all existing fields (including id, uri, context, and text)
					// but without adding the removed ones
					// keep the predicate/value pairs to ensure that the statement
					// cannot be added twice
					SetMap<String, String> copiedProperties = new SetMap<String, String>();
					for (Object oldFieldObject : document.getFields()) {
						Field oldField = (Field)oldFieldObject;
						// do not copy removed statements to the new version of the
						// document
						if (removedOfResource != null) {
							// which fields were removed?
							List<String> objectsRemoved = removedOfResource.get(oldField.name());
							if ((objectsRemoved != null) && (objectsRemoved.contains(oldField.stringValue())))
								continue;
						}
						newDocument.add(oldField);
						copiedProperties.put(oldField.name(), oldField.stringValue());
					}

					// add all statements to this document, except for those which
					// are already there
					{
						List<Statement> addedToResource = stmtsToAdd.get(contextId);
						String val;
						if (addedToResource != null && !addedToResource.isEmpty()) {
							for (Statement s : addedToResource) {
								val = getLiteralPropertyValueAsString(s);
								if (val != null) {
									if (!copiedProperties.containsKeyValuePair(s.getPredicate().stringValue(), val)) {
										addProperty(s, newDocument);
									}
								}
							}
						}
					}

					// update the index with the cloned document, if it contains any
					// meaningful non-system properties
					int nrProperties = numberOfPropertyFields(newDocument);
					if (nrProperties > 0) {
						writer.updateDocument(idTerm, newDocument);
					}
					else {
						writer.deleteDocuments(idTerm);
					}
				}
			}
		}
		// make sure that these updates are visible for new
		// IndexReaders/Searchers
		writer.commit();

		// the old IndexReaders/Searchers are not outdated
		invalidateReaders();

	}

	/**
	 * @param contexts
	 * @param sail
	 *        - the underlying native sail where to read the missing triples from
	 *        after deletion
	 * @throws SailException
	 */
	public synchronized void clearContexts(Resource[] contexts, Sail sail)
		throws IOException, SailException
	{

		// logger.warn("Clearing contexts operation did not change the index: contexts are not indexed at the moment");

		logger.debug("deleting contexts: {}", Arrays.toString(contexts));
		// these resources have to be read from the underlying rdf store
		// and their triples have to be added to the luceneindex after deletion of
		// documents
		// HashSet<Resource> resourcesToUpdate = new HashSet<Resource>();

		// remove all contexts passed
		for (Resource context : contexts) {
			// attention: context can be NULL!
			String contextString = getContextID(context);
			Term contextTerm = new Term(CONTEXT_FIELD_NAME, contextString);
			// IndexReader reader = getIndexReader();

			// now check all documents, and remember the URI of the resources
			// that were in multiple contexts
			// TermDocs termDocs = reader.termDocs(contextTerm);
			// try {
			// while (termDocs.next()) {
			// Document document = reader.document(termDocs.doc());
			// // does this document have any other contexts?
			// Field[] fields = document.getFields(CONTEXT_FIELD_NAME);
			// for (Field f : fields)
			// {
			// if
			// (!contextString.equals(f.stringValue())&&!f.stringValue().equals("null"))
			// // there is another context
			// {
			// logger.debug("test new contexts: {}", f.stringValue());
			// // is it in the also contexts (lucky us if it is)
			// Resource otherContextOfDocument =
			// getContextResource(f.stringValue()); // can return null
			// boolean isAlsoDeleted = false;
			// for (Resource c: contexts){
			// if (c==null) {
			// if (otherContextOfDocument == null)
			// isAlsoDeleted = true;
			// } else
			// if (c.equals(otherContextOfDocument))
			// isAlsoDeleted = true;
			// }
			// // the otherContextOfDocument is now eihter marked for deletion or
			// not
			// if (!isAlsoDeleted) {
			// // get ID of document
			// Resource r = getResource(document);
			// resourcesToUpdate.add(r);
			// }
			// }
			// }
			// }
			// } finally {
			// termDocs.close();
			// }

			// now delete all documents from the deleted context
			getIndexWriter().deleteDocuments(contextTerm);
		}

		// now add those again, that had other contexts also.
		// SailConnection con = sail.getConnection();
		// try {
		// // for each resource, add all
		// for (Resource resource : resourcesToUpdate) {
		// logger.debug("re-adding resource {}", resource);
		// ArrayList<Statement> toAdd = new ArrayList<Statement>();
		// CloseableIteration<? extends Statement, SailException> it =
		// con.getStatements(resource, null, null, false);
		// while (it.hasNext()) {
		// Statement s = it.next();
		// toAdd.add(s);
		// }
		// addDocument(resource, toAdd);
		// }
		// } finally {
		// con.close();
		// }
		getIndexWriter().commit();
		invalidateReaders();

	}

	/**
	 * Add a complete Lucene Document based on these statements. Do not search
	 * for an existing document with the same subject id. (assume the existing
	 * document was deleted)
	 * 
	 * @param statements
	 *        the statements that make up the resource
	 * @throws IOException
	 */
	public synchronized void addDocuments(Resource subject, List<Statement> statements)
		throws IOException
	{

		String resourceId = getResourceID(subject);

		SetMap<String, Statement> stmtsByContextId = new SetMap<String, Statement>();

		String contextId;
		for (Statement statement : statements) {
			contextId = getContextID(statement.getContext());

			stmtsByContextId.put(contextId, statement);
		}

		IndexWriter writer = getIndexWriter();
		for (Entry<String, Set<Statement>> entry : stmtsByContextId.entrySet()) {
			// create a new document
			Document document = new Document();

			String id = formIdString(resourceId, entry.getKey());
			addID(id, document);
			addResourceID(resourceId, document);
			addContext(entry.getKey(), document);

			for (Statement stmt : entry.getValue()) {
				// determine stuff to store
				addProperty(stmt, document);
			}
			// add it to the index
			writer.addDocument(document);
		}

	}

	/**
	 * 
	 */
	public synchronized void clear()
		throws IOException
	{
		// clear
		// the old IndexReaders/Searchers are not outdated
		invalidateReaders();
		if (indexWriter != null)
			indexWriter.close();

		// crate new writer
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		indexWriter = new IndexWriter(directory, indexWriterConfig);
		indexWriter.close();
		indexWriter = null;

	}

}
