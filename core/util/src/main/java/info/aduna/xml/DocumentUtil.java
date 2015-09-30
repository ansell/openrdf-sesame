/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package info.aduna.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Utilities to make working with DOM documents easier.
 * 
 * @author Herko ter Horst
 */
public class DocumentUtil {

	/**
	 * Create a Document representing the XML file at the specified location.
	 * 
	 * @param location
	 *            the location of an XML document
	 * @return a Document representing the XML file
	 * @throws IOException
	 *             when there was a problem retrieving or parsing the document.
	 */
	public static Document getDocument(URL location) throws IOException {
		return getDocument(location, false, false, null);
	}

	/**
	 * Create a Document representing the XML file at the specified location.
	 * 
	 * @param location
	 *            the location of an XML document
	 * @param validating
	 *            whether the XML parser used in the construction of the
	 *            document should validate the XML
	 * @param namespaceAware
	 *            whether the XML parser used in the construction of the
	 *            document should be aware of namespaces
	 * @return a Document representing the XML file
	 * @throws IOException
	 *             when there was a problem retrieving or parsing the document.
	 */
	public static Document getDocument(URL location, boolean validating,
			boolean namespaceAware) throws IOException {
		return getDocument(location, validating, namespaceAware, null);
	}

	/**
	 * Create a Document representing the XML file at the specified location.
	 * 
	 * @param location
	 *            the location of an XML document
	 * @param schema
	 *            a Schama instance to validate against
	 * @return a Document representing the XML file
	 * @throws IOException
	 *             when there was a problem retrieving or parsing the document.
	 */
	public static Document getDocument(URL location, Schema schema)
			throws IOException {
		return getDocument(location, false, true, schema);
	}

	private static Document getDocument(URL location, boolean validating,
			boolean namespaceAware, Schema schema) throws IOException {
		Document result = null;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(validating);
		factory.setNamespaceAware(namespaceAware);
		factory.setSchema(schema);

		InputStream in = null;
		try {
			in = new BufferedInputStream(location.openConnection()
					.getInputStream());
			DocumentBuilder builder = factory.newDocumentBuilder();
			result = builder.parse(in);
		} catch (SAXParseException e) {
			String message = "Parsing error" + ", line " + e.getLineNumber()
					+ ", uri " + e.getSystemId() + ", " + e.getMessage();
			throw toIOE(message, e);
		} catch (SAXException e) {
			throw toIOE(e);
		} catch (ParserConfigurationException e) {
			throw toIOE(e);
		} finally {
			if (in != null) {
				in.close();
			}
		}

		return result;
	}

	private static IOException toIOE(Exception e) {
		return toIOE(e.getMessage(), e);
	}

	private static IOException toIOE(String message, Exception e) {
		IOException result = new IOException(message);
		result.initCause(e);
		return result;
	}
}
