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

import java.util.Map;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.parser.ParsedUpdate;

public class ParsedUpdateTemplate extends ParsedUpdate implements ParsedTemplate {
	private final Template template;
	private final BindingSet args;

	public ParsedUpdateTemplate(Template template, BindingSet args) {
		this(template, (ParsedUpdate)template.getParsedOperation(), args);
	}

	private ParsedUpdateTemplate(Template template, ParsedUpdate update, BindingSet args) {
		super(update.getSourceString(), update.getNamespaces());
		for(UpdateExpr updateExpr : update.getUpdateExprs()) {
			addUpdateExpr(updateExpr);
		}
		for(Map.Entry<UpdateExpr,Dataset> entry : update.getDatasetMapping().entrySet()) {
			map(entry.getKey(), entry.getValue());
		}
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
