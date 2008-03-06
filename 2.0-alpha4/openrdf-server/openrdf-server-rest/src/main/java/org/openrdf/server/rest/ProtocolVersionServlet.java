/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.server.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.protocol.rest.Protocol;
import org.openrdf.util.log.ThreadLog;

/**
 * Servlet whose sole task it is to report the HTTP communication protocol
 * version.
 */
public class ProtocolVersionServlet extends HttpServlet {
	
	private static final long serialVersionUID = 8842675301711382081L;
	
	// Overrides HttpServlet.doGet(...)
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		try {
			ThreadLog.log("=== sending protocol version ===");

			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain");

			PrintWriter writer = response.getWriter();
			writer.print(Protocol.VERSION);
			writer.close();

			ThreadLog.trace("=== protocol version sent ===");
		}
		catch (Exception e) {
			ThreadLog.error("FAILED TO SEND PROTOCOL VERSION", e);
			response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
}
