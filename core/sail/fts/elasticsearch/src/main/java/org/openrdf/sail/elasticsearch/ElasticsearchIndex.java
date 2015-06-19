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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterIndexHealth;
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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.sail.SailException;
import org.openrdf.sail.lucene.AbstractSearchIndex;
import org.openrdf.sail.lucene.BulkUpdater;
import org.openrdf.sail.lucene.LuceneSail;
import org.openrdf.sail.lucene.SearchDocument;
import org.openrdf.sail.lucene.SearchFields;
import org.openrdf.sail.lucene.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * @see LuceneSail
 */
public class ElasticsearchIndex extends AbstractSearchIndex {

	/**
	 * Set the parameter "indexName=" to specify the index to use.
	 */
	public static final String INDEX_NAME_KEY = "indexName";
	/**
	 * Set the parameter "documentType=" to specify the document type to use.
	 * By default, the document type is "resource".
	 */
	public static final String DOCUMENT_TYPE_KEY = "documentType";
	/**
	 * Set the parameter "waitForStatus=" to configure if {@link #initialize(java.util.Properties) initialization}
	 * should wait for a particular health status.
	 * The value can be one of "green" or "yellow".
	 * Does not wait by default.
	 */
	public static final String WAIT_FOR_STATUS_KEY = "waitForStatus";
	/**
	 * Set the parameter "waitForNodes=" to configure if {@link #initialize(java.util.Properties) initialization}
	 * should wait until the specified number of nodes are available.
	 * Does not wait by default.
	 */
	public static final String WAIT_FOR_NODES_KEY = "waitForNodes";
	/**
	 * Set the parameter "waitForActiveShards=" to configure if {@link #initialize(java.util.Properties) initialization}
	 * should wait until the specified number of shards to be active.
	 * Does not wait by default.
	 */
	public static final String WAIT_FOR_ACTIVE_SHARDS_KEY = "waitForActiveShards";
	/**
	 * Set the parameter "waitForRelocatingShards=" to configure if {@link #initialize(java.util.Properties) initialization}
	 * should wait until the specified number of nodes are relocating.
	 * Does not wait by default.
	 */
	public static final String WAIT_FOR_RELOCATING_SHARDS_KEY = "waitForRelocatingShards";

	public static final String DEFAULT_INDEX_NAME = "elastic-search-sail";
	public static final String DEFAULT_DOCUMENT_TYPE = "resource";
	public static final String DEFAULT_ANALYZER = "standard";

	public static final String ELASTICSEARCH_KEY_PREFIX = "elasticsearch.";

	// we do everything synchronously so no point using another thread
	private static final boolean OPERATION_THREADED = false;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Node node;

	private Client client;

	private String clusterName;
	private String indexName;
	private String documentType;

	private String analyzer;
	private String queryAnalyzer = "standard";

	public ElasticsearchIndex()
	{
	}

	public String getClusterName()
	{
		return clusterName;
	}

	public String getIndexName()
	{
		return indexName;
	}

	public String[] getTypes()
	{
		return new String[] {documentType};
	}

