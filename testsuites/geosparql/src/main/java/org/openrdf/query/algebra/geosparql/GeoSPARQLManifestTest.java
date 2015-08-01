package org.openrdf.query.algebra.geosparql;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openrdf.query.parser.sparql.manifest.SPARQLQueryTest;

public abstract class GeoSPARQLManifestTest {

	public static Test suite(SPARQLQueryTest.Factory factory)
		throws Exception
	{
		TestSuite suite = new TestSuite(factory.getClass().getName());
		URL manifestUrl = GeoSPARQLManifestTest.class.getResource("/testcases-geosparql/manifest.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(manifestUrl.openStream(), "UTF-8"));
		String line;
		while ((line = reader.readLine()) != null) {
			URL url = new URL(manifestUrl, line);
			suite.addTest(SPARQLQueryTest.suite(url.toString(), factory, false));
		}
		return suite;
	}
}
