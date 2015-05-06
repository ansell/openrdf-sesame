package org.openrdf.sesame.spin;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
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
	private final boolean includeRdf;

	public SPINRenderer()
	{
		this(true);
	}

	public SPINRenderer(boolean includeRdf)
	{
		this.includeRdf = includeRdf;
	}

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
		handler.handleStatement(valueFactory.createStatement(querySubj, RDF.TYPE, SP.ASK_CLASS));
		handler.handleStatement(valueFactory.createStatement(querySubj, SP.TEXT_PROPERTY, valueFactory.createLiteral(query.getSourceString())));
		if(includeRdf) {
			Resource whereBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(querySubj, SP.WHERE_PROPERTY, whereBNode));
			TupleExpr expr = query.getTupleExpr();
			SPINQueryModelVisitor visitor = new SPINQueryModelVisitor(handler, whereBNode, null);
			expr.visit(visitor);
			visitor.endList(null);
			visitor.handleVars();
		}
		handler.endRDF();
	}

	public void render(ParsedTupleQuery query, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		Resource querySubj = valueFactory.createBNode();
		handler.handleStatement(valueFactory.createStatement(querySubj, RDF.TYPE, SP.SELECT_CLASS));
		handler.handleStatement(valueFactory.createStatement(querySubj, SP.TEXT_PROPERTY, valueFactory.createLiteral(query.getSourceString())));
		if(includeRdf) {
			TupleExpr expr = query.getTupleExpr();
			SPINQueryModelVisitor visitor = new SPINQueryModelVisitor(handler, null, querySubj);
			expr.visit(visitor);
			visitor.handleVars();
		}
		handler.endRDF();
	}

	public void render(ParsedGraphQuery query, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		Resource querySubj = valueFactory.createBNode();
		handler.handleStatement(valueFactory.createStatement(querySubj, RDF.TYPE, SP.CONSTRUCT_CLASS));
		handler.handleStatement(valueFactory.createStatement(querySubj, SP.TEXT_PROPERTY, valueFactory.createLiteral(query.getSourceString())));
		if(includeRdf) {
			TupleExpr expr = query.getTupleExpr();
			SPINQueryModelVisitor visitor = new SPINQueryModelVisitor(handler, null, querySubj);
			expr.visit(visitor);
			visitor.handleVars();
		}
		handler.endRDF();
	}


	class SPINQueryModelVisitor extends QueryModelVisitorBase<RDFHandlerException>
	{
		private final RDFHandler handler;
		private final Map<String,Resource> varResources = new HashMap<String,Resource>();
		private Resource list;
		private Resource subject;
		private URI predicate;
		private boolean isSubQuery;

		SPINQueryModelVisitor(RDFHandler handler, Resource list, Resource subject) {
			this.handler = handler;
			this.list = list;
			this.subject = subject;
		}

		private Context save() {
			return new Context(list, subject);
		}

		private void restore(Context ctx) {
			list = ctx.list;
			subject = ctx.subject;
		}

		private Context newList(Resource res) {
			Context ctx = save();
			list = res;
			subject = null;
			return ctx;
		}

		private void listEntry() throws RDFHandlerException {
			listEntry(null);
		}

		private void listEntry(Resource entry) throws RDFHandlerException {
			if(list == null) {
				list = valueFactory.createBNode();
			}
			if(subject != null) {
				nextListEntry(valueFactory.createBNode());
				subject = null;
			}
			if(entry == null) {
				entry = valueFactory.createBNode();
			}
			handler.handleStatement(valueFactory.createStatement(list, RDF.FIRST, entry));
			subject = entry;
		}

		private void endList(Context ctx) throws RDFHandlerException {
			nextListEntry(RDF.NIL);
			if(ctx != null) {
				restore(ctx);
			}
		}

		private void nextListEntry(Resource nextEntry) throws RDFHandlerException {
			handler.handleStatement(valueFactory.createStatement(list, RDF.REST, nextEntry));
			list = nextEntry;
			subject = null;
		}

		private Resource createVar(String name) {
			Resource res = valueFactory.createBNode(name);
			varResources.put(name, res);
			return res;
		}

		private void handleVars() throws RDFHandlerException {
			for(Map.Entry<String,Resource> entry : varResources.entrySet()) {
				handler.handleStatement(valueFactory.createStatement(entry.getValue(), SP.VAR_NAME_PROPERTY, valueFactory.createLiteral(entry.getKey())));
			}
		}

		@Override
		public void meet(Projection node) throws RDFHandlerException {
			if(isSubQuery) {
				listEntry();
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.SUB_QUERY_CLASS));
				Resource queryBNode = valueFactory.createBNode();
				handler.handleStatement(valueFactory.createStatement(subject, SP.QUERY_PROPERTY, queryBNode));
				subject = queryBNode;
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.SELECT_CLASS));
			}
			node.getProjectionElemList().visit(this);
			Resource whereBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.WHERE_PROPERTY, whereBNode));
			isSubQuery = true;
			Context ctx = newList(whereBNode);
			node.getArg().visit(this);
			endList(ctx);
		}

		@Override
		public void meet(ProjectionElemList node) throws RDFHandlerException {
			Resource elemListBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.RESULT_VARIABLES_PROPERTY, elemListBNode));
			Context ctx = newList(elemListBNode);
			node.visitChildren(this);
			endList(ctx);
		}

		@Override
		public void meet(ProjectionElem node) throws RDFHandlerException {
			Resource var = createVar(node.getSourceName());
			listEntry(var);
		}

		@Override
		public void meet(StatementPattern node) throws RDFHandlerException {
			listEntry();
			predicate = SP.SUBJECT_PROPERTY;
			node.getSubjectVar().visit(this);
			predicate = SP.PREDICATE_PROPERTY;
			node.getPredicateVar().visit(this);
			predicate = SP.OBJECT_PROPERTY;
			node.getObjectVar().visit(this);
		}

		@Override
		public void meet(Var node) throws RDFHandlerException {
			Value value;
			if(node.isConstant()) {
				value = node.getValue();
			}
			else {
				String varName = node.getName();
				if("this".equals(varName)) {
					value = SPIN.THIS_CONTEXT_INSTANCE;
				}
				else {
					value = createVar(varName);
				}
			}
			handler.handleStatement(valueFactory.createStatement(subject, predicate, value));
		}

		@Override
		public void meet(ValueConstant node) throws RDFHandlerException {
			handler.handleStatement(valueFactory.createStatement(subject, predicate, node.getValue()));
		}

		@Override
		public void meet(Filter node) throws RDFHandlerException {
			node.getArg().visit(this);
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.FILTER_CLASS));
			Resource expr = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.EXPRESSION_PROPERTY, expr));
			Context ctx = save();
			list = null;
			subject = expr;
			node.getCondition().visit(this);
			restore(ctx);
		}

		@Override
		public void meet(Compare node) throws RDFHandlerException {
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, toValue(node.getOperator())));
			predicate = SP.ARG1_PROPERTY;
			node.getLeftArg().visit(this);
			predicate = SP.ARG2_PROPERTY;
			node.getRightArg().visit(this);
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
	}


	static final class Context
	{
		final Resource list;
		final Resource subject;

		Context(Resource list, Resource subject) {
			this.list = list;
			this.subject = subject;
		}
	}
}
