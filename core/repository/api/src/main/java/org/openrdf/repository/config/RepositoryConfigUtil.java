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
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYID;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY_CONTEXT;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import info.aduna.iteration.Iterations;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

public class RepositoryConfigUtil {

	public static Set<String> getRepositoryIDs(Repository repository)
		throws RepositoryException
	{
		RepositoryConnection con = repository.getConnection();
		try {
			Set<String> idSet = new LinkedHashSet<String>();

			RepositoryResult<Statement> idStatementIter = con.getStatements(null, REPOSITORYID, null, true);
			try {
				while (idStatementIter.hasNext()) {
					Statement idStatement = idStatementIter.next();

					if (idStatement.getObject() instanceof Literal) {
						Literal idLiteral = (Literal)idStatement.getObject();
						idSet.add(idLiteral.getLabel());
					}
				}
			}
			finally {
				idStatementIter.close();
			}

			return idSet;
		}
		finally {
			con.close();
		}
	}

	/**
	 * Is configuration information for the specified repository ID present in
	 * the (system) repository?
	 * 
	 * @param repository
	 *        the repository to look in
	 * @param repositoryID
	 *        the repositoryID to look for
	 * @return true if configurion information for the specified repository ID
	 *         was found, false otherwise
	 * @throws RepositoryException
	 *         if an error occurred while trying to retrieve information from the
	 *         (system) repository
	 * @throws RepositoryConfigException
	 */
	public static boolean hasRepositoryConfig(Repository repository, String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		RepositoryConnection con = repository.getConnection();
		try {
			return getIDStatement(con, repositoryID) != null;
		}
		finally {
			con.close();
		}
	}

	public static RepositoryConfig getRepositoryConfig(Repository repository, String repositoryID)
		throws RepositoryConfigException, RepositoryException
	{
		RepositoryConnection con = repository.getConnection();
		try {
			Statement idStatement = getIDStatement(con, repositoryID);
			if (idStatement == null) {
				// No such config
				return null;
			}

			Resource repositoryNode = idStatement.getSubject();
			Resource context = idStatement.getContext();

			if (context == null) {
				throw new RepositoryException("No configuration context for repository " + repositoryID);
			}

			Model contextGraph = QueryResults.asModel(con.getStatements(null, null, null, true, context));

			return RepositoryConfig.create(contextGraph, repositoryNode);
		}
		finally {
			con.close();
		}
	}

	/**
	 * Update the specified Repository with the specified set of
	 * RepositoryConfigs. This will overwrite all existing configurations in the
	 * Repository that have a Repository ID occurring in these RepositoryConfigs.
	 * 
	 * @param repository
	 *        The Repository whose contents will be modified.
	 * @param configs
	 *        The RepositoryConfigs that should be added to or updated in the
	 *        Repository. The RepositoryConfig's ID may already occur in the
	 *        Repository, in which case all previous configuration data for that
	 *        Repository will be cleared before the RepositoryConfig is added.
	 * @throws RepositoryException
	 *         When access to the Repository's RepositoryConnection causes a
	 *         RepositoryException.
	 * @throws RepositoryConfigException
	 */
	public static void updateRepositoryConfigs(Repository repository, RepositoryConfig... configs)
		throws RepositoryException, RepositoryConfigException
	{
		RepositoryConnection con = repository.getConnection();

		try {
			updateRepositoryConfigs(con, configs);
		}
		finally {
			con.close();
		}
	}

	/**
	 * Update the specified RepositoryConnection with the specified set of
	 * RepositoryConfigs. This will overwrite all existing configurations in the
	 * Repository that have a Repository ID occurring in these RepositoryConfigs.
	 * 
	 * Note: this method does NOT commit the updates on the connection.
	 * 
	 * @param con
	 *        the repository connection to perform the update on
	 * @param configs
	 *        The RepositoryConfigs that should be added to or updated in the
	 *        Repository. The RepositoryConfig's ID may already occur in the
	 *        Repository, in which case all previous configuration data for that
	 *        Repository will be cleared before the RepositoryConfig is added.
	 * 
	 * @throws RepositoryException
	 * @throws RepositoryConfigException
	 */
	public static void updateRepositoryConfigs(RepositoryConnection con, RepositoryConfig... configs)
		throws RepositoryException, RepositoryConfigException
	{
		ValueFactory vf = con.getRepository().getValueFactory();

		con.begin();

		for (RepositoryConfig config : configs) {
			Resource context = getContext(con, config.getID());

			if (context != null) {
				con.clear(context);
			}
			else {
				context = vf.createBNode();
			}

			con.add(context, RDF.TYPE, REPOSITORY_CONTEXT);

			Model graph = new LinkedHashModel();
			config.export(graph);
			con.add(graph, context);
		}

		con.commit();
	}

	/**
	 * Removes one or more Repository configurations from a Repository. Nothing
	 * happens when this Repository does not contain configurations for these
	 * Repository IDs.
	 * 
	 * @param repository
	 *        The Repository to remove the configurations from.
	 * @param repositoryIDs
	 *        The IDs of the Repositories whose configurations need to be
	 *        removed.
	 * @throws RepositoryException
	 *         Whenever access to the Repository's RepositoryConnection causes a
	 *         RepositoryException.
	 * @throws RepositoryConfigException
	 */
	public static boolean removeRepositoryConfigs(Repository repository, String... repositoryIDs)
		throws RepositoryException, RepositoryConfigException
	{
		boolean changed = false;

		RepositoryConnection con = repository.getConnection();
		try {
			con.begin();

			for (String id : repositoryIDs) {
				Resource context = getContext(con, id);
				if (context != null) {
					con.clear(context);
					con.remove(context, RDF.TYPE, REPOSITORY_CONTEXT);
					changed = true;
				}
			}

			con.commit();
		}
		finally {
			con.close();
		}

		return changed;
	}

	public static Resource getContext(RepositoryConnection con, String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		Resource context = null;

		Statement idStatement = getIDStatement(con, repositoryID);
		if (idStatement != null) {
			context = idStatement.getContext();
		}

		return context;
	}

	private static Statement getIDStatement(RepositoryConnection con, String repositoryID)
		throws RepositoryException, RepositoryConfigException
	{
		Literal idLiteral = con.getRepository().getValueFactory().createLiteral(repositoryID);
		List<Statement> idStatementList = Iterations.asList(con.getStatements(null, REPOSITORYID, idLiteral, true));

		if (idStatementList.size() == 1) {
			return idStatementList.get(0);
		}
		else if (idStatementList.isEmpty()) {
			return null;
		}
		else {
			throw new RepositoryConfigException("Multiple ID-statements for repository ID " + repositoryID);
		}
	}
}
