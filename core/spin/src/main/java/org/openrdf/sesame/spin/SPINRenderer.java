package org.openrdf.sesame.spin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedDescribeQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import com.google.common.base.Function;

public class SPINRenderer {
	public enum Output {
		TEXT_AND_RDF(true, true), TEXT_ONLY(true, false), RDF_ONLY(false, true);

		final boolean text, rdf;

		Output(boolean text, boolean rdf) {
			this.text = text;
			this.rdf = rdf;
		}
	}

	private final ValueFactory valueFactory;
	private final Output output;
	private final Function<String,URI> wellKnownVars;

	public SPINRenderer()
	{
		this(Output.TEXT_AND_RDF);
	}

	public SPINRenderer(Output output)
	{
		this(output, SPINWellKnownVars.INSTANCE, ValueFactoryImpl.getInstance());
	}

	public SPINRenderer(Output output, Function<String,URI> wellKnownVarMapper, ValueFactory vf)
	{
		this.output = output;
		this.wellKnownVars = wellKnownVarMapper;
		this.valueFactory = vf;
	}

	public void render(ParsedQuery query, RDFHandler handler) throws RDFHandlerException
	{
		if(query instanceof ParsedBooleanQuery) {
			render((ParsedBooleanQuery)query, handler);
		}
		else if(query instanceof ParsedTupleQuery) {
			render((ParsedTupleQuery)query, handler);
		}
		// order matters as subclass of ParsedGraphQuery
		else if(query instanceof ParsedDescribeQuery) {
			render((ParsedDescribeQuery)query, handler);
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
		if(output.text) {
			handler.handleStatement(valueFactory.createStatement(querySubj, SP.TEXT_PROPERTY, valueFactory.createLiteral(query.getSourceString())));
		}
		if(output.rdf) {
			Resource whereBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(querySubj, SP.WHERE_PROPERTY, whereBNode));
			TupleExpr expr = query.getTupleExpr();
			SPINQueryModelVisitor visitor = new SPINQueryModelVisitor(handler, whereBNode, null);
			expr.visit(visitor);
			visitor.endList(null);
		}
		handler.endRDF();
	}

	public void render(ParsedTupleQuery query, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		Resource querySubj = valueFactory.createBNode();
		handler.handleStatement(valueFactory.createStatement(querySubj, RDF.TYPE, SP.SELECT_CLASS));
		if(output.text) {
			handler.handleStatement(valueFactory.createStatement(querySubj, SP.TEXT_PROPERTY, valueFactory.createLiteral(query.getSourceString())));
		}
		if(output.rdf) {
			TupleExpr expr = query.getTupleExpr();
			SPINQueryModelVisitor visitor = new SPINQueryModelVisitor(handler, null, querySubj);
			expr.visit(visitor);
		}
		handler.endRDF();
	}

	public void render(ParsedDescribeQuery query, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		Resource querySubj = valueFactory.createBNode();
		handler.handleStatement(valueFactory.createStatement(querySubj, RDF.TYPE, SP.DESCRIBE_CLASS));
		if(output.text) {
			handler.handleStatement(valueFactory.createStatement(querySubj, SP.TEXT_PROPERTY, valueFactory.createLiteral(query.getSourceString())));
		}
		if(output.rdf) {
			TupleExpr expr = query.getTupleExpr();
			SPINQueryModelVisitor visitor = new DescribeSPINQueryModelVisitor(handler, querySubj);
			expr.visit(visitor);
		}
		handler.endRDF();
	}

	public void render(ParsedGraphQuery query, RDFHandler handler) throws RDFHandlerException {
		handler.startRDF();
		Resource querySubj = valueFactory.createBNode();
		handler.handleStatement(valueFactory.createStatement(querySubj, RDF.TYPE, SP.CONSTRUCT_CLASS));
		if(output.text) {
			handler.handleStatement(valueFactory.createStatement(querySubj, SP.TEXT_PROPERTY, valueFactory.createLiteral(query.getSourceString())));
		}
		if(output.rdf) {
			TupleExpr expr = query.getTupleExpr();
			SPINQueryModelVisitor visitor = new ConstructSPINQueryModelVisitor(handler, querySubj);
			expr.visit(visitor);
		}
		handler.endRDF();
	}


