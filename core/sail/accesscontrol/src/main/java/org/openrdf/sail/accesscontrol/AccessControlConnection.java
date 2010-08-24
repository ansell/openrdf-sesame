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

	private URI _inheritanceProperty;

	private List<URI> _accessAttributes;

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
				else {
					throw new StoreException("insufficient access rights on subject " + subject.stringValue());
				}
			}
			toBeRemovedStatements.close();
		}
		else {

			if (isEditable(subj)) {
				super.removeStatements(subj, pred, obj, contexts);
			}
			else {
				throw new StoreException("insufficient access rights on subject " + subj.stringValue());
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

	@Override
	public void close()
		throws StoreException
	{

		// flush locally stored acl info
		_inheritanceProperty = null;
		_accessAttributes = null;
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
				Cursor<? extends Statement> parentStatements = getStatements(subject, inheritanceProperty, null,
						true);

				Statement parentStatement;
				while ((parentStatement = parentStatements.next()) != null) {
					Value value = parentStatement.getObject();
					if (value instanceof Resource) {
						result = getPropertyResourceValue((Resource)value, predicate, false, contexts);
						if (result != null) {
							break;
						}
					}

				}

				parentStatements.close();
			}
		}

		return result;
	}

	private List<Resource> getPropertyResourceValues(Resource subject, URI predicate, boolean inherit,
			Resource... contexts)
		throws StoreException
	{

		List<Resource> result = new ArrayList<Resource>();

		Cursor<? extends Statement> statements = getStatements(subject, predicate, null, true, contexts);

		Statement st;
		while ((st = statements.next()) != null) {
			if (st.getObject() instanceof Resource) {
				result.add((Resource)st.getObject());
			}
		}
		statements.close();

		// see if we should try and find a value from the supplied resource's
		// parent.
		if (inherit) {
			URI inheritanceProperty = getInheritanceProperty();

			if (inheritanceProperty != null) {
				Cursor<? extends Statement> parentStatements = getStatements(subject, inheritanceProperty, null,
						true);

				Statement parentStatement;
				while ((parentStatement = parentStatements.next()) != null) {
					Value value = parentStatement.getObject();
					if (value instanceof Resource) {
						result.addAll(getPropertyResourceValues((Resource)value, predicate, false, contexts));
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
					Cursor<? extends Statement> statements = getStatements(null, ACL.USERNAME, usernameLiteral,
							true, ACL.CONTEXT);

					Statement st;
					if ((st = statements.next()) != null) {
						session.setUserId((URI)st.getSubject());
					}
					statements.close();
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

		HashMap<URI, List<Resource>> attributeValues = new HashMap<URI, List<Resource>>();

		for (URI attribute : attributes) {
			List<Resource> attributeValueList = getPropertyResourceValues(subject, attribute, true);
			if (attributeValueList != null && attributeValueList.size() > 0) {
				attributeValues.put(attribute, attributeValueList);
			}
		}

		if (attributeValues.size() > 0) {
			List<URI> permissions = getAssignedPermissions(getRolesForUser(user), operation);

			boolean[] attributeMatch = new boolean[attributes.size()];

			HashMap<URI, Boolean> attributeMatches = new HashMap<URI, Boolean>(attributes.size());

			for (URI permission : permissions) {

				for (URI attribute : attributeValues.keySet()) {

					attributeMatches.put(attribute, false);

					List<Resource> attributeValueList = attributeValues.get(attribute);

					if (attributeValueList != null) {
						URI permissionAttributeValue = getAttributeValueForPermission(permission, attribute);
						attributeMatches.put(attribute, attributeValueList.contains(permissionAttributeValue));
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

	protected class AccessControlQueryExpander extends QueryModelVisitorBase<StoreException> {

		private List<Var> handledSubjects = new ArrayList<Var>();

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
			Var inheritPredVar = new Var("acl_inherit_pred", inheritanceProp);

			int i = 0;

			List<Var> attributeVars = new ArrayList<Var>();
			for (URI attribute : attributes) {

				Var attributeVar = new Var("acl_attr_" + i++);
				attributeVars.add(attributeVar);

				Var attributePredVar = new Var("acl_attr_pred_" + i, attribute);

				// SP(?subject, accessAttr_i, ?accessAttrValue_i)
				StatementPattern attributePattern = new StatementPattern(subjectVar, attributePredVar,
						attributeVar);

				if (inheritanceProp != null) {
					// create a union expression for this attribute.
					Union union = new Union();
					union.addArg(attributePattern);

					// the join for checking if the access attribute is inherited.
					Join inheritJoin = new Join();
					Var inheritVar = new Var("acl_inherited_value" + i);
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
