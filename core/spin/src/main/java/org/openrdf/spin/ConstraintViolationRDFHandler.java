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

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

public class ConstraintViolationRDFHandler extends RDFHandlerBase {

	private String label;
	private String root;
	private String path;
	private String value;
	private ConstraintViolationLevel level = ConstraintViolationLevel.ERROR;
	private ConstraintViolation violation;

	public ConstraintViolation getConstraintViolation() {
		return violation;
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		URI pred = st.getPredicate();
		if (RDFS.LABEL.equals(pred)) {
			Value labelValue = st.getObject();
			label = (labelValue instanceof Literal) ? labelValue.stringValue() : null;
		}
		else if (SPIN.VIOLATION_ROOT_PROPERTY.equals(pred)) {
			Value rootValue = st.getObject();
			root = (rootValue instanceof Resource) ? rootValue.stringValue() : null;
		}
		else if (SPIN.VIOLATION_PATH_PROPERTY.equals(pred)) {
			Value pathValue = st.getObject();
			path = (pathValue != null) ? pathValue.stringValue() : null;
		}
		else if (SPIN.VIOLATION_VALUE_PROPERTY.equals(pred)) {
			Value valueValue = st.getObject();
			value = (valueValue != null) ? valueValue.stringValue() : null;
		}
		else if (SPIN.VIOLATION_LEVEL_PROPERTY.equals(pred)) {
			Value levelValue = st.getObject();
			if(levelValue instanceof URI) {
				level = ConstraintViolationLevel.valueOf((URI) levelValue);
			}
			if (level == null) {
				throw new RDFHandlerException("Invalid value " + levelValue + " for "
						+ SPIN.VIOLATION_LEVEL_PROPERTY + ": " + st.getSubject());
			}
		}
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		violation = new ConstraintViolation(label, root, path, value, level);
	}
}
