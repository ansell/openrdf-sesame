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
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.sail.rdbms.RdbmsConnectionFactory;
import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

/**
 * Overrides {@link ValueTableFactory}, {@link QueryBuilderFactory}, and
 * {@link RdbmsQueryOptimizer}. This class also indicates that PostgreSQL does
 * not require a FROM clause.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlConnectionFactory extends RdbmsConnectionFactory {

	@Override
	protected ValueTableFactory createValueTableFactory() {
		return new PgSqlValueTableFactory();
	}

	@Override
	protected TableFactory createTableFactory() {
		return new PgSqlTableFactory();
	}

	@Override
	protected QueryBuilderFactory createQueryBuilderFactory() {
		return new PgQueryBuilderFactory();
	}

	@Override
	protected RdbmsQueryOptimizer createOptimizer() {
		return new PgSqlQueryOptimizer();
	}
}
