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
package org.eclipse.rdf4j.sail.helpers;

import java.io.File;
import java.util.List;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.StackableSail;

/**
 * An implementation of the StackableSail interface that wraps another Sail
 * object and forwards any relevant calls to the wrapped Sail.
 * 
 * @author Arjohn Kampman
 */
public class SailWrapper implements StackableSail, FederatedServiceResolverClient {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The base Sail for this SailWrapper.
	 */
	private Sail baseSail;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new SailWrapper. The base Sail for the created SailWrapper can
	 * be set later using {@link #setBaseSail}.
	 */
	public SailWrapper() {
	}

	/**
	 * Creates a new SailWrapper that wraps the supplied Sail.
	 */
	public SailWrapper(Sail baseSail) {
		setBaseSail(baseSail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setBaseSail(Sail baseSail) {
		this.baseSail = baseSail;
	}

	@Override
	public Sail getBaseSail() {
		return baseSail;
	}

	protected void verifyBaseSailSet() {
		if (baseSail == null) {
			throw new IllegalStateException("No base Sail has been set");
		}
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		if (baseSail instanceof FederatedServiceResolverClient) {
			((FederatedServiceResolverClient)baseSail).setFederatedServiceResolver(resolver);
		}
	}

	@Override
	public File getDataDir() {
		return baseSail.getDataDir();
	}

	@Override
	public void setDataDir(File dataDir) {
		baseSail.setDataDir(dataDir);
	}

	@Override
	public void initialize()
		throws SailException
	{
		verifyBaseSailSet();
		baseSail.initialize();
	}

	@Override
	public void shutDown()
		throws SailException
	{
		verifyBaseSailSet();
		baseSail.shutDown();
	}

	@Override
	public boolean isWritable()
		throws SailException
	{
		verifyBaseSailSet();
		return baseSail.isWritable();
	}

	@Override
	public SailConnection getConnection()
		throws SailException
	{
		verifyBaseSailSet();
		return baseSail.getConnection();
	}

	@Override
	public ValueFactory getValueFactory() {
		verifyBaseSailSet();
		return baseSail.getValueFactory();
	}

	@Override
	public List<IsolationLevel> getSupportedIsolationLevels() {
		verifyBaseSailSet();
		return baseSail.getSupportedIsolationLevels();
	}

	@Override
	public IsolationLevel getDefaultIsolationLevel() {
		verifyBaseSailSet();
		return baseSail.getDefaultIsolationLevel();
	}
}
