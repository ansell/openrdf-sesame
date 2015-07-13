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

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Constants for DOAP primitives and for the DOAP namespace.
 */
public class DOAP {

	public static final String NAMESPACE = "http://usefulinc.com/ns/doap#";

	/**
	 * Classes
	 */

	public final static IRI PROJECT;

	public final static IRI VERSION;

	/**
	 * Properties
	 */

	public final static IRI NAME;

	public final static IRI HOMEPAGE;

	public final static IRI OLD_HOMEPAGE;

	public final static IRI LICENSE;

	public final static IRI DESCRIPTION;

	public final static IRI PROGRAMMING_LANGUAGE;

	public final static IRI IMPLEMENTS;

	public final static IRI CATEGORY;

	public final static IRI DOWNLOAD_PAGE;

	public final static IRI MAILING_LIST;

	public final static IRI BUG_DATABASE;

	public final static IRI BLOG;

	public final static IRI DEVELOPER;

	public final static IRI MAINTAINER;

	public final static IRI DOCUMENTER;

	public final static IRI RELEASE;

	public final static IRI CREATED;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		PROJECT = factory.createIRI(DOAP.NAMESPACE, "Project");
		VERSION = factory.createIRI(DOAP.NAMESPACE, "Version");

		NAME = factory.createIRI(DOAP.NAMESPACE, "name");
		HOMEPAGE = factory.createIRI(DOAP.NAMESPACE, "homepage");
		OLD_HOMEPAGE = factory.createIRI(DOAP.NAMESPACE, "old-homepage");
		LICENSE = factory.createIRI(DOAP.NAMESPACE, "license");
		DESCRIPTION = factory.createIRI(DOAP.NAMESPACE, "description");
		PROGRAMMING_LANGUAGE = factory.createIRI(DOAP.NAMESPACE, "programming-language");
		IMPLEMENTS = factory.createIRI(DOAP.NAMESPACE, "implements");
		CATEGORY = factory.createIRI(DOAP.NAMESPACE, "category");
		DOWNLOAD_PAGE = factory.createIRI(DOAP.NAMESPACE, "download-page");
		MAILING_LIST = factory.createIRI(DOAP.NAMESPACE, "mailing-list");
		BUG_DATABASE = factory.createIRI(DOAP.NAMESPACE, "bug-database");
		BLOG = factory.createIRI(DOAP.NAMESPACE, "blog");
		DEVELOPER = factory.createIRI(DOAP.NAMESPACE, "developer");
		MAINTAINER = factory.createIRI(DOAP.NAMESPACE, "maintainer");
		DOCUMENTER = factory.createIRI(DOAP.NAMESPACE, "documenter");
		RELEASE = factory.createIRI(DOAP.NAMESPACE, "release");
		CREATED = factory.createIRI(DOAP.NAMESPACE, "created");
	}
}
