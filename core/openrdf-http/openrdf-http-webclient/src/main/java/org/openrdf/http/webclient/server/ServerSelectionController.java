/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.repository.RepositoryInfo;


public class ServerSelectionController extends SimpleFormController {

	static final String COOKIE_PREFIX = "server.select";
	static final String COOKIE_REMEMBER = "remember";
	static final String COOKIE_URL = "url";
	static final String COOKIE_TYPE = "type";
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
			Object commandObject, BindException be)
		throws Exception
	{
		ServerSelection command = (ServerSelection)commandObject;

		String path = request.getContextPath();

		Cookie useAlwaysCookie = new Cookie(COOKIE_PREFIX + "." + COOKIE_REMEMBER, String.valueOf(command.isRemember()));
		useAlwaysCookie.setPath(path);
		response.addCookie(useAlwaysCookie);

		Cookie serverURLCookie = new Cookie(COOKIE_PREFIX + "." + COOKIE_URL, command.getLocation());
		serverURLCookie.setPath(path);
		response.addCookie(serverURLCookie);

		Cookie serverTypeCookie = new Cookie(COOKIE_PREFIX + "." + COOKIE_TYPE, command.getType());
		serverTypeCookie.setPath(path);
		response.addCookie(serverTypeCookie);

		HttpSession session = request.getSession(true);
		session.setAttribute(Server.SERVER_KEY, new Server(command.getLocation()));
		session.removeAttribute(RepositoryInfo.REPOSITORY_KEY);

		return super.onSubmit(request, response, null, be);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request)
		throws Exception
	{
		Object result = super.formBackingObject(request);

		ServerSelection command = (ServerSelection)result;
		command.setDefaultServerURL(getDefaultServerUrl(request));

		setFromCookies(command, request.getCookies());

		return result;
	}

	static String getDefaultServerUrl(HttpServletRequest request) {
		StringBuilder result = new StringBuilder();

		try {
			URL requestURL = new URL(request.getRequestURL().toString());
			String protocol = requestURL.getProtocol();
			
			result.append(protocol);
			result.append("://");
			result.append(request.getServerName());
			if (!(protocol.equals("http") && request.getLocalPort() == 80)
					&& !(protocol.equals("https") && request.getLocalPort() == 443))
			{
				result.append(":");
				result.append(request.getLocalPort());
			}
			result.append("/openrdf/");
		}
		catch (MalformedURLException e) {
			// never happens
			e.printStackTrace();
		}

		return result.toString();
	}

	static void setFromCookies(ServerSelection command, Cookie[] cookies) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				if (cookieName.startsWith(COOKIE_PREFIX)) {
					String cookieValue = cookie.getValue();

					if (cookieName.endsWith(COOKIE_URL)) {
						command.setLocation(cookieValue);
					}
					else if (cookieName.endsWith(COOKIE_TYPE)) {
						command.setType(cookieValue);
					}
					else if (cookieName.endsWith(COOKIE_REMEMBER)) {
						command.setRemember(Boolean.parseBoolean(cookieValue));
					}
				}
			}
		}
	}
}
