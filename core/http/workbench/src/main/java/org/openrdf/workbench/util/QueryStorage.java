/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import java.io.File;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.workbench.exceptions.BadRequestException;

/**
 * Provides an interface to the private repository with the saved queries.
 * 
 * @author Dale Visser
 */
public final class QueryStorage {

	private static final Object LOCK = new Object();

	private static final QueryEvaluator EVAL = QueryEvaluator.INSTANCE;

	private static QueryStorage instance;

	public static QueryStorage getSingletonInstance(final ServletContext context)
		throws RepositoryException
	{
		synchronized (LOCK) {
			if (instance == null) {
				instance = new QueryStorage(context);
			}
			return instance;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryStorage.class);

	private static final String FOLDER = ".queries";

	private static final String PRE = "PREFIX : <https://openrdf.org/workbench/>\n";

	// SAVE needs xsd: prefix since explicit XSD data types will be substituted.
	private static final String SAVE = "PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\n"
			+ PRE
			+ "INSERT DATA { $<query> :userName $<userName> ; :queryName $<queryName> ; "
			+ ":repository $<repository> ; :shared $<shared> ; :queryLanguage $<queryLanguage> ; :query $<queryText> ; "
			+ ":infer $<infer> ; :rowsPerPage $<rowsPerPage> . }";

	private static final String ASK_EXISTS = PRE
			+ "ASK { [] :userName $<userName> ; :queryName $<queryName> ; :repository $<repository> . }";

	private static final String FILTER = "FILTER (?user = $<userName> || ?user = \"\" ) }";

	private static final String CAN_DELETE = PRE + "ASK { $<query> :userName ?user . " + FILTER;

	private static final String DELETE = PRE + "DELETE WHERE { $<query> :userName ?user ; ?p ?o . }";

	private static final String MATCH = ":shared ?s ; :queryLanguage ?ql ; :query ?q ; :rowsPerPage ?rpp .\n";

	private static final String UPDATE = PRE
			+ "DELETE { $<query> "
			+ MATCH
			+ "}\nINSERT { $<query> :shared $<shared> ; :queryLanguage $<queryLanguage> ; :query $<queryText> ; "
			+ ": infer $<infer> ; :rowsPerPage $<rowsPerPage> . } WHERE { $<query> :userName ?user ; " + MATCH
			+ FILTER;

	private static final String SELECT_URI = PRE
			+ "SELECT ?query { ?query :repository $<repository> ; :userName $<userName> ; :queryName $<queryName> . } ";

	private static final String SELECT = PRE
			+ "SELECT ?query ?user ?queryName ?shared ?queryLn ?queryText ?infer ?rowsPerPage "
			+ "{ ?query :repository $<repository> ; :userName ?user ; :queryName ?queryName ; :shared ?shared ; "
			+ ":queryLanguage ?queryLn ; :query ?queryText ; :infer ?infer ; :rowsPerPage ?rowsPerPage .\n"
			+ "FILTER (?user = $<userName> || ?user = \"\" || ?shared) } ORDER BY ?user ?queryName";

	private final Repository queries;

	private static final String USER_NAME = "$<userName>";

	private static final String REPOSITORY = "$<repository>";

	private static final String QUERY = "$<query>";

	private static final String QUERY_NAME = "$<queryName>";

	/**
	 * Create a new object for accessing the store of user queries.
	 * 
	 * @param context
	 *        the servlet context, used for determining the local repository
	 *        location
	 * @throws RepositoryException
	 *         if there is an issue creating the object to access the repository
	 */
	private QueryStorage(final ServletContext context)
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
	 * @param repository
	 *        the current repository
	 * @return true, if it is possible to request the size of the repository with
	 *         the given credentials
	 * @throws RepositoryException
	 *         if there is an issue closing the connection
	 */
	public boolean checkAccess(final HTTPRepository repository)
		throws RepositoryException
	{
		LOGGER.info("repository: {}", repository.getRepositoryURL());
		boolean rval = true;
		RepositoryConnection con = null;
		try {
			con = repository.getConnection();
			con.size();
		}
		catch (RepositoryException re) {
			rval = false;
		}
		finally {
			con.close();
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
	 * @param infer
	 * @param rowsPerPage
	 *        rows to display per page, may be 0 (all), 10, 50, 100, or 200)
	 * @throws OpenRDFException
	 */
	public void saveQuery(final HTTPRepository repository, final String queryName, final String userName,
			final boolean shared, final QueryLanguage queryLanguage, final String queryText,
			final boolean infer, final int rowsPerPage)
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
		this.checkQueryText(queryText);
		final QueryStringBuilder save = new QueryStringBuilder(SAVE);
		save.replaceURI(REPOSITORY, repository.getRepositoryURL());
		save.replaceURI(QUERY, "urn:uuid:" + UUID.randomUUID());
		save.replaceQuote(QUERY_NAME, queryName);
		this.replaceUpdateFields(save, userName, shared, queryLanguage, queryText, infer, rowsPerPage);
		updateQueryRepository(save.toString());
	}

	public boolean canDelete(final URI query, final String currentUser)
		throws RepositoryException, QueryEvaluationException, MalformedQueryException
	{
		final QueryStringBuilder canDelete = new QueryStringBuilder(CAN_DELETE);
		canDelete.replaceURI(QUERY, query.toString());
		canDelete.replaceQuote(USER_NAME, currentUser);
		LOGGER.info("{}", canDelete);
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			return connection.prepareBooleanQuery(QueryLanguage.SPARQL, canDelete.toString()).evaluate();
		}
		finally {
			connection.close();
		}
	}

