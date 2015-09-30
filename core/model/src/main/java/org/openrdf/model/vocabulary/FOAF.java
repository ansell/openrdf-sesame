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
package org.openrdf.model.vocabulary;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

/**
 * Constants for FOAF primitives and for the FOAF namespace.<br>
 * Resources here are defined according to the FOAF specs on <a href="http://xmlns.com/foaf/spec/">http://xmlns.com/foaf/spec/</a>, 
 * version 0.99, 14 January 2014
 */
public class FOAF {

	/**
	 * The FOAF namespace: http://xmlns.com/foaf/0.1/
	 */
	public static final String NAMESPACE = "http://xmlns.com/foaf/0.1/";

	/**
	 * The recommended prefix for the FOAF namespace: "foaf"
	 */
	public static final String PREFIX = "foaf";

	// ----- Classes ------
	public final static IRI AGENT;
	public final static IRI DOCUMENT;
	public final static IRI GROUP;
	public final static IRI IMAGE;
	public final static IRI LABEL_PROPERTY;
	public final static IRI ONLINE_ACCOUNT;
	public final static IRI ONLINE_CHAT_ACCOUNT;
	public final static IRI ONLINE_ECOMMERCE_ACCOUNT;
	public final static IRI ONLINE_GAMING_ACCOUNT;
	public final static IRI ORGANIZATION;
	public final static IRI PERSON;
	public final static IRI PERSONAL_PROFILE_DOCUMENT;
	public final static IRI PROJECT;

	// ----- Properties ------
	public final static IRI ACCOUNT;
	public final static IRI ACCOUNT_NAME;
	public final static IRI ACCOUNT_SERVICE_HOMEPAGE;
	public final static IRI AGE;
	public final static IRI AIM_CHAT_ID;
	public final static IRI BASED_NEAR;
	public final static IRI BIRTHDAY;
	public final static IRI CURRENT_PROJECT;
	public final static IRI DEPICTION;
	public final static IRI DEPICTS;
	public final static IRI DNA_CHECKSUM;
	public final static IRI FAMILY_NAME;
	/** @deprecated Use FAMILY_NAME instead for new statements */
	@Deprecated
	public final static IRI FAMILYNAME;
	public final static IRI FIRST_NAME;
	public final static IRI FOCUS;
	public final static IRI FUNDED_BY;
	public final static IRI GEEKCODE;
	public final static IRI GENDER;
	public final static IRI GIVEN_NAME;
	/** @deprecated Use GIVEN_NAME instead for new statements */
	@Deprecated
	public final static IRI GIVENNAME;
	public final static IRI HOLDS_ACCOUNT;
	public final static IRI HOMEPAGE;
	public final static IRI ICQ_CHAT_ID;
	public final static IRI IMG;
	public final static IRI INTEREST;
	public final static IRI IS_PRIMARY_TOPIC_OF;
	public final static IRI JABBER_ID;
	public final static IRI KNOWS;
	public final static IRI LAST_NAME;
	public final static IRI LOGO;
	public final static IRI MADE;
	public final static IRI MAKER;
	public static final IRI MBOX;
	public static final IRI MBOX_SHA1SUM;
	public static final IRI MEMBER;
	public static final IRI MEMBERSHIP_CLASS;
	public static final IRI MSN_CHAT_ID;
	public static final IRI MYERS_BRIGGS;
	public final static IRI NAME;
	public final static IRI NICK;
	public final static IRI OPENID;
	public final static IRI PAGE;
	public final static IRI PAST_PROJECT;
	public final static IRI PHONE;
	public final static IRI PLAN;
	public final static IRI PRIMARY_TOPIC;
	public final static IRI PUBLICATIONS;
	public final static IRI SCHOOL_HOMEPAGE;
	public final static IRI SHA1;
	public final static IRI SKYPE_ID;
	public final static IRI STATUS;
	public final static IRI SURNAME;
	public final static IRI THEME;
	public final static IRI THUMBNAIL;
	public final static IRI TIPJAR;
	public final static IRI TITLE;
	public final static IRI TOPIC;
	public final static IRI TOPIC_INTEREST;
	public final static IRI WEBLOG;
	public final static IRI WORK_INFO_HOMEPAGE;
	public final static IRI WORKPLACE_HOMEPAGE;
	public final static IRI YAHOO_CHAT_ID;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		
		// ----- Classes ------
		AGENT = factory.createIRI(FOAF.NAMESPACE, "Agent");
		DOCUMENT = factory.createIRI(FOAF.NAMESPACE, "Document");
		GROUP = factory.createIRI(FOAF.NAMESPACE, "Group");
		IMAGE = factory.createIRI(FOAF.NAMESPACE, "Image");
		LABEL_PROPERTY = factory.createIRI(FOAF.NAMESPACE, "LabelProperty");
		ONLINE_ACCOUNT = factory.createIRI(FOAF.NAMESPACE, "OnlineAccount");
		ONLINE_CHAT_ACCOUNT = factory.createIRI(FOAF.NAMESPACE, "OnlineChatAccount");
		ONLINE_ECOMMERCE_ACCOUNT = factory.createIRI(FOAF.NAMESPACE, "OnlineEcommerceAccount");
		ONLINE_GAMING_ACCOUNT = factory.createIRI(FOAF.NAMESPACE, "OnlineGamingAccount");
		ORGANIZATION = factory.createIRI(FOAF.NAMESPACE, "Organization");
		PERSON = factory.createIRI(FOAF.NAMESPACE, "Person");
		PERSONAL_PROFILE_DOCUMENT = factory.createIRI(FOAF.NAMESPACE, "PersonalProfileDocument");
		PROJECT = factory.createIRI(FOAF.NAMESPACE, "Project");

