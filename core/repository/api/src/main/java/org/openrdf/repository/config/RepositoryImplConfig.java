/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
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
