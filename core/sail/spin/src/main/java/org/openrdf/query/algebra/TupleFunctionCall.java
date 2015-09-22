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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.algebra.evaluation.function.TupleFunction;

/**
 * A call to a {@link TupleFunction}.
 */
public class TupleFunctionCall extends QueryModelNodeBase implements TupleExpr {

	private String uri;

	private List<ValueExpr> args = new ArrayList<ValueExpr>();

	private List<Var> resultVars = new ArrayList<Var>();

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public List<ValueExpr> getArgs() {
		return args;
	}

	public void setArgs(Iterable<ValueExpr> args) {
		this.args.clear();
		addArgs(args);
	}

	public void addArgs(ValueExpr... args) {
		for (ValueExpr arg : args) {
			addArg(arg);
		}
	}

	public void addArgs(Iterable<ValueExpr> args) {
		for (ValueExpr arg : args) {
			addArg(arg);
		}
	}

	public void addArg(ValueExpr arg) {
		assert arg != null : "arg must not be null";
		args.add(arg);
		arg.setParentNode(this);
	}

	public List<Var> getResultVars() {
		return resultVars;
	}

	public void setResultVars(Iterable<Var> resultVars) {
		this.resultVars.clear();
		addResultVars(resultVars);
	}

	public void addResultVars(Var... resultVars) {
		for (Var var : resultVars) {
			addResultVar(var);
		}
	}

	public void addResultVars(Iterable<Var> resultVars) {
		for (Var var : resultVars) {
			addResultVar(var);
		}
	}

	public void addResultVar(Var resultVar) {
		assert resultVar != null : "resultVar must not be null";
		resultVars.add(resultVar);
		resultVar.setParentNode(this);
	}

	@Override
	public Set<String> getBindingNames() {
		return getAssuredBindingNames();
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		// NB: preserve resultBindings order
		Set<String> bindingNames = new LinkedHashSet<String>(2*resultVars.size());
		for(Var var : resultVars) {
			bindingNames.add(var.getName());
		}
		return bindingNames;
	}

	@Override
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meetOther(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for(ValueExpr arg : args) {
			arg.visit(visitor);
		}

		for(Var var : resultVars) {
			var.visit(visitor);
		}

		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (replaceNodeInList(args, current, replacement)) {
			return;
		}
		else if (replaceNodeInList(resultVars, current, replacement)) {
			return;
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(super.getSignature());
		sb.append(" (").append(uri);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof TupleFunctionCall) {
			TupleFunctionCall o = (TupleFunctionCall)other;
			return uri.equals(o.getURI()) && args.equals(o.getArgs()) && resultVars.equals(o.getResultVars());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return uri.hashCode() ^ args.hashCode() ^ resultVars.hashCode();
	}

	@Override
	public TupleFunctionCall clone() {
		TupleFunctionCall clone = (TupleFunctionCall)super.clone();

		clone.args = new ArrayList<ValueExpr>(getArgs().size());
		for (ValueExpr arg : getArgs()) {
			clone.addArg(arg.clone());
		}

		clone.resultVars = new ArrayList<Var>(getResultVars().size());
		for (Var var : getResultVars()) {
			clone.addResultVar(var.clone());
		}

		return clone;
	}
}
