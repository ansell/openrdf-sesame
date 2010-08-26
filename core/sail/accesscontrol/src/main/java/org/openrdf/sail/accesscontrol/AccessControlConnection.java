/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.EmptyCursor;
import org.openrdf.cursor.FilteringCursor;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
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
import org.openrdf.query.algebra.Union;
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

	private volatile URI inheritanceProperty;

	private volatile List<URI> accessAttributes;

	/**
	 * @param delegate
	 */
	public AccessControlConnection(SailConnection delegate) {
		super(delegate);
	}

	/**
	 * Verifies if the supplied subject is editable by the current user.
	 * 
	 * @param subject
	 *        the subject for which edit permission is to be checked.
	 * @return true if the subject is editable by the current user, false if not.
	 * @throws StoreException
	 */
	public boolean isEditable(Resource subject)
		throws StoreException
	{
		return hasPermissionOnSubject(getCurrentUser(), subject, ACL.EDIT);
	}

	/**
	 * Verifies if the supplied subject is viewable by the current user.
	 * 
	 * @param subject
	 *        the subject for which view permission is to be checked.
	 * @return true if the subject is viewable by the current user, false if not.
	 * @throws StoreException
	 */
	public boolean isViewable(Resource subject)
		throws StoreException
	{
		return hasPermissionOnSubject(getCurrentUser(), subject, ACL.VIEW);
	}

	@Override
	public void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (isEditable(subj)) {
			super.addStatement(subj, pred, obj, contexts);
		}
		else {
			throw new StoreException("insufficient access rights on subject " + subj.stringValue());
		}
	}

	@Override
	public Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		if (subj != null) {
			if (isViewable(subj)) {
				return super.getStatements(subj, pred, obj, includeInferred, contexts);
			}
			else {
				return EmptyCursor.getInstance();
			}
		}
		else {
			Cursor<? extends Statement> result = super.getStatements(subj, pred, obj, includeInferred, contexts);
			return new FilteringCursor<Statement>(result) {

				@Override
				protected boolean accept(Statement st)
					throws StoreException
				{
					return isViewable(st.getSubject());
				}
			};
		}
	}

	@Override
	public void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (subj != null) {
			if (isEditable(subj)) {
				super.removeStatements(subj, pred, obj, contexts);
			}
			else {
				// TODO: ignore statements that aren't viewable?
				// Remark Jeen: I would only do that in the case where the subject
				// was not explicitly
				// specified in the method call. In the current case, the user
				// explicitly tried to perform
				// a remove on a specific subject to which he has no access rights.
				// IMHO, that should _always_
				// result in an error.
				throw new StoreException("insufficient access rights on subject " + subj.stringValue());
			}
		}
		else {
			Cursor<? extends Statement> toBeRemovedStatements = super.getStatements(null, pred, obj, false,
					contexts);
			try {
				Statement st;
				while ((st = toBeRemovedStatements.next()) != null) {
					Resource subject = st.getSubject();

					if (isEditable(subject)) {
						super.removeStatements(subject, pred, obj, contexts);
					}
					else {
						// Since the user did not explicitly specify the subject being
						// removed, we silently
						// ignore the statement if the subject is not viewable by the
						// user.
						if (isViewable(subject)) {
							throw new StoreException("insufficient access rights on subject "
									+ subject.stringValue());
						}
					}
				}
			}
			finally {
				toBeRemovedStatements.close();
			}
		}
	}

	@Override
	public Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings, boolean includeInferred)
		throws StoreException
	{
		// Clone the tuple expression to allow for more aggresive optimizations
		query = query.clone();
		query.visit(new AccessControlQueryExpander());
		return super.evaluate(query, bindings, includeInferred);
	}

	@Override
	public void close()
		throws StoreException
	{
		// flush locally stored acl info
		inheritanceProperty = null;
		accessAttributes = null;
		super.close();
	}

	/**
	 * Retrieves the property value for supplied subject and predicate, if that
	 * value is a resource. Optionally, it uses the acl:InheritanceProperty to
	 * find a value for the supplied predicate on one of the subject's parents.
	 * 
	 * @param subject
	 *        the subject node for which to retrieve the property value as a
	 *        Resource.
	 * @param predicate
	 *        the property name for which to retrieve the value.
	 * @param inherit
	 *        indicates if the subject's parent(s) should be checked for a
	 *        property value if the subject has no valid value itself.
	 * @param contexts
	 *        zero or more contexts in which to find the value.
	 * @return a property value as a resource, or null if no valid property value
	 *         was found.
	 * @throws StoreException
	 */
	private Resource getPropertyResourceValue(Resource subject, URI predicate, boolean inherit,
			Resource... contexts)
		throws StoreException
	{
		return getPropertyResourceValue(subject, predicate, inherit, new ArrayList<Resource>(), contexts);
	}

	private Resource getPropertyResourceValue(Resource subject, URI predicate, boolean inherit,
			List<Resource> visited, Resource... contexts)
		throws StoreException
	{
		if (visited == null) {
			visited = new ArrayList<Resource>();
		}

		// loop detection
		if (visited.contains(subject)) {
			return null;
		}

		Resource result = null;

		Cursor<? extends Statement> statements = super.getStatements(subject, predicate, null, true, contexts);
		try {
			Statement st;
			while ((st = statements.next()) != null) {
				if (st.getObject() instanceof Resource) {
					result = (Resource)st.getObject();
					break;
				}
			}
		}
		finally {
			statements.close();
		}

		// see if we should try and find a value from the supplied resource's
		// parent.
		if (result == null && inherit) {
			visited.add(subject);
			URI inheritanceProperty = getInheritanceProperty();

			if (inheritanceProperty != null) {
				Cursor<? extends Statement> parentStatements = super.getStatements(subject, inheritanceProperty,
						null, true);
				try {
					Statement parentStatement;
					while ((parentStatement = parentStatements.next()) != null) {
						Value value = parentStatement.getObject();
						if (value instanceof Resource) {
							result = getPropertyResourceValue((Resource)value, predicate, false, visited, contexts);
							if (result != null) {
								break;
							}
						}

					}
				}
				finally {
					parentStatements.close();
				}
			}
		}

		return result;
	}

	private List<Resource> getPropertyResourceValues(Resource subject, URI predicate, boolean inherit,
			Resource... contexts)
		throws StoreException
	{
		return getPropertyResourceValues(subject, predicate, inherit, new ArrayList<Resource>(), contexts);
	}

	private List<Resource> getPropertyResourceValues(Resource subject, URI predicate, boolean inherit,
			List<Resource> visited, Resource... contexts)
		throws StoreException
	{
		List<Resource> result = new ArrayList<Resource>();
		
		if (visited == null) {
			visited = new ArrayList<Resource>();
		}

		// loop detection
		if (visited.contains(subject)) {
			return null;
		}

		Cursor<? extends Statement> statements = super.getStatements(subject, predicate, null, true, contexts);

		try {
			Statement st;
			while ((st = statements.next()) != null) {
				if (st.getObject() instanceof Resource) {
					result.add((Resource)st.getObject());
				}
			}
		}
		finally {
			statements.close();
		}

		// see if we should try and find a value from the supplied resource's
		// parent.
		if (inherit) {
			visited.add(subject);
			URI inheritanceProperty = getInheritanceProperty();

			if (inheritanceProperty != null) {
				Cursor<? extends Statement> parentStatements = super.getStatements(subject, inheritanceProperty,
						null, true);

				try {
					Statement parentStatement;
					while ((parentStatement = parentStatements.next()) != null) {
						Value value = parentStatement.getObject();
						if (value instanceof Resource) {
							result.addAll(getPropertyResourceValues((Resource)value, predicate, false, visited, contexts));
						}
					}
				}
				finally {
					parentStatements.close();
				}
			}
		}

		return result;
	}

	private URI getInheritanceProperty()
		throws StoreException
	{
		// FIXME: attribute needs to be reset after updates to the data
		// TODO: share this info across connections
		// TODO: allow multiple inheritance properties?
		if (inheritanceProperty == null) {
			Cursor<? extends Statement> statements = super.getStatements(null, RDF.TYPE,
					ACL.INHERITANCE_PROPERTY, true);

			try {
				Statement st;
				while ((st = statements.next()) != null) {
					if (st.getSubject() instanceof URI) {
						inheritanceProperty = (URI)st.getSubject();
						break;
					}
				}
			}
			finally {
				statements.close();
			}
		}

		return inheritanceProperty;
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
	private List<URI> getAssignedPermissions(List<URI> roles, URI operation)
		throws StoreException
	{
		List<URI> permissions = new ArrayList<URI>();

		for (URI role : roles) {
			// TODO this would probably be more efficient using a SPARQL query.
			Cursor<? extends Statement> statements = super.getStatements(null, ACL.TO_ROLE, role, true,
					ACL.CONTEXT);

			try {
				Statement st;

				while ((st = statements.next()) != null) {
					Cursor<? extends Statement> permissionStatements = super.getStatements(st.getSubject(),
							ACL.HAS_PERMISSION, null, true, ACL.CONTEXT);
					try {
						Statement permStat;
						while ((permStat = permissionStatements.next()) != null) {
							Cursor<? extends Statement> operationStatements = super.getStatements(
									(URI)permStat.getObject(), ACL.HAS_OPERATION, operation, true, ACL.CONTEXT);
							try {
								if (operationStatements.next() != null) {
									permissions.add((URI)permStat.getObject());
								}
							}
							finally {
								operationStatements.close();
							}
						}
					}
					finally {
						permissionStatements.close();
					}
				}
			}
			finally {
				statements.close();
			}
		}

		return permissions;
	}

	private URI getAttributeValueForPermission(URI permission, URI attribute)
		throws StoreException
	{
		Resource match = getPropertyResourceValue(permission, ACL.HAS_MATCH, false, ACL.CONTEXT);
		URI attributeValue = (URI)getPropertyResourceValue(match, attribute, false, ACL.CONTEXT);
		return attributeValue;
	}

	private List<URI> getRolesForUser(URI username)
		throws StoreException
	{
		List<URI> roles = new ArrayList<URI>();

		if (username != null) {
			Cursor<? extends Statement> statements = super.getStatements(username, ACL.HAS_ROLE, null, true,
					ACL.CONTEXT);

			try {
				Statement st;
				while ((st = statements.next()) != null) {
					Value value = st.getObject();
					if (value instanceof URI) {
						roles.add((URI)value);
					}
				}
			}
			finally {
				statements.close();
			}
		}

		return roles;
	}

	private URI getCurrentUser()
		throws StoreException
	{
		Session session = SessionManager.get();
		if (session != null) {
			URI userId = session.getUserId();

			if (userId != null) {
				return userId;
			}

			String username = session.getUsername();

			if (username != null) {
				// backdoor for administrator user.
				if (username.equals("administrator")) {
					session.setUserId(ACL.ADMIN);
				}
				else {
					Literal usernameLiteral = this.getValueFactory().createLiteral(username, XMLSchema.STRING);
					Cursor<? extends Statement> statements = super.getStatements(null, ACL.USERNAME,
							usernameLiteral, true, ACL.CONTEXT);

					try {
						Statement st;
						if ((st = statements.next()) != null) {
							session.setUserId((URI)st.getSubject());
						}
					}
					finally {
						statements.close();
					}
				}
				return session.getUserId();
			}
		}
		return null;
	}

	/***
	 * Retrieve all instances of acl:AccessAttribute from the ACL context.
	 * 
	 * @return a list of URIs representing the attributes in terms of which
	 *         object matches are defined. May be empty.
	 * @throws StoreException
	 */
	private List<URI> getAccessAttributes()
		throws StoreException
	{
		// FIXME: attribute needs to be reset after updates to the data
		// TODO: share this info across connections
		if (accessAttributes == null) {
			accessAttributes = new ArrayList<URI>();

			Cursor<? extends Statement> statements = super.getStatements(null, RDF.TYPE, ACL.ACCESS_ATTRIBUTE,
					false, ACL.CONTEXT);

			try {
				Statement st;
				while ((st = statements.next()) != null) {
					Resource subject = st.getSubject();
					if (subject instanceof URI) {
						accessAttributes.add((URI)subject);
					}
				}
			}
			finally {
				statements.close();
			}
		}

		return accessAttributes;
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

		HashMap<URI, List<Resource>> attributeValues = new HashMap<URI, List<Resource>>();

		for (URI attribute : attributes) {
			List<Resource> attributeValueList = getPropertyResourceValues(subject, attribute, true);
			if (!attributeValueList.isEmpty()) {
				attributeValues.put(attribute, attributeValueList);
			}
		}

		if (!attributeValues.isEmpty()) {
			List<URI> permissions = getAssignedPermissions(getRolesForUser(user), operation);

			for (URI permission : permissions) {
				// check if all attributes match
				boolean allMatch = true;

				for (Entry<URI, List<Resource>> entry : attributeValues.entrySet()) {
					URI attribute = entry.getKey();
					List<Resource> attributeValueList = entry.getValue();

					URI permissionAttributeValue = getAttributeValueForPermission(permission, attribute);
					boolean isMatch = attributeValueList.contains(permissionAttributeValue);

					if (!isMatch) {
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

	protected class AccessControlQueryExpander extends QueryModelVisitorBase<StoreException> {

		private final List<Var> handledSubjects = new ArrayList<Var>();

		private List<URI> permissions;

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
			 *  OPTIONAL { 
			 *             { ?subject foo:accessAttr1 ?accessAttrValue1. }
			 *             UNION
			 *             { ?subject foo:inheritanceProp ?S1 .
			 *               ?S1 foo:accessAttr1 ?accessAttrValue1. 
			 *             }
			 *             { ?subject foo:accessAttr2 ?accessAttrValue2. }
			 *             UNION
			 *             { ?subject foo:inheritanceProp ?S2 .
			 *               ?S2 foo:accessAttr1 ?accessAttrValue2. 
			 *             }
			 *             ...
			 *            }
			 *            
			 *  or in terms of the algebra:
			 *            
			 *  LeftJoin(
			 *  	SP(?subject, ?predicate, ?object), 
			 *  	Join(
			 *  		Union(
			 *        SP(?subject, accessAttr_1, ?accessAttrValue_1),
			 *        Join (
			 *        	SP(?subject, inheritProp ?S_1),
			 *          SP(?S_1, acccessAttr_1, ?accessAttrValue_1)
			 *        )),
			 *  		Union(
			 *        SP(?subject, accessAttr_2, ?accessAttrValue_2),
			 *        Join (
			 *        	SP(?subject, inheritProp ?S_2),
			 *          SP(?S_2, acccessAttr_2, ?accessAttrValue_2)
			 *        )),
			 *       ...
			 *     )
			 *  )
			 */
			List<URI> attributes = getAccessAttributes();

			if (attributes == null || attributes.size() == 0) {
				return;
			}

			// join of the attribute match expressions.
			Join joinOfAttributePatterns = new Join();

			URI inheritanceProp = getInheritanceProperty();
			Var inheritPredVar = new Var("-acl_inherit_pred", inheritanceProp);

			int i = 0;

			List<Var> attributeVars = new ArrayList<Var>();
			for (URI attribute : attributes) {
				Var attributeVar = new Var("-acl_attr_" + i++);
				attributeVars.add(attributeVar);

				Var attributePredVar = new Var("-acl_attr_pred_" + i, attribute);

				// SP(?subject, accessAttr_i, ?accessAttrValue_i)
				StatementPattern attributePattern = new StatementPattern(subjectVar, attributePredVar,
						attributeVar);

				if (inheritanceProp != null) {
					// create a union expression for this attribute.
					Union union = new Union();
					union.addArg(attributePattern);

					// the join for checking if the access attribute is inherited.
					Join inheritJoin = new Join();
					Var inheritVar = new Var("-acl_inherited_value" + i);
					// SP (?subject, inheritProp, ?S_i)
					StatementPattern inheritPattern = new StatementPattern(subjectVar, inheritPredVar, inheritVar);
					inheritJoin.addArg(inheritPattern);
					// SP (?S_i, accessAttr_i, ?accessAttrValue_i)
					StatementPattern inheritAttrPattern = new StatementPattern(inheritVar, attributePredVar,
							attributeVar);
					inheritJoin.addArg(inheritAttrPattern);

					union.addArg(inheritJoin);

					joinOfAttributePatterns.addArg(union);
				}
				else {
					// no inheritance: the attribute can be matched with a simple
					// statement pattern
					joinOfAttributePatterns.addArg(attributePattern);
				}
			}

			TupleExpr expandedPattern = null;

			if (joinOfAttributePatterns.getNumberOfArguments() == 1) {
				expandedPattern = new LeftJoin(statementPattern, joinOfAttributePatterns.getArg(0));
			}
			else {
				expandedPattern = new LeftJoin(statementPattern, joinOfAttributePatterns);
			}

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

			if (and.getArgs().size() == 1) {
				filterConditions.addArg(and.getArg(0));
			}
			else {
				filterConditions.addArg(and);
			}

			if (permissions == null) {
				List<URI> roles = getRolesForUser(getCurrentUser());
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
					// filter conditions.
					filterConditions.addArg(permissionCondition);
				}
			}

			// set the filter conditions on the query pattern
			if (filterConditions.getNumberOfArguments() == 1) {
				// no second argument in the or
				expandedPattern = new Filter(expandedPattern, filterConditions.getArg(0));
			}
			else {
				expandedPattern = new Filter(expandedPattern, filterConditions);
			}

			// expand the query.
			parent.replaceChildNode(statementPattern, expandedPattern);
		}
	}
}
