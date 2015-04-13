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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.lucene.LuceneSail;
import org.openrdf.sail.lucene.QuerySpec;
import org.openrdf.sail.lucene.SearchFields;
import org.openrdf.sail.lucene.SearchIndex;
import org.openrdf.sail.lucene.util.ListMap;
import org.openrdf.sail.lucene.util.MapOfListMaps;
import org.openrdf.sail.lucene.util.SetMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see LuceneSail
 */
public class ElasticSearchIndex implements SearchIndex {

	public static final String INDEX_NAME_KEY = "indexName";
	public static final String DOCUMENT_TYPE_KEY = "documentType";
	public static final String DEFAULT_INDEX_NAME = "elastic-search-sail";
	public static final String DEFAULT_DOCUMENT_TYPE = "resource";
	public static final String DEFAULT_ANALYZER = "standard";

	private static final String HIGHLIGHTER_PRE_TAG = "<B>";
	private static final String HIGHLIGHTER_POST_TAG = "</B>";
	private static final Pattern HIGHLIGHTER_PATTERN = Pattern.compile("("+HIGHLIGHTER_PRE_TAG+".+?"+HIGHLIGHTER_POST_TAG+")");

	private static final List<String> REJECTED_DATATYPES = new ArrayList<String>();

	static {
		REJECTED_DATATYPES.add("http://www.w3.org/2001/XMLSchema#float");
	}

	static class Document {
		public final String id;
		public final String type;
		public final long version;
		public final Map<String,Object> fields;

		public Document(SearchHit hit) {
			this(hit.getId(), hit.getType(), hit.getVersion(), hit.getSource());
		}

		public Document(String id, String type, long version, Map<String,Object> fields) {
			this.id = id;
			this.type = type;
			this.version = version;
			this.fields = fields;
		}
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Node node;

	private Client client;

	private String indexName;
	private String documentType;

	private String analyzer;
	private String queryAnalyzer = "standard";

	public ElasticSearchIndex()
	{
	}

	@Override
	public void initialize(Properties parameters) throws IOException {
		indexName = parameters.getProperty(INDEX_NAME_KEY, DEFAULT_INDEX_NAME);
		documentType = parameters.getProperty(DOCUMENT_TYPE_KEY, DEFAULT_DOCUMENT_TYPE);
		analyzer = parameters.getProperty(LuceneSail.ANALYZER_CLASS_KEY, DEFAULT_ANALYZER);
		node = NodeBuilder.nodeBuilder().node();
		client = node.client();

		client.admin().cluster().prepareHealth(indexName).setWaitForYellowStatus().execute().actionGet();

		boolean exists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
		if(!exists) {
			createIndex();
		}

		logger.info("Field mappings:\n{}", getMappings());
	}

	public Map<String,Object> getMappings() throws IOException
	{
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> indexMappings = client.admin().indices().prepareGetMappings(indexName).setTypes(documentType).execute().actionGet().getMappings();
		ImmutableOpenMap<String, MappingMetaData> typeMappings = indexMappings.get(indexName);
		MappingMetaData mappings = typeMappings.get(documentType);
		return mappings.sourceAsMap();
	}

	public Map<String,Object> getFieldMappings() throws IOException
	{
		return (Map<String,Object>) getMappings().get("properties");
	}
	private void createIndex() throws IOException
	{
		String settings = XContentFactory.jsonBuilder()
			.startObject()
				.startObject("analysis")
					.startObject("analyzer")
						.startObject("default")
							.field("type", analyzer)
						.endObject()
					.endObject()
				.endObject()
			.endObject()
		.string();

		doAcknowledgedRequest(client.admin().indices().prepareCreate(indexName).setSettings(ImmutableSettings.settingsBuilder().loadFromSource(settings)));

		// use _source instead of explicit stored = true
		XContentBuilder typeMapping = XContentFactory.jsonBuilder();
		typeMapping.startObject()
			.startObject(documentType)
				.startObject("_all")
					.field("enabled", false)
				.endObject()
				.startObject("properties");
		typeMapping.startObject(SearchFields.CONTEXT_FIELD_NAME)
			.field("type", "string")
			.field("index", "not_analyzed")
		.endObject();
		typeMapping.startObject(SearchFields.URI_FIELD_NAME)
			.field("type", "string")
			.field("index", "not_analyzed")
		.endObject();
		typeMapping.startObject(SearchFields.TEXT_FIELD_NAME)
			.field("type", "string")
			.field("index", "analyzed")
		.endObject();
		typeMapping
				.endObject()
			.endObject()
		.endObject();

		doAcknowledgedRequest(client.admin().indices().preparePutMapping(indexName).setType(documentType).setSource(typeMapping));
	}

