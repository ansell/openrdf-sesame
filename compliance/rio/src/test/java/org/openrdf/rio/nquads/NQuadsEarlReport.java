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
package org.openrdf.rio.nquads;

import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.EarlReport;

/**
 * Class for generating EARL reports for N-Quads parser.
 * 
 * @author Peter Ansell
 */
public class NQuadsEarlReport {

	public static void main(String[] args)
		throws Exception
	{
		new EarlReport().generateReport(new NQuadsParserTest().createTestSuite(), EarlReport.ANSELL,
				SimpleValueFactory.getInstance().createIRI("http://www.w3.org/TR/n-quads/"));
	}

}