	public boolean askExists(final HTTPRepository repository, final String queryName, final String userName)
		throws QueryEvaluationException, RepositoryException, MalformedQueryException
	{
		final QueryStringBuilder ask = new QueryStringBuilder(ASK_EXISTS);
		ask.replaceURI(REPOSITORY, repository.getRepositoryURL());
		ask.replaceQuote(QUERY_NAME, queryName);
		ask.replaceQuote(USER_NAME, userName);
		LOGGER.info("{}", ask);
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
	 * calling code to call checkAccess() and canDelete() with the full
	 * credentials first.
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
		delete.replaceQuote(QueryStorage.USER_NAME, userName);
		delete.replaceURI(QUERY, query.toString());
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
	 * @param infer
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
			final QueryLanguage queryLanguage, final String queryText, final boolean infer, final int rowsPerPage)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		final QueryStringBuilder update = new QueryStringBuilder(UPDATE);
		update.replaceURI(QUERY, query);
		this.replaceUpdateFields(update, userName, shared, queryLanguage, queryText, infer, rowsPerPage);
		this.updateQueryRepository(update.toString());
	}

	/**
	 * Prepares a query to retrieve the queries accessible to the given user in
	 * the given repository. When evaluated, the query result will have the
	 * following binding names: query, user, queryName, shared, queryLn,
	 * queryText, rowsPerPage. It is the responsibility of the calling code to
	 * call checkAccess() with the full credentials first.
	 * 
	 * @param repository
	 *        that the saved queries run against
	 * @param userName
	 *        that is requesting the saved queries
	 * @param builder
	 * @return a query result listing all the saved queries against the given
	 *         repository and accessible to the given user
	 * @throws RepositoryException
	 *         if there's a problem connecting to the saved queries repository
	 * @throws MalformedQueryException
	 *         if the query is not legal SPARQL
	 * @throws QueryEvaluationException
	 *         if there is a problem while attempting to evaluate the query
	 */
	public void selectSavedQueries(final HTTPRepository repository, final String userName,
			final TupleResultBuilder builder)
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		final QueryStringBuilder select = new QueryStringBuilder(SELECT);
		select.replaceQuote(USER_NAME, userName);
		select.replaceURI(REPOSITORY, repository.getRepositoryURL());
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			EVAL.evaluateTupleQuery(builder,
					connection.prepareTupleQuery(QueryLanguage.SPARQL, select.toString()));
		}
		finally {
			connection.close();
		}
	}

	public URI selectSavedQuery(final HTTPRepository repository, final String userName, final String queryName)
		throws OpenRDFException, BadRequestException
	{
		final QueryStringBuilder select = new QueryStringBuilder(SELECT_URI);
		select.replaceQuote(QueryStorage.USER_NAME, userName);
		select.replaceURI(REPOSITORY, repository.getRepositoryURL());
		select.replaceQuote(QUERY_NAME, queryName);
		final RepositoryConnection connection = this.queries.getConnection();
		final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, select.toString());
		try {
			final TupleQueryResult result = query.evaluate();
			if (result.hasNext()) {
				return (URI)(result.next().getValue("query"));
			}
			else {
				throw new BadRequestException("Could not find query entry in storage.");
			}
		}
		finally {
			connection.close();
		}
	}

	private void updateQueryRepository(final String update)
		throws RepositoryException, UpdateExecutionException, MalformedQueryException
	{
		LOGGER.info("SPARQL/Update of Query Storage:\n--\n{}\n--", update);
		final RepositoryConnection connection = this.queries.getConnection();
		try {
			connection.prepareUpdate(QueryLanguage.SPARQL, update).execute();
		}
		finally {
			connection.close();
		}
	}

	/**
	 * Perform replacement on several common fields for update operations.
	 * 
	 * @param userName
	 *        the name of the current user
	 * @param shared
	 *        whether the saved query is to be shared with other users
	 * @param queryLanguage
	 *        the language of the saved query
	 * @param queryText
	 *        the actual text of the query to save
	 * @param infer
	 * @param rowsPerPage
	 *        the rows per page to display for results
	 */
	private void replaceUpdateFields(final QueryStringBuilder builder, final String userName,
			final boolean shared, final QueryLanguage queryLanguage, final String queryText,
			final boolean infer, final int rowsPerPage)
	{
		builder.replaceQuote(USER_NAME, userName);
		builder.replace("$<shared>", QueryStringBuilder.xsdQuote(String.valueOf(shared), "boolean"));
		builder.replaceQuote("$<queryLanguage>", queryLanguage.toString());
		checkQueryText(queryText);
		builder.replace("$<queryText>", QueryStringBuilder.quote(queryText, "'''", "'''"));
		builder.replace("$<infer>", QueryStringBuilder.xsdQuote(String.valueOf(infer), "boolean"));
		builder.replace("$<rowsPerPage>",
				QueryStringBuilder.xsdQuote(String.valueOf(rowsPerPage), "unsignedByte"));
	}

	/**
	 * Imposes the rule that the query may not contain '''-quoted string, since
	 * that is how we'll be quoting it in our SPARQL/Update statements. Quoting
	 * the query with ''' assuming all string literals in the query are of the
	 * STRING_LITERAL1, STRING_LITERAL2 or STRING_LITERAL_LONG2 types.
	 * 
	 * @param queryText
	 *        the query text
	 */
	private void checkQueryText(final String queryText) {
		if (queryText.indexOf("'''") > 0) {
			throw new IllegalArgumentException("queryText may not contain '''-quoted strings.");
		}
	}
}