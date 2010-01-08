/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import com.ontotext.trree.owlim_ext.SailImpl;

import info.aduna.io.FileUtil;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.result.TupleResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.accesscontrol.vocabulary.ACL;
import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;
import org.openrdf.store.StoreException;

/**
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 */
public class AccessControlSailTest extends TestCase {

	private static final String dataFile = "/accesscontrol/trezorix-data.trig";

	private static final String policyFile = "/accesscontrol/policies-trezorix.ttl";

	// resource _should_ inherit editing permission for user 'trezorix'
	private static final String CONCEPT_URI = "http://www.rnaproject.org/data/8997fddd-9b77-40ef-856e-b83c426dafa0";

	private Repository repository;

	private File dataDir;

	@Override
	protected void setUp()
		throws Exception
	{
		dataDir = FileUtil.createTempDir("acl");
		repository = createRepository(dataDir);

		uploadAclData(repository);

		// FileOutputStream os = new
		// FileOutputStream("D:/temp/export-acl-test.trig");
		// RDFWriter writer = new TriGWriterFactory().getWriter(os);
		// repository.getConnection().export(writer);
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		try {
			repository.shutDown();
			repository = null;
		}
		finally {
			FileUtil.deleteDir(dataDir);
		}
	}

	private static Repository createRepository(File dataDir)
		throws StoreException
	{
		Sail sail = new SailImpl();
		sail = new AccessControlSail(sail);
		Repository repository = new SailRepository(sail);
		repository.setDataDir(dataDir);
		repository.initialize();
		return repository;
	}

	private static void uploadAclData(Repository repository)
		throws Exception
	{
		Session session = SessionManager.getOrCreate();

		// DEBUG
		session.setUsername("administrator");

		RepositoryConnection conn = repository.getConnection();
		try {
			conn.add(AccessControlSailTest.class.getResource(policyFile), "", RDFFormat.forFileName(policyFile),
					ACL.CONTEXT);
			conn.add(AccessControlSailTest.class.getResource(dataFile), "", RDFFormat.forFileName(dataFile));
		}
		finally {
			conn.close();
		}

		session.setUsername(null);
	}

	/*
	public void testExportRNAToolsetDemo() throws Exception 
	{
		Repository rnaRepository = manager.getRepository("rna-toolset-rdfdemo");
		
		FileOutputStream os = new FileOutputStream("D:/temp/export-rna-demo.trig");

		RDFWriter writer = new TriGWriterFactory().getWriter(os);
		rnaRepository.getConnection().export(writer);
	}
	*/

	public void testSimpleQuery()
		throws Exception
	{
		RepositoryConnection con = repository.getConnection();

		try {
			Session session = SessionManager.getOrCreate();
			session.setUsername("trezorix");

			String simpleDocumentQuery = "SELECT DISTINCT ?X WHERE {?X a <http://example.org/Document>; ?P ?Y . } ";
			TupleResult tr = con.prepareTupleQuery(QueryLanguage.SPARQL, simpleDocumentQuery).evaluate();

			try {
				List<String> headers = tr.getBindingNames();

				URI pmdoc1 = con.getValueFactory().createURI("http://example.org/pmdocument1");
				URI pmdoc2 = con.getValueFactory().createURI("http://example.org/pmdocument2");

				while (tr.hasNext()) {
					BindingSet bs = tr.next();

					for (String header : headers) {
						Value value = bs.getValue(header);

						assertFalse(pmdoc1.stringValue(), value.equals(pmdoc1));
						assertFalse(pmdoc2.stringValue(), value.equals(pmdoc2));
						// System.out.println(header + " = " + value);
					}
					// System.out.println();
				}
			}
			finally {
				tr.close();
			}
		}
		finally {
			con.close();
		}
	}

	public void testAddStatementAuthenticated()
		throws Exception
	{
		SessionManager.getOrCreate().setUsername("trezorix");
		try {
			addProtectedStatement();
		}
		finally {
			SessionManager.remove();
		}
	}

	public void testAddStatementAnonymous()
		throws Exception
	{
		try {
			addProtectedStatement();
			fail("Expected StoreException not thrown");
		}
		catch (StoreException e) {
		}
	}

	private void addProtectedStatement()
		throws StoreException
	{
		RepositoryConnection con = repository.getConnection();
		try {
			ValueFactory vf = con.getValueFactory();
			con.add(vf.createURI(CONCEPT_URI), RDFS.LABEL, vf.createLiteral("test"));
		}
		finally {
			con.close();
		}
	}

	public void testRemoveStatementAuthenticated()
		throws Exception
	{
		SessionManager.getOrCreate().setUsername("trezorix");
		try {
			removeProtectedStatement();
		}
		finally {
			SessionManager.remove();
		}
	}

	public void testRemoveStatementAnonymous()
		throws Exception
	{
		try {
			removeProtectedStatement();
			fail("Expected StoreException not thrown");
		}
		catch (StoreException e) {
		}
	}

	private void removeProtectedStatement()
		throws StoreException
	{
		RepositoryConnection con = repository.getConnection();
		try {
			ValueFactory vf = con.getValueFactory();
			con.removeMatch(vf.createURI(CONCEPT_URI), RDFS.LABEL, vf.createLiteral("test"));
		}
		finally {
			con.close();
		}
	}
}
