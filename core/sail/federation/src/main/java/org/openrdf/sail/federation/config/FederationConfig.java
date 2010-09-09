/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.config;

import static org.openrdf.sail.federation.config.FederationSchema.DISTINCT;
import static org.openrdf.sail.federation.config.FederationSchema.LOCALPROPERTYSPACE;
import static org.openrdf.sail.federation.config.FederationSchema.MEMBER;
import static org.openrdf.sail.federation.config.FederationSchema.READ_ONLY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.sail.config.SailImplConfigBase;
import org.openrdf.store.StoreConfigException;

/**
 * Lists the members of a federation and which properties describe a resource
 * subject in a unique member.
 * 
 * @author James Leigh
 */
public class FederationConfig extends SailImplConfigBase {

	private final List<RepositoryImplConfig> members = new ArrayList<RepositoryImplConfig>();

	private final Set<String> localPropertySpace = new HashSet<String>();

	private boolean distinct = false;

	private boolean readOnly = false;

	public List<RepositoryImplConfig> getMembers() {
		return members;
	}

	public void setMembers(Iterable<? extends RepositoryImplConfig> members) {
		this.members.clear();
		for (RepositoryImplConfig member : members) {
			addMember(member);
		}
	}

	public void addMember(RepositoryImplConfig member) {
		if (member == null) {
			throw new IllegalArgumentException("member must not be null");
		}
		members.add(member);
	}

	public Set<String> getLocalPropertySpace() {
		return localPropertySpace;
	}

	public void addLocalPropertySpace(String localPropertySpace) {
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
	public void validate()
		throws StoreConfigException
	{
		super.validate();

		if (members.isEmpty()) {
			throw new StoreConfigException("No federation members specified");
		}

		for (RepositoryImplConfig member : members) {
			member.validate();
		}
	}

	@Override
	public Resource export(Model model) {
		Resource implNode = super.export(model);

		ValueFactory vf = ValueFactoryImpl.getInstance();

		for (RepositoryImplConfig member : members) {
			model.add(implNode, MEMBER, member.export(model));
		}

		for (String space : localPropertySpace) {
			model.add(implNode, LOCALPROPERTYSPACE, vf.createURI(space));
		}

		model.add(implNode, DISTINCT, vf.createLiteral(distinct));

		model.add(implNode, READ_ONLY, vf.createLiteral(readOnly));

		return implNode;
	}

	@Override
	public void parse(Model model, Resource implNode)
		throws StoreConfigException
	{
		super.parse(model, implNode);

		for (Value member : model.filter(implNode, MEMBER, null).objects()) {
			if (member instanceof Resource) {
				addMember(RepositoryImplConfigBase.create(model, (Resource)member));
			}
			else {
				throw new StoreConfigException("Found literal for federation member node, expected a resource");
			}
		}

		for (Value space : model.filter(implNode, LOCALPROPERTYSPACE, null).objects()) {
			addLocalPropertySpace(space.stringValue());
		}

		try {
			Literal distinctLit = model.filter(implNode, DISTINCT, null).objectLiteral();
			if (distinctLit != null) {
				try {
					distinct = distinctLit.booleanValue();
				}
				catch (IllegalArgumentException e) {
					throw new StoreConfigException(
							"Invalid boolean value for <distinct> parameter in federation config: " + distinctLit);
				}
			}
		}
		catch (ModelException e) {
			throw new StoreConfigException("Invalid or inconsistent <distinct> parameter for federation config");
		}

		try {
			Literal readOnlyLit = model.filter(implNode, READ_ONLY, null).objectLiteral();
			if (readOnlyLit != null) {
				try {
					readOnly = readOnlyLit.booleanValue();
				}
				catch (IllegalArgumentException e) {
					throw new StoreConfigException(
							"Invalid boolean value for <readOnly> parameter in federation config: " + readOnlyLit);
				}
			}
		}
		catch (ModelException e) {
			throw new StoreConfigException("Invalid or inconsistent <readOnly> parameter for federation config");
		}
	}
}
