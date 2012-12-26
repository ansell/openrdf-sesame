/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.turtle.TurtleUtil;
import org.openrdf.sail.federation.algebra.NaryJoin;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;

/**
 * Remove redundent {@link OwnedTupleExpr}.
 * 
 * @author James Leigh
 */
public class PrepareOwnedTupleExpr extends
		QueryModelVisitorBase<RepositoryException> implements QueryOptimizer {
	private OwnedTupleExpr owner;
	private String pattern;
	private TupleExpr patternNode;
	/** local name to sparql name */
	private Map<String, String> variables = new HashMap<String, String>();
	private boolean reduce;
	private boolean reduced;
	private boolean distinct;

	public void optimize(TupleExpr query, Dataset dataset, BindingSet bindings) {
		try {
			query.visit(this);
		} catch (RepositoryException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	@Override
	public void meetOther(QueryModelNode node) throws RepositoryException {
		if (node instanceof OwnedTupleExpr) {
			meetOwnedTupleExpr((OwnedTupleExpr) node);
		} else if (node instanceof NaryJoin) {
			meetMultiJoin((NaryJoin) node);
		} else {
			super.meetOther(node);
		}
	}

	private void meetOwnedTupleExpr(OwnedTupleExpr node)
			throws RepositoryException {
		OwnedTupleExpr before = this.owner;
		try {
			this.owner = node;
			meetNode(node);
			this.owner = null;
		} finally {
			this.owner = before;
		}
	}

	@Override
	protected void meetNode(QueryModelNode node) throws RepositoryException {
		super.meetNode(node);
		if (owner != null && patternNode != null
				&& !(patternNode instanceof StatementPattern)) {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT");
			if (distinct) {
				sb.append(" DISTINCT");
			} else if (reduced || reduce) {
				sb.append(" REDUCED");
			}
			boolean mapping = false;
			Map<String, String> bindings = new HashMap<String, String>();
			ProjectionElemList list = new ProjectionElemList();
			for (String name : patternNode.getBindingNames()) {
				if (variables.containsKey(name)) {
					String var = variables.get(name);
					sb.append(" ?").append(var);
					bindings.put(name, var);
					list.addElement(new ProjectionElem(var, name));
					if (!name.equals(var)) {
						mapping = true;
					}
				}
			}
			sb.append("\nWHERE {\n").append(pattern).append("}");
			try {
				QueryModelNode parent = patternNode.getParentNode();
				if (parent instanceof OwnedTupleExpr) {
					OwnedTupleExpr owned = (OwnedTupleExpr) parent;
					owned
							.prepare(QueryLanguage.SPARQL, sb.toString(),
									bindings);
					if (mapping) {
						Projection proj = new Projection(owned.clone(), list);
						owned.replaceWith(proj);
					}
				} else {
					OwnedTupleExpr owned = new OwnedTupleExpr(owner.getOwner(),
							patternNode.clone());
					owned
							.prepare(QueryLanguage.SPARQL, sb.toString(),
									bindings);
					if (mapping) {
						Projection proj = new Projection(owned, list);
						patternNode.replaceWith(proj);
					} else {
						patternNode.replaceWith(owned);
					}
				}
			} catch (MalformedQueryException e) {
				throw new AssertionError(e);
			}
		}
		reduced = false;
		distinct = false;
		pattern = null;
		patternNode = null;
	}

	@Override
	public void meet(Distinct node) throws RepositoryException {
		boolean before = reduce;
		try {
			reduce = true;
			node.getArg().visit(this);
		} finally {
			reduce = before;
		}
		if (patternNode == null) {
			return;
		}
		this.distinct = true;
		this.patternNode = node;
	}

	@Override
	public void meet(Reduced node) throws RepositoryException {
		boolean before = reduce;
		try {
			reduce = true;
			node.getArg().visit(this);
		} finally {
			reduce = before;
		}
		if (patternNode == null) {
			return;
		}
		this.reduced = true;
		this.patternNode = node;
	}

	@Override
	public void meet(Projection node) throws RepositoryException {
		TupleExpr arg = node.getArg();
		if (arg instanceof StatementPattern
				&& arg.getBindingNames().equals(node.getBindingNames())) {
			meetNode(node);
		} else {
			arg.visit(this);
			if (patternNode == null)
				return;
			Map<String, String> map = new HashMap<String, String>();
			for (ProjectionElem e : node.getProjectionElemList().getElements()) {
				String source = variables.get(e.getSourceName());
				if (source == null) {
					source = safe(e.getSourceName());
				}
				map.put(e.getTargetName(), source);
			}
			this.variables = map;
			this.patternNode = node;
		}
	}

	@Override
	public void meet(LeftJoin node) throws RepositoryException {
		if (node.getCondition() == null) {
			Map<String, String> vars = new HashMap<String, String>();
			StringBuilder sb = new StringBuilder();
			node.getLeftArg().visit(this);
			if (patternNode == null)
				return;
			sb.append(pattern);
			vars.putAll(variables);
			node.getRightArg().visit(this);
			if (patternNode == null)
				return;
			sb.append("OPTIONAL {").append(pattern).append("}\n");
			vars.putAll(variables);
			this.variables = vars;
			this.pattern = sb.toString();
			this.patternNode = node;
		} else {
			super.meet(node);
		}
	}

	public void meetMultiJoin(NaryJoin node) throws RepositoryException {
		Map<String, String> vars = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
		for (TupleExpr arg : node.getArgs()) {
			arg.visit(this);
			if (patternNode == null && owner != null) {
				return; // unsupported operation
			} else if (patternNode == null) {
				sb = null; // no owner
			} else if (sb != null) {
				sb.append("{").append(pattern).append("}\n");
				vars.putAll(variables);
			}
		}
		if (sb != null) {
			this.variables = vars;
			this.pattern = sb.toString();
			this.patternNode = node;
		}
	}

	@Override
	public void meet(Join node) throws RepositoryException {
		Map<String, String> vars = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
		node.getLeftArg().visit(this);
		if (patternNode == null)
			return;
		sb.append("{").append(pattern).append("}\n");
		vars.putAll(variables);
		node.getRightArg().visit(this);
		if (patternNode == null)
			return;
		sb.append("{").append(pattern).append("}\n");
		vars.putAll(variables);
		this.variables = vars;
		this.pattern = sb.toString();
		this.patternNode = node;
	}

	@Override
	public void meet(StatementPattern node) throws RepositoryException {
		StringBuilder sb = new StringBuilder();
		Scope scope = node.getScope();
		Var subj = node.getSubjectVar();
		Var pred = node.getPredicateVar();
		Var obj = node.getObjectVar();
		Var ctx = node.getContextVar();
		boolean cokay = ctx == null && scope.equals(Scope.DEFAULT_CONTEXTS)
				|| ctx != null && scope.equals(Scope.NAMED_CONTEXTS);
		boolean sokay = !subj.hasValue() || subj.isAnonymous()
				|| subj.getValue() instanceof URI;
		boolean ookay = !obj.hasValue() || obj.isAnonymous()
				|| obj.getValue() instanceof URI
				|| obj.getValue() instanceof Literal;
		if (cokay && sokay && ookay) {
			variables.clear();
			if (ctx != null) {
				sb.append("GRAPH ");
				appendVar(sb, ctx.getName());
				sb.append(" {\n");
			}
			appendVar(sb, subj);
			appendVar(sb, pred);
			appendVar(sb, obj);
			sb.append(" .");
			appendFilter(sb, subj);
			appendFilter(sb, pred);
			appendFilter(sb, obj);
			if (ctx != null) {
				if (ctx.hasValue()) {
					sb.append("\nFILTER sameTerm(");
					appendVar(sb, ctx.getName());
					sb.append(", ");
					writeValue(sb, ctx.getValue());
					sb.append(")\n");
				}
				sb.append("}");
			}
			this.pattern = sb.toString();
			this.patternNode = node;
		} else {
			this.patternNode = null;
		}
	}

	private void appendVar(StringBuilder sb, Var var) {
		if (var.hasValue() && var.isAnonymous()) {
			Value value = var.getValue();
			writeValue(sb, value);
		} else {
			String varName = var.getName();
			appendVar(sb, varName);
		}
		sb.append(" ");
	}

	private void appendVar(StringBuilder sb, String varName) {
		sb.append("?");
		String name = safe(varName);
		sb.append(name);
		variables.put(varName, name);
	}

	private String safe(String name) {
		return name.replace('-', '_');
	}

	private void appendFilter(StringBuilder sb, Var var) {
		if (var.hasValue() && !var.isAnonymous()) {
			sb.append("\nFILTER sameTerm(");
			appendVar(sb, var.getName());
			sb.append(", ");
			writeValue(sb, var.getValue());
			sb.append(")");
		}
	}

	private void writeValue(StringBuilder sb, Value val) {
		if (val instanceof Resource) {
			writeResource(sb, (Resource) val);
		} else {
			writeLiteral(sb, (Literal) val);
		}
	}

	private void writeResource(StringBuilder sb, Resource res) {
		if (res instanceof URI) {
			writeURI(sb, (URI) res);
		} else {
			writeBNode(sb, (BNode) res);
		}
	}

	private void writeURI(StringBuilder sb, URI uri) {
		sb.append("<");
		sb.append(TurtleUtil.encodeURIString(uri.stringValue()));
		sb.append(">");
	}

	private void writeBNode(StringBuilder sb, BNode bNode) {
		sb.append("_:");
		sb.append(bNode.stringValue());
	}

	private void writeLiteral(StringBuilder sb, Literal lit) {
		String label = lit.getLabel();

		if (label.indexOf('\n') > 0 || label.indexOf('\r') > 0
				|| label.indexOf('\t') > 0) {
			// Write label as long string
			sb.append("\"\"\"");
			sb.append(TurtleUtil.encodeLongString(label));
			sb.append("\"\"\"");
		} else {
			// Write label as normal string
			sb.append("\"");
			sb.append(TurtleUtil.encodeString(label));
			sb.append("\"");
		}

		if (lit.getDatatype() != null) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			sb.append("^^");
			writeURI(sb, lit.getDatatype());
		} else if (lit.getLanguage() != null) {
			// Append the literal's language
			sb.append("@");
			sb.append(lit.getLanguage());
		}
	}

}
