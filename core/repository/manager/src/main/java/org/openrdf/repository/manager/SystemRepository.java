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
package org.openrdf.repository.manager;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryConfigUtil;
import org.openrdf.repository.event.base.NotifyingRepositoryWrapper;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * FIXME: do not extend NotifyingRepositoryWrapper, because SystemRepository
 * shouldn't expose RepositoryWrapper behaviour, just implement
 * NotifyingRepository.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class SystemRepository extends NotifyingRepositoryWrapper {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * The repository identifier for the system repository that contains the
	 * configuration data.
	 */
	public static final String ID = "SYSTEM";

	public static final String TITLE = "System configuration repository";

	public static final String REPOSITORY_TYPE = "openrdf:SystemRepository";

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SystemRepository(File systemDir)
		throws RepositoryException
	{
		super();
		super.setDelegate(new SailRepository(new MemoryStore(systemDir)));
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void initialize()
		throws RepositoryException
	{
		super.initialize();

		RepositoryConnection con = getConnection();
		try {
			if (con.isEmpty()) {
				logger.debug("Initializing empty {} repository", ID);

				con.begin();
				con.setNamespace("rdf", RDF.NAMESPACE);
				con.setNamespace("sys", RepositoryConfigSchema.NAMESPACE);
				con.commit();

				RepositoryConfig repConfig = new RepositoryConfig(ID, TITLE, new SystemRepositoryConfig());
				RepositoryConfigUtil.updateRepositoryConfigs(con, repConfig);

			}
		}
		catch (RepositoryConfigException e) {
			throw new RepositoryException(e.getMessage(), e);
		}
		finally {
			con.close();
		}
	}

	@Override
	public void setDelegate(Repository delegate)
	{
		throw new UnsupportedOperationException("Setting delegate on system repository not allowed");
	}
}
