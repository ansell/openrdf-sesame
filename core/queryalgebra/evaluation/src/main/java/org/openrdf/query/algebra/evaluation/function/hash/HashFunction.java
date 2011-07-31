/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * Abstract hash function
 * 
 * @author jeen
 */
public abstract class HashFunction implements Function {

	protected String hash(String text, String algorithm)
		throws NoSuchAlgorithmException
	{
		byte[] hash = MessageDigest.getInstance(algorithm).digest(text.getBytes());
		BigInteger bi = new BigInteger(1, hash);
		String result = bi.toString(16);
		if (result.length() % 2 != 0) {
			return "0" + result;
		}
		return result;
	}

	public abstract Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException;

}
