/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.protocol;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.http.protocol.Protocol;

/**
 * Handles requests for protocol information.
 * 
 * Currently returns the protocol version as plain text.
 * 
 * @author Herko ter Horst
 */
public class ProtocolController extends AbstractController {

	public ProtocolController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(SimpleResponseView.CONTENT_KEY, Protocol.VERSION);
		return new ModelAndView(SimpleResponseView.getInstance(), model);
	}
}
