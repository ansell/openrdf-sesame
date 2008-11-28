/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.config;

import static org.openrdf.repository.config.RepositoryImplConfigBase.create;
import static org.openrdf.sail.federation.config.FederationSchema.MEMBER;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.sail.config.SailImplConfigBase;
import org.openrdf.store.StoreConfigException;

public class FederationConfig extends SailImplConfigBase {
	private List<RepositoryImplConfig> members = new ArrayList<RepositoryImplConfig>();

	public List<RepositoryImplConfig> getMembers() {
		return members;
	}

	public void setMembers(List<RepositoryImplConfig> members) {
		this.members = members;
	}

	public void addMember(RepositoryImplConfig member) {
		members.add(member);
	}

	@Override
	public Resource export(Model model) {
		Resource self = super.export(model);
		for (RepositoryImplConfig member : getMembers()) {
			model.add(self, MEMBER, member.export(model));
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
	}

}
