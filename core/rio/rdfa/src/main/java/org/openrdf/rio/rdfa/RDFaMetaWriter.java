/*
 * Copyright James Leigh (c) 2008.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.rio.rdfa;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

/**
 * @author James Leigh
 */
public class RDFaMetaWriter {

	private static final String XHTML = "http://www.w3.org/1999/xhtml";

	private static final String NS = XHTML + "#";

	private static final String VOCAB = "http://www.w3.org/1999/xhtml/vocab#";

	private XMLWriter writer;

	private String baseURI;

	/**
	 * Creates a new RDFaWriter that will writer.write to the supplied
	 * OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to writer.write the RDFa document to.
	 */
	public RDFaMetaWriter(OutputStream out) {
		this(new OutputStreamWriter(out, Charset.forName("UTF-8")));
	}

	/**
	 * Creates a new RDFaWriter that will writer.write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to writer.write the RDFa document to.
	 */
	public RDFaMetaWriter(Writer writer) {
		this.writer = new XMLWriter(writer);
	}

	public void close()
		throws IOException
	{
		writer.close();
	}

	public void startRDF(String baseURI, Map<String, String> namespaceTable)
		throws IOException
	{
		this.baseURI = baseURI;
		writer.processing("xml", "version=\"1.0\" encoding=\"UTF-8\"");
		writer.doctype("html", "PUBLIC", "-//W3C//DTD XHTML+RDFa 1.0//EN",
				"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd");

		writer.openStartTag(XHTML, "html");

		for (Map.Entry<String, String> entry : namespaceTable.entrySet()) {
			String name = entry.getKey();
			String prefix = entry.getValue();
			if ("".equals(prefix)) {
				continue;
			}

			writer.newLine();
			writer.indent(1);
			writer.xmlns(name, prefix);
		}

		writer.attribute("version", "XHTML+RDFa 1.0");

		writer.closeStartTag();
		writer.newLine();
	}

	public void endRDF()
		throws IOException
	{
		writer.newLine();
		writer.endTag(XHTML, "body");
		writer.endTag(XHTML, "html");

		writer.flush();
	}

	/**
	 * @throws IOException
	 */
	public void startMeta()
		throws IOException
	{
		writer.openStartTag(XHTML, "head");
		writer.closeStartTag();
		writer.newLine();
		if (baseURI != null) {
			writer.indent(1);
			writer.openStartTag(XHTML, "base");
			writer.attribute(XHTML, "href", baseURI);
			writer.closeEmptyTag(XHTML, "base");
		}
	}

	public void handleMetaAttribute(URI pred, Literal objLit)
		throws IOException
	{
		String meta = tagName(pred, "meta");
		writer.indent(1);
		writer.openStartTag(XHTML, meta);
		writer.curie(XHTML, "property", VOCAB, pred);

		// language attribute
		if (objLit.getLanguage() != null) {
			writer.attribute("xml:lang", objLit.getLanguage());
		}

		// datatype attribute
		URI datatype = objLit.getDatatype();
		if (datatype != null) {
			writer.curie(XHTML, "datatype", VOCAB, datatype);
		}

		if ("meta".equals(meta) || useContentAttribute(objLit)) {
			writer.attribute(XHTML, "content", objLit.getLabel());
			writer.closeEmptyTag(XHTML, meta);
		}
		else {
			writer.closeStartTag();
			// label
			if (RDF.XMLLITERAL.equals(datatype)) {
				// Write XML literal as plain XML
				writer.xml(objLit.getLabel());
			}
			else {
				writer.data(objLit.getLabel());
			}
			writer.endTag(XHTML, meta);
		}
	}

	public void handleMetaLink(URI pred, String uri)
		throws IOException
	{
		String link = tagName(pred, "link");
		writer.indent(1);
		writer.openStartTag(XHTML, link);
		writer.curie(XHTML, "rel", VOCAB, pred);
		writer.attribute(XHTML, "href", uri);
		writer.closeEmptyTag(XHTML, link);
	}

	public void endMeta()
		throws IOException
	{
		writer.endTag(XHTML, "head");
		writer.startTag(XHTML, "body");
		writer.newLine();
	}

	public void startNode(String relativize, Set<URI> types)
		throws IOException
	{
		writer.openStartTag(XHTML, tagName(types, "div"));
		writer.attribute(XHTML, "about", relativize);
		writer.curie(XHTML, "typeof", VOCAB, types);
		writer.closeStartTag();
		writer.newLine();
	}

