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
package org.eclipse.rdf4j.common.webapp.util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.common.lang.service.FileFormatServiceRegistry;
import org.eclipse.rdf4j.common.webapp.util.HttpServerUtil;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jeen
 */
public class HttpServerUtilTest {

	private ArrayList<String> tupleQueryMimeTypes;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		FileFormatServiceRegistry<? extends FileFormat, ?> registry = TupleQueryResultWriterRegistry.getInstance();

		tupleQueryMimeTypes = new ArrayList<String>(16);
		for (FileFormat format : registry.getKeys()) {
			tupleQueryMimeTypes.addAll(format.getMIMETypes());
		}
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.common.webapp.util.HttpServerUtil#selectPreferredMIMEType(java.util.Iterator, javax.servlet.http.HttpServletRequest)}
	 * .
	 */
	@Test
	public void testSelectPreferredMIMEType1() {

		ServletRequestStub testRequest = new ServletRequestStub("application/sparql-results+json, */*");

		String preferredType = HttpServerUtil.selectPreferredMIMEType(tupleQueryMimeTypes.iterator(),
				testRequest);

		assertEquals("application/sparql-results+json", preferredType);

	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.common.webapp.util.HttpServerUtil#selectPreferredMIMEType(java.util.Iterator, javax.servlet.http.HttpServletRequest)}
	 * .
	 */
	@Test
	public void testSelectPreferredMIMEType2() {

		ServletRequestStub testRequest = new ServletRequestStub("application/sparql-results+json, */*;q=0.9");

		String preferredType = HttpServerUtil.selectPreferredMIMEType(tupleQueryMimeTypes.iterator(),
				testRequest);

		assertEquals("application/sparql-results+json", preferredType);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.common.webapp.util.HttpServerUtil#selectPreferredMIMEType(java.util.Iterator, javax.servlet.http.HttpServletRequest)}
	 * .
	 */
	@Test
	public void testSelectPreferredMIMEType3() {

		ServletRequestStub testRequest = new ServletRequestStub("application/xml");

		String preferredType = HttpServerUtil.selectPreferredMIMEType(tupleQueryMimeTypes.iterator(),
				testRequest);

		assertEquals("application/xml", preferredType);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.common.webapp.util.HttpServerUtil#selectPreferredMIMEType(java.util.Iterator, javax.servlet.http.HttpServletRequest)}
	 * .
	 */
	@Test
	public void testSelectPreferredMIMEType4() {

		ServletRequestStub testRequest = new ServletRequestStub("*/*", "application/sparql-result+xml;q=0.9",
				"application/sparql-results+json");

		String preferredType = HttpServerUtil.selectPreferredMIMEType(tupleQueryMimeTypes.iterator(),
				testRequest);

		assertEquals("application/sparql-results+json", preferredType);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.common.webapp.util.HttpServerUtil#selectPreferredMIMEType(java.util.Iterator, javax.servlet.http.HttpServletRequest)}
	 * .
	 */
	@Test
	public void testSelectPreferredMIMEType5() {

		ServletRequestStub testRequest = new ServletRequestStub("application/*", "application/sparql-results+json");

		String preferredType = HttpServerUtil.selectPreferredMIMEType(tupleQueryMimeTypes.iterator(),
				testRequest);

		assertEquals("application/sparql-results+json", preferredType);
	}
	class ServletRequestStub implements javax.servlet.http.HttpServletRequest {

		private Enumeration<String> testHeaders;

		public ServletRequestStub(String... testHeaders) {
			this.testHeaders = Collections.enumeration(Arrays.asList(testHeaders));
		}

		public Object getAttribute(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getAttributeNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getCharacterEncoding() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException
		{
			// TODO Auto-generated method stub

		}

		public int getContentLength() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getContentType() {
			// TODO Auto-generated method stub
			return null;
		}

		public ServletInputStream getInputStream()
			throws IOException
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getParameter(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getParameterNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public String[] getParameterValues(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		public Map getParameterMap() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getProtocol() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getScheme() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getServerName() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getServerPort() {
			// TODO Auto-generated method stub
			return 0;
		}

		public BufferedReader getReader()
			throws IOException
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getRemoteAddr() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRemoteHost() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setAttribute(String name, Object o) {
			// TODO Auto-generated method stub

		}

		public void removeAttribute(String name) {
			// TODO Auto-generated method stub

		}

		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getLocales() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isSecure() {
			// TODO Auto-generated method stub
			return false;
		}

		public RequestDispatcher getRequestDispatcher(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRealPath(String path) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getRemotePort() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getLocalName() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getLocalAddr() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getLocalPort() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getAuthType() {
			// TODO Auto-generated method stub
			return null;
		}

		public Cookie[] getCookies() {
			// TODO Auto-generated method stub
			return null;
		}

		public long getDateHeader(String name) {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getHeader(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getHeaders(String name) {
			return testHeaders;
		}

		public Enumeration getHeaderNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getIntHeader(String name) {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getMethod() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getPathInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getPathTranslated() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getContextPath() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getQueryString() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRemoteUser() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isUserInRole(String role) {
			// TODO Auto-generated method stub
			return false;
		}

		public Principal getUserPrincipal() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRequestedSessionId() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRequestURI() {
			// TODO Auto-generated method stub
			return null;
		}

		public StringBuffer getRequestURL() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getServletPath() {
			// TODO Auto-generated method stub
			return null;
		}

		public HttpSession getSession(boolean create) {
			// TODO Auto-generated method stub
			return null;
		}

		public HttpSession getSession() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isRequestedSessionIdValid() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isRequestedSessionIdFromCookie() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isRequestedSessionIdFromURL() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isRequestedSessionIdFromUrl() {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
