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
package org.openrdf.sail.rdbms.optimizers;

import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.algebra.factories.BNodeExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.BooleanExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.DatatypeExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.HashExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.LabelExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.LanguageExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.NumericExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.SqlExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.TimeExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.URIExprFactory;
import org.openrdf.sail.rdbms.algebra.factories.ZonedExprFactory;
import org.openrdf.sail.rdbms.managers.TransTableManager;
import org.openrdf.sail.rdbms.schema.IdSequence;

/**
 * Initialises the {@link SelectQueryOptimizer} with the SQL expression
 * factories.
 * 
 * @author James Leigh
 * 
 */
public class SelectQueryOptimizerFactory {

	private RdbmsValueFactory vf;

	private TransTableManager tables;

	private IdSequence ids;

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	public void setTransTableManager(TransTableManager tables) {
		this.tables = tables;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public SelectQueryOptimizer createRdbmsFilterOptimizer() {
		LabelExprFactory label = new LabelExprFactory();
		BooleanExprFactory bool = createBooleanExprFactory();
		URIExprFactory uri = new URIExprFactory();
		SqlExprFactory sql = new SqlExprFactory();
		DatatypeExprFactory datatype = new DatatypeExprFactory();
		LanguageExprFactory language = new LanguageExprFactory();
		sql.setBNodeExprFactory(new BNodeExprFactory());
		sql.setBooleanExprFactory(bool);
		sql.setDatatypeExprFactory(datatype);
		sql.setLabelExprFactory(label);
		sql.setLanguageExprFactory(language);
		sql.setNumericExprFactory(new NumericExprFactory());
		sql.setTimeExprFactory(new TimeExprFactory());
		sql.setZonedExprFactory(new ZonedExprFactory(ids));
		sql.setHashExprFactory(new HashExprFactory(vf));
		sql.setURIExprFactory(uri);
		label.setSqlExprFactory(sql);
		uri.setSqlExprFactory(sql);
		bool.setSqlExprFactory(sql);
		SelectQueryOptimizer optimizer = new SelectQueryOptimizer();
		optimizer.setSqlExprFactory(sql);
		optimizer.setValueFactory(vf);
		optimizer.setTransTableManager(tables);
		optimizer.setIdSequence(ids);
		return optimizer;
	}

	protected BooleanExprFactory createBooleanExprFactory() {
		return new BooleanExprFactory();
	}
}
