/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An XML parser that generates "simple" SAX-like events from a limited subset
 * of XML documents. The SimpleSAXParser can parse simple XML documents; it
 * doesn't support processing instructions or elements that contain both
 * sub-element and character data; character data is only supported in the
 * "leaves" of the XML element tree.
 *
 * <h3>Example:</h3>
 * <p>
 * Parsing the following XML:
 * <pre>
 * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
 * &lt;xml-doc&gt;
 *   &lt;foo a="1" b="2&amp;amp;3"/&gt;
 *   &lt;bar&gt;Hello World!&lt;/bar&gt;
 * &lt;/xml-doc&gt;
 *</pre>
 * <p>
 * will result in the following method calls to the
 * <tt>SimpleSAXListener</tt>:
 * <pre>
 * startDocument()
 * startTag("xml-doc", emptyMap, "")
 *
 * startTag("foo", a_b_Map, "")
 * endTag("foo")
 *
 * startTag("bar", emptyMap, "Hello World!")
 * endTag("bar")
 *
 * endTag("xml-doc")
 * endDocument()
 * </pre>
 */
public class SimpleSAXParser {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The XMLReader to use for parsing the XML.
	 */
	private XMLReader _xmlReader;

	/**
	 * The listener to report the events to.
	 */
	private SimpleSAXListener _listener;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new SimpleSAXParser that will use the supplied
	 * <tt>XMLReader</tt> for parsing the XML. One must set a
	 * <tt>SimpleSAXListener</tt> on this object before calling one of
	 * the <tt>parse()</tt> methods.
	 *
	 * @param xmlReader The XMLReader to use for parsing.
	 *
	 * @see #setListener
	 */
	public SimpleSAXParser(XMLReader xmlReader) {
		super();
		_xmlReader = xmlReader;
	}

	/**
	 * Creates a new SimpleSAXParser that will try to create a new
	 * <tt>XMLReader</tt> using <tt>org.openrdf.util.xml.XMLReaderFactory</tt>
	 * for parsing the XML. One must set a <tt>SimpleSAXListener</tt> on
	 * this object before calling one of the <tt>parse()</tt> methods.
	 *
	 * @throws SAXException If the SimpleSAXParser was unable to create an
	 * XMLReader.
	 *
	 * @see #setListener
	 * @see org.xml.sax.XMLReader
	 * @see org.openrdf.util.xml.XMLReaderFactory
	 */
	public SimpleSAXParser()
		throws SAXException
	{
		this(XMLReaderFactory.createXMLReader());
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Sets the (new) listener that should receive any events from
	 * this parser. This listener will replace any previously set
	 * listener.
	 *
	 * @param listener The (new) listener for events from this parser.
	 */
	public void setListener(SimpleSAXListener listener) {
		_listener = listener;
	}

	/**
	 * Gets the listener that currently will receive any events from
	 * this parser.
	 *
	 * @return The listener for events from this parser.
	 */
	public SimpleSAXListener getListener() {
		return _listener;
	}

	/**
	 * Parses the content of the supplied <tt>File</tt> as XML.
	 *
	 * @param file The file containing the XML to parse.
	 */
	public void parse(File file)
		throws SAXException, IOException
	{
		InputStream in = new FileInputStream(file);
		try {
			parse(in);
		}
		finally {
			try {
				in.close();
			} catch (IOException ignore) {}
		}
	}

	/**
	 * Parses the content of the supplied <tt>InputStream</tt> as XML.
	 *
	 * @param in An <tt>InputStream</tt> containing XML data.
	 */
	public void parse(InputStream in)
		throws SAXException, IOException
	{
		_parse(new InputSource(in));
	}

	/**
	 * Parses the content of the supplied <tt>Reader</tt> as XML.
	 *
	 * @param reader A <tt>Reader</tt> containing XML data.
	 */
	public void parse(Reader reader)
		throws SAXException, IOException
	{
		_parse(new InputSource(reader));
	}

	/**
	 * Parses the content of the supplied <tt>InputSource</tt> as XML.
	 *
	 * @param inputSource An <tt>InputSource</tt> containing XML data.
	 */
	private synchronized void _parse(InputSource inputSource)
		throws SAXException, IOException
	{
		_xmlReader.setContentHandler(new SimpleSAXDefaultHandler());
		_xmlReader.parse(inputSource);
	}

	/*-------------------------------------*
	 * Inner class SimpleSAXDefaultHandler *
	 *-------------------------------------*/

	class SimpleSAXDefaultHandler extends DefaultHandler {

		/*-----------*
		 * Variables *
		 *-----------*/

		/**
		 * StringBuilder used to collect text during parsing.
		 */
		private StringBuilder _charBuf = new StringBuilder(512);

		/**
		 * The tag name of a deferred start tag.
		 */
		private String _deferredStartTag = null;

		/**
		 * The attributes of a deferred start tag.
		 */
		private Map<String, String> _deferredAttributes = null;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public SimpleSAXDefaultHandler() {
			super();
		}

		/*---------*
		 * Methods *
		 *---------*/

		// overrides DefaultHandler.startDocument()
		public void startDocument()
			throws SAXException
		{
			_listener.startDocument();
		}

		// overrides DefaultHandler.endDocument()
		public void endDocument()
			throws SAXException
		{
			_listener.endDocument();
		}

		// overrides DefaultHandler.characters()
		public void characters(char[] ch, int start, int length)
			throws SAXException
		{
			_charBuf.append(ch, start, length);
		}

		// overrides DefaultHandler.startElement()
		public void startElement(
			String namespaceURI, String localName,
			String qName, Attributes attributes)
			throws SAXException
		{
			// Report any deferred start tag
			if (_deferredStartTag != null) {
				_reportDeferredStartElement();
			}

			// Make current tag new deferred start tag
			_deferredStartTag = qName;

			// Copy attributes to _deferredAttributes
			int attCount = attributes.getLength();
			if (attCount == 0) {
				_deferredAttributes = Collections.emptyMap();
			}
			else {
				_deferredAttributes = new LinkedHashMap<String, String>(attCount * 2);

				for (int i = 0; i < attCount; i++) {
					_deferredAttributes.put(
							attributes.getQName(i), attributes.getValue(i));
				}
			}

			// Clear character buffer
			_charBuf.setLength(0);
		}

		private void _reportDeferredStartElement()
			throws SAXException
		{
			_listener.startTag(_deferredStartTag, _deferredAttributes, "");
			_deferredStartTag = null;
			_deferredAttributes = null;
		}

		// overrides DefaultHandler.endElement()
		public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException
		{
			if (_deferredStartTag != null) {
				// Check if any character data has been collected in the _charBuf
				String text = _charBuf.toString().trim();

				// Report deferred start tag
				_listener.startTag(_deferredStartTag, _deferredAttributes, text);
				_deferredStartTag = null;
				_deferredAttributes = null;
			}

			// Report the end tag
			_listener.endTag(qName);

			// Clear character buffer
			_charBuf.setLength(0);
		}
	}
}
