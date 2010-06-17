/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;


/**
 * Access Control vocabulary constants
 * 
 * @author Jeen Broekstra
 */
public class ACL {

	public static final String NAMESPACE = "http://www.openrdf.org/schema/sesame/acl/";
	
	public static final URI CONTEXT;
	public static final URI USER;
	public static final URI ROLE;
	public static final URI PERMISSION;
	public static final URI PERMISSION_ASSIGNMENT;
	public static final URI SUBJECT_MATCH;
	
	public static final URI HAS_ROLE;
	public static final URI HAS_PERMISSION;
	public static final URI HAS_OPERATION;
	public static final URI HAS_MATCH;
	public static final URI USERNAME;

	public static final URI TO_ROLE;
	
	
	// operations
	public static final URI VIEW;
	public static final URI EDIT;

	// 
	public static final URI ADMIN;

	public static final URI INHERITANCE_PROPERTY;
	public static final URI ACCESS_ATTRIBUTE;
	
	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();

		CONTEXT = factory.createURI(NAMESPACE, "context");
		
		USER = factory.createURI(NAMESPACE, "User");
		ROLE = factory.createURI(NAMESPACE, "Role");
		PERMISSION = factory.createURI(NAMESPACE, "Permission");
		PERMISSION_ASSIGNMENT = factory.createURI(NAMESPACE, "PermissionAssignment");
		SUBJECT_MATCH = factory.createURI(NAMESPACE, "SubjectMatch");
		
		HAS_ROLE = factory.createURI(NAMESPACE, "hasRole");
		HAS_PERMISSION = factory.createURI(NAMESPACE, "hasPermission");
		
		HAS_OPERATION = factory.createURI(NAMESPACE, "hasOperation");
		HAS_MATCH = factory.createURI(NAMESPACE, "hasMatch");
		USERNAME = factory.createURI(NAMESPACE, "username");
		
		TO_ROLE = factory.createURI(NAMESPACE, "toRole");
		
		VIEW = factory.createURI(NAMESPACE, "view");
		EDIT = factory.createURI(NAMESPACE, "edit");
	
		ADMIN = factory.createURI(NAMESPACE, "administrator");
		INHERITANCE_PROPERTY = factory.createURI(NAMESPACE, "InheritanceProperty");
		ACCESS_ATTRIBUTE = factory.createURI(NAMESPACE, "AccessAttribute");
		
	}
}
