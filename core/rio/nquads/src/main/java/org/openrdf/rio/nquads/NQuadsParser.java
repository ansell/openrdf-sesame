package org.openrdf.rio.nquads;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.ntriples.NTriplesParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * RDFParser implementation for the N-Quads RDF format.
 *
 * @author Joshua Shinavier (http://fortytwo.net). Extends the Aduna NTriplesParser
 */
public class NQuadsParser extends NTriplesParser {
    protected Resource context;

    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.NQUADS;
    }

    @Override
    public void parse(final InputStream inputStream,
                      final String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream can not be 'null'");
        }
        // Note: baseURI will be checked in parse(Reader, String)

        try {
            parse(new InputStreamReader(inputStream, "US-ASCII"), baseURI);
        } catch (UnsupportedEncodingException e) {
            // Every platform should support the US-ASCII encoding...
            throw new RuntimeException(e);
        }
    }

    @Override
    public void parse(final Reader reader,
                      final String baseURI) throws IOException, RDFParseException, RDFHandlerException {
        if (reader == null) {
            throw new IllegalArgumentException("Reader can not be 'null'");
        }
        if (baseURI == null) {
            throw new IllegalArgumentException("base URI can not be 'null'");
        }

        rdfHandler.startRDF();

        this.reader = reader;
        lineNo = 1;

        reportLocation(lineNo, 1);

        try {
            int c = reader.read();
            c = skipWhitespace(c);

            while (c != -1) {
                if (c == '#') {
                    // Comment, ignore
                    c = skipLine(c);
                } else if (c == '\r' || c == '\n') {
                    // Empty line, ignore
                    c = skipLine(c);
                } else {
                    c = parseQuad(c);
                }

                c = skipWhitespace(c);
            }
        } finally {
            clear();
        }

        rdfHandler.endRDF();
    }

    private int parseQuad(int c)
            throws IOException, RDFParseException, RDFHandlerException {
        
        boolean ignoredAnError = false;
        try
        {
            c = parseSubject(c);
    
            c = skipWhitespace(c);
    
            c = parsePredicate(c);
    
            c = skipWhitespace(c);
    
            c = parseObject(c);
    
            c = skipWhitespace(c);
    
            // Context is not required
            if (c != '.') {
                c = parseContext(c);
                c = skipWhitespace(c);
            }
            if (c == -1) {
                throwEOFException();
            } else if (c != '.') {
                reportFatalError("Expected '.', found: " + (char) c);
            }
    
            c = assertLineTerminates(c);
        } catch(RDFParseException rdfpe) {
            if(stopAtFirstError()) {
                throw rdfpe;
            } else {
                ignoredAnError = true;
            }
        }
        
        c = skipLine(c);

        if(!ignoredAnError) {
            Statement st = createStatement(subject, predicate, object, context);
            rdfHandler.handleStatement(st);
        }
        
        subject = null;
        predicate = null;
        object = null;
        context = null;

        return c;
    }

    protected int parseContext(int c)
            throws IOException, RDFParseException {
        // FIXME: context (in N-Quads) can be a literal
        StringBuilder sb = new StringBuilder(100);

        // subject is either an uriref (<foo://bar>) or a nodeID (_:node1)
        if (c == '<') {
            // subject is an uriref
            c = parseUriRef(c, sb);
            context = createURI(sb.toString());
        } else if (c == '_') {
            // subject is a bNode
            c = parseNodeID(c, sb);
            context = createBNode(sb.toString());
        } else if (c == -1) {
            throwEOFException();
        } else {
            reportFatalError("Expected '<' or '_', found: " + (char) c);
        }

        return c;
    }
}
