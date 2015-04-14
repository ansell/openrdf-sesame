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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.SailException;
import org.openrdf.sail.lucene.util.MapOfListMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public abstract class AbstractSearchIndex implements SearchIndex {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final Set<String> REJECTED_DATATYPES = new HashSet<String>();

	static {
		REJECTED_DATATYPES.add("http://www.w3.org/2001/XMLSchema#float");
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



	/**
	 * Indexes the specified Statement.
	 */
	@Override
	public final synchronized void addStatement(Statement statement)
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
		SearchDocument document = getDocument(id);

		if (document == null) {
			// there is no such Document: create one now
			document = newDocument(id, resourceId, context);
			document.addProperty(field, text);

			// add it to the index
			addDocument(document);
		}
		else {
			// update this Document when this triple has not been stored already
			if (!document.hasProperty(field, text)) {
				// create a copy of the old document; updating the retrieved
				// Document instance works ok for stored properties but indexed data
				// gets lots when doing an IndexWriter.updateDocument with it
				SearchDocument newDocument = copyDocument(document);

				// add the new triple to the cloned document
				newDocument.addProperty(field, text);

				// update the index with the cloned document
				updateDocument(newDocument);
			}
		}
	}

	@Override
	public final synchronized void removeStatement(Statement statement)
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
		SearchDocument document = getDocument(id);

		if (document != null) {
			// determine the values used in the index for this triple
			String fieldName = statement.getPredicate().toString();
			String text = ((Literal)object).getLabel();

			// see if this triple occurs in this Document
			if (document.hasProperty(fieldName, text)) {
				// if the Document only has one predicate field, we can remove the
				// document
				List<String> propertyNames = document.getPropertyNames(); // one or more
				List<String> propertyValues = document.getProperty(fieldName); // zero or more
				if (propertyNames.size() == 1 && propertyValues.size() == 1) {
					deleteDocument(id);
				}
				else {
					// there are more triples encoded in this Document: remove the
					// document and add a new Document without this triple
					SearchDocument newDocument = newDocument(id, resourceId, contextId);

					for (String oldFieldName : document.getPropertyNames()) {
						List<String> oldValues = document.getProperty(oldFieldName);
						boolean isField = fieldName.equals(oldFieldName);
						for(String oldValue : oldValues) {
							if (!(isField && text.equals(oldValue))) {
								newDocument.addProperty(oldFieldName, oldValue);
							}
						}
					}

					updateDocument(newDocument);
				}
			}
		}
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
	public final synchronized void addRemoveStatements(Collection<Statement> added, Collection<Statement> removed)
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

		BulkUpdater updater = newBulkUpdate();
		// for each resource, add/remove
		for (Resource resource : resources) {
			Map<String, List<Statement>> stmtsToRemove = rsRemoved.get(resource);
			Map<String, List<Statement>> stmtsToAdd = rsAdded.get(resource);

			Set<String> contextsToUpdate = new HashSet<String>(stmtsToAdd.keySet());
			contextsToUpdate.addAll(stmtsToRemove.keySet());

			Map<String, SearchDocument> docsByContext = new HashMap<String, SearchDocument>();
			// is the resource in the store?
			// fetch the Document representing this Resource
			String resourceId = SearchFields.getResourceID(resource);
			Iterable<? extends SearchDocument> documents = getDocuments(resourceId);

			for (SearchDocument doc : documents) {
				docsByContext.put(doc.getContext(), doc);
			}

			for (String contextId : contextsToUpdate) {
				String id = SearchFields.formIdString(resourceId, contextId);

				SearchDocument document = docsByContext.get(contextId);
				if (document == null) {
					// there are no such Documents: create one now
					document = newDocument(id, resourceId, contextId);
					// add all statements, remember the contexts
					// HashSet<Resource> contextsToAdd = new HashSet<Resource>();
					List<Statement> list = stmtsToAdd.get(contextId);
					if (list != null) {
						for (Statement s : list) {
							addProperty(s, document);
						}
					}

					// add it to the index
					updater.add(document);

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
					SearchDocument newDocument = newDocument(id, resourceId, contextId);
					// track if newDocument is actually different from document
					boolean mutated = false;

					// buffer the removed literal statements
					SetMultimap<String, String> removedOfResource = null;
					{
						List<Statement> removedStatements = stmtsToRemove.get(contextId);
						if (removedStatements != null && !removedStatements.isEmpty()) {
							removedOfResource = HashMultimap.create();
							for (Statement r : removedStatements) {
								if (r.getObject() instanceof Literal) {
									// remove value from both property field and the
									// corresponding text field
									String label = ((Literal)r.getObject()).getLabel();
									removedOfResource.put(r.getPredicate().toString(), label);
								}
							}
						}
					}

					// add all existing property fields
					// but without adding the removed ones
					// keep the predicate/value pairs to ensure that the statement
					// cannot be added twice
					SetMultimap<String, String> copiedProperties = HashMultimap.create();
					for (String oldFieldName : document.getPropertyNames()) {
						// which fields were removed?
						Set<String> objectsRemoved = (removedOfResource != null) ? removedOfResource.get(oldFieldName) : null;

						List<String> oldValues = document.getProperty(oldFieldName);
						for(String oldValue : oldValues) {
							// do not copy removed statements to the new version of the
							// document
							if ((objectsRemoved != null) && (objectsRemoved.contains(oldValue))) {
								mutated = true;
								continue;
							}
							newDocument.addProperty(oldFieldName, oldValue);
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
									if (!copiedProperties.containsEntry(s.getPredicate().stringValue(), val)) {
										addProperty(s, newDocument);
										mutated = true;
									}
								}
							}
						}
					}

					// update the index with the cloned document, if it contains any
					// meaningful non-system properties
					int nrProperties = newDocument.getPropertyNames().size();
					if (nrProperties > 0) {
						if(mutated) {
							updater.update(newDocument);
						}
					}
					else {
						updater.delete(id);
					}
				}
			}
		}
		updater.end();
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
	public final synchronized void addDocuments(Resource subject, List<Statement> statements)
		throws IOException
	{

		String resourceId = SearchFields.getResourceID(subject);

		SetMultimap<String, Statement> stmtsByContextId = HashMultimap.create();

		String contextId;
		for (Statement statement : statements) {
			contextId = SearchFields.getContextID(statement.getContext());

			stmtsByContextId.put(contextId, statement);
		}

		BulkUpdater batch = newBulkUpdate();
		for (Entry<String, Collection<Statement>> entry : stmtsByContextId.asMap().entrySet()) {
			// create a new document
			String id = SearchFields.formIdString(resourceId, entry.getKey());
			SearchDocument document = newDocument(id, resourceId, entry.getKey());

			for (Statement stmt : entry.getValue()) {
				// determine stuff to store
				addProperty(stmt, document);
			}
			// add it to the index
			batch.add(document);
		}
		batch.end();
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
	private void addProperty(Statement statement, SearchDocument document) {
		String text = SearchFields.getLiteralPropertyValueAsString(statement);
		if (text == null)
			return;
		String field = statement.getPredicate().toString();
		document.addProperty(field, text);
	}



	@Override
	public final Collection<BindingSet> evaluate(QuerySpec query) throws SailException
	{
		Iterable<? extends DocumentScore> result = evaluateQuery(query);

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
	private Iterable<? extends DocumentScore> evaluateQuery(QuerySpec query) {
		Iterable<? extends DocumentScore> hits = null;

		// get the subject of the query
		Resource subject = query.getSubject();

		try {
			// parse the query string to a lucene query

			String sQuery = query.getQueryString();

			if (!sQuery.isEmpty()) {
				SearchQuery searchQuery = parseQuery(query.getQueryString(), query.getPropertyURI());

				// if the query requests for the snippet, create a highlighter using
				// this query
				if (query.getSnippetVariableName() != null) {
					searchQuery.highlight(query.getPropertyURI());
				}

				// distinguish the two cases of subject == null
				hits = searchQuery.query(subject);
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
	 * @return a LinkedHashSet containing generated bindings
	 * @throws SailException
	 */
	private Collection<BindingSet> generateBindingSets(QuerySpec query, Iterable<? extends DocumentScore> hits)
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
			for (DocumentScore hit : hits) {
				// this takes the new bindings
				QueryBindingSet derivedBindings = new QueryBindingSet();
	
				// get the current hit
				SearchDocument doc = hit.getDocument();
				if (doc == null)
					continue;
	
				// get the score of the hit
				float score = hit.getScore();
	
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
					derivedBindings.addBinding(query.getScoreVariableName(), SearchFields.scoreToLiteral(score));
	
				if (query.getSnippetVariableName() != null) {
					if (hit.isHighlighted()) {
						// limit to the queried field, if there was one
						List<String> fields;
						if (query.getPropertyURI() != null) {
							String fieldname = query.getPropertyURI().toString();
							fields = Collections.singletonList(fieldname);
						}
						else {
							fields = doc.getPropertyNames();
						}
	
						// extract snippets from Lucene's query results
						for (String field : fields) {
							Iterable<String> snippets = hit.getSnippets(field);
							if(snippets != null) {
								for(String snippet : snippets) {
									if(snippet != null && !snippet.isEmpty()) {
										// create an individual binding set for each snippet
										QueryBindingSet snippetBindings = new QueryBindingSet(derivedBindings);
			
										snippetBindings.addBinding(query.getSnippetVariableName(), new LiteralImpl(snippet));
			
										if (query.getPropertyVariableName() != null && query.getPropertyURI() == null) {
											snippetBindings.addBinding(query.getPropertyVariableName(), new URIImpl(field));
										}
			
										bindingSets.add(snippetBindings);
									}
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

	/**
	 * Returns the Resource corresponding with the specified Document.
	 */
	protected Resource getResource(SearchDocument document) {
		return SearchFields.createResource(document.getResource());
	}



	protected abstract SearchDocument getDocument(String id) throws IOException;
	protected abstract Iterable<? extends SearchDocument> getDocuments(String resourceId) throws IOException;
	protected abstract SearchDocument newDocument(String id, String resourceId, String context);
	protected abstract SearchDocument copyDocument(SearchDocument doc);
	protected abstract void addDocument(SearchDocument doc) throws IOException;
	protected abstract void updateDocument(SearchDocument doc) throws IOException;
	protected abstract void deleteDocument(String id) throws IOException;

	protected abstract SearchQuery parseQuery(String q, URI property) throws MalformedQueryException;

	protected abstract BulkUpdater newBulkUpdate();
}
