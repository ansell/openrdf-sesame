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

import org.openrdf.query.BindingSet;
import org.openrdf.query.parser.ParsedGraphQuery;

public class ParsedGraphTemplate extends ParsedGraphQuery implements ParsedTemplate {
	private final Template template;
	private final BindingSet args;

	public ParsedGraphTemplate(Template template, BindingSet args) {
		this(template, (ParsedGraphQuery)template.getParsedOperation(), args);
	}

	private ParsedGraphTemplate(Template template, ParsedGraphQuery query, BindingSet args) {
		super(query.getSourceString(), query.getTupleExpr(), query.getQueryNamespaces());
		setDataset(query.getDataset());
		this.template = template;
		this.args = args;
	}

	@Override
	public Template getTemplate() {
		return template;
	}

	@Override
	public BindingSet getBindings() {
		return args;
	}
}
