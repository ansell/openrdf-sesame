/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

public class TupleResultBuilder {
	private PrintWriter out;
	private List<String> variables = new ArrayList<String>();
	private boolean started;
	private boolean headClosed;
	private Map<String, String> prefixes = new HashMap<String, String>();

	public TupleResultBuilder(PrintWriter out) {
		this.out = out;
	}

	public void prefix(String prefix, String namespace) {
		prefixes.put(namespace, prefix);
	}

	public TupleResultBuilder transform(String path, String xsl) {
		out.println("<?xml version='1.0' encoding='utf-8'?>");
		out.print("<?xml-stylesheet type='text/xsl' href='");
		out.print(path);
		out.print("/");
		out.print(xsl);
		out.println("'?>");
		started = true;
		return this;
	}

	public TupleResultBuilder start(String... variables) {
		if (!started) {
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
		}
		out.print("<sparql xmlns='http://www.w3.org/2005/sparql-results#'");
		out.println(" xmlns:q='http://www.openrdf.org/schema/qname#'>");
		out.println("  <head>");
		variables(variables);
		return this;
	}

	public TupleResultBuilder variables(String... names) {
		variables.addAll(Arrays.asList(names));
		for (String variable : names) {
			out.print("    <variable name='");
			out.print(variable);
			out.println("'/>");
		}
		return this;
	}

	public TupleResultBuilder link(String url) {
		out.print("    <link href='");
		out.print(url);
		out.println("'/>");
		return this;
	}

	public void bool(boolean result) {
		closeHeadBoolean();
		out.print("  <boolean>");
		out.print(result);
		out.println("</boolean>");
	}

	public TupleResultBuilder binding(String name, Object result) {
		closeHead();
		out.println("    <result>");
		_result(name, result);
		out.println("    </result>");
		return this;
	}

	public TupleResultBuilder result(Object... result) {
		closeHead();
		out.println("    <result>");
		for (int i = 0; i < result.length; i++) {
			if (result[i] == null)
				continue;
			_result(variables.get(i), result[i]);
		}
		out.println("    </result>");
		return this;
	}

	private void _result(String name, Object result) {
		out.print("      <binding name='");
		out.print(name);
		out.println("'>");
		if (result instanceof Boolean) {
			out.print("        <literal datatype='");
			out.print(XMLSchema.BOOLEAN);
			out.print("'>");
			out.print(result);
			out.println("</literal>");
		} else if (isQName(result)) {
			URI uri = (URI) result;
			out.print("        <uri q:qname='");
			out.print(enc(prefixes.get(uri.getNamespace())));
			out.print(":");
			out.print(enc(uri.getLocalName()));
			out.print("'>");
			out.print(enc(uri.stringValue()));
			out.println("</uri>");
		} else if (result instanceof URI) {
			URI uri = (URI) result;
			out.print("        <uri>");
			out.print(enc(uri.stringValue()));
			out.println("</uri>");
		} else if (result instanceof BNode) {
			BNode bnode = (BNode) result;
			out.print("        <bnode>");
			out.print(enc(bnode.stringValue()));
			out.println("</bnode>");
		} else if (result instanceof Literal) {
			Literal lit = (Literal) result;
			out.print("        <literal");
			URI uri = lit.getDatatype();
			if (isQName(uri)) {
				out.print(" q:qname='");
				out.print(enc(prefixes.get(uri.getNamespace())));
				out.print(":");
				out.print(enc(uri.getLocalName()));
				out.print("'");
			}
			if (uri != null) {
				out.print(" datatype='");
				out.print(enc(uri.stringValue()));
				out.print("'");
			}
			if (lit.getLanguage() != null) {
				out.print(" xml:lang='");
				out.print(enc(lit.getLanguage()));
				out.print("'");
			}
			out.print(">");
			out.print(enc(lit.stringValue()));
			out.println("</literal>");
		} else {
			out.print("        <literal>");
			out.print(enc(result.toString()));
			out.println("</literal>");
		}
		out.println("      </binding>");
	}

	private String enc(String stringValue) {
		String str = stringValue.replace("&", "&amp;");
		str = str.replace("<", "&lt;");
		str = str.replace(">", "&gt;");
		str = str.replace("\"", "&quot;");
		str = str.replace("'", "&apos;");
		return str;
	}

	private boolean isQName(Object result) {
		if (result instanceof URI) {
			URI uri = (URI) result;
			return prefixes.containsKey(uri.getNamespace());
		}
		return false;
	}

	public TupleResultBuilder end() {
		closeHead();
		out.println("  </results>");
		out.println("</sparql>");
		return this;
	}

	public TupleResultBuilder endBoolean() {
		closeHeadBoolean();
		out.println("</sparql>");
		return this;
	}

	private void closeHead() {
		if (!headClosed) {
			headClosed = true;
			out.println("  </head>");
			out.println("  <results ordered='false' distinct='false'>");
		}
	}

	private void closeHeadBoolean() {
		if (!headClosed) {
			headClosed = true;
			out.println("  </head>");
		}
	}

	public void flush() {
		out.flush();
	}

}
