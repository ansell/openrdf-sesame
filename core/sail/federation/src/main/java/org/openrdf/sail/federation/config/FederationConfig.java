/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.config;

import static org.openrdf.repository.config.RepositoryImplConfigBase.create;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfigBase;

/**
 * Lists the members of a federation and which properties describe a resource
 * subject in a unique member.
 * 
 * @author James Leigh
 */
public class FederationConfig extends SailImplConfigBase {

	/** http://www.openrdf.org/config/sail/federation# */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/federation#";

	public static final URI MEMBER = new URIImpl(NAMESPACE + "member");

	/**
	 * For all triples with a predicate in this space, the container RDF store
	 * contains all triples with that subject and any predicate in this space.
	 */
	public static final URI LOCALPROPERTYSPACE = new URIImpl(NAMESPACE // NOPMD
			+ "localPropertySpace");

	/**
	 * If no two members contain the same statement.
	 */
	public static final URI DISTINCT = new URIImpl(NAMESPACE + "distinct");

	/**
	 * If the federation should not try and add statements to its members.
	 */
	public static final URI READ_ONLY = new URIImpl(NAMESPACE + "readOnly");

	private List<RepositoryImplConfig> members = new ArrayList<RepositoryImplConfig>();

	private final Set<String> localPropertySpace = new HashSet<String>(); // NOPMD

	private boolean distinct;

	private boolean readOnly;

	public List<RepositoryImplConfig> getMembers() {
		return members;
	}

	public void setMembers(List<RepositoryImplConfig> members) {
		this.members = members;
	}

	public void addMember(RepositoryImplConfig member) {
		members.add(member);
	}

	public Set<String> getLocalPropertySpace() {
		return localPropertySpace;
	}

	public void addLocalPropertySpace(String localPropertySpace) { // NOPMD
		this.localPropertySpace.add(localPropertySpace);
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean disjoint) {
		this.distinct = disjoint;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public Resource export(Graph model) {
		ValueFactory valueFactory = ValueFactoryImpl.getInstance();
		Resource self = super.export(model);
		for (RepositoryImplConfig member : getMembers()) {
			model.add(self, MEMBER, member.export(model));
		}
		for (String space : getLocalPropertySpace()) {
			model.add(self, LOCALPROPERTYSPACE, valueFactory.createURI(space));
		}
		model.add(self, DISTINCT, valueFactory.createLiteral(distinct));
		model.add(self, READ_ONLY, valueFactory.createLiteral(readOnly));
		return self;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
			throws SailConfigException {
		super.parse(graph, implNode);
		LinkedHashModel model = new LinkedHashModel(graph);
		for (Value member : model.filter(implNode, MEMBER, null).objects()) {
			try {
				addMember(create(graph, (Resource) member));
			} catch (RepositoryConfigException e) {
				throw new SailConfigException(e);
			}
		}
		for (Value space : model.filter(implNode, LOCALPROPERTYSPACE, null)
				.objects()) {
			addLocalPropertySpace(space.stringValue());
		}
		try {
			Literal bool = model.filter(implNode, DISTINCT, null)
					.objectLiteral();
			if (bool != null && bool.booleanValue()) {
				distinct = true;
			}
			bool = model.filter(implNode, READ_ONLY, null).objectLiteral();
			if (bool != null && bool.booleanValue()) {
				readOnly = true;
			}
		} catch (ModelException e) {
			throw new SailConfigException(e);
		}
	}

	@Override
	public void validate() throws SailConfigException {
		super.validate();
		if (members.isEmpty()) {
			throw new SailConfigException("No federation members specified");
		}
		for (RepositoryImplConfig member : members) {
			try {
				member.validate();
			} catch (RepositoryConfigException e) {
				throw new SailConfigException(e);
			}
		}
	}

}
