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
import org.openrdf.store.SessionsManager;
import org.openrdf.store.StoreException;

/**
 * @author Jeen Broekstra
 */
public class AccessControlConnection extends SailConnectionWrapper {

	private URI getObject(Resource subject, URI predicate, Resource... contexts)
		throws StoreException
	{

		URI result = null;
		Cursor<? extends Statement> statements = getStatements(subject, predicate, null, true, contexts);

		Statement st;
		while ((st = statements.next()) != null) {
			if (st.getObject() instanceof URI) {
				result = (URI)st.getObject();
				break;
			}
		}
		statements.close();

		return result;
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
				// TODO this would probably be more efficient using a query.
				Cursor<? extends Statement> statements = getStatements(null, ACL.TO_ROLE, role, true, ACL.CONTEXT);

				Statement st;

				while ((st = statements.next()) != null) {
					Cursor<? extends Statement> permissionStatements = getStatements(st.getSubject(),
							ACL.HAS_PERMISSION, null, true, ACL.CONTEXT);
					Statement permStat;
					while ((permStat = permissionStatements.next()) != null) {
						permissions.add((URI)permStat.getObject());
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

	private URI getMatchPattern(URI permission)
		throws StoreException
	{
		URI match = getObject(permission, ACL.HAS_MATCH, ACL.CONTEXT);
		return match;
	}

	private URI getTeamForPermission(URI permission)
		throws StoreException
	{

		URI match = getMatchPattern(permission);

		URI team = getObject(match, ACL.HAS_TEAM, ACL.CONTEXT);

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

		URI match = getMatchPattern(permission);

		URI status = getObject(match, ACL.HAS_STATUS, ACL.CONTEXT);

		return status;
	}

	private boolean hasPermissionOnSubject(URI user, Resource subject, URI operation)
		throws StoreException
	{
		boolean hasPermission = false;
		
		URI subjectStatus = getObject(subject, ACL.HAS_STATUS);
		URI subjectTeam = getObject(subject, ACL.HAS_TEAM);
		
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
		Session session = SessionsManager.get();

		if (hasPermissionOnSubject(session.getCurrentUser(), subj, ACL.EDIT)) {
			super.addStatement(subj, pred, obj, contexts);
		}
		else {
			throw new StoreException("insufficient access rights on subject " + subj.stringValue());
		}

	}

	@Override
	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		Session session = SessionsManager.get();

		if (subj == null) {
			Cursor<? extends Statement> toBeRemovedStatements = this.getStatements(subj, pred, obj, false,
					contexts);
			Statement st;
			while ((st = toBeRemovedStatements.next()) != null) {
				Resource subject = st.getSubject();

				if (hasPermissionOnSubject(session.getCurrentUser(), subject, ACL.EDIT)) {
					super.removeStatements(subject, pred, obj, contexts);
				}
			}
			toBeRemovedStatements.close();
		}
		else {

			if (hasPermissionOnSubject(session.getCurrentUser(), subj, ACL.EDIT)) {
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
			session = SessionsManager.get();
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
