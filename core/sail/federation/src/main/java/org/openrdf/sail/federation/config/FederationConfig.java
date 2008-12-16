/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.config;

import static org.openrdf.repository.config.RepositoryImplConfigBase.create;
import static org.openrdf.sail.federation.config.FederationSchema.DISJOINT;
import static org.openrdf.sail.federation.config.FederationSchema.LOCALPROPERTYSPACE;
import static org.openrdf.sail.federation.config.FederationSchema.MEMBER;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.sail.config.SailImplConfigBase;
import org.openrdf.store.StoreConfigException;

/**
 * Lists the members of a federation and which properties describe a resource
 * subject in a unique member.
 * 
 * @author James Leigh
 */
public class FederationConfig extends SailImplConfigBase {

	private List<RepositoryImplConfig> members = new ArrayList<RepositoryImplConfig>();

	private Set<String> localPropertySpace = new HashSet<String>();

	private boolean disjoint;

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

	public void addLocalPropertySpace(String localPropertySpace) {
		this.localPropertySpace.add(localPropertySpace);
	}

	public boolean isDisjoint() {
		return disjoint;
	}

	public void setDisjoint(boolean disjoint) {
		this.disjoint = disjoint;
	}

	@Override
	public Resource export(Model model) {
		Resource self = super.export(model);
		for (RepositoryImplConfig member : getMembers()) {
			model.add(self, MEMBER, member.export(model));
		}
		for (String space : getLocalPropertySpace()) {
			model.add(self, LOCALPROPERTYSPACE, new URIImpl(space));
		}
		return self;
	}

	@Override
	public void parse(Model model, Resource implNode)
		throws StoreConfigException
	{
		super.parse(model, implNode);
		for (Value member : model.filter(implNode, MEMBER, null).objects()) {
			addMember(create(model, (Resource)member));
		}
		for (Value space : model.filter(implNode, LOCALPROPERTYSPACE, null).objects()) {
			addLocalPropertySpace(space.stringValue());
		}
		try {
			Literal bool = model.filter(implNode, DISJOINT, null).objectLiteral();
			if (bool != null && bool.booleanValue()) {
				disjoint = true;
			}
		}
		catch (ModelException e) {
			throw new StoreConfigException(e);
		}
	}

}
