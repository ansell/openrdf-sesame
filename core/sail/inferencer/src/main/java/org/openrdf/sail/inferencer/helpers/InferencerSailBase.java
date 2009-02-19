/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.helpers;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailConnectionTracker;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.store.StoreException;

/**
 * This class extends {@link SailBase} with {@link NotifyingSail} support.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public abstract class InferencerSailBase extends NotifyingSailBase {

	@Override
	protected SailConnectionTracker createSailConnectionTracker() {
		return new InferencerConnectionTracker();
	}

	@Override
	protected SailConnection wrapConnection(SailConnection con)
		throws StoreException
	{
		return new PreconditionInferencerConnection((InferencerConnection)con);
	}
}