		// ----- Properties ------
		ACCOUNT = factory.createIRI(FOAF.NAMESPACE, "account");
		ACCOUNT_NAME = factory.createIRI(FOAF.NAMESPACE, "accountName");
		ACCOUNT_SERVICE_HOMEPAGE = factory.createIRI(FOAF.NAMESPACE, "accountServiceHomepage");
		AGE = factory.createIRI(FOAF.NAMESPACE, "age");
		AIM_CHAT_ID = factory.createIRI(FOAF.NAMESPACE, "aimChatID");
		BASED_NEAR = factory.createIRI(FOAF.NAMESPACE, "based_near");
		BIRTHDAY = factory.createIRI(FOAF.NAMESPACE, "birthday");
		CURRENT_PROJECT = factory.createIRI(FOAF.NAMESPACE, "currentProject");
		DEPICTION = factory.createIRI(FOAF.NAMESPACE, "depiction");
		DEPICTS = factory.createIRI(FOAF.NAMESPACE, "depicts");
		DNA_CHECKSUM = factory.createIRI(FOAF.NAMESPACE, "dnaChecksum");
		FAMILY_NAME = factory.createIRI(FOAF.NAMESPACE, "familyName");
		FAMILYNAME = factory.createIRI(FOAF.NAMESPACE, "family_name");
		FIRST_NAME = factory.createIRI(FOAF.NAMESPACE, "firstName");
		FOCUS = factory.createIRI(FOAF.NAMESPACE, "focus");
		FUNDED_BY = factory.createIRI(FOAF.NAMESPACE, "fundedBy");
		GEEKCODE = factory.createIRI(FOAF.NAMESPACE, "geekcode");
		GENDER = factory.createIRI(FOAF.NAMESPACE, "gender");
		GIVEN_NAME = factory.createIRI(FOAF.NAMESPACE, "givenName");
		GIVENNAME = factory.createIRI(FOAF.NAMESPACE, "givenname");
		HOLDS_ACCOUNT = factory.createIRI(FOAF.NAMESPACE, "holdsAccount");
		HOMEPAGE = factory.createIRI(FOAF.NAMESPACE, "homepage");
		ICQ_CHAT_ID = factory.createIRI(FOAF.NAMESPACE, "icqChatID");
		IMG = factory.createIRI(FOAF.NAMESPACE, "img");
		INTEREST = factory.createIRI(FOAF.NAMESPACE, "interest");
		IS_PRIMARY_TOPIC_OF = factory.createIRI(FOAF.NAMESPACE, "isPrimaryTopicOf");
		JABBER_ID = factory.createIRI(FOAF.NAMESPACE, "jabberID");
		KNOWS = factory.createIRI(FOAF.NAMESPACE, "knows");
		LAST_NAME = factory.createIRI(FOAF.NAMESPACE, "lastName");
		LOGO = factory.createIRI(FOAF.NAMESPACE, "logo");
		MADE = factory.createIRI(FOAF.NAMESPACE, "made");
		MAKER = factory.createIRI(FOAF.NAMESPACE, "maker");
		MBOX = factory.createIRI(FOAF.NAMESPACE, "mbox");
		MBOX_SHA1SUM = factory.createIRI(FOAF.NAMESPACE, "mbox_sha1sum");
		MEMBER = factory.createIRI(FOAF.NAMESPACE, "member");
		MEMBERSHIP_CLASS = factory.createIRI(FOAF.NAMESPACE, "membershipClass");
		MSN_CHAT_ID = factory.createIRI(FOAF.NAMESPACE, "msnChatID");
		MYERS_BRIGGS = factory.createIRI(FOAF.NAMESPACE, "myersBriggs");
		NAME = factory.createIRI(FOAF.NAMESPACE, "name");
		NICK = factory.createIRI(FOAF.NAMESPACE, "nick");
		OPENID = factory.createIRI(FOAF.NAMESPACE, "openid");
		PAGE = factory.createIRI(FOAF.NAMESPACE, "page");
		PAST_PROJECT = factory.createIRI(FOAF.NAMESPACE, "pastProject");
		PHONE = factory.createIRI(FOAF.NAMESPACE, "phone");
		PLAN = factory.createIRI(FOAF.NAMESPACE, "plan");
		PRIMARY_TOPIC = factory.createIRI(FOAF.NAMESPACE, "primaryTopic");
		PUBLICATIONS = factory.createIRI(FOAF.NAMESPACE, "publications");
		SCHOOL_HOMEPAGE = factory.createIRI(FOAF.NAMESPACE, "schoolHomepage");
		SHA1 = factory.createIRI(FOAF.NAMESPACE, "sha1");
		SKYPE_ID = factory.createIRI(FOAF.NAMESPACE, "skypeID");
		STATUS = factory.createIRI(FOAF.NAMESPACE, "status");
		SURNAME = factory.createIRI(FOAF.NAMESPACE, "surname");
		THEME = factory.createIRI(FOAF.NAMESPACE, "theme");
		THUMBNAIL = factory.createIRI(FOAF.NAMESPACE, "thumbnail");
		TIPJAR = factory.createIRI(FOAF.NAMESPACE, "tipjar");
		TITLE = factory.createIRI(FOAF.NAMESPACE, "title");
		TOPIC = factory.createIRI(FOAF.NAMESPACE, "topic");
		TOPIC_INTEREST = factory.createIRI(FOAF.NAMESPACE, "topic_interest");
		WEBLOG = factory.createIRI(FOAF.NAMESPACE, "weblog");
		WORK_INFO_HOMEPAGE = factory.createIRI(FOAF.NAMESPACE, "workInfoHomepage");
		WORKPLACE_HOMEPAGE = factory.createIRI(FOAF.NAMESPACE, "workplaceHomepage");
		YAHOO_CHAT_ID = factory.createIRI(FOAF.NAMESPACE, "yahooChatID");
	}
}
