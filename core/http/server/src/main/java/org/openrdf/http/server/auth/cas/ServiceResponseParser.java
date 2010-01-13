/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.auth.cas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Arjohn Kampman
 */
class ServiceResponseParser {

	static ServiceResponse parse(Representation xmlEntity)
		throws IOException, CasParseException
	{
		DomRepresentation domEntity = new DomRepresentation(xmlEntity);
		Node typeNode = domEntity.getNode("/serviceResponse/*");
		if (typeNode instanceof Element) {
			Element typeElement = (Element)typeNode;
			String tagName = typeElement.getTagName();
			if ("cas:authenticationSuccess".equals(tagName)) {
				return parseAuthenticationSuccess(typeElement);
			}
			else if ("cas:authenticationFailure".equals(tagName)) {
				return parseAuthenticationFailure(typeElement);
			}
			else if ("cas:proxySuccess".equals(tagName)) {
				return parseProxySuccess(typeElement);
			}
			else if ("cas:proxyFailure".equals(tagName)) {
				return parseProxyFailure(typeElement);
			}
			else {
				throw new CasParseException();
			}
		}
		throw new CasParseException();
	}

	private static AuthSuccess parseAuthenticationSuccess(Element authSuccessEl)
		throws CasParseException
	{
		Node userNode = authSuccessEl.getElementsByTagName("cas:user").item(0);
		if (userNode == null) {
			throw new CasParseException();
		}
		String user = userNode.getTextContent().trim();

		Node pgtNode = authSuccessEl.getElementsByTagName("cas:proxyGrantingTicket").item(0);
		String pgt = pgtNode == null ? null : pgtNode.getTextContent().trim();

		NodeList proxyNodes = authSuccessEl.getElementsByTagName("cas:proxy");
		List<String> proxies = new ArrayList<String>(proxyNodes.getLength());
		for (int i = 0; i < proxyNodes.getLength(); i++) {
			proxies.add(proxyNodes.item(i).getTextContent().trim());
		}

		return new AuthSuccess(user, pgt, proxies);
	}

	private static AuthFailure parseAuthenticationFailure(Element authFailureEl) {
		String code = authFailureEl.getAttribute("code").trim();
		String message = authFailureEl.getTextContent().trim();
		return new AuthFailure(code, message);
	}

	private static ProxySuccess parseProxySuccess(Element proxySuccessEl)
		throws CasParseException
	{
		Node proxyTicketNode = proxySuccessEl.getElementsByTagName("cas:proxyTicket").item(0);
		if (proxyTicketNode == null) {
			throw new CasParseException();
		}
		String proxyTicket = proxyTicketNode.getTextContent().trim();
		return new ProxySuccess(proxyTicket);
	}

	private static ProxyFailure parseProxyFailure(Element proxyFailureEl) {
		String code = proxyFailureEl.getAttribute("code").trim();
		String message = proxyFailureEl.getTextContent().trim();
		return new ProxyFailure(code, message);
	}
}
