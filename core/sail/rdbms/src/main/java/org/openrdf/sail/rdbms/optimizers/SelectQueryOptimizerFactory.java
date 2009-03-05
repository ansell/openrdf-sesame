/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
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
		return optimizer;
	}

	protected BooleanExprFactory createBooleanExprFactory() {
		return new BooleanExprFactory();
	}
}
