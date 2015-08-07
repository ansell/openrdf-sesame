/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import info.aduna.io.GZipUtil;
import info.aduna.io.ZipUtil;

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.ParseErrorLogger;

/**
 * Handles common I/O to retrieve and parse RDF.
 * 
 * @author James Leigh
 */
public class RDFLoader {

	private final ParserConfig config;

	private final ValueFactory vf;

	/**
	 * @param config
	 * @param vf
	 */
	public RDFLoader(ParserConfig config, ValueFactory vf) {
		this.config = config;
		this.vf = vf;
	}

	/**
	 * Parses RDF data from the specified file to the given RDFHandler.
	 * 
	 * @param file
	 *        A file containing RDF data.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against. This defaults to the value of {@link java.io.File#toURI()
	 *        file.toURI()} if the value is set to <tt>null</tt>.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @param rdfHandler
	 *        Receives RDF parser events.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the file.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws RDFHandlerException
	 *         If thrown by the RDFHandler
	 */
	public void load(File file, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (baseURI == null) {
			// default baseURI to file
			baseURI = file.toURI().toString();
		}
		if (dataFormat == null) {
			dataFormat = Rio.getParserFormatForFileName(file.getName()).orElseThrow(
					() -> new UnsupportedRDFormatException("Could not find RDF format for file: " + file.getName()));
		}

		InputStream in = new FileInputStream(file);
		try {
			load(in, baseURI, dataFormat, rdfHandler);
		}
		finally {
			in.close();
		}
	}

	/**
	 * Parses the RDF data that can be found at the specified URL to the
	 * RDFHandler.
	 * 
	 * @param url
	 *        The URL of the RDF data.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against. This defaults to the value of
	 *        {@link java.net.URL#toExternalForm() url.toExternalForm()} if the
	 *        value is set to <tt>null</tt>.
	 * @param dataFormat
	 *        The serialization format of the data. If set to <tt>null</tt>, the
	 *        format will be automatically determined by examining the content
	 *        type in the HTTP response header, and failing that, the file name
	 *        extension of the supplied URL.
	 * @param rdfHandler
	 *        Receives RDF parser events.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the URL.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format, or the RDF
	 *         format could not be automatically determined.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws RDFHandlerException
	 *         If thrown by the RDFHandler
	 */
	public void load(URL url, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}

		URLConnection con = url.openConnection();

		// Set appropriate Accept headers
		if (dataFormat != null) {
			for (String mimeType : dataFormat.getMIMETypes()) {
				con.addRequestProperty("Accept", mimeType);
			}
		}
		else {
			Set<RDFFormat> rdfFormats = RDFParserRegistry.getInstance().getKeys();
			List<String> acceptParams = RDFFormat.getAcceptParams(rdfFormats, true, null);
			for (String acceptParam : acceptParams) {
				con.addRequestProperty("Accept", acceptParam);
			}
		}

		InputStream in = con.getInputStream();

		if (dataFormat == null) {
			// Try to determine the data's MIME type
			String mimeType = con.getContentType();
			int semiColonIdx = mimeType.indexOf(';');
			if (semiColonIdx >= 0) {
				mimeType = mimeType.substring(0, semiColonIdx);
			}
			dataFormat = Rio.getParserFormatForMIMEType(mimeType).orElseGet(
					() -> Rio.getParserFormatForFileName(url.getPath()).orElseThrow(
							() -> new UnsupportedRDFormatException("Could not find RDF format for URL: "
									+ url.getPath())));

		}