	@Override
	public void initialize(Properties parameters) throws Exception {
		super.initialize(parameters);
		indexName = parameters.getProperty(INDEX_NAME_KEY, DEFAULT_INDEX_NAME);
		documentType = parameters.getProperty(DOCUMENT_TYPE_KEY, DEFAULT_DOCUMENT_TYPE);
		analyzer = parameters.getProperty(LuceneSail.ANALYZER_CLASS_KEY, DEFAULT_ANALYZER);
		String dataDir = parameters.getProperty(LuceneSail.LUCENE_DIR_KEY);

		NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
		ImmutableSettings.Builder settingsBuilder = nodeBuilder.settings();
		for(Enumeration<?> iter = parameters.propertyNames(); iter.hasMoreElements(); ) {
			String propName = (String) iter.nextElement();
			if(propName.startsWith(ELASTICSEARCH_KEY_PREFIX)) {
				String esName = propName.substring(ELASTICSEARCH_KEY_PREFIX.length());
				settingsBuilder.put(esName, parameters.getProperty(propName));
			}
		}
		if(dataDir != null) {
			settingsBuilder.put("path.data", dataDir);
		}
		nodeBuilder.settings(settingsBuilder);
		node = nodeBuilder.node();
		clusterName = node.settings().get("cluster.name");
		client = node.client();

		boolean exists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
		if(!exists) {
			createIndex();
		}

		logger.info("Field mappings:\n{}", getMappings());

		ClusterHealthRequestBuilder healthReqBuilder = client.admin().cluster().prepareHealth(indexName);
		String waitForStatus = parameters.getProperty(WAIT_FOR_STATUS_KEY);
		if("green".equals(waitForStatus)) {
			healthReqBuilder.setWaitForGreenStatus();
		}
		else if("yellow".equals(waitForStatus)) {
			healthReqBuilder.setWaitForYellowStatus();
		}
		String waitForNodes = parameters.getProperty(WAIT_FOR_NODES_KEY);
		if(waitForNodes != null) {
			healthReqBuilder.setWaitForNodes(waitForNodes);
		}
		String waitForActiveShards = parameters.getProperty(WAIT_FOR_ACTIVE_SHARDS_KEY);
		if(waitForActiveShards != null) {
			healthReqBuilder.setWaitForActiveShards(Integer.parseInt(waitForActiveShards));
		}
		String waitForRelocatingShards = parameters.getProperty(WAIT_FOR_RELOCATING_SHARDS_KEY);
		if(waitForRelocatingShards != null) {
			healthReqBuilder.setWaitForRelocatingShards(Integer.parseInt(waitForRelocatingShards));
		}
		ClusterHealthResponse healthResponse = healthReqBuilder.execute().actionGet();
		logger.info("Cluster health: {}", healthResponse.getStatus());
		logger.info("Cluster nodes: {} (data {})", healthResponse.getNumberOfNodes(), healthResponse.getNumberOfDataNodes());
		ClusterIndexHealth indexHealth = healthResponse.getIndices().get(indexName);
		logger.info("Index health: {}", indexHealth.getStatus());
		logger.info("Index shards: {} (active {} [primary {}], initializing {}, unassigned {}, relocating {})", indexHealth.getNumberOfShards(),
				indexHealth.getActiveShards(), indexHealth.getActivePrimaryShards(),
				indexHealth.getInitializingShards(), indexHealth.getUnassignedShards(), indexHealth.getRelocatingShards());
		for(String err : healthResponse.getAllValidationFailures()) {
			logger.warn(err);
		}
	}

	public Map<String,Object> getMappings() throws IOException
	{
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> indexMappings = client.admin().indices().prepareGetMappings(indexName).setTypes(documentType).execute().actionGet().getMappings();
		ImmutableOpenMap<String, MappingMetaData> typeMappings = indexMappings.get(indexName);
		MappingMetaData mappings = typeMappings.get(documentType);
		return mappings.sourceAsMap();
	}

