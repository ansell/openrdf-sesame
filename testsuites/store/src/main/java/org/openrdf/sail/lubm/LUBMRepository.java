/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lubm;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;

import edu.lehigh.swat.bench.ubt.api.Query;
import edu.lehigh.swat.bench.ubt.api.QueryResult;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.result.TupleResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.sail.Sail;
import org.openrdf.store.StoreException;

public abstract class LUBMRepository implements edu.lehigh.swat.bench.ubt.api.Repository {

	private Repository repository;

	private String ontologyURL;

	public LUBMRepository() {
	}

	// implements edu.lehigh.swat.bench.ubt.api.Repository.open(...)
	public void open(String database) {
		try {
			repository = new SailRepository(createSail(database));
			repository.initialize();
		}
		catch (StoreException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates the (stack of) Sails that is subject to testing.
	 * 
	 * @param database
	 *        A identifier for the database that is to be tested, for example a
	 *        JDBC-URL or a data directory.
	 * @return An uninitialized Sail object for the specified 'database'.
	 */
	public abstract Sail createSail(String database);

	// implements edu.lehigh.swat.bench.ubt.api.Repository.close()
	public void close() {
		try {
			repository.shutDown();
		}
		catch (StoreException e) {
			throw new RuntimeException(e);
		}
	}

	// implements edu.lehigh.swat.bench.ubt.api.Repository.setOntology(...)
	public void setOntology(String ontology) {
		ontologyURL = ontology;
	}

	// implements edu.lehigh.swat.bench.ubt.api.Repository.load(...)
	public boolean load(String dataDir) {
		try {
			RepositoryConnection con = repository.getConnection();

			// load ontology first
			System.out.println("Loading ontology");
			con.add(new URL(ontologyURL), ontologyURL, RDFFormat.RDFXML);

			File dir = new File(dataDir);
			File[] fileList = dir.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					return name.endsWith(".owl");
				}
			});

			if (fileList == null) {
				System.err.println("Invalid data directory: " + dataDir);
				return false;
			}
			for (int i = 0; i < fileList.length; i++) {
				System.out.println("Loading " + fileList[i]);
				con.add(fileList[i], fileList[i].getPath(), RDFFormat.RDFXML);
			}

			// System.out.println("Commiting transaction");
			// txn.commit();

			System.out.println("Done loading");
			con.close();

			return true;
		}
		catch (RDFParseException e) {
			e.printStackTrace();
		}
		catch (StoreException e) {
			e.printStackTrace();
		}
		catch (java.io.IOException e) {
			e.printStackTrace();
		}
		catch (UnsupportedRDFormatException e) {
			e.printStackTrace();
		}

		return false;
	}

	// implements edu.lehigh.swat.bench.ubt.api.Repository.issueQuery(...)
	public QueryResult issueQuery(Query query) {

		try {
			RepositoryConnection con = repository.getConnection();
			TupleResult queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query.getString()).evaluate();
			con.close();

			return new LUBMQueryResult(queryResult);
		}
		catch (MalformedQueryException e) {
			e.printStackTrace();
		}
		catch (StoreException e) {
			e.printStackTrace();
		}
		catch (UnsupportedQueryLanguageException e) {
			e.printStackTrace();
		}

		return null;
	}

	// implements edu.lehigh.swat.bench.ubt.api.Repository.clear()
	public void clear() {
		try {
			RepositoryConnection con = repository.getConnection();
			con.clear();
			con.close();
		}
		catch (StoreException e) {
			throw new RuntimeException(e);
		}
	}

	private static class LUBMQueryResult implements QueryResult {

		private int num;

		private TupleResult queryResult;

		public LUBMQueryResult(TupleResult queryResult) {
			this.queryResult = queryResult;
		}

		public long getNum() {
			return num;
		}

		public boolean next() {
			try {
				if (queryResult.hasNext()) {
					queryResult.next();
					num++;
					return true;
				}
				else {
					queryResult.close();
					return false;
				}
			}
			catch (StoreException e) {
				e.printStackTrace();
				return false;
			}
		}

	}
}
