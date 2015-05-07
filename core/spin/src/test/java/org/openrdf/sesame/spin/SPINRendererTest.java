package org.openrdf.sesame.spin;

import static org.junit.Assert.assertTrue;
import info.aduna.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.util.Models;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

@RunWith(Parameterized.class)
public class SPINRendererTest {

	@Parameters(name="{0}, {1}")
	public static Collection<Object[]> testData() {
		int n=5;
		List<Object[]> params = new ArrayList<Object[]>(n);
		for(int i=0; i<n; i++) {
			String suffix = String.valueOf(i+1);
			String queryFile = "/renderer/sparql"+suffix+".rq";
			String rdfFile = "/renderer/spin"+suffix+".ttl";
			params.add(new Object[] {queryFile, rdfFile});
		}
		return params;
	}

	private final String queryFile;
	private final String rdfFile;
	private final SPINRenderer renderer = new SPINRenderer();

	public SPINRendererTest(String queryFile, String rdfFile) {
		this.queryFile = queryFile;
		this.rdfFile = rdfFile;
	}

	@Test
	public void testSPIN() throws IOException, OpenRDFException {
		URL queryURL = getClass().getResource(queryFile);
		InputStream queryStream = queryURL.openStream();
		String query = IOUtil.readString(new InputStreamReader(queryStream, "UTF-8"));
		queryStream.close();

		ParsedQuery pq = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, queryURL.toString());

		URL rdfURL = getClass().getResource(rdfFile);
		StatementCollector expected = new StatementCollector();
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		parser.setRDFHandler(expected);
		InputStream rdfStream = rdfURL.openStream();
		parser.parse(rdfStream, rdfURL.toString());
		rdfStream.close();

		StatementCollector actual = new StatementCollector();
		renderer.render(pq, actual);

		assertTrue("Testing "+queryFile+", expected "+rdfFile+" but was\n"+toRDF(actual.getStatements()), Models.isomorphic(actual.getStatements(), expected.getStatements()));
	}

	private String toRDF(Iterable<Statement> stmts) throws RDFHandlerException
	{
		StringBuilderWriter writer = new StringBuilderWriter();
		Rio.write(stmts, writer, RDFFormat.TURTLE);
		writer.close();
		return writer.toString();
	}
}
