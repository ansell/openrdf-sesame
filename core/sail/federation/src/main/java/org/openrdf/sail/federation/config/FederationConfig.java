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
package org.openrdf.sail.federation.config;

import static org.openrdf.repository.config.AbstractRepositoryImplConfig.create;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.SimpleIRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.ModelException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.AbstractSailImplConfig;

/**
 * Lists the members of a federation and which properties describe a resource
 * subject in a unique member.
 * 
 * @author James Leigh
 */
public class FederationConfig extends AbstractSailImplConfig {

	/** http://www.openrdf.org/config/sail/federation# */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/federation#";

	public static final IRI MEMBER = new SimpleIRI(NAMESPACE + "member");

	/**
	 * For all triples with a predicate in this space, the container RDF store
	 * contains all triples with that subject and any predicate in this space.
	 */
	public static final IRI LOCALPROPERTYSPACE = new SimpleIRI(NAMESPACE // NOPMD
			+ "localPropertySpace");

	/**
	 * If no two members contain the same statement.
	 */
	public static final IRI DISTINCT = new SimpleIRI(NAMESPACE + "distinct");

	/**
	 * If the federation should not try and add statements to its members.
	 */
	public static final IRI READ_ONLY = new SimpleIRI(NAMESPACE + "readOnly");

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
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		Resource self = super.export(model);
		for (RepositoryImplConfig member : getMembers()) {
			model.add(self, MEMBER, member.export(model));
		}
		for (String space : getLocalPropertySpace()) {
			model.add(self, LOCALPROPERTYSPACE, valueFactory.createIRI(space));
		}
		model.add(self, DISTINCT, valueFactory.createLiteral(distinct));
		model.add(self, READ_ONLY, valueFactory.createLiteral(readOnly));
		return self;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws SailConfigException
	{
		super.parse(graph, implNode);
		LinkedHashModel model = new LinkedHashModel(graph);
		for (Value member : model.filter(implNode, MEMBER, null).objects()) {
			try {
				addMember(create(graph, (Resource)member));
			}
			catch (RepositoryConfigException e) {
				throw new SailConfigException(e);
			}
		}
		for (Value space : model.filter(implNode, LOCALPROPERTYSPACE, null).objects()) {
			addLocalPropertySpace(space.stringValue());
		}
		try {
			Optional<Literal> bool = model.filter(implNode, DISTINCT, null).objectLiteral();
			if (bool.isPresent() && bool.get().booleanValue()) {
				distinct = true;
			}
			bool = model.filter(implNode, READ_ONLY, null).objectLiteral();
			if (bool.isPresent() && bool.get().booleanValue()) {
				readOnly = true;
			}
		}
		catch (ModelException e) {
			throw new SailConfigException(e);
		}
	}

	@Override
	public void validate()
		throws SailConfigException
	{
		super.validate();
		if (members.isEmpty()) {
			throw new SailConfigException("No federation members specified");
		}
		for (RepositoryImplConfig member : members) {
			try {
				member.validate();
			}
			catch (RepositoryConfigException e) {
				throw new SailConfigException(e);
			}
		}
	}

}
