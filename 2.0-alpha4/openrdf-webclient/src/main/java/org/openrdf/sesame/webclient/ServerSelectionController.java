/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.sesame.webclient.server.SesameServer;

public class ServerSelectionController extends SimpleFormController {

	static final String COOKIE_PREFIX = "overview.server";
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
			Object commandObject, BindException be)
		throws Exception
	{
		ServerSelection command = (ServerSelection)commandObject;

		String path = request.getContextPath();

		Cookie useAlwaysCookie = new Cookie(COOKIE_PREFIX + ".useAlways", String.valueOf(command.isUseAlways()));
		useAlwaysCookie.setPath(path);
		response.addCookie(useAlwaysCookie);

		Cookie serverURLCookie = new Cookie(COOKIE_PREFIX + ".url", command.getServerURL());
		serverURLCookie.setPath(path);
		response.addCookie(serverURLCookie);

		Cookie serverTypeCookie = new Cookie(COOKIE_PREFIX + ".type", command.getType());
		serverTypeCookie.setPath(path);
		response.addCookie(serverTypeCookie);

		HttpSession session = request.getSession(true);
		session.setAttribute(SesameServer.SERVER_URL_KEY, command.getServerURL());

		return super.onSubmit(request, response, null, be);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request)
		throws Exception
	{
		Object result = super.formBackingObject(request);

		ServerSelection command = (ServerSelection)result;
		command.setLocalServerURL(buildLocalSesameURL(request));

		setFromCookies(command, request.getCookies());

		return result;
	}

	static String buildLocalSesameURL(HttpServletRequest request) {
		StringBuilder result = new StringBuilder();

		try {
			URL requestUrl = new URL(request.getRequestURL().toString());
			String protocol = requestUrl.getProtocol();
			
			result.append(protocol);
			result.append("://");
			result.append(request.getServerName());
			if (!(protocol.equals("http") && request.getLocalPort() == 80)
					&& !(protocol.equals("https") && request.getLocalPort() == 443))
			{
				result.append(":");
				result.append(request.getLocalPort());
			}
			result.append("/sesame/");
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

					if (cookieName.endsWith("url")) {
						command.setServerURL(cookieValue);
					}
					else if (cookieName.endsWith("type")) {
						command.setType(cookieValue);
					}
					else if (cookieName.endsWith("useAlways")) {
						command.setUseAlways(Boolean.parseBoolean(cookieValue));
					}
				}
			}
		}
	}
}