		try {
			load(in, baseURI, dataFormat, rdfHandler);
		}
		finally {
			in.close();
		}
	}

	/**
	 * Parses RDF data from an InputStream to the RDFHandler.
	 * 
	 * @param in
	 *        An InputStream from which RDF data can be read.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @param rdfHandler
	 *        Receives RDF parser events.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the input stream.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws RDFHandlerException
	 *         If thrown by the RDFHandler
	 */
	public void load(InputStream in, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (!in.markSupported()) {
			in = new BufferedInputStream(in, 1024);
		}

		if (ZipUtil.isZipStream(in)) {
			loadZip(in, baseURI, dataFormat, rdfHandler);
		}
		else if (GZipUtil.isGZipStream(in)) {
			load(new GZIPInputStream(in), baseURI, dataFormat, rdfHandler);
		}
		else {
			loadInputStreamOrReader(in, baseURI, dataFormat, rdfHandler);
		}
	}

	/**
	 * Parses RDF data from a Reader to the RDFHandler. <b>Note: using a Reader
	 * to upload byte-based data means that you have to be careful not to destroy
	 * the data's character encoding by enforcing a default character encoding
	 * upon the bytes. If possible, adding such data using an InputStream is to
	 * be preferred.</b>
	 * 
	 * @param reader
	 *        A Reader from which RDF data can be read.
	 * @param baseURI
	 *        The base URI to resolve any relative URIs that are in the data
	 *        against.
	 * @param dataFormat
	 *        The serialization format of the data.
	 * @param rdfHandler
	 *        Receives RDF parser events.
	 * @throws IOException
	 *         If an I/O error occurred while reading from the reader.
	 * @throws UnsupportedRDFormatException
	 *         If no parser is available for the specified RDF format.
	 * @throws RDFParseException
	 *         If an error was found while parsing the RDF data.
	 * @throws RDFHandlerException
	 *         If thrown by the RDFHandler
	 */
	public void load(Reader reader, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler)
		throws IOException, RDFParseException, RDFHandlerException
	{
		loadInputStreamOrReader(reader, baseURI, dataFormat, rdfHandler);
	}

	private void loadZip(InputStream in, String baseURI, RDFFormat dataFormat, RDFHandler rdfHandler)
		throws IOException, RDFParseException, RDFHandlerException
	{
		ZipInputStream zipIn = new ZipInputStream(in);

		try {
			for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
				if (entry.isDirectory()) {
					continue;
				}

				RDFFormat format = Rio.getParserFormatForFileName(entry.getName()).orElse(dataFormat);

				try {
					// Prevent parser (Xerces) from closing the input stream
					FilterInputStream wrapper = new FilterInputStream(zipIn) {

						public void close() {
						}
					};
					load(wrapper, baseURI, format, rdfHandler);

				}
				catch (RDFParseException e) {
					String msg = e.getMessage() + " in " + entry.getName();
					RDFParseException pe = new RDFParseException(msg, e.getLineNumber(), e.getColumnNumber());
					pe.initCause(e);
					throw pe;
				}
				finally {
					zipIn.closeEntry();
				}
			} // end for
		}
		finally {
			zipIn.close();
		}
	}

	/**
	 * Adds the data that can be read from the supplied InputStream or Reader to
	 * this repository.
	 * 
	 * @param inputStreamOrReader
	 *        An {@link InputStream} or {@link Reader} containing RDF data that
	 *        must be added to the repository.
	 * @param baseURI
	 *        The base URI for the data.
	 * @param dataFormat
	 *        The file format of the data.
	 * @param rdfHandler
	 *        handles all data from all documents
	 * @throws IOException
	 * @throws UnsupportedRDFormatException
	 * @throws RDFParseException
	 * @throws RDFHandlerException
	 */
	private void loadInputStreamOrReader(Object inputStreamOrReader, String baseURI, RDFFormat dataFormat,
			RDFHandler rdfHandler)
		throws IOException, RDFParseException, RDFHandlerException
	{
		RDFParser rdfParser = Rio.createParser(dataFormat, vf);
		rdfParser.setParserConfig(config);
		rdfParser.setParseErrorListener(new ParseErrorLogger());

		rdfParser.setRDFHandler(rdfHandler);

		if (inputStreamOrReader instanceof InputStream) {
			rdfParser.parse((InputStream)inputStreamOrReader, baseURI);
		}
		else if (inputStreamOrReader instanceof Reader) {
			rdfParser.parse((Reader)inputStreamOrReader, baseURI);
		}
		else {
			throw new IllegalArgumentException("Must be an InputStream or a Reader, is a: "
					+ inputStreamOrReader.getClass());
		}
	}
}
