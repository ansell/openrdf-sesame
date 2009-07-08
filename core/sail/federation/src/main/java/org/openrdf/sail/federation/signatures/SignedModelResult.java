/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.result.ModelResult;
import org.openrdf.result.impl.ModelResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class SignedModelResult extends ModelResultImpl {

	private final BNodeSigner signer;

	private final ModelResult result;

	public SignedModelResult(ModelResult result, BNodeSigner signer) {
		super(result);
		this.result = result;
		this.signer = signer;
	}

	@Override
	public Map<String, String> getNamespaces()
		throws StoreException
	{
		return result.getNamespaces();
	}

	@Override
	public Statement next()
		throws StoreException
	{
		return signer.sign(super.next());
	}
}
