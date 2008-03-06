/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class ProjectionElem extends QueryModelNodeBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _sourceName;

	private String _targetName;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ProjectionElem() {
	}

	public ProjectionElem(String name) {
		this(name, name);
	}

	public ProjectionElem(String sourceName, String targetName) {
		setSourceName(sourceName);
		setTargetName(targetName);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getSourceName() {
		return _sourceName;
	}

	public void setSourceName(String sourceName) {
		assert sourceName != null : "sourceName must not be null";
		_sourceName = sourceName;
	}

	public String getTargetName() {
		return _targetName;
	}

	public void setTargetName(String targetName) {
		assert targetName != null : "targetName must not be null";
		_targetName = targetName;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("PROJ_ELEM ");

		sb.append(_sourceName);

		if (!_sourceName.equals(_targetName)) {
			sb.append(" AS ").append(_targetName);
		}

		return sb.toString();
	}

	public ProjectionElem cloneProjectionElem() {
		return new ProjectionElem(getSourceName(), getTargetName());
	}
}
