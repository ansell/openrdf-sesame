/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;

import org.openrdf.OpenRDFException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 * Provides an interface to the private repository with the saved queries.
 * 
 * @author Dale Visser
 */
public class QueryStorage {

	private static final String FOLDER = ".queries";

	private static final String ORWB = "PREFIX orwb: <https://openrdf.org/workbench/>\n";

	private static final String SAVE = "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n" + ORWB
			+ "INSERT DATA { BIND (UUID() AS ?query)\n"
			+ "?query orwb:userName $<userName> ; orwb:queryName $<queryName> ; "
			+ "orwb:repository $<repository> ; orwb:shared $<shared> ; "
			+ "orwb:queryLanguage $<queryLanguage> ; orwb:query $<queryText> ;"
			+ "orwb:rowsPerPage $<rowsPerPage> . }";

	private static final String ASK_EXISTS = ORWB
			+ "ASK { [] orwb:userName $<userName> ; orwb:queryName $<queryName> ; orwb:repository $<repository> . }";

	private static final String FILTER = "FILTER (?user == $<userName> || ?user == “” ) }";

	private static final String DELETE = ORWB + "DELETE WHERE { $<query> orwb:userName ?user ; ?p ?o . "
			+ FILTER;

	private static final String MATCH = "orwb:shared ?s ; orwb:queryLanguage ?ql ; orwb:query ?q ; orwb:rowsPerPage ?rpp . }\n";

	private static final String UPDATE = ORWB + "DELETE { $<query> " + MATCH
			+ "INSERT { ?<query> orwb:shared $<shared> ; "
			+ "orwb:queryLanguage $<queryLanguage> ; orwb:query $<queryText> ; "
			+ "orwb:rowsPerPage $<rowsPerPage> . }\n" + "WHERE { $<query> orwb:userName ?user ; " + MATCH
			+ FILTER;

	private static final String SELECT = "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n" + ORWB
			+ "SELECT ?query ?user ?queryName ?shared ?queryLn ?queryText ?rowsPerPage "
			+ "{ ?query orwb:repository $<repository> ; orwb:userName ?user ; orwb:queryName ?queryName ; "
			+ "orwb:shared ?shared ; orwb:queryLanguage ?queryLn ; orwb:query ?queryText ; "
			+ "orwb:rowsPerPage ?rowsPerPage .\n" + "FILTER (?user == $<user> || ?user == “” || ?shared) }\n"
			+ "ORDER BY ?user ?queryName";

	private final Repository queries;

	/**
	 * Create a new object for accessing the store of user queries.
	 * 
	 * @param context
	 *        the servlet context, used for determining the local repository
	 *        location
	 * @throws RepositoryException
	 *         if there is an issue creating the object to access the repository
	 */
	public QueryStorage(final ServletContext context)
		throws RepositoryException
	{
		final String folder = FilenameUtils.concat(context.getRealPath(""), FOLDER);
		queries = new SailRepository(new NativeStore(new File(folder)));
		queries.initialize();
	}

	/**
	 * Checks whether the current user/password credentials can really access the
	 * current repository.
	 * 
	 * @param url
	 *        the URL of the repository for which queries are saved
	 * @param user
	 *        the name of the current user
	 * @param password
	 *        the password of the current user
	 * @return true, if it is possible to request the size of the repository with
	 *         the given credentials
	 */
	public boolean checkAccess(final URL url, final String user, final String password) {
		boolean rval = true;
		final HTTPRepository remote = new HTTPRepository(url.toString());
		remote.setUsernameAndPassword(user, password);
		try {
			remote.getConnection().size();
		}
		catch (RepositoryException re) {
			rval = false;
		}
		return rval;
	}

