/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.common.app.config;

import java.io.IOException;

public interface Configuration {

	public static final String DIR = "conf";

	public static final String RESOURCES_LOCATION = "/info/aduna/app/config/";

	public static final String DEFAULT_RESOURCES_LOCATION = RESOURCES_LOCATION + "defaults/";

	/**
	 * Initialize the configuration settings.
	 * 
	 * @throws IOException
	 *         if the configuration settings could not be initialized because of
	 *         an I/O problem.
	 */
	public void init()
		throws IOException;

	/**
	 * Load the configuration settings.
	 * 
	 * Settings will be loaded from a user and application specific location
	 * first. If no such settings exists, an attempt will be made to retrieve
	 * settings from a resource on the classpath. If no such settings exist
	 * either, settings will be loaded from a default resource on the classpath.
	 * 
	 * @throws IOException
	 *         if the configuration settings could not be loaded due to an I/O
	 *         problem.
	 */
	public void load()
		throws IOException;

	/**
	 * Store configuration settings.
	 * 
	 * Settings will be stored in a user and application specific location.
	 * 
	 * @throws IOException
	 *         if the configuration settings could not be saved due to an I/O
	 *         problem.
	 */
	public void save()
		throws IOException;

	/**
	 * Clean up configuration resources.
	 * 
	 * @throws IOException
	 *         if one or more resources could not be cleaned up. Implementations
	 *         should attempt to clean up as many resources as possible before
	 *         returning or throwing an exception.
	 */
	public void destroy()
		throws IOException;
}
