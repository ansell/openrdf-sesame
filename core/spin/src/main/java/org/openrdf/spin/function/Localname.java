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
package org.openrdf.spin.function;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.AFN;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.UnaryFunction;

public class Localname extends UnaryFunction {

	@Override
	public String getURI() {
		return AFN.LOCALNAME.toString();
	}

	@Override
	protected Value evaluate(ValueFactory valueFactory, Value arg)
		throws ValueExprEvaluationException
	{
		if (arg instanceof URI) {
			URI uri = (URI)arg;
			return valueFactory.createLiteral(uri.getLocalName());
		}
		else {
			throw new ValueExprEvaluationException();
		}
	}
}
