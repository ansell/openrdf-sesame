/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Constants for DOAP primitives and for the DOAP namespace.
 */
public class DOAP {

	public static final String NAMESPACE = "http://usefulinc.com/ns/doap#";

	/**
	 * Classes
	 */

	public final static URI PROJECT;

	public final static URI VERSION;

	/**
	 * Properties
	 */

	public final static URI NAME;

	public final static URI HOMEPAGE;

	public final static URI OLD_HOMEPAGE;

	public final static URI LICENSE;

	public final static URI DESCRIPTION;

	public final static URI PROGRAMMING_LANGUAGE;

	public final static URI IMPLEMENTS;

	public final static URI CATEGORY;

	public final static URI DOWNLOAD_PAGE;

	public final static URI MAILING_LIST;

	public final static URI BUG_DATABASE;

	public final static URI BLOG;

	public final static URI DEVELOPER;

	public final static URI MAINTAINER;

	public final static URI DOCUMENTER;

	public final static URI RELEASE;

	public final static URI CREATED;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PROJECT = factory.createURI(DOAP.NAMESPACE, "Project");
		VERSION = factory.createURI(DOAP.NAMESPACE, "Version");

		NAME = factory.createURI(DOAP.NAMESPACE, "name");
		HOMEPAGE = factory.createURI(DOAP.NAMESPACE, "homepage");
		OLD_HOMEPAGE = factory.createURI(DOAP.NAMESPACE, "old-homepage");
		LICENSE = factory.createURI(DOAP.NAMESPACE, "license");
		DESCRIPTION = factory.createURI(DOAP.NAMESPACE, "description");
		PROGRAMMING_LANGUAGE = factory.createURI(DOAP.NAMESPACE, "programming-language");
		IMPLEMENTS = factory.createURI(DOAP.NAMESPACE, "implements");
		CATEGORY = factory.createURI(DOAP.NAMESPACE, "category");
		DOWNLOAD_PAGE = factory.createURI(DOAP.NAMESPACE, "download-page");
		MAILING_LIST = factory.createURI(DOAP.NAMESPACE, "mailing-list");
		BUG_DATABASE = factory.createURI(DOAP.NAMESPACE, "bug-database");
		BLOG = factory.createURI(DOAP.NAMESPACE, "blog");
		DEVELOPER = factory.createURI(DOAP.NAMESPACE, "developer");
		MAINTAINER = factory.createURI(DOAP.NAMESPACE, "maintainer");
		DOCUMENTER = factory.createURI(DOAP.NAMESPACE, "documenter");
		RELEASE = factory.createURI(DOAP.NAMESPACE, "release");
		CREATED = factory.createURI(DOAP.NAMESPACE, "created");
	}
}
