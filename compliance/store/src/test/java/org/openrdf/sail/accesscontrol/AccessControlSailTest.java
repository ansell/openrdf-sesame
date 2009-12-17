/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import com.ontotext.trree.owlim_ext.config.OWLIMSailConfig;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.sail.config.SailRepositoryConfig;
import org.openrdf.result.TupleResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.trig.TriGWriterFactory;
import org.openrdf.sail.accesscontrol.config.AccessControlSailConfig;
import org.openrdf.sail.accesscontrol.vocabulary.ACL;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * @author Jeen Broekstra
 */
public class AccessControlSailTest extends TestCase {

	private static String dataFile = "example-data.ttl";

	private static String policyFile = "policies.ttl";

	private static String ruleFile = "rules.ttl";
	
	private static String resourcePath = "accesscontrol/";

	private ClassLoader cl = AccessControlSailTest.class.getClassLoader();

	private static RepositoryManager manager;

	private static String repositoryId = "test-acl";

	private Repository rep;

	static {
		File tmpDir = new File("D:/temp/acl-test/");
		System.out.println("Using repository dir: " + tmpDir.getAbsolutePath());
		manager = new LocalRepositoryManager(tmpDir);
		try {
			manager.initialize();
		}
		catch (StoreConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createNewTestingRepository()
		throws StoreException, StoreConfigException
	{

		SailImplConfig sailImplConfig = new OWLIMSailConfig();
		sailImplConfig = new AccessControlSailConfig(sailImplConfig);
		RepositoryConfig repConfig = new RepositoryConfig(new SailRepositoryConfig(sailImplConfig));
		manager.addRepositoryConfig(repositoryId, repConfig.export());

		Repository rep = manager.getRepository(repositoryId);

		Session session = SessionManager.get();
		
		// DEBUG 
		session.setCurrentUser(ACL.ADMIN);

		RepositoryConnection conn = rep.getConnection();
		try {
			conn.add(cl.getResource(resourcePath + policyFile), "", RDFFormat.TURTLE, ACL.CONTEXT);
			conn.add(cl.getResource(resourcePath + dataFile), "", RDFFormat.TURTLE);
		}
		catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (StoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn.close();
		
		session.setCurrentUser(null);
	}

	protected void setUp()
		throws Exception
	{
		createNewTestingRepository();
		rep = manager.getRepository(repositoryId);
		
		FileOutputStream os = new FileOutputStream("D:/temp/export-acl-test.trig");

		RDFWriter writer = new TriGWriterFactory().getWriter(os);
		rep.getConnection().export(writer);
	}

	public void testSimpleQuery()
		throws Exception
	{

		RepositoryConnection conn = rep.getConnection();

		try {
			Session session = SessionManager.get();
			session.setCurrentUser(conn.getValueFactory().createURI("http://example.org/bob"));

			String simpleDocumentQuery = "SELECT DISTINCT ?X WHERE {?X a <http://example.org/Document>; ?P ?Y . } ";
			TupleResult tr = conn.prepareTupleQuery(QueryLanguage.SPARQL, simpleDocumentQuery).evaluate();

			List<String> headers = tr.getBindingNames();

			URI pmdoc1 = conn.getValueFactory().createURI("http://example.org/pmdocument1");
			URI pmdoc2 = conn.getValueFactory().createURI("http://example.org/pmdocument2");

			while (tr.hasNext()) {
				BindingSet bs = tr.next();

				for (String header : headers) {
					Value value = bs.getValue(header);
					
					assertFalse(pmdoc1.stringValue(), value.equals(pmdoc1));
					assertFalse(pmdoc2.stringValue(), value.equals(pmdoc2));
					System.out.println(header + " = " + value);
				}
				System.out.println();
			}
		}
		finally {
			conn.close();
		}
	}

	public void testAddStatement()
		throws Exception
	{
		RepositoryConnection conn = rep.getConnection();
		try {
			Session session = SessionManager.get();
			session.setCurrentUser(conn.getValueFactory().createURI("http://example.org/bob"));
			
			ValueFactory valueFactory = conn.getValueFactory();
			
			URI pmdoc1 = conn.getValueFactory().createURI("http://example.org/pmdocument1");
			URI prdoc1 = conn.getValueFactory().createURI("http://example.org/prdocument1");
			URI subsubdoc1 = conn.getValueFactory().createURI("http://example.org/subsubdoc1");
			
			Statement legalStatement = valueFactory.createStatement(prdoc1, RDFS.LABEL, valueFactory.createLiteral("legal new statement"));
			
			conn.add(legalStatement);

			Statement legalSubStatement = valueFactory.createStatement(subsubdoc1,RDFS.LABEL, valueFactory.createLiteral("legal sub stateement"));
			
			conn.add(legalSubStatement);
			
			Statement illegalStatement = valueFactory.createStatement(pmdoc1, RDFS.LABEL, valueFactory.createLiteral("illegal new statement"));
			try {
				conn.add(illegalStatement);
				
				fail("should have thrown exception");
			}
			catch (StoreException e) {
				// expected
				System.out.println(" expected store exception thrown: " + e.getMessage());
			}
			
		}
		finally {
			conn.close();
		}
	}

}
