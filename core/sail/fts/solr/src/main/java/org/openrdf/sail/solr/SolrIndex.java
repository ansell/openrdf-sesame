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
package org.openrdf.sail.solr;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
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
public class SolrIndex extends AbstractSearchIndex {

	public static final String SERVER_KEY = "server";

	public static final String GEO_FIELD_PREFIX = "_geo_";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private SolrClient client;

	public SolrIndex()
	{
	}

	@Override
	public void initialize(Properties parameters) throws Exception {
		super.initialize(parameters);
		String server = parameters.getProperty(SERVER_KEY);
		if(server == null) {
			throw new SailException("Missing "+SERVER_KEY+" parameter");
		}
		int pos = server.indexOf(':');
		if(pos == -1) {
			throw new SailException("Missing scheme in "+SERVER_KEY+" parameter: "+server);
		}
		String scheme = server.substring(0, pos);
		Class<?> clientFactoryCls = Class.forName("org.openrdf.sail.solr.client."+scheme+".Factory");
		SolrClientFactory clientFactory = (SolrClientFactory) clientFactoryCls.newInstance();
		client = clientFactory.create(server);
	}

	public SolrClient getClient()
	{
		return client;
	}

	@Override
	public void shutDown()
		throws IOException
	{
		if(client != null) {
			client.close();
			client = null;
		}
	}

	// //////////////////////////////// Methods for updating the index

	/**
	 * Returns a Document representing the specified document ID (combination of
	 * resource and context), or null when no such Document exists yet.
	 * @throws SolrServerException 
	 */
	@Override
	protected SearchDocument getDocument(String id) throws IOException
	{
		SolrDocument doc;
		try {
			doc = (SolrDocument) client.query(new SolrQuery().setRequestHandler("/get").set(SearchFields.ID_FIELD_NAME, id)).getResponse().get("doc");
		} catch (SolrServerException e) {
			throw new IOException(e);
		}
		return (doc != null) ? new SolrSearchDocument(doc) : null;
	}

	@Override
	protected Iterable<? extends SearchDocument> getDocuments(String resourceId) throws IOException {
		SolrQuery query = new SolrQuery(termQuery(SearchFields.URI_FIELD_NAME, resourceId));
		SolrDocumentList docs;
		try {
			docs = getDocuments(query);
		} catch (SolrServerException e) {
			throw new IOException(e);
		}
		return Iterables.transform(docs, new Function<SolrDocument,SearchDocument>()
		{
			@Override
			public SearchDocument apply(SolrDocument hit) {
				return new SolrSearchDocument(hit);
			}
		});
	}

	@Override
	protected SearchDocument newDocument(String id, String resourceId, String context)
	{
		return new SolrSearchDocument(id, resourceId, context);
	}

	@Override
	protected SearchDocument copyDocument(SearchDocument doc)
	{
		SolrDocument document = ((SolrSearchDocument)doc).getDocument();
		SolrDocument newDocument = new SolrDocument();
		newDocument.putAll(document);
		return new SolrSearchDocument(newDocument);
	}

	@Override
	protected void addDocument(SearchDocument doc) throws IOException
	{
		SolrDocument document = ((SolrSearchDocument)doc).getDocument();
		try {
			client.add(ClientUtils.toSolrInputDocument(document));
		} catch (SolrServerException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected void updateDocument(SearchDocument doc) throws IOException
	{
		addDocument(doc);
	}

	@Override
	protected void deleteDocument(SearchDocument doc) throws IOException
	{
		try {
			client.deleteById(doc.getId());
		} catch (SolrServerException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected BulkUpdater newBulkUpdate()
	{
		return new SolrBulkUpdater(client);
	}

	static String termQuery(String field, String value)
	{
		return field+":\""+value+"\"";
	}

	/**
	 * Returns a list of Documents representing the specified Resource (empty
	 * when no such Document exists yet). Each document represent a set of
	 * statements with the specified Resource as a subject, which are stored in a
	 * specific context
	 */
	private SolrDocumentList getDocuments(SolrQuery query)
		throws SolrServerException, IOException
	{
		return search(query).getResults();
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
	public static Set<String> getPropertyFields(Set<String> fields) {
		Set<String> result = new HashSet<String>(fields.size());
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
		try {
			client.commit();
		} catch (SolrServerException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void rollback() throws IOException
	{
		try {
			client.rollback();
		} catch (SolrServerException e) {
			throw new IOException(e);
		}
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
		SolrQuery q = prepareQuery(propertyURI, new SolrQuery(query));
		return new SolrSearchQuery(q, this);
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
	 * @throws SolrServerException 
	 */
	public QueryResponse search(Resource resource, SolrQuery query) throws SolrServerException, IOException
	{
		// rewrite the query
		String idQuery = termQuery(SearchFields.URI_FIELD_NAME, SearchFields.getResourceID(resource));
		query.setQuery(query.getQuery()+" AND "+idQuery);
		return search(query);
	}

	/**
	 * Evaluates the given query and returns the results as a TopDocs instance.
	 * @throws SolrServerException 
	 */
	public QueryResponse search(SolrQuery query) throws SolrServerException, IOException
	{
		int nDocs;
		if(maxDocs > 0) {
			nDocs = maxDocs;
		}
		else {
			long docCount = client.query(query.setRows(0)).getResults().getNumFound();
			nDocs = Math.max((int) Math.min(docCount, Integer.MAX_VALUE), 1);
		}
		return client.query(query.setRows(nDocs));
	}

	private SolrQuery prepareQuery(URI propertyURI, SolrQuery query) {
		// check out which query parser to use, based on the given property URI
		if (propertyURI == null)
			// if we have no property given, we create a default query parser which
			// has the TEXT_FIELD_NAME as the default field
			query.set(CommonParams.DF, SearchFields.TEXT_FIELD_NAME);
		else
			// otherwise we create a query parser that has the given property as
			// the default field
			query.set(CommonParams.DF, propertyURI.toString());
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

		try {
			// remove all contexts passed
			for (Resource context : contexts) {
				// attention: context can be NULL!
				String contextString = SearchFields.getContextID(context);
				client.deleteByQuery(termQuery(SearchFields.CONTEXT_FIELD_NAME, contextString));
			}
		} catch (SolrServerException e) {
			throw new IOException(e);
		}
	}

	/**
	 * 
	 */
	@Override
	public synchronized void clear()
		throws IOException
	{
		try {
			client.deleteByQuery("*:*");
		} catch (SolrServerException e) {
			throw new IOException(e);
		}
	}
}
