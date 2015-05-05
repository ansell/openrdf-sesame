package org.openrdf.sesame.spin;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import info.aduna.io.ResourceUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collection;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.google.common.collect.Multimap;

public class SPINParserTest {

	private static final Resource PROTEIN = ValueFactoryImpl.getInstance()
			.createURI("http://purl.uniprot.org/core/Protein");

	@Test
	public void test() throws RDFParseException, RDFHandlerException,
			IOException, RepositoryException, MalformedQueryException,
			MalformedRuleException {
		SPINParser spinParser = new SPINParser(new StringReader(""), "",
				RDFFormat.TURTLE);
		assertThat(spinParser.parseConstraint().size(), is(equalTo(0)));
	}

	@Test
	public void testBasic() throws RDFParseException, RDFHandlerException,
			IOException, RepositoryException, MalformedQueryException,
			MalformedRuleException {
		assertOneConstraint(createParser("constraint.ttl").parseConstraint(),
				PROTEIN);
	}

	@Test
	public void testBasicText() throws RDFParseException, RDFHandlerException,
			IOException, RepositoryException, MalformedQueryException,
			MalformedRuleException {
		assertOneConstraint(createParser("constraintText.ttl")
				.parseConstraint(), PROTEIN);
	}

	@Test(expected = MalformedRuleException.class)
	public void testRequireAttachedResource() throws RDFParseException,
			RepositoryException, IOException, MalformedQueryException,
			MalformedRuleException {
		createParser("unattachedConstraintText.ttl").parseConstraint();
	}

	@Test
	public void testExplictlyUnbound() throws RDFParseException,
			RepositoryException, IOException, MalformedQueryException,
			MalformedRuleException {
		assertOneConstraint(createParser("unattachedUnboundConstraintText.ttl")
				.parseConstraint(), null);
	}

	private static void assertOneConstraint(
			Multimap<Resource, ParsedQuery> parse, Resource resource) {
		assertThat(parse.size(), is(1));
		assertThat(parse.containsKey(resource), is(true));
		Collection<ParsedQuery> queries = parse.get(resource);
		assertThat(queries.size(), is(1));
		assertThat(queries.iterator().next(),
				is(instanceOf(ParsedGraphQuery.class)));
	}

	private static SPINParser createParser(String filename)
			throws RDFParseException, RepositoryException, IOException {
		return new SPINParser(new InputStreamReader(
				ResourceUtil.getInputStream(filename)), "", RDFFormat.TURTLE);
	}
}