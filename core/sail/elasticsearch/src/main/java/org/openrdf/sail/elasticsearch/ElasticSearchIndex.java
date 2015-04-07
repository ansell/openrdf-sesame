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
package org.openrdf.sail.elasticsearch;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.StoredFieldVisitor;
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
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.lucene.SearchIndex;
import org.openrdf.sail.lucene.util.ListMap;
import org.openrdf.sail.lucene.util.MapOfListMaps;
import org.openrdf.sail.lucene.util.SetMap;

/**
 * @see LuceneSail
 */
public class ElasticSearchIndex implements SearchIndex {


	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Node node;

	private Client client;

	public ElasticSearchIndex()
	{
	}

	public void initialize(Properties parameters) {
		node = NodeBuilder.nodeBuilder().node();
		client = node.client();
	}

	public void shutDown()
		throws IOException
	{
		if(client != null) {
			client.close();
			client = null;
		}

		if (node != null) {
			node.close();
			node = null;
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

		try {
			if (document == null) {
				// there is no such Document: create one now
				document = new Document();
				addIDField(id, document);
				addURIField(resourceId, document);
				// add context
				addContextField(context, document);
	
				addPropertyFields(field, text, document);
	
				// add it to the index
				writer = getIndexWriter();
				if(!updated) {
					begin();
				}
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
					addPropertyFields(field, text, newDocument);
	
					// update the index with the cloned document
					writer = getIndexWriter();
					if(!updated) {
						begin();
					}
					writer.updateDocument(idTerm, newDocument);
					updated = true;
				}
			}
	
			if (updated) {
				// make sure that these updates are visible for new
				// IndexReaders/Searchers
				commit();
			}
		}
		catch(Exception e) {
			if(updated) {
				rollback();
			}
		}
	}

	/**
	 * Returns a Document representing the specified document ID (combination of
	 * resource and context), or null when no such Document exists yet.
	 */
	private Document getDocument(Term idTerm)
		throws IOException
	{
		IndexReader reader = getIndexReader();
		List<LeafReaderContext> leaves = reader.leaves();
		int size = leaves.size();
		for(int i=0; i<size; i++) {
			LeafReader lreader = leaves.get(i).reader();
			Document document = getDocument(lreader, idTerm);
			if(document != null)
			{
				return document;
			}
		}
		// no such Document
		return null;
	}

