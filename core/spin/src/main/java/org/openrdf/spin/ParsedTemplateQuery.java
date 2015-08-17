package org.openrdf.spin;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;


public class ParsedTemplateQuery extends ParsedQuery {
	private final ParsedOperation template;
	private final BindingSet args;

	public ParsedTemplateQuery(URI uri, ParsedOperation template, BindingSet args) {
		super(uri.stringValue());
		this.template = template;
		this.args = args;
	}

	public BindingSet getArguments() {
		return args;
	}
}
