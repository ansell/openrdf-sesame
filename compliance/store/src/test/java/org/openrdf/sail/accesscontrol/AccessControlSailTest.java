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

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
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

	private static final String dataFile = "/accesscontrol/t.trig";

	private static final String policyFile = "/accesscontrol/p.ttl";

	private static final String RNA_NS = "http://www.rnaproject.org/data/";

	private static final String SKOS_NS = "http://www.w3.org/2008/05/skos#";

	/** NSR is a top-level concept on which user 'trezorix' has viewing rights. */
	private static final String RNA_NSR = RNA_NS + "cf72e334-b04d-4914-acd6-1d37c42fe3aa";

	/**
	 * Animalia is a concept which inherits viewing rights from its broader
	 * concept, NSR.
	 */
	private static final String RNA_ANIMALIA = RNA_NS + "8997fddd-9b77-40ef-856e-b83c426dafa0";

	/**
	 * Naturalis is a concept on which 'trezorix' has no viewing rights: it has
	 * inherited access attributes that give some permissions, but trezorix is
	 * not in a role to which those permissions are assigned.
	 */
	private static final String RNA_NATURALIS = RNA_NS + "b132960c-8500-4a88-9358-c8507ac626d5";

	/** Recycle bin is a concept on which 'trezorix' has editing rights */
	private static final String RNA_RECYCLE = RNA_NS + "5ebcb863-69ff-4be0-b05f-eb9645d4fdf2";

	/** Chordata is a concept on which Trezorix has recently reported a bug */
	private static final String RNA_CHORDATA = RNA_NS + "3349da6b-4ee6-4a0b-a4c4-970c3a585d19";

	private Repository repository;

	private static Sail sail;

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
		sail = new SailImpl();
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
	public void testContentsOfAclContext() 
	throws Exception
	{
		Session session = SessionManager.getOrCreate();

		// DEBUG
		session.setUsername("trezorix");

		SailConnection con = sail.getConnection();

		try {
			Cursor<? extends Statement> statements = con.getStatements(null, null, null, true, ACL.CONTEXT);
			
			Statement st;
			while ((st = statements.next()) != null) {
				System.out.println("* " + st.getSubject() + " " + st.getPredicate() + " " + st.getObject());
			}
		}
		finally {
			con.close();
		}

		session.setUsername(null);
	}
	*/
	/*
	public void testExportRNAToolsetDemo() throws Exception 
	{
		Repository rnaRepository = manager.getRepository("rna-toolset-rdfdemo");
		
		FileOutputStream os = new FileOutputStream("D:/temp/export-rna-demo.trig");

		RDFWriter writer = new TriGWriterFactory().getWriter(os);
		rnaRepository.getConnection().export(writer);
	}
	*/

	public void testInheritanceProperty()
		throws Exception
	{
		RepositoryConnection conn = repository.getConnection();
		try {
			String chordataQuery = "SELECT DISTINCT * WHERE {<" + RNA_CHORDATA + "> ?P ?Y . } ";

			// first test: evaluate the query anonymously
			TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, chordataQuery);

			TupleResult tr = query.evaluate();
			try {
				assertFalse("query result should be empty: item is protected and current user has no viewing permission", tr.hasNext());
			}
			finally {
				tr.close();
			}

			// second test: evaluate the query as an unauthorized user
			Session session = SessionManager.getOrCreate();
			session.setUsername("no_access");
			tr = query.evaluate();
			try {
				assertFalse("query result should be empty: item is protected and current user has no viewing permission", tr.hasNext());
			}
			finally {
				tr.close();
			}
			
			// third test: evaluate the query as an authorized user
			session.setUsername("trezorix");
			tr = query.evaluate();
			try {
				assertTrue("query result should not be empty: current user has viewing permission by inheritance", tr.hasNext());
			}
			finally {
				tr.close();
			}

			// fourth test: evaluate the query after user has logged out
			SessionManager.remove();
			tr = query.evaluate();
			try {
				assertFalse("query result should be empty: item is protected and current user has no viewing permission", tr.hasNext());
			}
			finally {
				tr.close();
			}
		}
		finally {
			conn.close();
		}
	}
	
	public void testQuery1()
		throws Exception
	{
		RepositoryConnection con = repository.getConnection();

		try {
			Session session = SessionManager.getOrCreate();
			session.setUsername("trezorix");

			String conceptQuery = "SELECT DISTINCT ?X WHERE {?X a <" + SKOS_NS + "Concept" + "> ; ?P ?Y . } ";
			TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, conceptQuery);

			TupleResult tr = query.evaluate();
			System.out.println(query.toString());

			try {
				List<String> headers = tr.getBindingNames();

				boolean nsrRetrieved = false;
				boolean animaliaRetrieved = false;
				while (tr.hasNext()) {
					BindingSet bs = tr.next();

					for (String header : headers) {
						Value value = bs.getValue(header);

						if (!nsrRetrieved && RNA_NSR.equals(value.stringValue())) {
							nsrRetrieved = true;
						}

						if (!animaliaRetrieved && RNA_ANIMALIA.equals(value.stringValue())) {
							animaliaRetrieved = true;
						}

						assertFalse(RNA_NATURALIS + " should not be retrieved",
								RNA_NATURALIS.equals(value.stringValue()));
					}
				}

				assertTrue(RNA_NSR + " was not retrieved", nsrRetrieved);
				assertTrue(RNA_ANIMALIA + " was not retrieved", animaliaRetrieved);

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
			con.add(vf.createURI(RNA_RECYCLE), RDFS.LABEL, vf.createLiteral("test"));
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
			con.removeMatch(vf.createURI(RNA_RECYCLE), RDFS.LABEL, vf.createLiteral("test"));
		}
		finally {
			con.close();
		}
	}
}
