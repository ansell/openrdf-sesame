package org.openrdf.spin;

import java.util.List;

import org.openrdf.model.URI;

public class RuleProperty {
	private final URI uri;
	private final List<URI> nextRules;
	private final int maxIterCount;

	public RuleProperty(URI uri, List<URI> nextRules, int maxIterCount) {
		this.uri = uri;
		this.nextRules = nextRules;
		this.maxIterCount = maxIterCount;
	}
}