	public void openProperty(int indent, URI predicate)
		throws IOException
	{
		writer.indent(indent);
		writer.openStartTag(XHTML, tagName(predicate, "div"));
		writer.curie(XHTML, "rel", VOCAB, predicate);
		writer.closeStartTag();
		writer.newLine();
	}

	public void startBlankNode(int indent, BNode bnode, Set<URI> types)
		throws IOException
	{
		writer.indent(indent);
		writer.openStartTag(XHTML, tagName(types, "div"));
		writer.attribute(XHTML, "about", "[_:" + bnode.getID() + "]");
		writer.curie(XHTML, "typeof", VOCAB, types);
		writer.closeStartTag();
		writer.newLine();
	}

	public void endBlankNode(int indent, BNode bnode, Set<URI> types)
		throws IOException
	{
		writer.indent(indent);
		writer.endTag(XHTML, tagName(types, "div"));
	}

	public void closeProperty(int indent, URI predicate)
		throws IOException
	{
		writer.indent(indent);
		writer.endTag(XHTML, tagName(predicate, "div"));
	}

	public void endNode(String relativize, Set<URI> types)
		throws IOException
	{
		writer.endTag(XHTML, tagName(types, "div"));
	}

	public void handleComment(String comment)
		throws IOException
	{
		writer.comment(comment);
	}

	public void handleLiteral(int indent, URI pred, Literal objLit)
		throws IOException
	{
		String span = tagName(pred, "span");
		writer.indent(indent);
		writer.openStartTag(XHTML, span);
		writer.curie(XHTML, "property", VOCAB, pred);

		// language attribute
		if (objLit.getLanguage() != null) {
			writer.attribute("xml:lang", objLit.getLanguage());
		}

		// datatype attribute
		URI datatype = objLit.getDatatype();
		if (datatype != null) {
			writer.curie(XHTML, "datatype", VOCAB, datatype);
		}

		if (useContentAttribute(objLit)) {
			writer.attribute(XHTML, "content", objLit.getLabel());
		}

		writer.closeStartTag();

		// label
		if (RDF.XMLLITERAL.equals(datatype)) {
			// Write XML literal as plain XML
			writer.xml(objLit.getLabel());
		}
		else {
			writer.data(objLit.getLabel());
		}

		writer.endTag(XHTML, span);
	}

	public void handleBlankNode(int indent, URI pred, BNode node)
		throws IOException
	{
		String span = tagName(pred, "span");
		writer.indent(indent);
		writer.openStartTag(XHTML, span);
		writer.curie(XHTML, "rel", VOCAB, pred);
		writer.attribute(XHTML, "resource", "[_:" + node.getID() + "]");
		writer.closeStartTag();
		writer.data(node.toString());
		writer.endTag(XHTML, span);
	}

	public void handleURI(int indent, URI pred, String relativize)
		throws IOException
	{
		String a = tagName(pred, "a");
		writer.indent(indent);
		writer.openStartTag(XHTML, a);
		writer.curie(XHTML, "rel", VOCAB, pred);
		if ("a".equals(a)) {
			writer.attribute(XHTML, "href", relativize);
		}
		else {
			writer.attribute(XHTML, "src", relativize);
		}
		writer.closeStartTag();
		writer.data(relativize.toString());
		writer.endTag(XHTML, a);
	}

	private String tagName(URI pred, String defaultTag) {
		if (NS.equals(pred.getNamespace())) {
			return pred.getLocalName();
		}
		return defaultTag;
	}

	private String tagName(Set<URI> types, String defaultTag) {
		for (URI type : types) {
			if (NS.equals(type.getNamespace())) {
				return type.getLocalName();
			}
		}
		return defaultTag;
	}

	/**
	 * If this Literal should be repeated in an attribute.
	 */
	private boolean useContentAttribute(Literal literal) {
		if (RDF.XMLLITERAL.equals(literal.getDatatype())) {
			return false;
		}
		String label = literal.getLabel();
		if (label.length() == 0) {
			return false;
		}
		char first = label.charAt(0);
		char last = label.charAt(label.length() - 1);
		return Character.isWhitespace(first) || Character.isWhitespace(last);
	}
}
