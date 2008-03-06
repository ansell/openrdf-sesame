package org.openrdf.protocol.rest;

import junit.framework.TestCase;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class ProtocolUtilTest extends TestCase {

	private String testValueString;
	private Value testValue;
	
	public void setUp() {
		testValueString = "urn:x-local:graph1";
		testValue = new URIImpl(testValueString);
		
	}
	public void testEncodeParameterValue() {
		String result = ProtocolUtil.encodeParameterValue(testValue);
		assertEquals(result, "<"+testValueString+">");
	}

	public void testDecodeParameterValue() {
		ValueFactory valueFactory = new ValueFactoryImpl();
		
		Value result = ProtocolUtil.decodeParameterValue("<"+testValueString+">", valueFactory);
		assertEquals(result, testValue);
	}

}
