/*
 * Copyright James Leigh (c) 2008.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.rio.rdfa;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.rdfxml.RDFXMLParser;

/**
 * @author James Leigh
 */
public class RDFaParser implements RDFParser {

	static String XSLT = "META-INF/transformations/RDFa2RDFXML.xsl";

	RDFXMLParser parser = new RDFXMLParser();

	volatile Exception exception;

	private Executor executor;

	private Transformer transformer;

	public RDFaParser()
		throws TransformerConfigurationException
	{
		executor = Executors.newSingleThreadExecutor();
		TransformerFactory transFact = TransformerFactory.newInstance();
		ClassLoader cl = RDFaParser.class.getClassLoader();
		transformer = transFact.newTransformer(new StreamSource(cl.getResourceAsStream(XSLT)));
	}

	RDFaParser(Executor executor, Transformer transformer) {
		this.executor = executor;
		this.transformer = transformer;
	}

	public RDFFormat getRDFFormat() {
		return RDFFormat.RDFA;
	}

	public void parse(InputStream in, final String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		parse(new StreamSource(in), baseURI);
	}

	public void parse(Reader reader, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		parse(new StreamSource(reader), baseURI);
	}

	private void parse(StreamSource source, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		CountDownLatch latch = new CountDownLatch(1);
		PipedInputStream pipe = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream(pipe);

		try {
			parse(pipe, baseURI, latch);

			transformer.transform(source, new StreamResult(out));
		}
		catch (TransformerException e) {
			handleException();
			throw new RDFParseException(e);
		}
		finally {
			out.close();
		}

		await(latch);
	}

	private void parse(final PipedInputStream pipe, final String baseURI, final CountDownLatch latch) {
		executor.execute(new Runnable() {

			public void run() {
				try {
					try {
						parser.parse(pipe, baseURI);
					}
					finally {
						pipe.close();
					}
				}
				catch (Exception e) {
					exception = e;
				}
				finally {
					latch.countDown();
				}
			}
		});
	}

	private void await(final CountDownLatch latch)
		throws RDFParseException, IOException, RDFHandlerException
	{
		try {
			latch.await();
		}
		catch (InterruptedException e) {
			handleException();
			throw new RDFParseException(e);
		}
		handleException();
	}

	private void handleException()
		throws IOException, RDFHandlerException, RDFParseException
	{
		if (exception != null) {
			try {
				throw exception;
			}
			catch (IOException e) {
				throw e;
			}
			catch (RDFHandlerException e) {
				throw e;
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RDFParseException(e);
			}
		}
	}

	public DatatypeHandling datatypeHandling() {
		return parser.datatypeHandling();
	}

	public ParseErrorListener getParseErrorListener() {
		return parser.getParseErrorListener();
	}

	public ParseLocationListener getParseLocationListener() {
		return parser.getParseLocationListener();
	}

	public boolean getParseStandAloneDocuments() {
		return parser.getParseStandAloneDocuments();
	}

	public RDFHandler getRDFHandler() {
		return parser.getRDFHandler();
	}

	public boolean preserveBNodeIDs() {
		return parser.preserveBNodeIDs();
	}

	public void setDatatypeHandling(DatatypeHandling datatypeHandling) {
		parser.setDatatypeHandling(datatypeHandling);
	}

	public void setParseErrorListener(ParseErrorListener el) {
		parser.setParseErrorListener(el);
	}

	public void setParseLocationListener(ParseLocationListener el) {
		parser.setParseLocationListener(el);
	}

	public void setParseStandAloneDocuments(boolean standAloneDocs) {
		parser.setParseStandAloneDocuments(standAloneDocs);
	}

	public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
		parser.setPreserveBNodeIDs(preserveBNodeIDs);
	}

	public void setRDFHandler(RDFHandler handler) {
		parser.setRDFHandler(handler);
	}

	public void setStopAtFirstError(boolean stopAtFirstError) {
		parser.setStopAtFirstError(stopAtFirstError);
	}

	public void setValueFactory(ValueFactory valueFactory) {
		parser.setValueFactory(valueFactory);
	}

	public void setVerifyData(boolean verifyData) {
		parser.setVerifyData(verifyData);
	}

	public boolean stopAtFirstError() {
		return parser.stopAtFirstError();
	}

	public boolean verifyData() {
		return parser.verifyData();
	}

}
