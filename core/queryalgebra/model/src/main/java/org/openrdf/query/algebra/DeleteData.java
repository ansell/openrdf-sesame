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
package org.openrdf.query.algebra;

/**
 * @author jeen
 */
public class DeleteData extends AbstractQueryModelNode implements UpdateExpr {

	private final String dataBlock;

	public DeleteData(String dataBlock) {
		this.dataBlock = dataBlock;
	}

	@Override
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String getDataBlock() {
		return dataBlock;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof DeleteData) {
			DeleteData o = (DeleteData)other;
			return dataBlock.equals(o.dataBlock);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return dataBlock.hashCode();
	}

	@Override
	public DeleteData clone() {
		return new DeleteData(dataBlock);
	}

	@Override
	public boolean isSilent() {
		return false;
	}

}
