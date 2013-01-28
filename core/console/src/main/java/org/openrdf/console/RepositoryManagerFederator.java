/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.net.MalformedURLException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.OpenRDFException;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.http.config.HTTPRepositoryConfig;
import org.openrdf.repository.http.config.HTTPRepositoryFactory;
import org.openrdf.repository.http.config.HTTPRepositorySchema;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryFactory;
import org.openrdf.repository.sail.config.SailRepositorySchema;
import org.openrdf.repository.sparql.config.SPARQLRepositoryConfig;
import org.openrdf.repository.sparql.config.SPARQLRepositoryFactory;
import org.openrdf.sail.config.SailConfigSchema;
import org.openrdf.sail.federation.config.FederationConfig;
import org.openrdf.sail.federation.config.FederationFactory;

/**
 * Utility class for handling the details of federating "user repositories"
 * managed by a {@link org.openrdf.repository.manager.RepositoryManager}.
 * 
 * @author Dale Visser
 */
public class RepositoryManagerFederator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryManagerFederator.class);

	private final RepositoryManager manager;

	private final ValueFactory valueFactory;

	/**
	 * Create an instance capable of federating "user repositories" within the
	 * given {@link org.openrdf.repository.manager.RepositoryManager}.
	 * 
	 * @param manager
	 *        must manage the repositories to be added to new federations, and
	 *        becomes the manager of any created federations
	 */
	protected RepositoryManagerFederator(RepositoryManager manager) {
		this.manager = manager;
		this.valueFactory = manager.getSystemRepository().getValueFactory();
	}

	/**
	 * Adds a new repository to the
	 * {@link org.openrdf.repository.manager.RepositoryManager}, which is a
	 * federation of the given repository id's, which must also refer to
	 * repositories already managed by the
	 * {@link org.openrdf.repository.manager.RepositoryManager}.
	 * 
	 * @param memberType
	 *        either
	 *        {@link org.openrdf.repository.http.config.HTTPRepositoryFactory#REPOSITORY_TYPE}
	 *        or
	 *        {@link org.openrdf.repository.sparql.config.SPARQLRepositoryFactory#REPOSITORY_TYPE}
	 * @param fedID
	 *        the desired identifier for the new federation repository
	 * @param description
	 *        the desired description for the new federation repository
	 * @param members
	 *        the identifiers of the repositories to federate, which must already
	 *        exist and be managed by the
	 *        {@link org.openrdf.repository.manager.RepositoryManager}
	 * @throws MalformedURLException
	 *         if the {@link org.openrdf.repository.manager.RepositoryManager}
	 *         has a malformed location
	 * @throws OpenRDFException
	 *         if an problem otherwise occurs while creating the federation
	 */
	protected void addFed(String memberType, String fedID, String description, Collection<String> members)
		throws MalformedURLException, OpenRDFException
	{
		Graph graph = new GraphImpl(valueFactory);
		BNode fedRepoNode = valueFactory.createBNode();
		LOGGER.debug("Federation repository root node: {}", fedRepoNode);
		addToGraph(graph, fedRepoNode, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
		addToGraph(graph, fedRepoNode, RepositoryConfigSchema.REPOSITORYID, valueFactory.createLiteral(fedID));
		addToGraph(graph, fedRepoNode, RDFS.LABEL, valueFactory.createLiteral(description));
		RepositoryConnection con = manager.getSystemRepository().getConnection();
		try {
			addImplementation(members, graph, fedRepoNode, memberType, con);
		}
		finally {
			con.close();
		}
		RepositoryConfig fedConfig = RepositoryConfig.create(graph, fedRepoNode);
		fedConfig.validate();
		manager.addRepositoryConfig(fedConfig);
	}

	private void addImplementation(Collection<String> members, Graph graph, BNode fedRepoNode,
			String memberType, RepositoryConnection con)
		throws OpenRDFException, MalformedURLException
	{
		BNode implRoot = valueFactory.createBNode();
		addToGraph(graph, fedRepoNode, RepositoryConfigSchema.REPOSITORYIMPL, implRoot);
		addToGraph(graph, implRoot, RepositoryConfigSchema.REPOSITORYTYPE,
				valueFactory.createLiteral(SailRepositoryFactory.REPOSITORY_TYPE));
		addSail(members, graph, implRoot, memberType, con);
	}

	private void addSail(Collection<String> members, Graph graph, BNode implRoot, String memberType,
			RepositoryConnection con)
		throws OpenRDFException, MalformedURLException
	{
		BNode sailRoot = valueFactory.createBNode();
		addToGraph(graph, implRoot, SailRepositorySchema.SAILIMPL, sailRoot);
		addToGraph(graph, sailRoot, SailConfigSchema.SAILTYPE,
				valueFactory.createLiteral(FederationFactory.SAIL_TYPE));
		for (String member : members) {
			addMember(graph, sailRoot, member, memberType, con);
		}
	}

	private void addMember(Graph graph, BNode sailRoot, String identifier, String memberType,
			RepositoryConnection con)
		throws OpenRDFException, MalformedURLException
	{
		LOGGER.debug("Adding member: {}", identifier);
		BNode memberNode = valueFactory.createBNode();
		addToGraph(graph, sailRoot, FederationConfig.MEMBER, memberNode);
		addToGraph(graph, memberNode, RepositoryConfigSchema.REPOSITORYTYPE,
				valueFactory.createLiteral(memberType));
		String memberRepoType = manager.getRepositoryConfig(identifier).getRepositoryImplConfig().getType();
		String url = getMemberURL(identifier, memberType, con, memberRepoType);
		URI predicate = SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberType) ? SPARQLRepositoryConfig.ENDPOINT
				: HTTPRepositorySchema.REPOSITORYURL;
		addToGraph(graph, memberNode, predicate, valueFactory.createURI(url));
		LOGGER.debug("Added member {}: ", identifier);
	}

	private String getMemberURL(String identifier, String memberType, RepositoryConnection con,
			String memberRepoType)
		throws MalformedURLException, RepositoryConfigException, OpenRDFException
	{
		String url = manager.getLocation().toString() + "/repositories/" + identifier;
		if (HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
			if (!HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberType)) {
				throw new RepositoryConfigException("Candidate member " + identifier + ": can't federate a "
						+ memberRepoType + " as a " + memberType);
			}
			url = ((HTTPRepositoryConfig)manager.getRepositoryConfig(identifier).getRepositoryImplConfig()).getURL();
		}
		else if (SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
			if (!SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberType)) {
				throw new RepositoryConfigException("Candidate member " + identifier + ": can't federate a "
						+ memberRepoType + " as a " + memberType);
			}
			url = ((SPARQLRepositoryConfig)manager.getRepositoryConfig(identifier).getRepositoryImplConfig()).getURL();
		}
		return url;
	}

	private static void addToGraph(Graph graph, Resource subject, URI predicate, Value object) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(subject + " " + predicate + " " + object);
		}
		graph.add(subject, predicate, object);
	}

}
