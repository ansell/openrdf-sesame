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

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.sail.SailException;

/**
 * An implementation of the StackableSail interface that wraps another Sail
 * object and forwards any relevant calls to the wrapped Sail.
 * 
 * @author Arjohn Kampman
 */
public class NotifyingSailWrapper extends SailWrapper implements NotifyingSail {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new SailWrapper. The base Sail for the created SailWrapper can
	 * be set later using {@link #setBaseSail}.
	 */
	public NotifyingSailWrapper() {
	}

	/**
	 * Creates a new SailWrapper that wraps the supplied Sail.
	 */
	public NotifyingSailWrapper(NotifyingSail baseSail) {
		setBaseSail(baseSail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setBaseSail(Sail baseSail) {
		super.setBaseSail((NotifyingSail)baseSail);
	}

	@Override
	public NotifyingSail getBaseSail() {
		return (NotifyingSail)super.getBaseSail();
	}

	@Override
	public NotifyingSailConnection getConnection()
		throws SailException
	{
		return (NotifyingSailConnection)super.getConnection();
	}

	@Override
	public void addSailChangedListener(SailChangedListener listener) {
		verifyBaseSailSet();
		getBaseSail().addSailChangedListener(listener);
	}

	@Override
	public void removeSailChangedListener(SailChangedListener listener) {
		verifyBaseSailSet();
		getBaseSail().removeSailChangedListener(listener);
	}
}
