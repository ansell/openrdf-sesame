/*
 * Copyright James Leigh (c) 2008.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.rio.rdfa;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.aduna.xml.XMLUtil;

import org.openrdf.model.URI;

/**
 * @author James Leigh
 */
public class XMLWriter implements Flushable {

	/*-----------*
	 * Variables *
	 *-----------*/

	private int idx;

	private Map<String, String> namespaceTable;

	private boolean root = true;

	private Writer writer;

	private List<String> defaultNamespaces = new ArrayList<String>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFaWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the RDFa document to.
	 */
	public XMLWriter(OutputStream out) {
		this(new OutputStreamWriter(out, Charset.forName("UTF-8")));
	}

	/**
	 * Creates a new RDFaWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the RDFa document to.
	 */
	public XMLWriter(Writer writer) {
		this.writer = writer;
		namespaceTable = new LinkedHashMap<String, String>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void close()
		throws IOException
	{
		writer.close();
	}

	public void flush()
		throws IOException
	{
		writer.flush();
	}

	public void processing(String name, String instruction)
		throws IOException
	{
		writer.write("<?");
		writer.write(name);
		if (instruction != null) {
			writer.write(" ");
			writer.write(instruction);
		}
		writer.write("?>\n");
	}

	public void doctype(String html, String system, String id, String ddt)
		throws IOException
	{
		writer.write("<!DOCTYPE ");
		writer.write(html);
		writer.write(" ");
		writer.write(system);
		writer.write(" \"");
		writer.write(id);
		writer.write("\" \"");
		writer.write(ddt);
		writer.write("\">\n");
	}

	public void xmlns(String name, String prefix)
		throws IOException
	{
		if (root) {
			namespaceTable.put(name, prefix);
		}

		if (prefix.length() > 0) {
			attribute("xmlns:" + prefix, name);
		}
		else {
			attribute("xmlns", name);
		}
	}

	public void newLine()
		throws IOException
	{
		writer.write("\n");
	}

	public void indent(int size)
		throws IOException
	{

		for (int i = 0; i < size; i++) {
			writer.write("\t");
		}
	}

	public void comment(String comment)
		throws IOException
	{
		writer.write("<!-- ");
		writer.write(comment);
		writer.write(" -->");
		newLine();
	}

	public void startTag(String namespace, String localName)
		throws IOException
	{
		openStartTag(namespace, localName);
		closeStartTag();
	}

	public void endTag(String namespace, String localName)
		throws IOException
	{
		String prefix = namespaceTable.get(namespace);

		if (prefix == null) {
			writer.write("</");
			writer.write(localName);
			writer.write(">\n");
			popDefaultNamespace();
		}
		else {
			writer.write("</");
			writer.write(prefix);
			writer.write(":");
			writer.write(localName);
			writer.write(">\n");
		}
	}

	public void emptyTag(String namespace, String localName)
		throws IOException
	{
		openStartTag(namespace, localName);
		closeEmptyTag(namespace, localName);
	}

	public void openStartTag(String namespace, String localName)
		throws IOException
	{
		String prefix = namespaceTable.get(namespace);

		if (prefix == null) {
			writer.write("<");
			writer.write(localName);
			if (!namespace.equals(getDefaultNamespace())) {
				writer.write(" xmlns=\"");
				writer.write(XMLUtil.escapeDoubleQuotedAttValue(namespace));
				writer.write("\"");
			}
			pushDefaultNamespace(namespace);
		}
		else {
			writer.write("<");
			writer.write(prefix);
			writer.write(":");
			writer.write(localName);
		}
	}

	public void closeEmptyTag(String namespace, String localName)
		throws IOException
	{
		writer.write("/>\n");
		if (namespaceTable.get(namespace) == null) {
			popDefaultNamespace();
		}
	}

	public void closeStartTag()
		throws IOException
	{
		writer.write(">");
		root = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void attribute(String attName, String value)
		throws IOException
	{
		writer.write(" ");
		writer.write(attName);
		writer.write("=\"");
		writer.write(XMLUtil.escapeDoubleQuotedAttValue(value));
		writer.write("\"");
	}

	public void attribute(String namespace, String attName, String value)
		throws IOException
	{
		String prefix = namespaceTable.get(namespace);

		writer.write(" ");
		if (!namespace.equals(getDefaultNamespace())) {
			prefix = assignPrefix(namespace);
			writer.write("xmlns:");
			writer.write(prefix);
			writer.write("=\"");
			writer.write(XMLUtil.escapeDoubleQuotedAttValue(namespace));
			writer.write("\"");
			writer.write(" ");
			writer.write(prefix);
			writer.write(":");
		}
		else if (prefix != null) {
			writer.write(prefix);
			writer.write(":");
		}
		writer.write(attName);
		writer.write("=\"");
		writer.write(XMLUtil.escapeDoubleQuotedAttValue(value));
		writer.write("\"");

	}

	public void curie(String attrNS, String attr, String vocab, URI pred)
		throws IOException
	{
		String predPrefix = namespaceTable.get(pred.getNamespace());
		if (vocab.equals(pred.getNamespace())) {
			attribute(attrNS, attr, pred.getLocalName());
		}
		else if (predPrefix == null) {
			predPrefix = assignPrefix(pred.getNamespace());
			writer.write(" xmlns:");
			writer.write(predPrefix);
			writer.write("=\"");
			writer.write(XMLUtil.escapeDoubleQuotedAttValue(pred.getNamespace()));
			writer.write("\"");
			attribute(attrNS, attr, predPrefix + ":" + pred.getLocalName());
		}
		else if (predPrefix.length() == 0) {
			attribute(attrNS, attr, pred.getLocalName());
		}
		else {
			attribute(attrNS, attr, predPrefix + ":" + pred.getLocalName());
		}
	}

	public void curie(String attrNS, String attr, String vocab, Set<URI> types)
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (URI type : types) {
			String predPrefix = namespaceTable.get(type.getNamespace());
			if (vocab.equals(type.getNamespace())) {
				sb.append(" ").append(type.getLocalName());
			}
			else if (predPrefix == null) {
				predPrefix = assignPrefix(type.getNamespace());
				writer.write(" xmlns:");
				writer.write(predPrefix);
				writer.write("=\"");
				writer.write(XMLUtil.escapeDoubleQuotedAttValue(type.getNamespace()));
				writer.write("\"");
				sb.append(" ").append(predPrefix).append(":").append(type.getLocalName());
			}
			else if (predPrefix.length() == 0) {
				sb.append(" ").append(type.getLocalName());
			}
			else {
				sb.append(" ").append(predPrefix).append(":").append(type.getLocalName());
			}
		}
		if (sb.length() > 1) {
			attribute(attrNS, attr, sb.substring(1));
		}
	}

	public void data(String chars)
		throws IOException
	{
		writer.write(XMLUtil.escapeCharacterData(chars));
	}

	public void xml(String str)
		throws IOException
	{
		writer.write(str);
	}

	private String getDefaultNamespace() {
		if (defaultNamespaces.isEmpty())
			return null;
		return defaultNamespaces.get(defaultNamespaces.size() - 1);
	}

	private void pushDefaultNamespace(String namespace) {
		defaultNamespaces.add(namespace);
	}

	private void popDefaultNamespace() {
		defaultNamespaces.remove(defaultNamespaces.size() - 1);
	}

	private String assignPrefix(String predNamespace) {
		return "pred" + (++idx);
	}

}
