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
package org.eclipse.rdf4j.runtime;

import java.net.MalformedURLException;
import java.util.Collection;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.http.config.HTTPRepositoryConfig;
import org.eclipse.rdf4j.repository.http.config.HTTPRepositoryFactory;
import org.eclipse.rdf4j.repository.http.config.HTTPRepositorySchema;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.ProxyRepositoryFactory;
import org.eclipse.rdf4j.repository.sail.config.ProxyRepositorySchema;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryFactory;
import org.eclipse.rdf4j.repository.sail.config.SailRepositorySchema;
import org.eclipse.rdf4j.repository.sparql.config.SPARQLRepositoryConfig;
import org.eclipse.rdf4j.repository.sparql.config.SPARQLRepositoryFactory;
import org.eclipse.rdf4j.sail.config.SailConfigSchema;
import org.eclipse.rdf4j.sail.federation.config.FederationConfig;
import org.eclipse.rdf4j.sail.federation.config.FederationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for handling the details of federating "user repositories"
 * managed by a {@link org.eclipse.rdf4j.repository.manager.RepositoryManager}.
 * 
 * @author Dale Visser
 */
public class RepositoryManagerFederator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryManagerFederator.class);

	private final RepositoryManager manager;

	private final ValueFactory valueFactory;

	/**
	 * Create an instance capable of federating "user repositories" within the
	 * given {@link org.eclipse.rdf4j.repository.manager.RepositoryManager}.
	 * 
	 * @param manager
	 *        must manage the repositories to be added to new federations, and
	 *        becomes the manager of any created federations
	 */
	public RepositoryManagerFederator(RepositoryManager manager) {
		this.manager = manager;
		this.valueFactory = manager.getSystemRepository().getValueFactory();
	}

	/**
	 * Adds a new repository to the
	 * {@link org.eclipse.rdf4j.repository.manager.RepositoryManager}, which is a
	 * federation of the given repository id's, which must also refer to
	 * repositories already managed by the
	 * {@link org.eclipse.rdf4j.repository.manager.RepositoryManager}.
	 * 
	 * @param fedID
	 *        the desired identifier for the new federation repository
	 * @param description
	 *        the desired description for the new federation repository
	 * @param members
	 *        the identifiers of the repositories to federate, which must already
	 *        exist and be managed by the
	 *        {@link org.eclipse.rdf4j.repository.manager.RepositoryManager}
	 * @param readonly
	 *        whether the federation is read-only
	 * @param distinct
	 *        whether the federation enforces distinct results from its members
	 * @throws MalformedURLException
	 *         if the {@link org.eclipse.rdf4j.repository.manager.RepositoryManager}
	 *         has a malformed location
	 * @throws OpenRDFException
	 *         if a problem otherwise occurs while creating the federation
	 */
	public void addFed(String fedID, String description, Collection<String> members, boolean readonly,
			boolean distinct)
				throws MalformedURLException, OpenRDFException
	{
		if (members.contains(fedID)) {
			throw new RepositoryConfigException(
					"A federation member may not have the same ID as the federation.");
		}
		Model graph = new LinkedHashModel();
		BNode fedRepoNode = valueFactory.createBNode();
		LOGGER.debug("Federation repository root node: {}", fedRepoNode);
		addToGraph(graph, fedRepoNode, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
		addToGraph(graph, fedRepoNode, RepositoryConfigSchema.REPOSITORYID, valueFactory.createLiteral(fedID));
		addToGraph(graph, fedRepoNode, RDFS.LABEL, valueFactory.createLiteral(description));
		RepositoryConnection con = manager.getSystemRepository().getConnection();
		try {
			addImplementation(members, graph, fedRepoNode, con, readonly, distinct);
		}
		finally {
			con.close();
		}
		RepositoryConfig fedConfig = RepositoryConfig.create(graph, fedRepoNode);
		fedConfig.validate();
		manager.addRepositoryConfig(fedConfig);
	}

	private void addImplementation(Collection<String> members, Model graph, BNode fedRepoNode,
			RepositoryConnection con, boolean readonly, boolean distinct)
				throws OpenRDFException, MalformedURLException
	{
		BNode implRoot = valueFactory.createBNode();
		addToGraph(graph, fedRepoNode, RepositoryConfigSchema.REPOSITORYIMPL, implRoot);
		addToGraph(graph, implRoot, RepositoryConfigSchema.REPOSITORYTYPE,
				valueFactory.createLiteral(SailRepositoryFactory.REPOSITORY_TYPE));
		addSail(members, graph, implRoot, con, readonly, distinct);
	}

	private void addSail(Collection<String> members, Model graph, BNode implRoot, RepositoryConnection con,
			boolean readonly, boolean distinct)
				throws OpenRDFException, MalformedURLException
	{
		BNode sailRoot = valueFactory.createBNode();
		addToGraph(graph, implRoot, SailRepositorySchema.SAILIMPL, sailRoot);
		addToGraph(graph, sailRoot, SailConfigSchema.SAILTYPE,
				valueFactory.createLiteral(FederationFactory.SAIL_TYPE));
		addToGraph(graph, sailRoot, FederationConfig.READ_ONLY, valueFactory.createLiteral(readonly));
		addToGraph(graph, sailRoot, FederationConfig.DISTINCT, valueFactory.createLiteral(distinct));
		for (String member : members) {
			addMember(graph, sailRoot, member, con);
		}
	}

	private void addMember(Model graph, BNode sailRoot, String identifier, RepositoryConnection con)
		throws OpenRDFException, MalformedURLException
	{
		LOGGER.debug("Adding member: {}", identifier);
		BNode memberNode = valueFactory.createBNode();
		addToGraph(graph, sailRoot, FederationConfig.MEMBER, memberNode);
		String memberRepoType = manager.getRepositoryConfig(identifier).getRepositoryImplConfig().getType();
		if (!(SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)
				|| HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)))
		{
			memberRepoType = ProxyRepositoryFactory.REPOSITORY_TYPE;
		}
		addToGraph(graph, memberNode, RepositoryConfigSchema.REPOSITORYTYPE,
				valueFactory.createLiteral(memberRepoType));
		addToGraph(graph, memberNode, getLocationPredicate(memberRepoType),
				getMemberLocator(identifier, con, memberRepoType));
		LOGGER.debug("Added member {}: ", identifier);
	}

	private IRI getLocationPredicate(String memberRepoType) {
		IRI predicate;
		if (SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
			predicate = SPARQLRepositoryConfig.QUERY_ENDPOINT;
		}
		else if (HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
			predicate = HTTPRepositorySchema.REPOSITORYURL;
		}
		else {
			predicate = ProxyRepositorySchema.PROXIED_ID;
		}
		return predicate;
	}

	private Value getMemberLocator(String identifier, RepositoryConnection con, String memberRepoType)
		throws MalformedURLException, RepositoryConfigException, OpenRDFException
	{
		Value locator;
		if (HTTPRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
			locator = valueFactory.createIRI(((HTTPRepositoryConfig)manager.getRepositoryConfig(
					identifier).getRepositoryImplConfig()).getURL());
		}
		else if (SPARQLRepositoryFactory.REPOSITORY_TYPE.equals(memberRepoType)) {
			locator = valueFactory.createIRI(((SPARQLRepositoryConfig)manager.getRepositoryConfig(
					identifier).getRepositoryImplConfig()).getQueryEndpointUrl());
		}
		else {
			locator = valueFactory.createLiteral(identifier);
		}
		return locator;
	}

	private static void addToGraph(Model graph, Resource subject, IRI predicate, Value object) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(subject + " " + predicate + " " + object);
		}
		graph.add(subject, predicate, object);
	}

}
