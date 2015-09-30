/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.model.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

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
