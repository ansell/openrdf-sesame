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
package org.openrdf.repository.config;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;

/**
 * @author Arjohn Kampman
 */
public interface RepositoryImplConfig {

	public String getType();

	/**
	 * Validates this configuration. A {@link RepositoryConfigException} is
	 * thrown when the configuration is invalid. The exception should contain an
	 * error message that indicates why the configuration is invalid.
	 * 
	 * @throws RepositoryConfigException
	 *         If the configuration is invalid.
	 */
	public void validate()
		throws RepositoryConfigException;


	/**
	 * Export this {@link RepositoryImplConfig} to its RDF representation
	 * 
	 * @param model
	 *        a {@link Model} object. After successful completion of this method
	 *        this Model will contain the RDF representation of this
	 *        {@link RepositoryImplConfig}.
	 * @return the subject {@link Resource} that identifies this
	 *         {@link RepositoryImplConfig} in the Model.
	 */
	public Resource export(Model model);

	/**
	 * Reads the properties of this {@link RepositoryImplConfig} from the
	 * supplied Model and sets them accordingly.
	 * 
	 * @param model
	 *        a {@link Model} containing repository configuration data.
	 * @param resource
	 *        the subject {@link Resource} that identifies the
	 *        {@link RepositoryImplConfig} in the Model.
	 * @throws RepositoryConfigException
	 *         if the configuration data could not be read from the supplied
	 *         Model.
	 */
	public void parse(Model model, Resource resource)
		throws RepositoryConfigException;

}
