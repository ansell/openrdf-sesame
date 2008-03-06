/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.xml;

import java.util.Map;

import org.xml.sax.SAXException;

/**
 * An implementation of <tt>SimpleSAXListener</tt> providing dummy
 * implementations for all its methods.
 */
public class SimpleSAXAdapter implements SimpleSAXListener {

	// implements SimpleSAXListener.startDocument()
	public void startDocument()
		throws SAXException
	{
	}

	// implements SimpleSAXListener.endDocument()
	public void endDocument()
		throws SAXException
	{
	}

	// implements SimpleSAXListener.startTag()
	public void startTag(String tagName, Map<String, String> atts, String text)
		throws SAXException
	{
	}

	// implements SimpleSAXListener.endTag()
	public void endTag(String tagName)
		throws SAXException
	{
	}
}
