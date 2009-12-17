/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.accesscontrol.vocabulary.ACL;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;
import org.openrdf.store.StoreException;

/**
 * @author Jeen Broekstra
 */
public class AccessControlConnection extends SailConnectionWrapper {

	private URI _inheritanceProperty;

	private List<URI> _accessAttributes;

	private Resource getPropertyResourceValue(Resource subject, URI predicate, boolean inherit,
			Resource... contexts)
		throws StoreException
	{

		Resource result = null;
		Cursor<? extends Statement> statements = getStatements(subject, predicate, null, true, contexts);

		Statement st;
		while ((st = statements.next()) != null) {
			if (st.getObject() instanceof Resource) {
				result = (Resource)st.getObject();
				break;
			}
		}
		statements.close();

		// see if we should try and find a value from the supplied resource's
		// parent.
		if (result == null && inherit) {
			URI inheritanceProperty = getInheritanceProperty();

			if (inheritanceProperty != null) {
				Cursor<? extends Statement> parentStatements = getStatements(null, inheritanceProperty, subject,
						true);

				Statement parentStatement;
				while ((parentStatement = parentStatements.next()) != null) {
					result = getPropertyResourceValue(parentStatement.getSubject(), predicate, false, contexts);
					if (result != null) {
						break;
					}
				}

				parentStatements.close();
			}
		}

		return result;
	}

	private URI getInheritanceProperty()
		throws StoreException
	{

		if (_inheritanceProperty == null) {

			Cursor<? extends Statement> statements = getStatements(null, RDF.TYPE, ACL.INHERITANCE_PROPERTY,
					true);

			Statement st;
			while ((st = statements.next()) != null) {
				if (st.getSubject() instanceof URI) {
					_inheritanceProperty = (URI)st.getSubject();
					break;
				}
			}
			statements.close();
		}

		return _inheritanceProperty;
	}

