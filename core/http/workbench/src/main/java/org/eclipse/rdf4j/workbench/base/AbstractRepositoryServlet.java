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
package org.eclipse.rdf4j.workbench.base;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RepositoryInfo;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.workbench.RepositoryServlet;
import org.eclipse.rdf4j.workbench.exceptions.MissingInitParameterException;

public abstract class AbstractRepositoryServlet extends AbstractServlet implements RepositoryServlet {
	public static final String REPOSITORY_PARAM = "repository";
	public static final String MANAGER_PARAM = "repository-manager";
	protected RepositoryManager manager;
	protected RepositoryInfo info;
	protected Repository repository;
	protected ValueFactory vf;

	public void setRepositoryManager(RepositoryManager manager) {
		this.manager = manager;
	}

	public void setRepositoryInfo(RepositoryInfo info) {
		this.info = info;
	}

	public void setRepository(Repository repository) {
		if (repository == null) {
			this.vf = SimpleValueFactory.getInstance();
		} else {
			this.repository = repository;
			this.vf = repository.getValueFactory();
			
			if (this.repository instanceof HTTPRepository) {
				((HTTPRepository)this.repository).setPreferredRDFFormat(RDFFormat.BINARY);
			}
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (repository == null) {
			if (config.getInitParameter(REPOSITORY_PARAM) != null)
				setRepository((Repository) lookup(config, REPOSITORY_PARAM));
		}
		if (manager == null) {
			if (config.getInitParameter(MANAGER_PARAM) == null)
				throw new MissingInitParameterException(MANAGER_PARAM);
			manager = (RepositoryManager) lookup(config, MANAGER_PARAM);
		}
		if (info == null) {
			info = new RepositoryInfo();
			info.setId(config.getInitParameter("id"));
			info.setDescription(config.getInitParameter("description"));
			try {
				if (repository == null) {
					info.setReadable(false);
					info.setWritable(false);
				} else {
					info.setReadable(true);
					info.setWritable(repository.isWritable());
				}
				String location = config.getInitParameter("location");
				if (location != null && location.trim().length() > 0) {
					info.setLocation(new URL(location));
				}
			} catch (MalformedURLException e) {
				throw new ServletException(e);
			} catch (RepositoryException e) {
				throw new ServletException(e);
			}
		}
	}

	private Object lookup(ServletConfig config, String name) throws ServletException {
		String param = config.getInitParameter(name);
		try {
			InitialContext ctx = new InitialContext();
			return ctx.lookup(param);
		} catch (NamingException e) {
			throw new ServletException(e);
		}
	}
}