	private static Document getDocument(LeafReader reader, Term term) throws IOException {
		DocsEnum docs = reader.termDocsEnum(term);
		if(docs != null)
		{
			int docId = docs.nextDoc();
			if(docId != DocsEnum.NO_MORE_DOCS) {
				if(docs.nextDoc() != DocsEnum.NO_MORE_DOCS) {
					throw new IllegalStateException("Multiple Documents for term " + term.text());
				}
				return readDocument(reader, docId);
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
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
		List<Document> result = new ArrayList<Document>();

		IndexReader reader = getIndexReader();
		List<LeafReaderContext> leaves = reader.leaves();
		int size = leaves.size();
		for(int i=0; i<size; i++) {
			LeafReader lreader = leaves.get(i).reader();
			addDocuments(lreader, uriTerm, result);
		}

		return result;
	}

	private static void addDocuments(LeafReader reader, Term term, Collection<Document> documents) throws IOException {
		DocsEnum docs = reader.termDocsEnum(term);
		if(docs != null)
		{
			int docId;
			while((docId = docs.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
				Document document = readDocument(reader, docId);
				documents.add(document);
			}
		}
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
	private static void addIDField(String id, Document document) {
		document.add(new StringField(ID_FIELD_NAME, id, Store.YES));
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
	private static void addContextField(String context, Document document) {
		if (context != null) {
			document.add(new StringField(CONTEXT_FIELD_NAME, context, Store.YES));
		}
	}

	/**
	 * Stores and indexes the resource ID in a Document.
	 */
	private static void addURIField(String resourceId, Document document) {
		document.add(new StringField(URI_FIELD_NAME, resourceId, Store.YES));
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
		addPropertyFields(field, text, document);
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
	private static void addPropertyFields(String predicate, String text, Document document) {
		// store this predicate
		addPredicateField(predicate, text, document);

		// and in TEXT_FIELD_NAME
		addTextField(text, document);
	}

	private static void addPredicateField(String predicate, String text, Document document) {
		// store this predicate
		document.add(new TextField(predicate, text, Store.YES));
	}

	private static void addTextField(String text, Document document) {
		// and in TEXT_FIELD_NAME
		document.add(new TextField(TEXT_FIELD_NAME, text, Store.YES));
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
			for (Iterator<AbstractReaderMonitor> i = oldmonitors.iterator(); i.hasNext();) {
				AbstractReaderMonitor monitor = i.next();
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
					if (isDeleted(reader, i))
						continue;
					doc = readDocument(reader, i);
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

		try {
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
						if(!updated) {
							begin();
						}
						writer.deleteDocuments(idTerm);
						updated = true;
					}
					else {
						// there are more triples encoded in this Document: remove the
						// document and add a new Document without this triple
						Document newDocument = new Document();
						addIDField(id, newDocument);
						addURIField(resourceId, newDocument);
						addContextField(contextId, newDocument);
	
						for (Object oldFieldObject : document.getFields()) {
							Field oldField = (Field)oldFieldObject;
							String oldFieldName = oldField.name();
							String oldValue = oldField.stringValue();
	
							if (isPropertyField(oldFieldName)
									&& !(fieldName.equals(oldFieldName) && text.equals(oldValue)))
							{
								addPropertyFields(oldFieldName, oldValue, newDocument);
							}
						}
	
						writer = getIndexWriter();
						if(!updated) {
							begin();
						}
						writer.updateDocument(idTerm, newDocument);
						updated = true;
					}
				}
			}
	
			if (updated) {
				// make sure that these updates are visible for new
				// IndexReaders/Searchers
				commit();
			}
		}
		catch(Exception e) {
			if(updated) {
				rollback();
			}
		}
	}

	@Override
	public void begin()
		throws IOException
	{
		// nothing to do
	}

	/**
	 * Commits any changes done to the LuceneIndex since the last commit. The
	 * semantics is synchronous to SailConnection.commit(), i.e. the LuceneIndex
	 * should be committed/rollbacked whenever the LuceneSailConnection is
	 * committed/rollbacked.
	 */
	@Override
	public void commit()
		throws IOException
	{
		getIndexWriter().commit();
		// the old IndexReaders/Searchers are not outdated
		invalidateReaders();
	}

	@Override
	public void rollback()
		throws IOException
	{
		getIndexWriter().rollback();
	}

	// //////////////////////////////// Methods for querying the index

	@Override
	public Collection<BindingSet> evaluate(QuerySpec query) throws SailException
	{
		QueryResult result = evaluateQuery(query);

		// generate bindings
		return generateBindingSets(query, result.hits, result.highlighter);
	}

	/**
	 * Evaluates one Lucene Query. It distinguishes between two cases, the one
	 * where no subject is given and the one were it is given.
	 * 
	 * @param query
	 *        the Lucene query to evaluate
	 * @return QueryResult consisting of hits and highlighter
	 */
	private QueryResult evaluateQuery(QuerySpec query) {
		TopDocs hits = null;
		Highlighter highlighter = null;

		// get the subject of the query
		Resource subject = query.getSubject();

		try {
			// parse the query string to a lucene query

			String sQuery = query.getQueryString();

			if (!sQuery.isEmpty()) {
				Query lucenequery = parseQuery(query.getQueryString(), query.getPropertyURI());

				// if the query requests for the snippet, create a highlighter using
				// this query
				if (query.getSnippetVariableName() != null) {
					Formatter formatter = new SimpleHTMLFormatter();
					highlighter = new Highlighter(formatter, new QueryScorer(lucenequery));
				}

				// distinguish the two cases of subject == null
				if (subject == null) {
					hits = search(lucenequery);
				}
				else {
					hits = search(subject, lucenequery);
				}
			}
			else {
				hits = new TopDocs(0, new ScoreDoc[0], 0.0f);
			}
		}
		catch (Exception e) {
			logger.error("There was a problem evaluating query '" + query.getQueryString() + "' for property '"
					+ query.getPropertyURI() + "!", e);
		}

		return new QueryResult(hits, highlighter);
	}

	/**
	 * This method generates bindings from the given result of a Lucene query.
	 * 
	 * @param query
	 *        the Lucene query
	 * @param hits
	 *        the query result
	 * @param highlighter
	 *        a Highlighter for the query
	 * @return a LinkedHashSet containing generated bindings
	 * @throws SailException
	 */
	private LinkedHashSet<BindingSet> generateBindingSets(QuerySpec query, TopDocs hits,
			Highlighter highlighter)
		throws SailException
	{
		// Since one resource can be returned many times, it can lead now to
		// multiple occurrences
		// of the same binding tuple in the BINDINGS clause. This in turn leads to
		// duplicate answers in the original SPARQL query.
		// We want to avoid this, so BindingSets added to the result must be
		// unique.
		LinkedHashSet<BindingSet> bindingSets = new LinkedHashSet<BindingSet>();

		// for each hit ...
		ScoreDoc[] docs = hits.scoreDocs;
		for (int i = 0; i < docs.length; i++) {
			// this takes the new bindings
			QueryBindingSet derivedBindings = new QueryBindingSet();

			// get the current hit
			int docId = docs[i].doc;
			Document doc = getDoc(docId);
			if (doc == null)
				continue;

			// get the score of the hit
			float score = docs[i].score;

			// bind the respective variables
			String matchVar = query.getMatchesVariableName();
			if (matchVar != null) {
				try {
					Resource resource = getResource(doc);
					Value existing = derivedBindings.getValue(matchVar);
					// if the existing binding contradicts the current binding, than
					// we can safely skip this permutation
					if ((existing != null) && (!existing.stringValue().equals(resource.stringValue()))) {
						// invalidate the binding
						derivedBindings = null;

						// and exit the loop
						break;
					}
					derivedBindings.addBinding(matchVar, resource);
				}
				catch (NullPointerException e) {
					SailException e1 = new SailException(
							"NullPointerException when retrieving a resource from LuceneSail. Possible cause is the obsolete index structure. Re-creating the index can help",
							e);
					logger.error(e1.getMessage());
					logger.debug("Details: ", e);
					throw e1;
				}
			}

			if ((query.getScoreVariableName() != null) && (score > 0.0f))
				derivedBindings.addBinding(query.getScoreVariableName(), scoreToLiteral(score));

			if (query.getSnippetVariableName() != null) {
				if (highlighter != null) {
					// limit to the queried field, if there was one
					IndexableField[] fields;
					if (query.getPropertyURI() != null) {
						String fieldname = query.getPropertyURI().toString();
						fields = doc.getFields(fieldname);
					}
					else {
						fields = getPropertyFields(doc.getFields());
					}

					// extract snippets from Lucene's query results
					for (IndexableField field : fields) {
						// create an individual binding set for each snippet
						QueryBindingSet snippetBindings = new QueryBindingSet(derivedBindings);

						String text = field.stringValue();

						String fragments = null;
						try {
							TokenStream tokenStream = getAnalyzer().tokenStream(field.name(),
									new StringReader(text));
							fragments = highlighter.getBestFragments(tokenStream, text, 2, "...");
						}
						catch (Exception e) {
							logger.error("Exception while getting snippet for filed " + field.name()
									+ " for query\n" + query, e);
							continue;
						}

						if (fragments != null && !fragments.isEmpty()) {
							snippetBindings.addBinding(query.getSnippetVariableName(), new LiteralImpl(fragments));

							if (query.getPropertyVariableName() != null && query.getPropertyURI() == null) {
								snippetBindings.addBinding(query.getPropertyVariableName(), new URIImpl(field.name()));
							}

							bindingSets.add(snippetBindings);
						}
					}
				}
				else {
					logger.warn(
							"Lucene Query requests snippet, but no highlighter was generated for it, no snippets will be generated!\n{}",
							query);
					bindingSets.add(derivedBindings);
				}
			}
			else {
				bindingSets.add(derivedBindings);
			}
		}

		// we succeeded
		return bindingSets;
	}

	/**
	 * Returns the lucene hit with the given id of the respective lucene query
	 * 
	 * @param id
	 *        the id of the document to return
	 * @return the requested hit, or null if it fails
	 */
	private Document getDoc(int docId) {
		try {
			return getIndexSearcher().doc(docId);
		}
		catch (CorruptIndexException e) {
			logger.error("The index seems to be corrupted:", e);
			return null;
		}
		catch (IOException e) {
			logger.error("Could not read from index:", e);
			return null;
		}
	}

	/**
	 * Returns a score value encoded as a Literal.
	 * 
	 * @param score
	 *        the float score to convert
	 * @return the score as a literal
	 */
	private Literal scoreToLiteral(float score) {
		return new LiteralImpl(String.valueOf(score), XMLSchema.FLOAT);
	}

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
	@Override
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
		begin();
		try {
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
						addIDField(id, document);
						addURIField(resourceId, document);
						addContextField(contextId, document);
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
			commit();
		}
		catch(Exception e) {
			rollback();
		}

	}

	/**
	 * @param contexts
	 * @param sail
	 *        - the underlying native sail where to read the missing triples from
	 *        after deletion
	 * @throws SailException
	 */
	@Override
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
		begin();
		try {
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
				// Document document = readDocument(reader, termDocs.doc());
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
			commit();
		}
		catch(Exception e) {
			rollback();
		}

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
	@Override
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
			addIDField(id, document);
			addURIField(resourceId, document);
			addContextField(entry.getKey(), document);

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
	@Override
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

	//
	// Lucene helper methods
	//

	private static boolean isDeleted(IndexReader reader, int docId)
	{
		if(reader.hasDeletions()) {
			List<LeafReaderContext> leaves = reader.leaves();
			int size = leaves.size();
			for(int i=0; i<size; i++) {
				Bits liveDocs = leaves.get(i).reader().getLiveDocs();
				if(docId < liveDocs.length()) {
					boolean isDeleted = !liveDocs.get(docId);
					if(isDeleted) {
						return true;
					}
				}
			}
			return false;
		}
		else {
			return false;
		}
	}

	private static Document readDocument(IndexReader reader, int docId) throws IOException
	{
		AllStoredFieldVisitor visitor = new AllStoredFieldVisitor();
		reader.document(docId, visitor);
		return visitor.getDocument();
	}



	static class AllStoredFieldVisitor extends StoredFieldVisitor
	{
		private Document document = new Document();

		@Override
		public Status needsField(FieldInfo fieldInfo)
			throws IOException
		{
			return Status.YES;
		}

		@Override
		public void stringField(FieldInfo fieldInfo, String value)
		{
			String name = fieldInfo.name;
			if(ID_FIELD_NAME.equals(name)) {
				addIDField(value, document);
			} else if(CONTEXT_FIELD_NAME.equals(name)) {
				addContextField(value, document);
			} else if(URI_FIELD_NAME.equals(name)) {
				addURIField(value, document);
			} else if(TEXT_FIELD_NAME.equals(name)) {
				addTextField(value, document);
			} else {
				addPredicateField(name, value, document);
			}
		}

		Document getDocument()
		{
			return document;
		}
	}
}
