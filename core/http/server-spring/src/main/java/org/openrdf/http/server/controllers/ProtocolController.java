/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import java.io.StringReader;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.openrdf.http.protocol.Protocol;

/**
 * Handles requests for protocol information. Currently returns the protocol
 * version as plain text.
 * 
 * @author Herko ter Horst
 * @author James Leigh
 */
@Controller
public class ProtocolController {

	@ModelAttribute
	@RequestMapping(method = RequestMethod.GET, value = "/protocol")
	public StringReader protocol() {
		return new StringReader(Protocol.VERSION);
	}
}
