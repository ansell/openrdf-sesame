/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import java.util.Map;

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;

/**
 * A modify update operation.
 * 
 * @author Jeen Broekstra
 */
public class ParsedModify extends ParsedUpdate {

	/*----------------*
	 * private fields *
	 *----------------*/

	private ValueConstant with;
	private TupleExpr delete;
	private TupleExpr insert;
	private TupleExpr where;

	
	/*--------------*
	 * Constructors *
	 *--------------*/
	/**
	 * Creates a new modify update. To complete this update, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedModify() {
		super();
	}

	/**
	 * Creates a new update. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 * 
	 * @param namespaces
	 *        A mapping of namespace prefixes to namespace names representing the
	 *        namespaces that are used in the update.
	 */
	public ParsedModify(Map<String, String> namespaces) {
		super(namespaces);
	}

	/**
	 * @param with
	 * @param delete
	 * @param insert
	 * @param where
	 */
	public ParsedModify(ValueConstant with, TupleExpr delete, TupleExpr insert, TupleExpr where) {
		this.setWith(with);
		this.setDelete(delete);
		this.setInsert(insert);
		this.setWhere(where);
	}


	/*---------*
	 * Methods *
	 *---------*/
	
	/**
	 * @param with The with to set.
	 */
	public void setWith(ValueConstant with) {
		this.with = with;
	}

	/**
	 * @return Returns the with.
	 */
	public ValueConstant getWith() {
		return with;
	}

	/**
	 * @param delete The delete to set.
	 */
	public void setDelete(TupleExpr delete) {
		this.delete = delete;
	}

	/**
	 * @return Returns the delete.
	 */
	public TupleExpr getDelete() {
		return delete;
	}

	/**
	 * @param insert The insert to set.
	 */
	public void setInsert(TupleExpr insert) {
		this.insert = insert;
	}

	/**
	 * @return Returns the insert.
	 */
	public TupleExpr getInsert() {
		return insert;
	}

	/**
	 * @param where The where to set.
	 */
	public void setWhere(TupleExpr where) {
		this.where = where;
	}

	/**
	 * @return Returns the where.
	 */
	public TupleExpr getWhere() {
		return where;
	}


	@Override
	public String toString() {
		String stringRep = "";
		if (with != null) {
			stringRep += " WITH " + with.toString();
			stringRep += "\n";
		}
		if (delete != null) {
			stringRep += " DELETE " + delete.toString();
			stringRep += "\n";
		}
		if (insert != null) {
			stringRep += " INSERT " + insert.toString();
			stringRep += "\n";
		}
		if (where != null) {
			stringRep += " WHERE " + where.toString();
		}
		return stringRep;
	}
}
