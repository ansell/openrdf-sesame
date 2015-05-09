package org.openrdf.sesame.spin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
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
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Service;
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
			SPINVisitor visitor = new SPINVisitor(handler, whereBNode, null);
			expr.visit(visitor);
			visitor.end();
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
			SPINVisitor visitor = new SPINVisitor(handler, null, querySubj);
			expr.visit(visitor);
			visitor.end();
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
			SPINVisitor visitor = new DescribeVisitor(handler, querySubj);
			expr.visit(visitor);
			visitor.end();
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
			SPINVisitor visitor = new ConstructVisitor(handler, querySubj);
			expr.visit(visitor);
			visitor.end();
		}
		handler.endRDF();
	}


	class DescribeVisitor extends SPINVisitor
	{
		DescribeVisitor(RDFHandler handler, Resource subject) {
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
				ListContext ctx = newList(elemListBNode);
				meetNode(node);
				endList(ctx);
			}
		}
	}


	class ConstructVisitor extends SPINVisitor
	{
		ConstructVisitor(RDFHandler handler, Resource subject) {
			super(handler, null, subject);
		}

		private ListContext startTemplateList() throws RDFHandlerException {
			Resource elemListBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.TEMPLATES_PROPERTY, elemListBNode));
			return newList(elemListBNode);
		}

		private void endTemplateList(ListContext ctx) throws RDFHandlerException {
			endList(ctx);
		}

		@Override
		public void meet(MultiProjection node) throws RDFHandlerException {
			ListContext ctx = startTemplateList();
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
				ListContext ctx = startTemplateList();
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
				ValueExpr valueExpr = inlineBindings.getValueExpr(varName);
				Value value = (valueExpr instanceof ValueConstant) ? ((ValueConstant)valueExpr).getValue() : getVar(varName);
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


	class SPINVisitor extends QueryModelVisitorBase<RDFHandlerException>
	{
		final RDFHandler handler;
		final Map<String,BNode> varBNodes = new HashMap<String,BNode>();
		ExtensionContext inlineBindings;
		Resource list;
		Resource subject;
		URI predicate;
		boolean isMultiProjection;
		boolean isSubQuery;

		SPINVisitor(RDFHandler handler, Resource list, Resource subject) {
			this.handler = handler;
			this.list = list;
			this.subject = subject;
		}

		private ExtensionContext meetExtension(TupleExpr expr) {
			ExtensionContext extVisitor = new ExtensionContext();
			expr.visit(extVisitor);
			ExtensionContext oldInlineBindings = inlineBindings;
			inlineBindings = (extVisitor.extension) != null ? extVisitor : null;
			return oldInlineBindings;
		}

		ListContext save() {
			return new ListContext(list, subject);
		}

		void restore(ListContext ctx) {
			list = ctx.list;
			subject = ctx.subject;
		}

		ListContext newList(Resource res) {
			ListContext ctx = save();
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

		void endList(ListContext ctx) throws RDFHandlerException {
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

		Resource getVar(String name) throws RDFHandlerException {
			Resource res = (wellKnownVars != null) ? wellKnownVars.apply(name) : null;
			if(res == null) {
				res = varBNodes.get(name);
				if(res == null)
				{
					BNode bnode = valueFactory.createBNode(name);
					varBNodes.put(name, bnode);
					res = bnode;
				}
			}
			return res;
		}

		public void end() throws RDFHandlerException {
			if(list != null) {
				endList(null);
			}

			// output all the var BNodes together to give a more friendlier RDF structure
			for(Map.Entry<String,BNode> entry : varBNodes.entrySet()) {
				handler.handleStatement(valueFactory.createStatement(entry.getValue(), SP.VAR_NAME_PROPERTY, valueFactory.createLiteral(entry.getKey())));
			}
		}

		@Override
		public void meet(MultiProjection node) throws RDFHandlerException {
			ExtensionContext oldInlineBindings = meetExtension(node.getArg());
			isMultiProjection = true;
			super.meet(node);
			isMultiProjection = false;
			isSubQuery = true;
			inlineBindings = oldInlineBindings;
		}

		@Override
		public void meet(Projection node) throws RDFHandlerException {
			ExtensionContext oldInlineBindings = meetExtension(node.getArg());
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

			ListContext ctx = newList(whereBNode);
			node.getArg().visit(this);
			endList(ctx);
			inlineBindings = oldInlineBindings;
		}

		@Override
		public void meet(ProjectionElemList node) throws RDFHandlerException {
			Resource elemListBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.RESULT_VARIABLES_PROPERTY, elemListBNode));
			ListContext ctx = newList(elemListBNode);
			super.meet(node);
			endList(ctx);
		}

		@Override
		public void meet(ProjectionElem node) throws RDFHandlerException {
			Resource res = getVar(node.getTargetName());
			listEntry(res);
			String varName = node.getSourceName();
			if(inlineBindings != null) {
				ValueExpr valueExpr = inlineBindings.getValueExpr(varName);
				if(valueExpr != null) {
					valueExpr.visit(new ExtensionVisitor());
				}
			}
		}

		@Override
		public void meet(Extension node) throws RDFHandlerException {
			if(inlineBindings != null && inlineBindings.extension == node) {
				// skip over ExtensionElem - already handled by meetExtension()
				node.getArg().visit(this);
			}
			else {
				super.meet(node);
			}
		}

		@Override
		public void meet(ExtensionElem node) throws RDFHandlerException {
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.BIND_CLASS));
			Resource var = getVar(node.getName());
			handler.handleStatement(valueFactory.createStatement(subject, SP.VARIABLE_PROPERTY, var));
			Resource expr = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.EXPRESSION_PROPERTY, expr));
			ListContext ctx = save();
			list = null;
			subject = expr;
			node.getExpr().visit(this);
			restore(ctx);
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
			predicate = null;
		}

		@Override
		public void meet(Var node) throws RDFHandlerException {
			Value value;
			if(node.isConstant()) {
				value = node.getValue();
			}
			else {
				value = getVar(node.getName());
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
			ListContext ctx = save();
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
			predicate = null;
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
		public void meet(MathExpr node) throws RDFHandlerException {
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, toValue(node.getOperator())));
			predicate = SP.ARG1_PROPERTY;
			node.getLeftArg().visit(this);
			predicate = SP.ARG2_PROPERTY;
			node.getRightArg().visit(this);
			predicate = null;
		}

		private Value toValue(MathOp op)
		{
			switch(op)
			{
				case PLUS: return SP.ADD;
				case MINUS: return SP.SUB;
				case MULTIPLY: return SP.MUL;
				case DIVIDE: return SP.DIVIDE;
			}
			throw new AssertionError("Unrecognised MathOp: "+op);
		}

		@Override
		public void meet(Group node) throws RDFHandlerException {
			// skip over GroupElem
			node.getArg().visit(this);
		}

		@Override
		public void meet(Service node) throws RDFHandlerException {
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.SERVICE_CLASS));
			predicate = SP.SERVICE_URI_PROPERTY;
			node.getServiceRef().visit(this);
			predicate = null;
			Resource elementsList = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.ELEMENTS_PROPERTY, elementsList));
			ListContext elementsCtx = newList(elementsList);
			node.getArg().visit(this);
			endList(elementsCtx);
		}

		@Override
		public void meet(BindingSetAssignment node) throws RDFHandlerException {
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.VALUES_CLASS));
			Resource bindingList = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.BINDINGS_PROPERTY, bindingList));
			ListContext bindingCtx = newList(bindingList);
			List<String> bindingVars = new ArrayList<String>();
			for(ValueExpr valueExpr : inlineBindings.getValueExprs()) {
				if(valueExpr instanceof Var) {
					bindingVars.add(((Var)valueExpr).getName());
				}
			}
			for(BindingSet bs : node.getBindingSets()) {
				listEntry();
				ListContext setCtx = newList(subject);
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
			ListContext varnameCtx = newList(varNameList);
			for(String varName : bindingVars) {
				listEntry(valueFactory.createLiteral(varName));
			}
			endList(varnameCtx);
		}


		final class ExtensionVisitor extends QueryModelVisitorBase<RDFHandlerException>
		{
			@Override
			public void meet(Count node) throws RDFHandlerException {
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.COUNT_CLASS));
				Resource oldSubject = subject;
				super.meet(node);
				handler.handleStatement(valueFactory.createStatement(oldSubject, SP.EXPRESSION_PROPERTY, subject));
				subject = oldSubject;
			}

			@Override
			public void meet(Var node) throws RDFHandlerException {
				subject = getVar(node.getName());
			}
		}
	}


	static final class ExtensionContext extends QueryModelVisitorBase<RuntimeException>
	{
		Extension extension;
		Map<String,ValueExpr> extensionExprs;

		public ValueExpr getValueExpr(String name) {
			return extensionExprs.get(name);
		}

		public Collection<ValueExpr> getValueExprs() {
			return extensionExprs.values();
		}

		@Override
		public void meet(Extension node) {
			extension = node;
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


	static final class ListContext
	{
		final Resource list;
		final Resource subject;

		ListContext(Resource list, Resource subject) {
			this.list = list;
			this.subject = subject;
		}
	}

	static final class SPINWellKnownVars implements Function<String,URI>
	{
		static final SPINWellKnownVars INSTANCE = new SPINWellKnownVars();
		static final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

		final Map<String,URI> wkvs = new HashMap<String,URI>();

		public SPINWellKnownVars() {
			wkvs.put("this", SPIN.THIS_CONTEXT_INSTANCE);
			wkvs.put("arg1", SPIN.ARG1_INSTANCE);
			wkvs.put("arg2", SPIN.ARG2_INSTANCE);
			wkvs.put("arg3", SPIN.ARG3_INSTANCE);
			wkvs.put("arg4", SPIN.ARG4_INSTANCE);
			wkvs.put("arg5", SPIN.ARG5_INSTANCE);
		}

		@Override
		public URI apply(String name) {
			URI wkv = wkvs.get(name);
			if(wkv == null && name.startsWith("arg")) {
				try {
					Integer.parseInt(name.substring("arg".length()));
					wkv = valueFactory.createURI(SPIN.NAMESPACE, name);
				}
				catch(NumberFormatException nfe) {
					// ignore - not a well-known argN variable
				}
			}
			return wkv;
		}
	}
}
