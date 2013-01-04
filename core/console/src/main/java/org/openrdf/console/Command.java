/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.console;

import java.io.IOException;

/**
 * Abstraction of console commands.
 * 
 * @author Dale Visser
 */
public interface Command {

	/**
	 * Execute the given parameters.
	 * 
	 * @param parameters
	 *        parameters typed by user
	 * @throws IOException 
	 * 	if a problem occurs reading or writing
	 */
	void execute(String... parameters) throws IOException;
}
