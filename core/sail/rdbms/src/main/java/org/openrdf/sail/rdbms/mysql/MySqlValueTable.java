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
package org.openrdf.sail.rdbms.mysql;

import java.sql.Types;

import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * 
 * @author James Leigh
 */
public class MySqlValueTable extends ValueTable {

	private static final String FEILD_COLLATE = " CHARACTER SET utf8 COLLATE utf8_bin";

	@Override
	public String sql(int type, int length) {
		String declare = super.sql(type, length);
		if (type == Types.VARCHAR) {
			return declare + FEILD_COLLATE;
		}
		else if (type == Types.LONGVARCHAR) {
			return "LONGTEXT" + FEILD_COLLATE;
		}
		else {
			return declare;
		}
	}

}
