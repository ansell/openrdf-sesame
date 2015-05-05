package org.openrdf.sesame.spin;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class SPINRenderer {
	private final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

	public void render(ParsedQuery query, RDFHandler handler) throws RDFHandlerException
	{
		if(query instanceof ParsedBooleanQuery) {
			render((ParsedBooleanQuery)query, handler);
		}
		else if(query instanceof ParsedTupleQuery) {
			render((ParsedTupleQuery)query, handler);
		}
		else if(query instanceof ParsedGraphQuery) {
			render((ParsedGraphQuery)query, handler);
		}
		else {
			throw new AssertionError("Unrecognised ParsedQuery: "+query.getClass());
		}
	}

	public void render(ParsedBooleanQuery query, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		Resource querySubj = valueFactory.createBNode();
		handler.handleStatement(valueFactory.createStatement(querySubj, RDF.TYPE, SP.ASK));
		Resource whereBNode = valueFactory.createBNode();
		handler.handleStatement(valueFactory.createStatement(querySubj, SP.WHERE, whereBNode));
		TupleExpr expr = query.getTupleExpr();
		expr.visit(new SPINQueryModelVisitor(handler, whereBNode));
		handler.endRDF();
	}

	public void render(ParsedTupleQuery query, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		handler.endRDF();
	}

	public void render(ParsedGraphQuery query, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		handler.endRDF();
	}


	private Value toValue(Var var)
	{
		if(var.isConstant()) {
			return var.getValue();
		}
		else {
			return valueFactory.createBNode(var.getName());
		}
	}

	private Value toValue(CompareOp op)
	{
		switch(op)
		{
			case EQ: return SP.EQ;
			case NE: return SP.NE;
			case LT: return SP.LT;
			case LE: return SP.LE;
			case GE: return SP.GE;
			case GT: return SP.GT;
		}
		throw new AssertionError("Unrecognised CompareOp: "+op);
	}

	class SPINQueryModelVisitor extends QueryModelVisitorBase<RDFHandlerException>
	{
		private final RDFHandler handler;
		private Resource subject;
		private URI predicate;

		SPINQueryModelVisitor(RDFHandler handler, Resource subj) {
			this.handler = handler;
			this.subject = subj;
		}

		private void nextListEntry() throws RDFHandlerException {
			Resource nextNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.REST, nextNode));
			subject = nextNode;
		}

		@Override
		public void meet(StatementPattern node) throws RDFHandlerException {
			predicate = SP.SUBJECT;
			node.getSubjectVar().visit(this);
			predicate = SP.PREDICATE;
			node.getPredicateVar().visit(this);
			predicate = SP.OBJECT;
			node.getObjectVar().visit(this);
			nextListEntry();
		}

		@Override
		public void meet(Var node) throws RDFHandlerException {
			handler.handleStatement(valueFactory.createStatement(subject, predicate, toValue(node)));
		}

		@Override
		public void meet(ValueConstant node) throws RDFHandlerException {
			handler.handleStatement(valueFactory.createStatement(subject, predicate, node.getValue()));
		}

		@Override
		public void meet(Filter node) throws RDFHandlerException {
			node.getArg().visit(this);
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.FILTER));
			Resource exprNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.EXPRESSION, exprNode));
			Resource listNode = subject;
			subject = exprNode;
			node.getCondition().visit(this);
			subject = listNode;
			nextListEntry();
		}

		@Override
		public void meet(Compare node) throws RDFHandlerException {
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, toValue(node.getOperator())));
			predicate = SP.ARG_1;
			node.getLeftArg().visit(this);
			predicate = SP.ARG_2;
			node.getRightArg().visit(this);
		}
	}
}
