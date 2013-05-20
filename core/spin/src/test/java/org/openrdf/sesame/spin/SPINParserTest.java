package org.openrdf.sesame.spin;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;
import org.openrdf.query.Query;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;

public class SPINParserTest {
	private String basicTest = "@PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
			+ "@prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> .\n"
			+ "@prefix skos:<http://www.w3.org/2004/02/skos/core#> .\n"
			+ "@prefix owl:<http://www.w3.org/2002/07/owl#> .\n"
			+ "@prefix xsd:<http://www.w3.org/2001/XMLSchema#> .\n"
			+ "@prefix sp:<http:spinrdf.org/sp#> .\n"
			+ "@prefix spin:<http:spinrdf.org/spin#> .\n"
			+ "<http://purl.uniprot.org/core/Protein>\n"
			+ "      a       rdfs:Class ;\n"
			+ "      spin:constraint\n"
			+ "              [ a       sp:Construct ;"
			+ "                sp:templates ([ sp:object spin:ConstraintViolation ;"
			+ "                            sp:predicate rdf:type ;\n"
			+ "                            sp:subject _:b1\n"
			+ "                          ] [ sp:object\n"
			+ "                                    [ sp:varName \"link\"^^xsd:string\n"
			+ "                                    ] ;\n"
			+ "                            sp:predicate spin:violationRoot ;\n"
			+ "                            sp:subject _:b1\n"
			+ "                          ] [ sp:object rdfs:seeAlso ;\n"
			+ "                            sp:predicate spin:violationPath ;\n"
			+ "                            sp:subject _:b1\n"
			+ "                          ] [ sp:object \"URC17: WormBase links may only be found in Caenorhabditis briggsae or Caenorhabditis elegans entries.\" ;\n"
			+ "                            sp:predicate rdfs:label ;\n"
			+ "                            sp:subject _:b1\n"
			+ "                          ]) ;\n"
			+ "                sp:where ([ sp:object <http://purl.uniprot.org/core/Protein> ;\n"
			+ "                            sp:predicate rdf:type ;\n"
			+ "                            sp:subject spin:_this\n"
			+ "                          ] [ a       sp:NotExists ;\n"
			+ "                            sp:elements ([ sp:object <http://purl.uniprot.org/taxonomy/6238> ;\n"
			+ "                                        sp:predicate <http://purl.uniprot.org/core/organism> ;\n"
			+ "                                        sp:subject spin:_this\n"
			+ "                                      ])\n"
			+ "                          ] [ a       sp:NotExists ;\n"
			+ "                            sp:elements ([ sp:object <http://purl.uniprot.org/taxonomy/6239> ;\n"
			+ "                                        sp:predicate <http://purl.uniprot.org/core/organism> ;\n"
			+ "                                        sp:subject spin:_this\n"
			+ "                                      ])\n"
			+ "                          ] [ sp:object\n"
			+ "                                    [ sp:varName \"link\"^^xsd:string\n"
			+ "                                    ] ;\n"
			+ "                            sp:predicate rdfs:seeAlso ;\n"
			+ "                            sp:subject spin:_this\n"
			+ "                          ] [ sp:object <http://purl.uniprot.org/database/WormBase> ;\n"
			+ "                            sp:predicate <http://purl.uniprot.org/core/database> ;\n"
			+ "                            sp:subject\n\n"
			+ "                                    [ sp:varName \"link\"^^xsd:string\n\n"
			+ "                                    ]\n"
			+ "                          ])\n" + "              ] .";

	@Test
	public void test() throws RDFParseException, RDFHandlerException,
			IOException, RepositoryException {
		SPINParser spinParser = new SPINParser(new StringReader(""), "",
				RDFFormat.TURTLE);
		List<ParsedQuery> parsed = spinParser.parse();
		assertEquals(0, parsed.size());
	}

	@Test
	public void testBasic() throws RDFParseException, RDFHandlerException,
			IOException, RepositoryException {
		SPINParser spinParser = new SPINParser(new StringReader(basicTest), "",
				RDFFormat.TURTLE);
		List<ParsedQuery> parsed = spinParser.parse();
		assertEquals(1, parsed.size());
	}
}
