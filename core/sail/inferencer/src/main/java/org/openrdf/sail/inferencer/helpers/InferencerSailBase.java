/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.helpers;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.helpers.SailConnectionTracker;

/**
 * This class extends {@link SailBase} with {@link NotifyingSail} support.
 * 
 * @author Herko ter Horst
 * @author jeen
 * @author Arjohn Kampman
 * @author James Leigh
 */
public abstract class InferencerSailBase extends NotifyingSailBase {

	/*---------*
	 * Methods *
	 *---------*/

	protected SailConnectionTracker createSailConnectionTracker() {
		return new InferencerConnectionTracker();
	}
}
