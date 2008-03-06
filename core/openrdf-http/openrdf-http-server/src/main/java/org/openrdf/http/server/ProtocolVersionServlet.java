/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.protocol.Protocol;

/**
 * Servlet whose sole task it is to report the HTTP communication protocol
 * version.
 */
public class ProtocolVersionServlet extends HttpServlet {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final long serialVersionUID = 8842675301711382081L;

	// Overrides HttpServlet.doGet(...)
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		String oldName = Thread.currentThread().getName();
		Thread.currentThread().setName("protocol");
		try {
			try {
				logger.info("=== sending protocol version ===");

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("text/plain");

				PrintWriter writer = response.getWriter();
				writer.print(Protocol.VERSION);
				writer.close();

				logger.debug("=== protocol version sent ===");
			}
			catch (Exception e) {
				logger.error("FAILED TO SEND PROTOCOL VERSION", e);
				response.sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		}
		finally {
			Thread.currentThread().setName(oldName);
		}
	}
}
