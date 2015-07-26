/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.spin;

import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;

public class RuleProperty {

	private final URI uri;

	private List<URI> nextRules = Collections.emptyList();

	private int maxIterCount = -1;

	private boolean thisUnbound = false;

	public RuleProperty(URI ruleUri) {
		this.uri = ruleUri;
	}

	public URI getUri() {
		return uri;
	}

	public List<URI> getNextRules() {
		return nextRules;
	}

	public void setNextRules(List<URI> nextRules) {
		this.nextRules = nextRules;
	}

	public int getMaxIterationCount() {
		return maxIterCount;
	}

	public void setMaxIterationCount(int maxIterCount) {
		this.maxIterCount = maxIterCount;
	}

	public boolean isThisUnbound() {
		return thisUnbound;
	}

	public void setThisUnbound(boolean thisUnbound) {
		this.thisUnbound = thisUnbound;
	}
}
