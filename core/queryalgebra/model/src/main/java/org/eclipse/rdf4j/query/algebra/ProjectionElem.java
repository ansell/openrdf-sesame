/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra;

public class ProjectionElem extends AbstractQueryModelNode {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String sourceName;

	private String targetName;

	private boolean aggregateOperatorInExpression;
	
	private ExtensionElem sourceExpression;
	
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
	public String getSignature() {
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

	@Override
	public boolean equals(Object other) {
		if (other instanceof ProjectionElem) {
			ProjectionElem o = (ProjectionElem)other;
			return sourceName.equals(o.getSourceName()) && targetName.equals(o.getTargetName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		// Note: don't xor source and target since they will often be equal
		return targetName.hashCode();
	}

	@Override
	public ProjectionElem clone() {
		return (ProjectionElem)super.clone();
	}

	/**
	 * @return Returns the aggregateOperatorInExpression.
	 */
	public boolean hasAggregateOperatorInExpression() {
		return aggregateOperatorInExpression;
	}

	/**
	 * @param aggregateOperatorInExpression The aggregateOperatorInExpression to set.
	 */
	public void setAggregateOperatorInExpression(boolean aggregateOperatorInExpression) {
		this.aggregateOperatorInExpression = aggregateOperatorInExpression;
	}

	/**
	 * @return Returns the sourceExpression.
	 */
	public ExtensionElem getSourceExpression() {
		return sourceExpression;
	}

	/**
	 * @param sourceExpression The sourceExpression to set.
	 */
	public void setSourceExpression(ExtensionElem sourceExpression) {
		this.sourceExpression = sourceExpression;
	}
	
	
}
