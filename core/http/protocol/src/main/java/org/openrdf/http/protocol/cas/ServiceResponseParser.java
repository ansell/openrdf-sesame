/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.cas;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Arjohn Kampman
 */
public class ServiceResponseParser {

	public static ServiceResponse parse(InputStream xmlStream)
		throws IOException, CasParseException, ParserConfigurationException, SAXException
	{
		Document document = createBuilder().parse(xmlStream);
		return parse(document);
	}

	public static ServiceResponse parse(String xmlString)
		throws IOException, CasParseException, ParserConfigurationException, SAXException
	{
		Document document = createBuilder().parse(new InputSource(new StringReader(xmlString)));
		return parse(document);
	}

	private static DocumentBuilder createBuilder()
		throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		return factory.newDocumentBuilder();
	}

	private static ServiceResponse parse(Document document)
		throws CasParseException
	{
		NodeList nodeList = document.getElementsByTagName("cas:authenticationSuccess");
		if (nodeList.getLength() > 0) {
			return parseAuthenticationSuccess((Element)nodeList.item(0));
		}

		nodeList = document.getElementsByTagName("cas:authenticationFailure");
		if (nodeList.getLength() > 0) {
			return parseAuthenticationFailure((Element)nodeList.item(0));
		}

		nodeList = document.getElementsByTagName("cas:proxySuccess");
		if (nodeList.getLength() > 0) {
			return parseProxySuccess((Element)nodeList.item(0));
		}

		nodeList = document.getElementsByTagName("cas:proxyFailure");
		if (nodeList.getLength() > 0) {
			return parseProxyFailure((Element)nodeList.item(0));
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
