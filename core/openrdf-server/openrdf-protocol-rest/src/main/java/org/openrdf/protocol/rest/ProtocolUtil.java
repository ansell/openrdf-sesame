package org.openrdf.protocol.rest;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ntriples.NTriplesUtil;

public abstract class ProtocolUtil {

	/**
	 * Encode a value for use a parameter value suitable for this protocol.
	 * 
	 * @param value
	 *            the value to encode
	 * @return a String suitable as parameter value for this procotol, or null
	 *         if the input was null
	 */
	public static final String encodeParameterValue(Value value) {
		String result = null;
		if (value != null) {
			result = NTriplesUtil.toNTriplesString(value);
		}
		return result;
	}

	/**
	 * Decode a parameter value encoded according to protocol.
	 * 
	 * @param value
	 *            the encoded value
	 * @param valueFactory
	 *            the value factory to use to create the result
	 * @return a openrdf.model.Value object representing the input value, or
	 *         null if the input was null
	 * @throws IllegalArgumentException
	 *             if the input did not contain a suitably encoded value
	 */
	public static final Value decodeParameterValue(String value, ValueFactory valueFactory) throws IllegalArgumentException {
		Value result = null;
		if (value != null) {
			result = NTriplesUtil.parseValue(value, valueFactory);
		}
		return result;
	}
}
