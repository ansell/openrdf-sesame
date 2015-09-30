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
package org.eclipse.rdf4j.sail.federation.config;

import static org.eclipse.rdf4j.repository.config.AbstractRepositoryImplConfig.create;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.sail.config.AbstractSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;

/**
 * Lists the members of a federation and which properties describe a resource
 * subject in a unique member.
 * 
 * @author James Leigh
 */
public class FederationConfig extends AbstractSailImplConfig {

	/** http://www.openrdf.org/config/sail/federation# */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/federation#";

	private static final ValueFactory vf = SimpleValueFactory.getInstance();
	
	public static final IRI MEMBER = vf.createIRI(NAMESPACE + "member");

	/**
	 * For all triples with a predicate in this space, the container RDF store
	 * contains all triples with that subject and any predicate in this space.
	 */
	public static final IRI LOCALPROPERTYSPACE = vf.createIRI(NAMESPACE // NOPMD
			+ "localPropertySpace");

	/**
	 * If no two members contain the same statement.
	 */
	public static final IRI DISTINCT = vf.createIRI(NAMESPACE + "distinct");

	/**
	 * If the federation should not try and add statements to its members.
	 */
	public static final IRI READ_ONLY = vf.createIRI(NAMESPACE + "readOnly");

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
	public Resource export(Model model) {
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
	public void parse(Model graph, Resource implNode)
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
			Optional<Literal> bool = Models.objectLiteral(model.filter(implNode, DISTINCT, null));
			if (bool.isPresent() && bool.get().booleanValue()) {
				distinct = true;
			}
			bool = Models.objectLiteral(model.filter(implNode, READ_ONLY, null));
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
