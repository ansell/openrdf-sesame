package org.openrdf.spin;

import org.openrdf.model.URI;
import org.openrdf.query.parser.ParsedOperation;


public class Template {

	private final URI uri;

	private ParsedOperation parsedOp;

	public Template(URI uri) {
		this.uri = uri;
	}

	public void setParsedOperation(ParsedOperation op) {
		this.parsedOp = op;
	}

	public ParsedOperation getParsedOperation() {
		return parsedOp;
	}
}
