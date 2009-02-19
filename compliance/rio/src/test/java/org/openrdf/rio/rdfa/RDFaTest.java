/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfa;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import junit.framework.TestCase;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RepositoryUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author James Leigh
 */
public class RDFaTest extends TestCase {

	private RDFWriterFactory rdfWriterFactory = new RDFaWriterFactory();

	public void test()
		throws Exception
	{
		Repository rep1 = new SailRepository(new MemoryStore());
		rep1.initialize();

		RepositoryConnection con1 = rep1.getConnection();

		URL rdf = this.getClass().getResource("/testcases/rdfa-test.rdf");

		con1.add(rdf, rdf.toExternalForm(), RDFFormat.forFileName(rdf.toExternalForm()));

		StringWriter writer = new StringWriter();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(writer);
		rdfWriter.setBaseURI(rdf.toExternalForm());
		con1.export(rdfWriter);

		long before = con1.size();
		con1.close();

		Repository rep2 = new SailRepository(new MemoryStore());
		rep2.initialize();

		RepositoryConnection con2 = rep2.getConnection();

		con2.add(new StringReader(writer.toString()), rdf.toExternalForm(), rdfWriterFactory.getRDFFormat());
		long after = con2.size();
		con2.close();

		assertEquals("result of serialization and re-upload should be equal to original", before, after);

		assertTrue("result of serialization and re-upload should be equal to original", RepositoryUtil.equals(
				rep1, rep2));
	}
}