	/**
	 * Save a query. UNSAFE from an injection point of view. It is the
	 * responsibility of the calling code to call checkAccess() with the full
	 * credentials first.
	 * 
	 * @param repository
	 *        the repository the query is associated with
	 * @param queryName
	 *        the name for the query
	 * @param userName
	 *        the user saving the query
	 * @param shared
	 *        whether the query is to be shared with other users
	 * @param queryLanguage
	 *        the language, SeRQL or SPARQL, of the query
	 * @param queryText
	 *        the actual query text
	 * @param rowsPerPage
	 *        rows to display per page, may be 0 (all), 10, 50, 100, or 200)
	 * @throws OpenRDFException
	 */
	public void saveQuery(final URL repository, final String queryName, final String userName,
			final boolean shared, final QueryLanguage queryLanguage, final String queryText,
			final int rowsPerPage)
		throws OpenRDFException
	{
		if (QueryLanguage.SPARQL != queryLanguage && QueryLanguage.SERQL != queryLanguage) {
			throw new RepositoryException("May only save SPARQL or SeRQL queries, not"
					+ queryLanguage.toString());
		}
		if (0 != rowsPerPage && 10 != rowsPerPage && 20 != rowsPerPage && 50 != rowsPerPage
				&& 100 != rowsPerPage)
		{
			throw new RepositoryException("Illegal value for rows per page: " + rowsPerPage);
		}
		final QueryStringBuilder save = new QueryStringBuilder(SAVE);
		save.replaceRepository(repository);
		save.replaceQueryName(queryName);
		save.replaceUpdateFields(userName, shared, queryLanguage, queryText, rowsPerPage);
		updateQueryRepository(save.toString());
	}

	public boolean askExists(final URL repository, final String queryName, final String userName)
		throws QueryEvaluationException, RepositoryException, MalformedQueryException {
		final QueryStringBuilder ask = new QueryStringBuilder(ASK_EXISTS);
		ask.replaceRepository(repository);
		ask.replaceQueryName(queryName);
		ask.replaceUserName(userName);
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			return connection.prepareBooleanQuery(QueryLanguage.SPARQL, ask.toString()).evaluate();
		}
		finally {
			connection.close();
		}
	}

	/**
	 * Delete the given query for the given user. It is the responsibility of the
	 * calling code to call checkAccess() with the full credentials first.
	 * 
	 * @param query
	 * @param userName
	 * @throws RepositoryException
	 * @throws UpdateExecutionException
	 * @throws MalformedQueryException
	 */
	public void deleteQuery(final URI query, final String userName)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		final QueryStringBuilder delete = new QueryStringBuilder(DELETE);
		delete.replaceUserName(userName);
		delete.replace("$<query>", QueryStringBuilder.uriQuote(query));
		updateQueryRepository(delete.toString());
	}

	/**
	 * Update the entry for the given query. It is the responsibility of the
	 * calling code to call checkAccess() with the full credentials first.
	 * 
	 * @param query
	 *        the query to update
	 * @param userName
	 *        the user name
	 * @param shared
	 *        whether to share with other users
	 * @param queryLanguage
	 *        the query language
	 * @param queryText
	 *        the text of the query
	 * @param rowsPerPage
	 *        the rows per page to display of the query
	 * @throws RepositoryException
	 *         if a problem occurs during the update
	 * @throws UpdateExecutionException
	 *         if a problem occurs during the update
	 * @throws MalformedQueryException
	 *         if a problem occurs during the update
	 */
	public void updateQuery(final URI query, final String userName, final boolean shared,
			final QueryLanguage queryLanguage, final String queryText, final int rowsPerPage)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		final QueryStringBuilder update = new QueryStringBuilder(UPDATE);
		update.replaceUpdateFields(userName, shared, queryLanguage, queryText, rowsPerPage);
		this.updateQueryRepository(update.toString());
	}

	/**
	 * Executes a query to retrieve the queries accessible to the given user in
	 * the given repository. The query result will have the following binding
	 * names: query, user, queryName, shared, queryLn, queryText, rowsPerPage. It
	 * is the responsibility of the calling code to call checkAccess() with the
	 * full credentials first.
	 * 
	 * @param repository
	 *        that the saved queries run against
	 * @param userName
	 *        that is requesting the saved queries
	 * @return a query result listing all the saved queries against the given
	 *         repository and accessible to the given user
	 * @throws RepositoryException
	 *         if there's a problem connecting to the saved queries repository
	 * @throws MalformedQueryException
	 *         if the query is not legal SPARQL
	 * @throws QueryEvaluationException
	 *         if there is a problem while attempting to evaluate the query
	 */
	public TupleQueryResult selectQueries(final URL repository, final String userName)
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		final QueryStringBuilder select = new QueryStringBuilder(SELECT);
		select.replaceUserName(userName);
		select.replaceRepository(repository);
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			return connection.prepareTupleQuery(QueryLanguage.SPARQL, select.toString()).evaluate();
		}
		finally {
			connection.close();
		}
	}

	private void updateQueryRepository(final String update)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			connection.prepareUpdate(QueryLanguage.SPARQL, update).execute();
		}
		finally {
			connection.close();
		}
	}
}
