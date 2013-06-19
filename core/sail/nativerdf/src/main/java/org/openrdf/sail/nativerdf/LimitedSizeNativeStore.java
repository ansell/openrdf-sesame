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
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeNativeStore extends NativeStore {

	/**
	 * @param dataDir
	 * @param string
	 */
	public LimitedSizeNativeStore(File dataDir, String string) {
		super(dataDir, string);
	}

	public LimitedSizeNativeStore() {
		super();
	}

	public LimitedSizeNativeStore(File dataDir) {
		super(dataDir);
	}

	@Override
	protected NotifyingSailConnection getConnectionInternal()
		throws SailException
	{
		try {
			return new LimitedSizeNativeStoreConnection(this);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}
}