	/**
	 * Retrieve the permissions assigned to the supplied role and involving the
	 * supplied operation.
	 * 
	 * @param roles
	 *        a list of roles
	 * @param operation
	 *        an operation identifier
	 * @return a Cursor containing URIs of permissions.
	 */
	private List<URI> getAssignedPermissions(List<URI> roles, URI operation) {

		List<URI> permissions = new ArrayList<URI>();
		try {

			for (URI role : roles) {
				// TODO this would probably be more efficient using a SPARQL query.
				Cursor<? extends Statement> statements = getStatements(null, ACL.TO_ROLE, role, true, ACL.CONTEXT);

				Statement st;

				while ((st = statements.next()) != null) {
					Cursor<? extends Statement> permissionStatements = getStatements(st.getSubject(),
							ACL.HAS_PERMISSION, null, true, ACL.CONTEXT);
					Statement permStat;
					while ((permStat = permissionStatements.next()) != null) {

						Cursor<? extends Statement> operationStatements = getStatements((URI)permStat.getObject(),
								ACL.HAS_OPERATION, operation, true, ACL.CONTEXT);

						if (operationStatements.next() != null) {
							permissions.add((URI)permStat.getObject());
						}
						operationStatements.close();
					}
					permissionStatements.close();
				}
				statements.close();
			}
		}
		catch (StoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return permissions;
	}

	private Resource getMatchPattern(URI permission)
		throws StoreException
	{
		Resource match = getPropertyResourceValue(permission, ACL.HAS_MATCH, false, ACL.CONTEXT);
		return match;
	}

	private URI getAttributeValueForPermission(URI permission, URI attribute)
		throws StoreException
	{

		Resource match = getMatchPattern(permission);

		URI attributeValue = (URI)getPropertyResourceValue(match, attribute, false, ACL.CONTEXT);

		return attributeValue;

	}

	private List<URI> getRolesForUser(URI username)
		throws StoreException
	{
		List<URI> roles = new ArrayList<URI>();

		if (username != null) {
			Cursor<? extends Statement> statements = getStatements(username, ACL.HAS_ROLE, null, true,
					ACL.CONTEXT);

			Statement st;
			while ((st = statements.next()) != null) {
				Value value = st.getObject();
				if (value instanceof URI) {
					roles.add((URI)value);
				}
			}
			statements.close();
		}

		return roles;
	}

	public boolean isEditable(Resource subject)
		throws StoreException
	{

		Session session = SessionManager.get();
		if (session != null) {
			URI user = session.getCurrentUser();

			return hasPermissionOnSubject(user, subject, ACL.EDIT);
		}
		return false;
	}

	public boolean isViewable(Resource subject)
		throws StoreException
	{

		Session session = SessionManager.get();
		if (session != null) {
			URI user = session.getCurrentUser();

			return hasPermissionOnSubject(user, subject, ACL.VIEW);
		}
		return false;
	}

	private List<URI> getAccessAttributes()
		throws StoreException
	{
		if (_accessAttributes == null) {
			_accessAttributes = new ArrayList<URI>();

			Cursor<? extends Statement> statements = getStatements(null, RDF.TYPE, ACL.ACCESS_ATTRIBUTE, false,
					ACL.CONTEXT);

			Statement st;
			while ((st = statements.next()) != null) {
				Resource subject = st.getSubject();
				if (subject instanceof URI) {
					_accessAttributes.add((URI)subject);
				}
			}
			statements.close();
		}

		return _accessAttributes;
	}

	private boolean hasPermissionOnSubject(URI user, Resource subject, URI operation)
		throws StoreException
	{
		boolean hasPermission = false;

		// TODO this is a backdoor for testing purposes.
		if (ACL.ADMIN.equals(user)) {
			return true;
		}

		Collection<URI> attributes = getAccessAttributes();

		HashMap<URI, URI> attributeValues = new HashMap<URI, URI>();

		for (URI attribute : attributes) {
			URI attributeValue = (URI)getPropertyResourceValue(subject, attribute, true);
			if (attributeValue != null) {
				attributeValues.put(attribute, attributeValue);
			}
		}

		if (attributeValues.size() > 0) {
			List<URI> permissions = getAssignedPermissions(getRolesForUser(user), operation);

			boolean[] attributeMatch = new boolean[attributes.size()];

			HashMap<URI, Boolean> attributeMatches = new HashMap<URI, Boolean>(attributes.size());

			for (URI permission : permissions) {

				for (URI attribute : attributeValues.keySet()) {

					attributeMatches.put(attribute, false);

					URI attributeValue = attributeValues.get(attribute);

					if (attributeValue != null) {
						URI permissionAttributeValue = getAttributeValueForPermission(permission, attribute);
						attributeMatches.put(attribute, attributeValue.equals(permissionAttributeValue));
					}
				}

				// check if all attributes match
				boolean allMatch = true;
				for (URI attribute : attributeMatches.keySet()) {
					boolean match = attributeMatches.get(attribute);
					if (!match) {
						allMatch = false;
						break;
					}
				}

				if (allMatch) {
					hasPermission = true;
					break;
				}

			}
		}
		else {
			hasPermission = true;
		}

		return hasPermission;

	}

	/**
	 * @param delegate
	 */
	public AccessControlConnection(SailConnection delegate) {
		super(delegate);
	}

	@Override
	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{

		if (isEditable(subj)) {
			getDelegate().addStatement(subj, pred, obj, contexts);
		}
		else {
			throw new StoreException("insufficient access rights on subject " + subj.stringValue());
		}

	}

	@Override
	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (subj == null) {
			Cursor<? extends Statement> toBeRemovedStatements = this.getStatements(subj, pred, obj, false,
					contexts);
			Statement st;
			while ((st = toBeRemovedStatements.next()) != null) {
				Resource subject = st.getSubject();

				if (isEditable(subject)) {
					getDelegate().removeStatements(subject, pred, obj, contexts);
				}
			}
			toBeRemovedStatements.close();
		}
		else {

			if (isEditable(subj)) {
				super.removeStatements(subj, pred, obj, contexts);
			}
		}

	}

	@Override
	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		query.visit(new AccessControlQueryExpander());

