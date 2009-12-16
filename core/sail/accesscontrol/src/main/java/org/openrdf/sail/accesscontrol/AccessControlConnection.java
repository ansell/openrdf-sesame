/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import java.util.ArrayList;
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

	private Resource getPropertyResourceValue(Resource subject, URI predicate, boolean inherit, Resource... contexts)
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

		// see if we should try and find a value from the supplied resource's parent.
		if (result == null && inherit) {
			URI inheritanceProperty = getInheritanceProperty();
			
			if (inheritanceProperty != null) {
				Cursor<? extends Statement> parentStatements = getStatements(null, inheritanceProperty, subject, true);
				
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

	private URI getTeamForPermission(URI permission)
		throws StoreException
	{

		Resource match = getMatchPattern(permission);

		URI team = (URI)getPropertyResourceValue(match, ACL.HAS_TEAM, false, ACL.CONTEXT);

		return team;
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

	private URI getStatusForPermission(URI permission)
		throws StoreException
	{

		Resource match = getMatchPattern(permission);

		URI status = (URI)getPropertyResourceValue(match, ACL.HAS_STATUS, false, ACL.CONTEXT);

		return status;
	}

	public boolean isEditable(Resource subject) throws StoreException {
		
		Session session = SessionManager.get();
		if (session != null) {
			URI user = session.getCurrentUser();
			
			return hasPermissionOnSubject(user, subject, ACL.EDIT);
		}
		return false;
	}

	public boolean isViewable(Resource subject) throws StoreException {
		
		Session session = SessionManager.get();
		if (session != null) {
			URI user = session.getCurrentUser();
			
			return hasPermissionOnSubject(user, subject, ACL.VIEW);
		}
		return false;
	}
	
	private boolean hasPermissionOnSubject(URI user, Resource subject, URI operation)
		throws StoreException
	{
		boolean hasPermission = false;

		// TODO this is a backdoor for testing purposes.
		if (ACL.ADMIN.equals(user)) {
			return true;
		}

		URI subjectStatus = (URI)getPropertyResourceValue(subject, ACL.HAS_STATUS, true);
		URI subjectTeam = (URI)getPropertyResourceValue(subject, ACL.HAS_TEAM, true);

		if (subjectStatus != null || subjectTeam != null) {
			List<URI> permissions = getAssignedPermissions(getRolesForUser(user), operation);

			for (URI permission : permissions) {
				boolean subjectMatch = false;
				boolean teamMatch = false;

				if (subjectStatus != null) {
					subjectMatch = subjectStatus.equals(getStatusForPermission(permission));
				}
				else {
					subjectMatch = true;
				}

				if (subjectTeam != null) {
					teamMatch = subjectTeam.equals(getTeamForPermission(permission));
				}
				else {
					teamMatch = true;
				}

				if (teamMatch && subjectMatch) {
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

			/*
			 * Create this pattern:
			 *
			 *  ?subject ?predicate ?object. (= the original statementPattern)
			 *  OPTIONAL { ?subject acl:hasStatus ?status .
			 *             ?subject acl:hasTeam ?team .
			 *            }
			 * OPTIONAL { ?parent ex:subItem ?subject .
			 *            ?parent acl:hasStatus ?parentStatus .
			 *            ?parent acl:hasTeam ?parentTeam.
			 *          }
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
			Var statusVar = new Var("acl_status");
			StatementPattern statusPattern = new StatementPattern(subjectVar, new Var("acl_status_pred",
					ACL.HAS_STATUS), statusVar);

			Var teamVar = new Var("acl_team");
			StatementPattern teamPattern = new StatementPattern(subjectVar, new Var("acl_team_pred",
					ACL.HAS_TEAM), teamVar);

			Join teamAndStatus = new Join(statusPattern, teamPattern);

			TupleExpr expandedPattern = new LeftJoin(statementPattern, teamAndStatus);

			// build an Or-ed set of filter conditions on the status and team.
			Or filterConditions = new Or();

			/* first condition is that neither are bound: this is the case where the subject
			 * is not a restricted resource (and therefore has no associated team and status)
			 * 
			 * And(Not(Bound(?status)), Not(Bound(?team)))
			 */
			filterConditions.addArg(new And(new Not(new Bound(statusVar)), new Not(new Bound(teamVar))));

			if (permissions == null) {
				List<URI> roles = getRolesForUser(session.getCurrentUser());
				permissions = getAssignedPermissions(roles, ACL.VIEW);

			}

			// for each permission, we add an additional condition to the filter,
			// checking that either
			// team, or status, or both match.
			for (URI permission : permissions) {
				URI status = getStatusForPermission(permission);
				URI team = getTeamForPermission(permission);

				Compare statusCompare = null;
				Compare teamCompare = null;

				ValueExpr permissionCondition = null;

				if (status != null) {
					statusCompare = new Compare(statusVar, new Var("acl_status_val", status));
					permissionCondition = statusCompare;
				}

				if (team != null) {
					teamCompare = new Compare(teamVar, new Var("acl_team_val", team));
					permissionCondition = teamCompare;
				}

				if (statusCompare != null && teamCompare != null) {
					permissionCondition = new And(statusCompare, teamCompare);
				}

				// add the permission-defined condition to the set of Or-ed filter
				// conditions.
				filterConditions.addArg(permissionCondition);
			}

			// set the filter conditions on the query pattern
			expandedPattern = new Filter(expandedPattern, filterConditions);

			// expand the query.
			parent.replaceChildNode(statementPattern, expandedPattern);

		}

	}

}
