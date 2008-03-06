/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.xml;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.openrdf.util.log.ThreadLog;

/**
 * Factory class for creating an XMLReader. This factory tries to use the
 * system property 'org.xml.sax.driver', if that fails it falls back on
 * javax.xml.sax.parsers.SAXParserFactory. If the SAXParserFactory class
 * can not be found (this can happen when using a Java 1.3 or older), or
 * the initialization using SAXParserFactory fails otherwise, the factory
 * falls back on the Xerces 2 SAX Parser.
 */
public class XMLReaderFactory {

	public static final String XERCES_SAXPARSER = "org.apache.xerces.parsers.SAXParser";

	/**
	 * creates an org.xml.sax.XMLReader object. The method first tries to
	 * create the XMLReader object specified in the 'org.xml.sax.driver'
	 * system property. If that fails, it tries to use
	 * java.xml.parsers.SAXParserFactory. If that also fails, it tries to
	 * initialize the Xerces 2 SAX parser. If that also fails, a
	 * SAXException is thrown.
	 *
	 * @return an XMLReader
	 * @throws SAXException when no default XMLReader class can be found or
	 * instantiated.
	 */
	public static XMLReader createXMLReader() 
		throws SAXException 
	{
		XMLReader reader = null;
		
		// first, try and initialize based on the system property.
		String xmlReaderName = System.getProperty("org.xml.sax.driver");
		if (xmlReaderName != null) {
			try {
				reader = _createXMLReader(xmlReaderName);
			}
			catch (ClassNotFoundException e) {
				ThreadLog.warning("Class " + xmlReaderName + " not found");
			}
			catch (ClassCastException e) {
				ThreadLog.warning(xmlReaderName + " is not a valid XMLReader.");
			}
			catch (Exception e) {
				ThreadLog.warning("could not create instance of " + xmlReaderName);
			}
			ThreadLog.trace("XMLReader initialized using system property: " + xmlReaderName);
		}

		// next, try using javax.xml.parsers.SAXParserFactory
		if (reader == null) {
			try {
				javax.xml.parsers.SAXParserFactory factory = 
					javax.xml.parsers.SAXParserFactory.newInstance();
				factory.setNamespaceAware(true);

				reader = factory.newSAXParser().getXMLReader();
			}
			catch (NoClassDefFoundError e) {
				ThreadLog.warning("javax.xml.parsers.SAXParserFactory not available");
			}
			catch (Exception e) {
				ThreadLog.warning("Failed to initialize XMLReader through JAXP");
			}
			ThreadLog.trace("XMLReader initialized using JAXP: " + reader);
		}

		// Last resort: try using the Xerces 2 SAX Parser
		if (reader == null) {
			try {
				reader = _createXMLReader(XERCES_SAXPARSER);
			}
			catch (ClassNotFoundException e) {
				String message = "Class " + XERCES_SAXPARSER + " not found";
				ThreadLog.error(message);
				throw new SAXException(message);
			}
			catch (ClassCastException e) {
				String message = XERCES_SAXPARSER + " is not a valid XMLReader.";
				ThreadLog.error(message);
				throw new SAXException(message);
			}
			catch (Exception e) {
				String message = "Could not create instance of " + XERCES_SAXPARSER;
				ThreadLog.error(message);
				throw new SAXException(message);
			}
			ThreadLog.trace("XMLReader initialized using default Xerces SAX parser " + XERCES_SAXPARSER);
		}
		return reader;
	}

	/**
	 * Creates an org.xml.sax.XMLReader object using the supplied name.	 
	 * 
	 * @return an XMLReader
	 * @throws SAXException when the supplied XMLReader class name can not be
	 * found or instantiated.
	 */
	public static XMLReader createXMLReader(String name) 
		throws SAXException
	{
		XMLReader reader = null;
		try {
			reader = _createXMLReader(name);
		}
		catch (ClassNotFoundException e) {
			ThreadLog.error("Class " + name + " not found");
			throw new SAXException(e);
		}
		catch (ClassCastException e) {
			ThreadLog.error(name + " is not a valid XMLReader.");
			throw new SAXException(e);
		}
		catch (Exception e) {
			ThreadLog.error("Could not create instance of " + name);
			throw new SAXException(e);
		}
		return reader;
	}

	protected static XMLReader _createXMLReader(String name) 
		throws ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException
	{
		return (XMLReader)Class.forName(name).newInstance();
	}
}