	private void createIndex() throws IOException
	{
		String settings = XContentFactory.jsonBuilder()
			.startObject()
				.field("index.query.default_field", SearchFields.TEXT_FIELD_NAME)
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

	// //////////////////////////////// Methods for updating the index

	/**
	 * Returns a Document representing the specified document ID (combination of
	 * resource and context), or null when no such Document exists yet.
	 */
	@Override
	protected SearchDocument getDocument(String id) throws IOException
	{
		GetResponse response = client.prepareGet(indexName, documentType, id).setOperationThreaded(OPERATION_THREADED).execute().actionGet();
		if(response.isExists()) {
			return new ElasticsearchDocument(response.getId(), response.getType(), response.getIndex(), response.getVersion(), response.getSource());
		}
		// no such Document
		return null;
	}

	@Override
	protected Iterable<? extends SearchDocument> getDocuments(String resourceId) throws IOException {
		SearchHits hits = getDocuments(QueryBuilders.termQuery(SearchFields.URI_FIELD_NAME, resourceId));
		return Iterables.transform(hits, new Function<SearchHit,SearchDocument>()
		{
			@Override
			public SearchDocument apply(SearchHit hit) {
				return new ElasticsearchDocument(hit);
			}
		});
	}

	@Override
	protected SearchDocument newDocument(String id, String resourceId, String context)
	{
		return new ElasticsearchDocument(id, documentType, indexName, resourceId, context);
	}

	@Override
	protected SearchDocument copyDocument(SearchDocument doc)
	{
		ElasticsearchDocument esDoc = (ElasticsearchDocument) doc;
		Map<String,Object> source = esDoc.getSource();
		Map<String,Object> newDocument = new HashMap<String,Object>(source);
		return new ElasticsearchDocument(esDoc.getId(), esDoc.getType(), esDoc.getIndex(), esDoc.getVersion(), newDocument);
	}

	@Override
	protected void addDocument(SearchDocument doc) throws IOException
	{
		ElasticsearchDocument esDoc = (ElasticsearchDocument) doc;
		doIndexRequest(client.prepareIndex(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setSource(esDoc.getSource()).setOperationThreaded(OPERATION_THREADED));
	}

	@Override
	protected void updateDocument(SearchDocument doc) throws IOException
	{
		ElasticsearchDocument esDoc = (ElasticsearchDocument) doc;
		doUpdateRequest(client.prepareUpdate(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setVersion(esDoc.getVersion()).setDoc(esDoc.getSource()));
	}

	@Override
	protected void deleteDocument(SearchDocument doc) throws IOException
	{
		ElasticsearchDocument esDoc = (ElasticsearchDocument) doc;
		client.prepareDelete(esDoc.getIndex(), esDoc.getType(), esDoc.getId()).setVersion(esDoc.getVersion()).setOperationThreaded(OPERATION_THREADED).execute().actionGet();
	}

	@Override
	protected BulkUpdater newBulkUpdate()
	{
		return new ElasticsearchBulkUpdater(client);
	}

	/**
	 * Returns a list of Documents representing the specified Resource (empty
	 * when no such Document exists yet). Each document represent a set of
	 * statements with the specified Resource as a subject, which are stored in a
	 * specific context
	 */
	private SearchHits getDocuments(QueryBuilder query)
		throws IOException
	{
		return search(client.prepareSearch(), query);
	}

	/**
	 * Returns a Document representing the specified Resource & Context
	 * combination, or null when no such Document exists yet.
	 */
	public SearchDocument getDocument(Resource subject, Resource context)
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
	public Iterable<? extends SearchDocument> getDocuments(Resource subject)
		throws IOException
	{
		String resourceId = SearchFields.getResourceID(subject);
		return getDocuments(resourceId);
	}

	/**
	 * Filters the given list of fields, retaining all property fields.
	 */
	public static List<String> getPropertyFields(Collection<String> fields) {
		List<String> result = new ArrayList<String>(fields.size());
		for (String field : fields) {
			if (SearchFields.isPropertyField(field))
				result.add(field);
		}
		return result;
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

	/**
	 * Parse the passed query.
	 * 
	 * @param query
	 *        string
	 * @return the parsed query
	 * @throws ParseException
	 *         when the parsing brakes
	 */
	@Override
	protected SearchQuery parseQuery(String query, URI propertyURI) throws MalformedQueryException
	{
		QueryBuilder qb = prepareQuery(propertyURI, QueryBuilders.queryStringQuery(query));
		return new ElasticsearchQuery(client.prepareSearch(), qb, this);
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
	 * Evaluates the given query and returns the results as a TopDocs instance.
	 */
	public SearchHits search(SearchRequestBuilder request, QueryBuilder query)
	{
		String[] types = getTypes();
		int nDocs;
		if(maxDocs > 0) {
			nDocs = maxDocs;
		}
		else {
			long docCount = client.prepareCount(indexName).setTypes(types).setQuery(query).execute().actionGet().getCount();
			nDocs = Math.max((int) Math.min(docCount, Integer.MAX_VALUE), 1);
		}
		SearchResponse response = request.setIndices(indexName).setTypes(types).setVersion(true).setQuery(query).setSize(nDocs).execute().actionGet();
		return response.getHits();
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

	private static long doIndexRequest(ActionRequestBuilder<?, ? extends IndexResponse, ?, ?> request) throws IOException
	{
		IndexResponse response = request.execute().actionGet();
		boolean ok = response.isCreated();
		if(!ok) {
			throw new IOException("Document not created: "+request.get().getClass().getName());
		}
		return response.getVersion();
	}

	private static long doUpdateRequest(ActionRequestBuilder<?, ? extends UpdateResponse, ?, ?> request) throws IOException
	{
		UpdateResponse response = request.execute().actionGet();
		boolean isUpsert = response.isCreated();
		if(isUpsert) {
			throw new IOException("Unexpected upsert: "+request.get().getClass().getName());
		}
		return response.getVersion();
	}
}