	@Override
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

	/**
	 * Returns whether the provided literal is accepted by the LuceneIndex to be
	 * indexed. It for instance does not make much since to index xsd:float.
	 * 
	 * @param literal
	 *        the literal to be accepted
	 * @return true if the given literal will be indexed by this LuceneIndex
	 */
	@Override
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

	// //////////////////////////////// Methods for updating the index

	/**
	 * Indexes the specified Statement.
	 */
	@Override
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
		String context = SearchFields.getContextID(statement.getContext());

		// fetch the Document representing this Resource
		String resourceId = SearchFields.getResourceID(statement.getSubject());
		String contextId = SearchFields.getContextID(statement.getContext());

		String id = SearchFields.formIdString(resourceId, contextId);
		Document document = getDocument(id);

		if (document == null) {
			// there is no such Document: create one now
			Map<String,Object> newDocument = new HashMap<String,Object>();
			addURIField(resourceId, newDocument);
			// add context
			addContextField(context, newDocument);

			addPropertyFields(field, text, newDocument);

			// add it to the index
			doIndexRequest(client.prepareIndex(indexName, documentType, id).setSource(newDocument));
		}
		else {
			// update this Document when this triple has not been stored already
			if (!hasProperty(field, text, document.fields)) {
				Map<String,Object> newDocument = new HashMap<String,Object>(document.fields);

				// add the new triple to the cloned document
				addPropertyFields(field, text, newDocument);

				// update the index with the cloned document
				doUpdateRequest(client.prepareUpdate(indexName, document.type, document.id).setVersion(document.version).setDoc(newDocument));
			}
		}
	}

	/**
	 * Returns a Document representing the specified document ID (combination of
	 * resource and context), or null when no such Document exists yet.
	 */
	private Document getDocument(String id)
		throws IOException
	{
		GetResponse response = client.prepareGet(indexName, documentType, id).execute().actionGet();
		if(response.isExists()) {
			return new Document(response.getId(), response.getType(), response.getVersion(), response.getSource());
		}
		// no such Document
		return null;
	}