		return super.evaluate(query, bindings, includeInferred);
	}

	protected class AccessControlQueryExpander extends QueryModelVisitorBase<StoreException> {

		private Session session;

		private List<Var> handledSubjects = new ArrayList<Var>();

		private List<URI> permissions;

		public AccessControlQueryExpander() {
			session = SessionManager.get();
		}

		@Override
		public void meet(StatementPattern statementPattern)
			throws StoreException
		{
			super.meet(statementPattern);

			// keep a reference to the original parent.
			QueryModelNode parent = statementPattern.getParentNode();

			Var subjectVar = statementPattern.getSubjectVar();
			if (handledSubjects.contains(subjectVar)) {
				// we have already expanded the query for this particular subject
				return;
			}

			handledSubjects.add(subjectVar);

			// TODO handle usage of parent for retrieval of attributes.
			/*
			 * Create this pattern:
			 *
			 *  ?subject ?predicate ?object. (= the original statementPattern)
			 *  OPTIONAL { { ?subject acl:hasStatus ?status .
			 *               ?subject acl:hasTeam ?team . }
			 *              UNION 
			 *             { 
			 *               ?parent ex:subItem ?subject .
			 *               ?parent acl:hasStatus ?status .
			 *               ?parent acl:hasTeam ?team. 
			 *              }
			 *            }
			 *            
			 *  or in terms of the algebra:
			 *            
			 *  LeftJoin(
			 *  	SP(?subject, ?predicate, ?object), 
			 *  	Join(
			 *  		SP(?subject, acl:hasStatus, ?status), 
			 *       SP(?subject, acl:hasTeam, ?team)
			 *     )
			 *  )
			 */
			List<URI> attributes = getAccessAttributes();

			Join attributeJoin = new Join();
			int i = 0;

			List<Var> attributeVars = new ArrayList<Var>();
			for (URI attribute : attributes) {
				Var attributeVar = new Var("acl_attr_" + i++);
				attributeVars.add(attributeVar);
				StatementPattern attributePattern = new StatementPattern(subjectVar, new Var(
						"acl_attr_pred_" + i, attribute), attributeVar);
				attributeJoin.addArg(attributePattern);
			}

			TupleExpr expandedPattern = new LeftJoin(statementPattern, attributeJoin);

			// build an Or-ed set of filter conditions on the status and team.
			Or filterConditions = new Or();

			/* first condition is that none are bound: this is the case where the subject
			 * is not a restricted resource (and therefore has no associated attributes)
			 * 
			 * And(Not(Bound(?acl_attr1)), Not(Bound(?acl_attr_1), ...)
			 */
			And and = new And();
			for (Var attributeVar : attributeVars) {
				and.addArg(new Not(new Bound(attributeVar)));
			}
			filterConditions.addArg(and);

			if (permissions == null) {
				List<URI> roles = getRolesForUser(session.getCurrentUser());
				permissions = getAssignedPermissions(roles, ACL.VIEW);

			}

			// for each permission, we add an additional condition to the filter,
			// checking that either
			// team, or status, or both match.
			for (URI permission : permissions) {

				And permissionCondition = new And();

				for (int j = 0; j < attributes.size(); j++) {
					URI attribute = attributes.get(j);
					URI attributePermissionValue = getAttributeValueForPermission(permission, attribute);

					Compare attributeValueCompare = null;
					if (attributePermissionValue != null) {
						attributeValueCompare = new Compare(attributeVars.get(j), new Var("acl_attr_val_" + j,
								attributePermissionValue));
						permissionCondition.addArg(attributeValueCompare);
					}
				}

				if (permissionCondition.getNumberOfArguments() == 1) {
					filterConditions.addArg(permissionCondition.getArg(0));
				}
				else {
					// add the permission-defined condition to the set of Or-ed
					// filter
					// conditions.
					filterConditions.addArg(permissionCondition);
				}
			}

			// set the filter conditions on the query pattern
			expandedPattern = new Filter(expandedPattern, filterConditions);

			// expand the query.
			parent.replaceChildNode(statementPattern, expandedPattern);

		}

	}

}
