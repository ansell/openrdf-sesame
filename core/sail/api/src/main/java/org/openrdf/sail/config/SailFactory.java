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
package org.openrdf.sail.config;

import org.openrdf.sail.Sail;

/**
 * A SailFactory takes care of creating and initializing a specific type of
 * {@link Sail} based on RDF configuration data. SailFactory's are used to
 * create specific Sails and to initialize them based on the configuration data
 * that is supplied to it, for example in a server environment.
 * 
 * @author Arjohn Kampman
 */
public interface SailFactory {

	/**
	 * Returns the type of the Sails that this factory creates. Sail types are
	 * used for identification and should uniquely identify specific
	 * implementations of the Sail API. This type <em>can</em> be equal to the
	 * fully qualified class name of the Sail, but this is not required.
	 */
	public String getSailType();

	public SailImplConfig getConfig();

	/**
	 * Returns a Sail instance that has been initialized using the supplied
	 * configuration data.
	 * 
	 * @param config
	 *        TODO
	 * @return The created (but un-initialized) Sail.
	 * @throws SailConfigException
	 *         If no Sail could be created due to invalid or incomplete
	 *         configuration data.
	 */
	public Sail getSail(SailImplConfig config)
		throws SailConfigException;
}