	/**
	 * Returns a list of Documents representing the specified Resource (empty
	 * when no such Document exists yet). Each document represent a set of
	 * statements with the specified Resource as a subject, which are stored in a
	 * specific context
	 */
	private List<Document> getDocuments(QueryBuilder query)
		throws IOException
	{
		List<Document> result = new ArrayList<Document>();

		SearchHits searchHits = search(client.prepareSearch(), query);
		SearchHit[] hits = searchHits.getHits();
		int size = hits.length;
		for(int i=0; i<size; i++) {
			result.add(new Document(hits[i]));
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
		String resourceId = SearchFields.getResourceID(subject);
		String contextId = SearchFields.getContextID(context);
		return getDocument(SearchFields.formIdString(resourceId, contextId));
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
		String resourceId = SearchFields.getResourceID(subject);
		QueryBuilder query = QueryBuilders.termQuery(SearchFields.URI_FIELD_NAME, resourceId);
		return getDocuments(query);
	}

	/**
	 * Checks whether a field occurs with a specified value in a Document.
	 */
	private boolean hasProperty(String fieldName, String value, Map<String,Object> document) {
		List<String> fields = asStringList(document.get(fieldName));
		if (fields != null) {
			for (String field : fields) {
				if (value.equals(field)) {
					return true;
				}
			}
		}

		return false;
	}

	private static List<String> asStringList(Object value) {
		List<String> l;
		if(value == null) {
			l = null;
		}
		else if(value instanceof List<?>) {
			l = (List<String>) value;
		}
		else {
			l = Collections.singletonList((String) value);
		}
		return l;
	}

	private static List<String> makeModifiable(List<String> l)
	{
		List<String> modList;
		if(!(l instanceof ArrayList<?>)) {
			modList = new ArrayList<String>(l.size()+1);
			modList.addAll(l);
		}
		else {
			modList = l;
		}
		return modList;
	}
	/**
	 * Determines the number of properties stored in a Document.
	 */
	private int numberOfPropertyFields(Collection<String> fields) {
		// count the properties that are NOT id nor context nor text
		int propsize = 0;
		for (String field : fields) {
			if (SearchFields.isPropertyField(field))
				propsize++;
		}
		return propsize;
	}

	/**
	 * Filters the given list of fields, retaining all property fields.
	 */
	public List<String> getPropertyFields(Collection<String> fields) {
		List<String> result = new ArrayList<String>();
		for (String field : fields) {
			if (SearchFields.isPropertyField(field))
				result.add(field);
		}
		return result;
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
	private static void addContextField(String context, Map<String,Object> document) {
		if (context != null) {
			document.put(SearchFields.CONTEXT_FIELD_NAME, context);
		}
	}

	/**
	 * Stores and indexes the resource ID in a Document.
	 */
	private static void addURIField(String resourceId, Map<String,Object> document) {
		document.put(SearchFields.URI_FIELD_NAME, resourceId);
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
	private void addProperty(Statement statement, Map<String,Object> document) {
		String text = SearchFields.getLiteralPropertyValueAsString(statement);
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
	private static void addPropertyFields(String predicate, String text, Map<String,Object> document) {
		// store this predicate
		addField(predicate, text, document);

		// and in TEXT_FIELD_NAME
		addField(SearchFields.TEXT_FIELD_NAME, text, document);
	}

	private static void addField(String name, String value, Map<String,Object> document) {
		Object oldValue = document.get(name);
		Object newValue;
		if(oldValue != null) {
			List<String> newList = makeModifiable(asStringList(oldValue));
			newList.add(value);
			newValue = newList;
		}
		else {
			newValue = value;
		}
		document.put(name, newValue);
	}

	@Override
	public synchronized void removeStatement(Statement statement)
		throws IOException
	{
		Value object = statement.getObject();
		if (!(object instanceof Literal)) {
			return;
		}

		// fetch the Document representing this Resource
		String resourceId = SearchFields.getResourceID(statement.getSubject());
		String contextId = SearchFields.getContextID(statement.getContext());
		String id = SearchFields.formIdString(resourceId, contextId);
		Document document = getDocument(id);

		if (document != null) {
			// determine the values used in the index for this triple
			String fieldName = statement.getPredicate().toString();
			String text = ((Literal)object).getLabel();

			// see if this triple occurs in this Document
			if (hasProperty(fieldName, text, document.fields)) {
				// if the Document only has one predicate field, we can remove the
				// document
				int nrProperties = numberOfPropertyFields(document.fields.keySet());
				if (nrProperties == 0) {
					logger.info("encountered document with zero properties, should have been deleted: {}",
							resourceId);
				}
				else if (nrProperties == 1) {
					client.prepareDelete(indexName, document.type, document.id).setVersion(document.version).execute().actionGet();
				}
				else {
					// there are more triples encoded in this Document: remove the
					// document and add a new Document without this triple
					Map<String,Object> newDocument = new HashMap<String,Object>();
					addURIField(resourceId, newDocument);
					addContextField(contextId, newDocument);

					for (Map.Entry<String,Object> oldField : document.fields.entrySet()) {
						String oldFieldName = oldField.getKey();
						List<String> oldValues = asStringList(oldField.getValue());

						if (SearchFields.isPropertyField(oldFieldName))
						{
							boolean isField = fieldName.equals(oldFieldName);
							for(String oldValue : oldValues)
							{
								if (!(isField && text.equals(oldValue)))
								{
									addPropertyFields(oldFieldName, oldValue, newDocument);
								}
							}
						}
					}

					client.prepareUpdate(indexName, document.type, document.id).setVersion(document.version).setDoc(newDocument).execute().actionGet();
				}
			}
		}
	}

	@Override
	public void begin() throws IOException
	{
	}

	@Override
	public void commit() throws IOException
	{
		client.admin().indices().prepareRefresh(indexName).execute().actionGet();
	}

	@Override
	public void rollback() throws IOException
	{
	}

	@Override
	public void beginReading() throws IOException
	{
	}

	@Override
	public void endReading() throws IOException
	{
	}

	// //////////////////////////////// Methods for querying the index

	@Override
	public Collection<BindingSet> evaluate(QuerySpec query) throws SailException
	{
		SearchHits result = evaluateQuery(query);

		// generate bindings
		return generateBindingSets(query, result);
	}

	/**
	 * Evaluates one Lucene Query. It distinguishes between two cases, the one
	 * where no subject is given and the one were it is given.
	 * 
	 * @param query
	 *        the Lucene query to evaluate
	 * @return QueryResult consisting of hits and highlighter
	 */
	private SearchHits evaluateQuery(QuerySpec query) {
		SearchHits hits = null;

		// get the subject of the query
		Resource subject = query.getSubject();

		try {
			// parse the query string to a lucene query

			String sQuery = query.getQueryString();

			if (!sQuery.isEmpty()) {
				QueryBuilder lucenequery = parseQuery(query.getQueryString(), query.getPropertyURI());
				SearchRequestBuilder request = client.prepareSearch();

				// if the query requests for the snippet, create a highlighter using
				// this query
				if (query.getSnippetVariableName() != null) {
					List<String> fields;
					if(query.getPropertyURI() != null) {
						fields = Collections.singletonList(query.getPropertyURI().toString());
					}
					else {
						fields = getPropertyFields(getFieldMappings().keySet());
					}
					for(String field : fields) {
						request.addHighlightedField(field);
					}
					request.setHighlighterPreTags(HIGHLIGHTER_PRE_TAG);
					request.setHighlighterPostTags(HIGHLIGHTER_POST_TAG);
					// Elastic Search doesn't really have the same support for fragments as Lucene.
					// So, we have to get back the whole highlighted value (comma-separated if it is a list)
					// and then post-process it into fragments ourselves.
					request.setHighlighterNumOfFragments(0);
				}

				// distinguish the two cases of subject == null
				if (subject == null) {
					hits = search(request, lucenequery);
				}
				else {
					hits = search(subject, request, lucenequery);
				}
			}
			else {
				hits = null;
			}
		}
		catch (Exception e) {
			logger.error("There was a problem evaluating query '" + query.getQueryString() + "' for property '"
					+ query.getPropertyURI() + "!", e);
		}

		return hits;
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
	private LinkedHashSet<BindingSet> generateBindingSets(QuerySpec query, SearchHits hits)
		throws SailException
	{
		// Since one resource can be returned many times, it can lead now to
		// multiple occurrences
		// of the same binding tuple in the BINDINGS clause. This in turn leads to
		// duplicate answers in the original SPARQL query.
		// We want to avoid this, so BindingSets added to the result must be
		// unique.
		LinkedHashSet<BindingSet> bindingSets = new LinkedHashSet<BindingSet>();

		if(hits != null) {
			// for each hit ...
			SearchHit[] docs = hits.getHits();
			for (int i = 0; i < docs.length; i++) {
				// this takes the new bindings
				QueryBindingSet derivedBindings = new QueryBindingSet();
	
				// get the current hit
				SearchHit hit = docs[i];

				Map<String,Object> doc = hit.getSource();
				// get the score of the hit
				float score = hit.getScore();

				// bind the respective variables
				String matchVar = query.getMatchesVariableName();
				if (matchVar != null) {
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
	
				if ((query.getScoreVariableName() != null) && (score > 0.0f))
					derivedBindings.addBinding(query.getScoreVariableName(), SearchFields.scoreToLiteral(score));
	
				if (query.getSnippetVariableName() != null) {
					Map<String,HighlightField> highlights = hit.getHighlightFields();
					if (highlights != null) {
						// limit to the queried field, if there was one
						List<String> fields;
						if (query.getPropertyURI() != null) {
							String fieldname = query.getPropertyURI().toString();
							fields = Collections.singletonList(fieldname);
						}
						else {
							fields = getPropertyFields(doc.keySet());
						}
	
						// extract snippets from Lucene's query results
						for (String field : fields) {
							HighlightField highlightField = highlights.get(field);
							if(highlightField != null) {
								Text[] fragments = highlightField.getFragments();
								for(Text fragment : fragments) {
									// create an individual binding set for each snippet
									QueryBindingSet snippetBindings = new QueryBindingSet(derivedBindings);

									String snippet = createSnippet(fragment.string());

									snippetBindings.addBinding(query.getSnippetVariableName(), new LiteralImpl(snippet));
		
									if (query.getPropertyVariableName() != null && query.getPropertyURI() == null) {
										snippetBindings.addBinding(query.getPropertyVariableName(), new URIImpl(field));
									}
		
									bindingSets.add(snippetBindings);
								}
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
		}

		// we succeeded
		return bindingSets;
	}

	private static String createSnippet(String highlightedValue)
	{
		if(highlightedValue.length() > 100) {
			StringBuilder buf = new StringBuilder();
			String separator = "";
			Matcher matcher = HIGHLIGHTER_PATTERN.matcher(highlightedValue);
			for(int i=0; i<2 && matcher.find(); i++) {
				buf.append(separator);
				buf.append(matcher.group());
				separator = "...";
			}
			highlightedValue = buf.toString();
		}
		return highlightedValue;
	}

	/**
	 * Returns the Resource corresponding with the specified Document number.
	 * Note that all of Lucene's restrictions of using document numbers apply.
	 */
	public Resource getResource(String documentNumber)
		throws IOException
	{
		GetResponse response = client.prepareGet(indexName, documentType, documentNumber).execute().actionGet();
		return response.isExists() ? getResource(response.getSource()) : null;
	}

	/**
	 * Returns the Resource corresponding with the specified Document.
	 */
	public Resource getResource(Map<String,Object> document) {
		String idString = (String) document.get(SearchFields.URI_FIELD_NAME);
		return SearchFields.createResource(idString);
	}

	private String getContextID(Map<String,Object> document) {
		return (String) document.get(SearchFields.CONTEXT_FIELD_NAME);
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
	public SearchHits search(String query)
		throws IOException
	{
		return search(client.prepareSearch(), parseQuery(query, null));
	}

	/**
	 * Evaluates the given query only for the given resource.
	 */
	public SearchHits search(Resource resource, SearchRequestBuilder request, QueryBuilder query)
	{
		// rewrite the query
		QueryBuilder idQuery = QueryBuilders.termQuery(SearchFields.URI_FIELD_NAME, SearchFields.getResourceID(resource));
		QueryBuilder combinedQuery = QueryBuilders.boolQuery().must(idQuery).must(query);
		return search(request, combinedQuery);
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
	public QueryBuilder parseQuery(String query, URI propertyURI)
	{
		return prepareQuery(propertyURI, QueryBuilders.queryStringQuery(query));
	}

	/**
	 * Evaluates the given query and returns the results as a TopDocs instance.
	 */
	public SearchHits search(SearchRequestBuilder request, QueryBuilder query)
	{
		long docCount = client.prepareCount(indexName).setQuery(query).execute().actionGet().getCount();
		int nDocs = Math.max((int) Math.min(docCount, Integer.MAX_VALUE), 1);
		SearchResponse response = request.setIndices(indexName).setTypes(documentType).setVersion(true).setQuery(query).setSize(nDocs).execute().actionGet();
		return response.getHits();
	}

	/**
	 * Gets the score for a particular Resource and query. Returns a value < 0
	 * when the Resource does not match the query.
	 * @throws IOException 
	 */
	public float getScore(Resource resource, String query, URI propertyURI) throws IOException
	{
		return getScore(resource, parseQuery(query, propertyURI));
	}

	/**
	 * Gets the score for a particular Resource and query. Returns a value < 0
	 * when the Resource does not match the query.
	 */
	public float getScore(Resource resource, QueryBuilder query)
		throws IOException
	{
		// rewrite the query
		QueryBuilder idQuery = QueryBuilders.termQuery(SearchFields.URI_FIELD_NAME, SearchFields.getResourceID(resource));
		QueryBuilder combinedQuery = QueryBuilders.boolQuery().must(idQuery).must(query);

		// fetch the score when the URI matches the original query
		SearchHits docs = client.prepareSearch(indexName).setTypes(documentType).setQuery(combinedQuery).setSize(1).execute().actionGet().getHits();
		if (docs.getTotalHits() == 0) {
			return -1f;
		}
		else {
			return docs.getAt(0).getScore();
		}
	}

	private QueryStringQueryBuilder prepareQuery(URI propertyURI, QueryStringQueryBuilder query) {
		// check out which query parser to use, based on the given property URI
		if (propertyURI == null)
			// if we have no property given, we create a default query parser which
			// has the TEXT_FIELD_NAME as the default field
			query.defaultField(SearchFields.TEXT_FIELD_NAME).analyzer(queryAnalyzer);
		else
			// otherwise we create a query parser that has the given property as
			// the default field
			query.defaultField(propertyURI.toString()).analyzer(queryAnalyzer);
		return query;
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
		throws IOException
	{
		// Buffer per resource
		MapOfListMaps<Resource, String, Statement> rsAdded = new MapOfListMaps<Resource, String, Statement>();
		MapOfListMaps<Resource, String, Statement> rsRemoved = new MapOfListMaps<Resource, String, Statement>();

		HashSet<Resource> resources = new HashSet<Resource>();
		for (Statement s : added) {
			rsAdded.add(s.getSubject(), SearchFields.getContextID(s.getContext()), s);
			resources.add(s.getSubject());
		}
		for (Statement s : removed) {
			rsRemoved.add(s.getSubject(), SearchFields.getContextID(s.getContext()), s);
			resources.add(s.getSubject());
		}

		logger.debug("Removing " + removed.size() + " statements, adding " + added.size() + " statements");

		BulkRequestBuilder bulkRequest = client.prepareBulk();
		// for each resource, add/remove
		for (Resource resource : resources) {
			Map<String, List<Statement>> stmtsToRemove = rsRemoved.get(resource);
			Map<String, List<Statement>> stmtsToAdd = rsAdded.get(resource);

			Set<String> contextsToUpdate = new HashSet<String>(stmtsToAdd.keySet());
			contextsToUpdate.addAll(stmtsToRemove.keySet());

			Map<String, Document> docsByContext = new HashMap<String, Document>();
			// is the resource in the store?
			// fetch the Document representing this Resource
			String resourceId = SearchFields.getResourceID(resource);
			List<Document> documents = getDocuments(QueryBuilders.termQuery(SearchFields.URI_FIELD_NAME, resourceId));

			for (Document doc : documents) {
				docsByContext.put(this.getContextID(doc.fields), doc);
			}

			for (String contextId : contextsToUpdate) {
				String id = SearchFields.formIdString(resourceId, contextId);

				Document document = docsByContext.get(contextId);
				if (document == null) {
					// there are no such Documents: create one now
					Map<String,Object> newDocument = new HashMap<String,Object>();
					addURIField(resourceId, newDocument);
					addContextField(contextId, newDocument);
					// add all statements, remember the contexts
					// HashSet<Resource> contextsToAdd = new HashSet<Resource>();
					List<Statement> list = stmtsToAdd.get(contextId);
					if (list != null) {
						for (Statement s : list) {
							addProperty(s, newDocument);
						}
					}

					// add it to the index
					bulkRequest.add(client.prepareIndex(indexName, documentType, id).setSource(newDocument));

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
					Map<String,Object> newDocument = new HashMap<String,Object>();
					// track if newDocument is actually different from document
					boolean mutated = false;

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
									removedOfResource.put(SearchFields.TEXT_FIELD_NAME, label);
								}
							}
						}
					}

					// add all existing fields (including uri, context, and text)
					// but without adding the removed ones
					// keep the predicate/value pairs to ensure that the statement
					// cannot be added twice
					SetMap<String, String> copiedProperties = new SetMap<String, String>();
					for (Map.Entry<String,Object> oldField : document.fields.entrySet()) {
						String oldFieldName = oldField.getKey();
						// which fields were removed?
						List<String> objectsRemoved = removedOfResource != null ? removedOfResource.get(oldFieldName) : null;

						List<String> oldValues = asStringList(oldField.getValue());
						for(String oldValue : oldValues) {
							// do not copy removed statements to the new version of the
							// document
							if ((objectsRemoved != null) && (objectsRemoved.contains(oldValue))) {
								mutated = true;
								continue;
							}
							addField(oldFieldName, oldValue, newDocument);
							copiedProperties.put(oldFieldName, oldValue);
						}
					}

					// add all statements to this document, except for those which
					// are already there
					{
						List<Statement> addedToResource = stmtsToAdd.get(contextId);
						String val;
						if (addedToResource != null && !addedToResource.isEmpty()) {
							for (Statement s : addedToResource) {
								val = SearchFields.getLiteralPropertyValueAsString(s);
								if (val != null) {
									if (!copiedProperties.containsKeyValuePair(s.getPredicate().stringValue(), val)) {
										addProperty(s, newDocument);
										mutated = true;
									}
								}
							}
						}
					}

					// update the index with the cloned document, if it contains any
					// meaningful non-system properties
					int nrProperties = numberOfPropertyFields(newDocument.keySet());
					if (nrProperties > 0) {
						if(mutated) {
							bulkRequest.add(client.prepareUpdate(indexName, documentType, id).setVersion(document.version).setDoc(newDocument));
						}
					}
					else {
						bulkRequest.add(client.prepareDelete(indexName, documentType, id).setVersion(document.version));
					}
				}
			}
		}
		if(bulkRequest.numberOfActions() > 0) {
			BulkResponse response = bulkRequest.execute().actionGet();
			if(response.hasFailures())
			{
				throw new IOException(response.buildFailureMessage());
			}
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
	public synchronized void clearContexts(Resource... contexts)
		throws IOException
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
			String contextString = SearchFields.getContextID(context);
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
			client.prepareDeleteByQuery(indexName).setQuery(QueryBuilders.termQuery(SearchFields.CONTEXT_FIELD_NAME, contextString)).execute().actionGet();
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

		String resourceId = SearchFields.getResourceID(subject);

		SetMap<String, Statement> stmtsByContextId = new SetMap<String, Statement>();

		String contextId;
		for (Statement statement : statements) {
			contextId = SearchFields.getContextID(statement.getContext());

			stmtsByContextId.put(contextId, statement);
		}

		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (Entry<String, Set<Statement>> entry : stmtsByContextId.entrySet()) {
			// create a new document
			Map<String,Object> newDocument = new HashMap<String,Object>();

			String id = SearchFields.formIdString(resourceId, entry.getKey());
			addURIField(resourceId, newDocument);
			addContextField(entry.getKey(), newDocument);

			for (Statement stmt : entry.getValue()) {
				// determine stuff to store
				addProperty(stmt, newDocument);
			}
			// add it to the index
			bulkRequest.add(client.prepareIndex(indexName, documentType, id).setSource(newDocument));
		}

		if(bulkRequest.numberOfActions() > 0) {
			BulkResponse response = bulkRequest.execute().actionGet();
			if(response.hasFailures()) {
				throw new IOException(response.buildFailureMessage());
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public synchronized void clear()
		throws IOException
	{
		doAcknowledgedRequest(client.admin().indices().prepareDelete(indexName));
		createIndex();
	}



	private static void doAcknowledgedRequest(ActionRequestBuilder<?, ? extends AcknowledgedResponse, ?, ?> request) throws IOException
	{
		boolean ok = request.execute().actionGet().isAcknowledged();
		if(!ok) {
			throw new IOException("Request not acknowledged: "+request.get().getClass().getName());
		}
	}

	private static void doIndexRequest(ActionRequestBuilder<?, ? extends IndexResponse, ?, ?> request) throws IOException
	{
		boolean ok = request.execute().actionGet().isCreated();
		if(!ok) {
			throw new IOException("Document not created: "+request.get().getClass().getName());
		}
	}

	private static void doUpdateRequest(ActionRequestBuilder<?, ? extends UpdateResponse, ?, ?> request) throws IOException
	{
		boolean ok = request.execute().actionGet().isCreated();
		if(!ok) {
			throw new IOException("Document not updated: "+request.get().getClass().getName());
		}
	}
}
