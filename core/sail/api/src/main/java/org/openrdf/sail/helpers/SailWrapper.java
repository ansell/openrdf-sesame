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
package org.openrdf.sail.helpers;

import java.io.File;
import java.util.List;

import org.openrdf.IsolationLevel;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.StackableSail;

/**
 * An implementation of the StackableSail interface that wraps another Sail
 * object and forwards any relevant calls to the wrapped Sail.
 * 
 * @author Arjohn Kampman
 */
public class SailWrapper implements StackableSail {

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

	public void setBaseSail(Sail baseSail) {
		this.baseSail = baseSail;
	}

	public Sail getBaseSail() {
		return baseSail;
	}

	protected void verifyBaseSailSet() {
		if (baseSail == null) {
			throw new IllegalStateException("No base Sail has been set");
		}
	}

	public File getDataDir() {
		return baseSail.getDataDir();
	}

	public void setDataDir(File dataDir) {
		baseSail.setDataDir(dataDir);
	}

	public void initialize()
		throws SailException
	{
		verifyBaseSailSet();
		baseSail.initialize();
	}

	public void shutDown()
		throws SailException
	{
		verifyBaseSailSet();
		baseSail.shutDown();
	}

	public boolean isWritable()
		throws SailException
	{
		verifyBaseSailSet();
		return baseSail.isWritable();
	}

	public SailConnection getConnection()
		throws SailException
	{
		verifyBaseSailSet();
		return baseSail.getConnection();
	}

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
