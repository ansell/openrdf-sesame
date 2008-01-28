/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.protocol;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.ServerInterceptor;

/**
 * Interceptor for protocol requests. Should not be a singleton bean! Configure
 * as inner bean in openrdf-servlet.xml
 * 
 * @author Herko ter Horst
 */
public class ProtocolInterceptor extends ServerInterceptor {

	@Override
	protected String getThreadName()
	{
		return Protocol.PROTOCOL;
	}
}
