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
	public final static URI AGENT;
	public final static URI DOCUMENT;
	public final static URI GROUP;
	public final static URI IMAGE;
	public final static URI LABEL_PROPERTY;
	public final static URI ONLINE_ACCOUNT;
	public final static URI ONLINE_CHAT_ACCOUNT;
	public final static URI ONLINE_ECOMMERCE_ACCOUNT;
	public final static URI ONLINE_GAMING_ACCOUNT;
	public final static URI ORGANIZATION;
	public final static URI PERSON;
	public final static URI PERSONAL_PROFILE_DOCUMENT;
	public final static URI PROJECT;

	// ----- Properties ------
	public final static URI ACCOUNT;
	public final static URI ACCOUNT_NAME;
	public final static URI ACCOUNT_SERVICE_HOMEPAGE;
	public final static URI AGE;
	public final static URI AIM_CHAT_ID;
	public final static URI BASED_NEAR;
	public final static URI BIRTHDAY;
	public final static URI CURRENT_PROJECT;
	public final static URI DEPICTION;
	public final static URI DEPICTS;
	public final static URI DNA_CHECKSUM;
	public final static URI FAMILY_NAME;
	/** @Deprecated Use FAMILY_NAME instead for new statements */
	@Deprecated
	public final static URI FAMILYNAME;
	public final static URI FIRST_NAME;
	public final static URI FOCUS;
	public final static URI FUNDED_BY;
	public final static URI GEEKCODE;
	public final static URI GENDER;
	public final static URI GIVEN_NAME;
	/** @Deprecated Use GIVEN_NAME instead for new statements */
	@Deprecated
	public final static URI GIVENNAME;
	public final static URI HOLDS_ACCOUNT;
	public final static URI HOMEPAGE;
	public final static URI ICQ_CHAT_ID;
	public final static URI IMG;
	public final static URI INTEREST;
	public final static URI IS_PRIMARY_TOPIC_OF;
	public final static URI JABBER_ID;
	public final static URI KNOWS;
	public final static URI LAST_NAME;
	public final static URI LOGO;
	public final static URI MADE;
	public final static URI MAKER;
	public static final URI MBOX;
	public static final URI MBOX_SHA1SUM;
	public static final URI MEMBER;
	public static final URI MEMBERSHIP_CLASS;
	public static final URI MSN_CHAT_ID;
	public static final URI MYERS_BRIGGS;
	public final static URI NAME;
	public final static URI NICK;
	public final static URI OPENID;
	public final static URI PAGE;
	public final static URI PAST_PROJECT;
	public final static URI PHONE;
	public final static URI PLAN;
	public final static URI PRIMARY_TOPIC;
	public final static URI PUBLICATIONS;
	public final static URI SCHOOL_HOMEPAGE;
	public final static URI SHA1;
	public final static URI SKYPE_ID;
	public final static URI STATUS;
	public final static URI SURNAME;
	public final static URI THEME;
	public final static URI THUMBNAIL;
	public final static URI TIPJAR;
	public final static URI TITLE;
	public final static URI TOPIC;
	public final static URI TOPIC_INTEREST;
	public final static URI WEBLOG;
	public final static URI WORK_INFO_HOMEPAGE;
	public final static URI WORKPLACE_HOMEPAGE;
	public final static URI YAHOO_CHAT_ID;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		
		// ----- Classes ------
		AGENT = factory.createURI(FOAF.NAMESPACE, "Agent");
		DOCUMENT = factory.createURI(FOAF.NAMESPACE, "Document");
		GROUP = factory.createURI(FOAF.NAMESPACE, "Group");
		IMAGE = factory.createURI(FOAF.NAMESPACE, "Image");
		LABEL_PROPERTY = factory.createURI(FOAF.NAMESPACE, "LabelProperty");
		ONLINE_ACCOUNT = factory.createURI(FOAF.NAMESPACE, "OnlineAccount");
		ONLINE_CHAT_ACCOUNT = factory.createURI(FOAF.NAMESPACE, "OnlineChatAccount");
		ONLINE_ECOMMERCE_ACCOUNT = factory.createURI(FOAF.NAMESPACE, "OnlineEcommerceAccount");
		ONLINE_GAMING_ACCOUNT = factory.createURI(FOAF.NAMESPACE, "OnlineGamingAccount");
		ORGANIZATION = factory.createURI(FOAF.NAMESPACE, "Organization");
		PERSON = factory.createURI(FOAF.NAMESPACE, "Person");
		PERSONAL_PROFILE_DOCUMENT = factory.createURI(FOAF.NAMESPACE, "PersonalProfileDocument");
		PROJECT = factory.createURI(FOAF.NAMESPACE, "Project");

		// ----- Properties ------
		ACCOUNT = factory.createURI(FOAF.NAMESPACE, "account");
		ACCOUNT_NAME = factory.createURI(FOAF.NAMESPACE, "accountName");
		ACCOUNT_SERVICE_HOMEPAGE = factory.createURI(FOAF.NAMESPACE, "accountServiceHomepage");
		AGE = factory.createURI(FOAF.NAMESPACE, "age");
		AIM_CHAT_ID = factory.createURI(FOAF.NAMESPACE, "aimChatID");
		BASED_NEAR = factory.createURI(FOAF.NAMESPACE, "based_near");
		BIRTHDAY = factory.createURI(FOAF.NAMESPACE, "birthday");
		CURRENT_PROJECT = factory.createURI(FOAF.NAMESPACE, "currentProject");
		DEPICTION = factory.createURI(FOAF.NAMESPACE, "depiction");
		DEPICTS = factory.createURI(FOAF.NAMESPACE, "depicts");
		DNA_CHECKSUM = factory.createURI(FOAF.NAMESPACE, "dnaChecksum");
		FAMILY_NAME = factory.createURI(FOAF.NAMESPACE, "familyName");
		FAMILYNAME = factory.createURI(FOAF.NAMESPACE, "family_name");
		FIRST_NAME = factory.createURI(FOAF.NAMESPACE, "firstName");
		FOCUS = factory.createURI(FOAF.NAMESPACE, "focus");
		FUNDED_BY = factory.createURI(FOAF.NAMESPACE, "fundedBy");
		GEEKCODE = factory.createURI(FOAF.NAMESPACE, "geekcode");
		GENDER = factory.createURI(FOAF.NAMESPACE, "gender");
		GIVEN_NAME = factory.createURI(FOAF.NAMESPACE, "givenName");
		GIVENNAME = factory.createURI(FOAF.NAMESPACE, "givenname");
		HOLDS_ACCOUNT = factory.createURI(FOAF.NAMESPACE, "holdsAccount");
		HOMEPAGE = factory.createURI(FOAF.NAMESPACE, "homepage");
		ICQ_CHAT_ID = factory.createURI(FOAF.NAMESPACE, "icqChatID");
		IMG = factory.createURI(FOAF.NAMESPACE, "img");
		INTEREST = factory.createURI(FOAF.NAMESPACE, "interest");
		IS_PRIMARY_TOPIC_OF = factory.createURI(FOAF.NAMESPACE, "isPrimaryTopicOf");
		JABBER_ID = factory.createURI(FOAF.NAMESPACE, "jabberID");
		KNOWS = factory.createURI(FOAF.NAMESPACE, "knows");
		LAST_NAME = factory.createURI(FOAF.NAMESPACE, "lastName");
		LOGO = factory.createURI(FOAF.NAMESPACE, "logo");
		MADE = factory.createURI(FOAF.NAMESPACE, "made");
		MAKER = factory.createURI(FOAF.NAMESPACE, "maker");
		MBOX = factory.createURI(FOAF.NAMESPACE, "mbox");
		MBOX_SHA1SUM = factory.createURI(FOAF.NAMESPACE, "mbox_sha1sum");
		MEMBER = factory.createURI(FOAF.NAMESPACE, "member");
		MEMBERSHIP_CLASS = factory.createURI(FOAF.NAMESPACE, "membershipClass");
		MSN_CHAT_ID = factory.createURI(FOAF.NAMESPACE, "msnChatID");
		MYERS_BRIGGS = factory.createURI(FOAF.NAMESPACE, "myersBriggs");
		NAME = factory.createURI(FOAF.NAMESPACE, "name");
		NICK = factory.createURI(FOAF.NAMESPACE, "nick");
		OPENID = factory.createURI(FOAF.NAMESPACE, "openid");
		PAGE = factory.createURI(FOAF.NAMESPACE, "page");
		PAST_PROJECT = factory.createURI(FOAF.NAMESPACE, "pastProject");
		PHONE = factory.createURI(FOAF.NAMESPACE, "phone");
		PLAN = factory.createURI(FOAF.NAMESPACE, "plan");
		PRIMARY_TOPIC = factory.createURI(FOAF.NAMESPACE, "primaryTopic");
		PUBLICATIONS = factory.createURI(FOAF.NAMESPACE, "publications");
		SCHOOL_HOMEPAGE = factory.createURI(FOAF.NAMESPACE, "schoolHomepage");
		SHA1 = factory.createURI(FOAF.NAMESPACE, "sha1");
		SKYPE_ID = factory.createURI(FOAF.NAMESPACE, "skypeID");
		STATUS = factory.createURI(FOAF.NAMESPACE, "status");
		SURNAME = factory.createURI(FOAF.NAMESPACE, "surname");
		THEME = factory.createURI(FOAF.NAMESPACE, "theme");
		THUMBNAIL = factory.createURI(FOAF.NAMESPACE, "thumbnail");
		TIPJAR = factory.createURI(FOAF.NAMESPACE, "tipjar");
		TITLE = factory.createURI(FOAF.NAMESPACE, "title");
		TOPIC = factory.createURI(FOAF.NAMESPACE, "topic");
		TOPIC_INTEREST = factory.createURI(FOAF.NAMESPACE, "topic_interest");
		WEBLOG = factory.createURI(FOAF.NAMESPACE, "weblog");
		WORK_INFO_HOMEPAGE = factory.createURI(FOAF.NAMESPACE, "workInfoHomepage");
		WORKPLACE_HOMEPAGE = factory.createURI(FOAF.NAMESPACE, "workplaceHomepage");
		YAHOO_CHAT_ID = factory.createURI(FOAF.NAMESPACE, "yahooChatID");
	}
}
