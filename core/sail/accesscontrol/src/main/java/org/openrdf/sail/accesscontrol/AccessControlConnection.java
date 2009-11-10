/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.cursor.CollectionCursor;
import org.openrdf.cursor.Cursor;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.accesscontrol.vocabulary.ACL;
import org.openrdf.sail.helpers.SailConnectionWrapper;
import org.openrdf.store.Session;
import org.openrdf.store.StoreException;

/**
 * @author Jeen Broekstra
 */
public class AccessControlConnection extends SailConnectionWrapper {

	/**
	 * @param delegate
	 */
	public AccessControlConnection(SailConnection delegate) {
		super(delegate);
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

		public AccessControlQueryExpander() {
			session = new ThreadLocal<Session>().get();
		}

		@Override
		public void meet(StatementPattern statementPattern)
			throws StoreException
		{
			super.meet(statementPattern);

			QueryModelNode parent = statementPattern.getParentNode();

			Var subjectVar = statementPattern.getSubjectVar();

			// TODO cache?
			Cursor<URI> permissions = getAssignedPermissions(session.getActiveRole(), ACL.VIEW);
			
			URI permission;
			while((permission = permissions.next()) != null) {
				URI status = getStatusForPermission(permission);
				URI team = getTeamForPermission(permission);
				
				/* TODO add a graph pattern + filter conditions on the subject: 
				 * 
				 *  OPTIONAL { ?subject acl:hasStatus ?status .
				 *             ?subject acl:hasTeam ?team .
				 *            }
				 *  FILTER ( (?team = T1 && ?status = shared) 
				 *            || (?status = public) 
				 *            || (!BOUND(?status) && !BOUND(?team))
				 *            )
				 */
			}
			permissions.close();
		}

		/**
		 * Retrieve the permissions assigned to the supplied role and involving
		 * the supplied operation.
		 * 
		 * @param role
		 *        the role identifier
		 * @param operation
		 *        an operation identifier
		 * @return a Cursor containing URIs of permissions.
		 */
		private Cursor<URI> getAssignedPermissions(URI role, URI operation) {

			List<URI> permissions = new ArrayList<URI>();
			try {

				// TODO this would probably be more efficient using a query.
				Cursor<? extends Statement> statements = getStatements(null, ACL.HAS_ROLE, role, true,
						ACL.CONTEXT);

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
			catch (StoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return new CollectionCursor<URI>(permissions);
		}
		
		private URI getMatchPattern(URI permission) throws StoreException {
			URI match = getObject(permission, ACL.HAS_MATCH, ACL.CONTEXT);
			return match;
		}

		private URI getObject(URI subject, URI predicate, URI context) throws StoreException {
			
			URI result = null;
			Cursor<? extends Statement> statements = getStatements(subject, predicate, null, true, context);
			
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
		
		private URI getStatusForPermission(URI permission) throws StoreException {
			
			URI match = getMatchPattern(permission);
		

			URI status = getObject(match, ACL.HAS_STATUS, ACL.CONTEXT);
			
			return status;
		}
		
		
		private URI getTeamForPermission(URI permission) throws StoreException {
			
			URI match = getMatchPattern(permission);
		

			URI team = getObject(match, ACL.HAS_TEAM, ACL.CONTEXT);
			
			return team;
		}
		
		
	}
	

	
}
