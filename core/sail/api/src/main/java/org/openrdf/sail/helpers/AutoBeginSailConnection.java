/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

/**
 * Auto begins and commits transactions and rolls back transaction before close.
 * 
 * @author James Leigh
 */
public class AutoBeginSailConnection extends SailConnectionWrapper {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AutoBeginSailConnection(SailConnection con) {
		super(con);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void close()
		throws StoreException
	{
		if (isActive()) {
			rollback();
		}
		super.close();
	}

	@Override
	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (isActive()) {
			super.addStatement(subj, pred, obj, contexts);
		}
		else {
			try {
				begin();
				super.addStatement(subj, pred, obj, contexts);
				commit();
			}
			catch (RuntimeException e) {
				rollback();
				throw e;
			}
			catch (StoreException e) {
				rollback();
				throw e;
			}
		}
	}

	@Override
	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		if (isActive()) {
			super.removeStatements(subj, pred, obj, contexts);
		}
		else {
			try {
				begin();
				super.removeStatements(subj, pred, obj, contexts);
				commit();
			}
			catch (RuntimeException e) {
				rollback();
				throw e;
			}
			catch (StoreException e) {
				rollback();
				throw e;
			}
		}
	}

	@Override
	public final void setNamespace(String prefix, String name)
		throws StoreException
	{
		if (isActive()) {
			super.setNamespace(prefix, name);
		}
		else {
			try {
				begin();
				super.setNamespace(prefix, name);
				commit();
			}
			catch (RuntimeException e) {
				rollback();
				throw e;
			}
			catch (StoreException e) {
				rollback();
				throw e;
			}
		}
	}

	@Override
	public final void removeNamespace(String prefix)
		throws StoreException
	{
		if (isActive()) {
			super.removeNamespace(prefix);
		}
		else {
			try {
				begin();
				super.removeNamespace(prefix);
				commit();
			}
			catch (RuntimeException e) {
				rollback();
				throw e;
			}
			catch (StoreException e) {
				rollback();
				throw e;
			}
		}
	}

	@Override
	public final void clearNamespaces()
		throws StoreException
	{
		if (isActive()) {
			super.clearNamespaces();
		}
		else {
			try {
				begin();
				super.clearNamespaces();
				commit();
			}
			catch (RuntimeException e) {
				rollback();
				throw e;
			}
			catch (StoreException e) {
				rollback();
				throw e;
			}
		}
	}
}