	class DescribeSPINQueryModelVisitor extends SPINQueryModelVisitor
	{
		DescribeSPINQueryModelVisitor(RDFHandler handler, Resource subject) {
			super(handler, null, subject);
		}

		@Override
		public void meet(ProjectionElemList node) throws RDFHandlerException {
			if(isSubQuery) {
				super.meet(node);
			}
			else {
				Resource elemListBNode = valueFactory.createBNode();
				handler.handleStatement(valueFactory.createStatement(subject, SP.RESULT_NODES_PROPERTY, elemListBNode));
				Context ctx = newList(elemListBNode);
				meetNode(node);
				endList(ctx);
			}
		}
	}


	class ConstructSPINQueryModelVisitor extends SPINQueryModelVisitor
	{
		ConstructSPINQueryModelVisitor(RDFHandler handler, Resource subject) {
			super(handler, null, subject);
		}

		private Context startTemplateList() throws RDFHandlerException {
			Resource elemListBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.TEMPLATES_PROPERTY, elemListBNode));
			return newList(elemListBNode);
		}

		private void endTemplateList(Context ctx) throws RDFHandlerException {
			endList(ctx);
		}

		@Override
		public void meet(MultiProjection node) throws RDFHandlerException {
			Context ctx = startTemplateList();
			super.meet(node);
			endTemplateList(ctx);
		}

		@Override
		public void meet(ProjectionElemList node) throws RDFHandlerException {
			if(isSubQuery) {
				super.meet(node);
			}
			else if(isMultiProjection) {
				listEntry();
				meetNode(node);
			}
			else {
				Context ctx = startTemplateList();
				listEntry();
				meetNode(node);
				endTemplateList(ctx);
			}
		}

		@Override
		public void meet(ProjectionElem node) throws RDFHandlerException {
			if(isSubQuery) {
				super.meet(node);
			}
			else {
				String varName = node.getSourceName();
				ValueExpr valueExpr = extensionExprs.get(varName);
				Value value = (valueExpr instanceof ValueConstant) ? ((ValueConstant)valueExpr).getValue() : createVar(varName);
				String targetName = node.getTargetName();
				URI pred;
				if("subject".equals(targetName)) {
					pred = SP.SUBJECT_PROPERTY;
				}
				else if("predicate".equals(targetName)) {
					pred = SP.PREDICATE_PROPERTY;
				}
				else if("object".equals(targetName)) {
					pred = SP.OBJECT_PROPERTY;
				}
				else {
					throw new AssertionError("Unexpected ProjectionElem: "+node);
				}
				handler.handleStatement(valueFactory.createStatement(subject, pred, value));
			}
		}
	}


	class SPINQueryModelVisitor extends QueryModelVisitorBase<RDFHandlerException>
	{
		final RDFHandler handler;
		Map<String,ValueExpr> extensionExprs;
		Resource list;
		Resource subject;
		URI predicate;
		boolean isMultiProjection;
		boolean isSubQuery;

		SPINQueryModelVisitor(RDFHandler handler, Resource list, Resource subject) {
			this.handler = handler;
			this.list = list;
			this.subject = subject;
		}

		private void meetExtension(TupleExpr expr) {
			ExtensionQueryModelVisitor extVisitor = new ExtensionQueryModelVisitor();
			expr.visit(extVisitor);
			extensionExprs = extVisitor.extensionExprs;
		}

		Context save() {
			return new Context(list, subject);
		}

		void restore(Context ctx) {
			list = ctx.list;
			subject = ctx.subject;
		}

		Context newList(Resource res) {
			Context ctx = save();
			list = res;
			subject = null;
			return ctx;
		}

		void listEntry() throws RDFHandlerException {
			listEntry(null);
		}

		void listEntry(Value entry) throws RDFHandlerException {
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
			if(entry instanceof Resource) {
				subject = (Resource) entry;
			} else {
				subject = list;
			}
		}

		void endList(Context ctx) throws RDFHandlerException {
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

		Resource createVar(String name) throws RDFHandlerException {
			Resource res = (wellKnownVars != null) ? wellKnownVars.apply(name) : null;
			if(res == null) {
				res = valueFactory.createBNode(name);
				handler.handleStatement(valueFactory.createStatement(res, SP.VAR_NAME_PROPERTY, valueFactory.createLiteral(name)));
			}
			return res;
		}

		@Override
		public void meet(MultiProjection node) throws RDFHandlerException {
			meetExtension(node.getArg());
			isMultiProjection = true;
			super.meet(node);
			isMultiProjection = false;
			isSubQuery = true;
		}

		@Override
		public void meet(Projection node) throws RDFHandlerException {
			meetExtension(node.getArg());
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

			if(!isMultiProjection) {
				isSubQuery = true;
			}

			Context ctx = newList(whereBNode);
			node.getArg().visit(this);
			endList(ctx);
		}

		@Override
		public void meet(ProjectionElemList node) throws RDFHandlerException {
			Resource elemListBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.RESULT_VARIABLES_PROPERTY, elemListBNode));
			Context ctx = newList(elemListBNode);
			super.meet(node);
			endList(ctx);
		}

		@Override
		public void meet(ProjectionElem node) throws RDFHandlerException {
			Resource var = createVar(node.getSourceName());
			listEntry(var);
		}

		@Override
		public void meet(Extension node) throws RDFHandlerException {
			node.getArg().visit(this);
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
				value = createVar(node.getName());
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

		@Override
		public void meet(Group node) throws RDFHandlerException {
			node.getArg().visit(this);
		}

		@Override
		public void meet(Count node) throws RDFHandlerException {
			super.meet(node);
		}

		@Override
		public void meet(BindingSetAssignment node) throws RDFHandlerException {
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.VALUES_CLASS));
			Resource bindingList = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.BINDINGS_PROPERTY, bindingList));
			Context bindingCtx = newList(bindingList);
			List<String> bindingVars = new ArrayList<String>();
			for(ValueExpr valueExpr : extensionExprs.values()) {
				if(valueExpr instanceof Var) {
					bindingVars.add(((Var)valueExpr).getName());
				}
			}
			for(BindingSet bs : node.getBindingSets()) {
				listEntry();
				Context setCtx = newList(subject);
				for(String varName : bindingVars) {
					Value v = bs.getValue(varName);
					if(v == null) {
						v = SP.UNDEF;
					}
					listEntry(v);
				}
				endList(setCtx);
			}
			endList(bindingCtx);

			Resource varNameList = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.VAR_NAMES_PROPERTY, varNameList));
			Context varnameCtx = newList(varNameList);
			for(String varName : bindingVars) {
				listEntry(valueFactory.createLiteral(varName));
			}
			endList(varnameCtx);
		}
	}


	static final class ExtensionQueryModelVisitor extends QueryModelVisitorBase<RuntimeException>
	{
		Map<String,ValueExpr> extensionExprs;

		@Override
		public void meet(Extension node) {
			List<ExtensionElem> elements = node.getElements();
			extensionExprs = new HashMap<String,ValueExpr>(elements.size());
			for(ExtensionElem elem : elements) {
				extensionExprs.put(elem.getName(), elem.getExpr());
			}
		}

		@Override
		protected void meetNode(QueryModelNode node) {
			// stop
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

	static final class SPINWellKnownVars implements Function<String,URI>
	{
		static final SPINWellKnownVars INSTANCE = new SPINWellKnownVars();

		@Override
		public URI apply(String name) {
			return "this".equals(name) ? SPIN.THIS_CONTEXT_INSTANCE : null;
		}
	}
}
