/*
 * Copyright Jsames Leigh (c) 2008.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.rio.rdfa;


import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;

/**
 * An {@link RDFParserFactory} for RDFa parsers.
 * 
 * @author James Leigh
 */
public class RDFaParserFactory implements RDFParserFactory {

	private Executor executor;

	private Transformer transformer;

	public RDFaParserFactory()
		throws TransformerConfigurationException
	{
		executor = Executors.newCachedThreadPool();
		TransformerFactory transFact = TransformerFactory.newInstance();
		ClassLoader cl = RDFaParser.class.getClassLoader();
		InputStream xslt = cl.getResourceAsStream(RDFaParser.XSLT);
		transformer = transFact.newTransformer(new StreamSource(xslt));
	}

	/**
	 * Returns the RDF format for this factory.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.RDFA;
	}

	/**
	 * Returns a new instance of RDFaParser.
	 */
	public RDFParser getParser() {
		return new RDFaParser(executor, transformer);
	}
}
