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

	private String sourceName;

	private String targetName;

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
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		assert sourceName != null : "sourceName must not be null";
		this.sourceName = sourceName;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		assert targetName != null : "targetName must not be null";
		this.targetName = targetName;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature()
	{
		StringBuilder sb = new StringBuilder(32);
		sb.append(super.getSignature());

		sb.append(" \"");
		sb.append(sourceName);
		sb.append("\"");

		if (!sourceName.equals(targetName)) {
			sb.append(" AS \"").append(targetName).append("\"");
		}

		return sb.toString();
	}

	public ProjectionElem clone() {
		return (ProjectionElem)super.clone();
	}
}
