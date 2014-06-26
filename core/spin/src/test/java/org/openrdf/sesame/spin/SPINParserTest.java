package org.openrdf.sesame.spin;

import static org.junit.Assert.assertEquals;
import info.aduna.io.ResourceUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Map;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

public class SPINParserTest {

	@Test
	public void test() throws RDFParseException, RDFHandlerException,
			IOException, RepositoryException, MalformedQueryException,
			MalformedRuleException {
		SPINParser spinParser = new SPINParser(new StringReader(""), "",
				RDFFormat.TURTLE);
		assertEquals(0, spinParser.parseConstraint().size());
	}

	@Test
	public void testBasic() throws RDFParseException, RDFHandlerException,
			IOException, RepositoryException, MalformedQueryException,
			MalformedRuleException {
		SPINParser spinParser = new SPINParser(new InputStreamReader(
				ResourceUtil.getInputStream("constraint.ttl")), "",
				RDFFormat.TURTLE);
		assertEquals(1, spinParser.parseConstraint().size());
	}

	@Test
	public void testBasicText() throws RDFParseException, RDFHandlerException,
			IOException, RepositoryException, MalformedQueryException,
			MalformedRuleException {
		SPINParser spinParser = new SPINParser(new InputStreamReader(
				ResourceUtil.getInputStream("constraintText.ttl")), "",
				RDFFormat.TURTLE);
		assertEquals(1, spinParser.parseConstraint().size());
	}

	@Test(expected = MalformedRuleException.class)
	public void testRequireAttachedResource() throws RDFParseException,
			RepositoryException, IOException, MalformedQueryException,
			MalformedRuleException {
		new SPINParser(new InputStreamReader(
				ResourceUtil.getInputStream("unattachedConstraintText.ttl")),
				"", RDFFormat.TURTLE).parseConstraint();
	}
	
	@Test
	public void testExplictlyUnbound() throws RDFParseException,
			RepositoryException, IOException, MalformedQueryException,
			MalformedRuleException {
		SPINParser parser = new SPINParser(new InputStreamReader(
				ResourceUtil.getInputStream("unattachedUnboundConstraintText.ttl")),
				"", RDFFormat.TURTLE);
		assertEquals(1, parser.parseConstraint().size());

	}
}