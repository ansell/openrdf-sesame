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
package org.openrdf.spin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.AFN;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Coalesce;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.IRIFunction;
import org.openrdf.query.algebra.If;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsNumeric;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
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

public class SpinRenderer {
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
	private final Function<String,URI> wellKnownFunctions;

	public SpinRenderer()
	{
		this(Output.TEXT_AND_RDF);
	}

	public SpinRenderer(Output output)
	{
		this(output,
			new Function<String,URI>() {
				@Override
				public URI apply(String name) {
					return SpinWellKnownVars.INSTANCE.getURI(name);
				}
			},
			new Function<String,URI>() {
				@Override
				public URI apply(String name) {
					return SpinWellKnownFunctions.INSTANCE.getURI(name);
				}
			},
			ValueFactoryImpl.getInstance());
	}

	public SpinRenderer(Output output, Function<String,URI> wellKnownVarMapper, Function<String,URI> wellKnownFuncMapper, ValueFactory vf)
	{
		this.output = output;
		this.wellKnownVars = wellKnownVarMapper;
		this.wellKnownFunctions = wellKnownFuncMapper;
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
			SPINVisitor visitor = new AskVisitor(handler, whereBNode);
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


	class AskVisitor extends SPINVisitor
	{
		AskVisitor(RDFHandler handler, Resource list) {
			super(handler, list, null);
		}

		@Override
		public void meet(Slice node) throws RDFHandlerException {
			if(node.getParentNode() == null) { // ignore root slice
				node.getArg().visit(this);
			}
			else {
				super.meet(node);
			}
		}
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

		@Override
		public void meet(Reduced node) throws RDFHandlerException {
			if(node.getParentNode() == null) { // ignore root reduced
				node.getArg().visit(this);
			}
			else {
				super.meet(node);
			}
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
		final Map<String,ListContext> namedGraphLists = new HashMap<String,ListContext>();
		ExtensionContext inlineBindings;
		Resource list;
		Resource subject;
		URI predicate;
		ListContext namedGraphContext;
		boolean isMultiProjection;
		boolean isSubQuery;
		boolean hasGroup;

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

		void update(ListContext ctx) {
			ctx.list = list;
			ctx.subject = subject;
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
			}
			if(entry == null) {
				entry = valueFactory.createBNode();
			}
			handler.handleStatement(valueFactory.createStatement(list, RDF.FIRST, entry));
			if(entry instanceof Resource) {
				subject = (Resource) entry;
			} else {
				// in this case, actual value doesn't matter, only that it is not null
				subject = RDF.NIL;
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

		ListContext getNamedGraph(Var context) throws RDFHandlerException {
			ListContext currentCtx;
			namedGraphContext = namedGraphLists.get(context.getName());
			if(namedGraphContext == null) {
				listEntry();
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.NAMED_GRAPH_CLASS));
				predicate = SP.GRAPH_NAME_NODE_PROPERTY;
				context.visit(this);
				Resource elementsList = valueFactory.createBNode();
				handler.handleStatement(valueFactory.createStatement(subject, SP.ELEMENTS_PROPERTY, elementsList));
				currentCtx = newList(elementsList);
				namedGraphContext = save();
				namedGraphLists.put(context.getName(), namedGraphContext);
			}
			else {
				currentCtx = save();
				restore(namedGraphContext);
			}
			return currentCtx;
		}

		void restoreNamedGraph(ListContext ctx) {
			update(namedGraphContext);
			restore(ctx);
		}

		public void end() throws RDFHandlerException {
			if(list != null) {
				endList(null);
			}

			// end any named graph lists
			for(ListContext elementsCtx : namedGraphLists.values()) {
				restore(elementsCtx);
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
			ListContext ctx = startTemplateList();
			isMultiProjection = true;
			for(ProjectionElemList proj : node.getProjections()) {
				proj.visit(this);
			}
			endTemplateList(ctx);
			isMultiProjection = false;
			visitWhere(node.getArg());
			inlineBindings = oldInlineBindings;
		}

		ListContext startTemplateList() throws RDFHandlerException {
			Resource elemListBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.TEMPLATES_PROPERTY, elemListBNode));
			return newList(elemListBNode);
		}

		void endTemplateList(ListContext ctx) throws RDFHandlerException {
			endList(ctx);
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
			visitWhere(node.getArg());
			node.getArg().visit(new GroupVisitor());
			node.getArg().visit(new OrderVisitor());
			inlineBindings = oldInlineBindings;
			hasGroup = false;
		}

		private void visitWhere(TupleExpr where) throws RDFHandlerException {
			Resource whereBNode = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.WHERE_PROPERTY, whereBNode));

			isSubQuery = true; // further projection elements are for sub-queries

			ListContext ctx = newList(whereBNode);
			where.visit(this);
			endList(ctx);
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
			ValueExpr valueExpr = null;
			if(inlineBindings != null) {
				String varName = node.getSourceName();
				valueExpr = inlineBindings.getValueExpr(varName);
			}
			Resource targetVar = getVar(node.getTargetName());
			listEntry(targetVar);
			if(valueExpr != null && !(valueExpr instanceof Var)) {
				Resource currentSubj = subject;
				subject = valueFactory.createBNode();
				handler.handleStatement(valueFactory.createStatement(targetVar, SP.EXPRESSION_PROPERTY, subject));
				valueExpr.visit(new ExtensionVisitor());
				subject = currentSubj;
			}
		}

		@Override
		public void meet(Extension node) throws RDFHandlerException {
			if(inlineBindings != null && inlineBindings.extension == node) {
				// this is the first Extension node and has already been handled by meetExtension()
				// to produce inline bindings in SELECT so we can skip it here
				node.getArg().visit(this);
			}
			else {
				// any further Extension nodes produce BIND() clauses
				node.getArg().visit(this);
				for (ExtensionElem elem : node.getElements()) {
					elem.visit(this);
				}
			}
		}

		@Override
		public void meet(ExtensionElem node) throws RDFHandlerException {
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.BIND_CLASS));
			Resource var = getVar(node.getName());
			handler.handleStatement(valueFactory.createStatement(subject, SP.VARIABLE_PROPERTY, var));
			meet(node.getExpr());
		}

		private void meet(ValueExpr node) throws RDFHandlerException {
			predicate = SP.EXPRESSION_PROPERTY;
			ListContext ctx = save();
			list = null;
			node.visit(this);
			restore(ctx);
		}

		private void flushPendingStatement() throws RDFHandlerException {
			if(predicate != null) {
				Resource res = valueFactory.createBNode();
				handler.handleStatement(valueFactory.createStatement(subject, predicate, res));
				subject = res;
			}
		}

		@Override
		public void meet(StatementPattern node) throws RDFHandlerException {
			ListContext ctx;
			if(StatementPattern.Scope.NAMED_CONTEXTS == node.getScope()) {
				ctx = getNamedGraph(node.getContextVar());
			}
			else {
				ctx = null;
			}
			listEntry();
			predicate = SP.SUBJECT_PROPERTY;
			node.getSubjectVar().visit(this);
			predicate = SP.PREDICATE_PROPERTY;
			node.getPredicateVar().visit(this);
			predicate = SP.OBJECT_PROPERTY;
			node.getObjectVar().visit(this);
			predicate = null;
			if(ctx != null) {
				restoreNamedGraph(ctx);
			}
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
			hasGroup = false;
			node.getArg().visit(this);
			if(!hasGroup) {
				listEntry();
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.FILTER_CLASS));
				meet(node.getCondition());
			}
		}

		@Override
		public void meet(Compare node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, toValue(node.getOperator())));
			predicate = SP.ARG1_PROPERTY;
			node.getLeftArg().visit(this);
			predicate = SP.ARG2_PROPERTY;
			node.getRightArg().visit(this);
			subject = currentSubj;
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
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, toValue(node.getOperator())));
			predicate = SP.ARG1_PROPERTY;
			node.getLeftArg().visit(this);
			predicate = SP.ARG2_PROPERTY;
			node.getRightArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(And node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.AND));
			predicate = SP.ARG1_PROPERTY;
			node.getLeftArg().visit(this);
			predicate = SP.ARG2_PROPERTY;
			node.getRightArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(Or node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.OR));
			predicate = SP.ARG1_PROPERTY;
			node.getLeftArg().visit(this);
			predicate = SP.ARG2_PROPERTY;
			node.getRightArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(Bound node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.BOUND));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(If node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.IF));
			predicate = SP.ARG1_PROPERTY;
			node.getCondition().visit(this);
			predicate = SP.ARG2_PROPERTY;
			node.getResult().visit(this);
			predicate = SP.ARG3_PROPERTY;
			node.getAlternative().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(Coalesce node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.COALESCE));
			List<ValueExpr> args = node.getArguments();
			for(int i=0; i<args.size(); i++) {
				predicate = toArgProperty(i+1);
				args.get(i).visit(this);
			}
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(IsURI node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.IS_IRI));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(IsBNode node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.IS_BLANK));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(IsLiteral node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.IS_LITERAL));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(IsNumeric node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.IS_NUMERIC));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(Str node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.STR));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(Lang node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.LANG));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(Datatype node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.DATATYPE));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(IRIFunction node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.IRI));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(BNodeGenerator node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.BNODE));
			if(node.getNodeIdExpr() != null) {
				predicate = SP.ARG1_PROPERTY;
				node.getNodeIdExpr().visit(this);
			}
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(Regex node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.REGEX));
			predicate = SP.ARG1_PROPERTY;
			node.getLeftArg().visit(this);
			predicate = SP.ARG2_PROPERTY;
			node.getRightArg().visit(this);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(LocalName node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, AFN.LOCALNAME));
			predicate = SP.ARG1_PROPERTY;
			node.getArg().visit(this);
			subject = currentSubj;
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
		public void meet(FunctionCall node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, toValue(node)));
			List<ValueExpr> args = node.getArgs();
			for(int i=0; i<args.size(); i++) {
				predicate = toArgProperty(i+1);
				args.get(i).visit(this);
			}
			subject = currentSubj;
			predicate = null;
		}

		private Value toValue(FunctionCall node) {
			String funcName = node.getURI();
			URI funcUri = wellKnownFunctions.apply(funcName);
			if(funcUri == null) {
				funcUri = valueFactory.createURI(funcName);
			}
			return funcUri;
		}

		private URI toArgProperty(int i) {
			switch(i) {
			case 1: return SP.ARG1_PROPERTY;
			case 2: return SP.ARG2_PROPERTY;
			case 3: return SP.ARG3_PROPERTY;
			case 4: return SP.ARG4_PROPERTY;
			case 5: return SP.ARG5_PROPERTY;
			default:
				return valueFactory.createURI(SP.NAMESPACE, "arg"+i);
			}
		}

		@Override
		public void meet(Not node) throws RDFHandlerException {
			if(node.getArg() instanceof Exists) {
				super.meet(node);
			}
			else {
				Resource currentSubj = subject;
				flushPendingStatement();
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.NOT));
				predicate = SP.ARG1_PROPERTY;
				node.getArg().visit(this);
				subject = currentSubj;
				predicate = null;
			}
		}

		@Override
		public void meet(Exists node) throws RDFHandlerException {
			Resource currentSubj = subject;
			flushPendingStatement();
			Resource op = (node.getParentNode() instanceof Not) ? SP.NOT_EXISTS : SP.EXISTS;
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, op));
			Resource elementsList = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.ELEMENTS_PROPERTY, elementsList));
			ListContext elementsCtx = newList(elementsList);
			node.getSubQuery().visit(this);
			endList(elementsCtx);
			subject = currentSubj;
			predicate = null;
		}

		@Override
		public void meet(Group node) throws RDFHandlerException {
			// skip over GroupElem - leave this to the GroupVisitor later
			node.getArg().visit(this);
			hasGroup = true;
		}

		@Override
		public void meet(Order node) throws RDFHandlerException {
			// skip over OrderElem - leave this to the OrderVisitor later
			node.getArg().visit(this);
		}

		@Override
		public void meet(Slice node) throws RDFHandlerException {
			node.getArg().visit(this);
			if(node.hasLimit()) {
				handler.handleStatement(valueFactory.createStatement(subject, SP.LIMIT_PROPERTY, valueFactory.createLiteral(Long.toString(node.getLimit()), XMLSchema.INTEGER)));
			}
			if(node.hasOffset()) {
				handler.handleStatement(valueFactory.createStatement(subject, SP.OFFSET_PROPERTY, valueFactory.createLiteral(Long.toString(node.getOffset()), XMLSchema.INTEGER)));
			}
		}

		@Override
		public void meet(Distinct node) throws RDFHandlerException {
			node.getArg().visit(this);
			handler.handleStatement(valueFactory.createStatement(subject, SP.DISTINCT_PROPERTY, BooleanLiteralImpl.TRUE));
		}

		@Override
		public void meet(Reduced node) throws RDFHandlerException {
			node.getArg().visit(this);
			handler.handleStatement(valueFactory.createStatement(subject, SP.REDUCED_PROPERTY, BooleanLiteralImpl.TRUE));
		}

		@Override
		public void meet(Join node) throws RDFHandlerException {
			boolean isGroupGraphPattern = (node.getRightArg() instanceof Join);
			if(!isGroupGraphPattern) {
				super.meet(node);
			}
			else {
				listEntry();
				ListContext leftGroupCtx = newList(subject);
				node.getLeftArg().visit(this);
				endList(leftGroupCtx);
				listEntry();
				ListContext rightGroupCtx = newList(subject);
				node.getRightArg().visit(this);
				endList(rightGroupCtx);
			}
		}

		@Override
		public void meet(LeftJoin node) throws RDFHandlerException {
			node.getLeftArg().visit(this);
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.OPTIONAL_CLASS));
			Resource elementsList = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.ELEMENTS_PROPERTY, elementsList));
			ListContext elementsCtx = newList(elementsList);
			node.getRightArg().visit(this);
			endList(elementsCtx);
		}

		@Override
		public void meet(Union node) throws RDFHandlerException {
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.UNION_CLASS));
			Resource elementsList = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.ELEMENTS_PROPERTY, elementsList));
			ListContext elementsCtx = newList(elementsList);
			listEntry();
			ListContext leftCtx = newList(subject);
			node.getLeftArg().visit(this);
			endList(leftCtx);

			listEntry();
			ListContext rightCtx = newList(subject);
			node.getRightArg().visit(this);
			endList(rightCtx);
			endList(elementsCtx);
		}

		@Override
		public void meet(Difference node) throws RDFHandlerException {
			node.getLeftArg().visit(this);
			listEntry();
			handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.MINUS_CLASS));
			Resource elementsList = valueFactory.createBNode();
			handler.handleStatement(valueFactory.createStatement(subject, SP.ELEMENTS_PROPERTY, elementsList));
			ListContext elementsCtx = newList(elementsList);
			node.getRightArg().visit(this);
			endList(elementsCtx);
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
			List<String> bindingVars = new ArrayList<String>(node.getBindingNames());
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
				if(node.isDistinct()) {
					handler.handleStatement(valueFactory.createStatement(subject, SP.DISTINCT_PROPERTY, BooleanLiteralImpl.TRUE));
				}
				Resource oldSubject = subject;
				super.meet(node);
				handler.handleStatement(valueFactory.createStatement(oldSubject, SP.EXPRESSION_PROPERTY, subject));
				subject = oldSubject;
			}

			@Override
			public void meet(Max node) throws RDFHandlerException {
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.MAX_CLASS));
				if(node.isDistinct()) {
					handler.handleStatement(valueFactory.createStatement(subject, SP.DISTINCT_PROPERTY, BooleanLiteralImpl.TRUE));
				}
				Resource oldSubject = subject;
				super.meet(node);
				handler.handleStatement(valueFactory.createStatement(oldSubject, SP.EXPRESSION_PROPERTY, subject));
				subject = oldSubject;
			}

			@Override
			public void meet(Min node) throws RDFHandlerException {
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.MIN_CLASS));
				if(node.isDistinct()) {
					handler.handleStatement(valueFactory.createStatement(subject, SP.DISTINCT_PROPERTY, BooleanLiteralImpl.TRUE));
				}
				Resource oldSubject = subject;
				super.meet(node);
				handler.handleStatement(valueFactory.createStatement(oldSubject, SP.EXPRESSION_PROPERTY, subject));
				subject = oldSubject;
			}

			@Override
			public void meet(Sum node) throws RDFHandlerException {
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.SUM_CLASS));
				if(node.isDistinct()) {
					handler.handleStatement(valueFactory.createStatement(subject, SP.DISTINCT_PROPERTY, BooleanLiteralImpl.TRUE));
				}
				Resource oldSubject = subject;
				super.meet(node);
				handler.handleStatement(valueFactory.createStatement(oldSubject, SP.EXPRESSION_PROPERTY, subject));
				subject = oldSubject;
			}

			@Override
			public void meet(Avg node) throws RDFHandlerException {
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.AVG_CLASS));
				if(node.isDistinct()) {
					handler.handleStatement(valueFactory.createStatement(subject, SP.DISTINCT_PROPERTY, BooleanLiteralImpl.TRUE));
				}
				Resource oldSubject = subject;
				super.meet(node);
				handler.handleStatement(valueFactory.createStatement(oldSubject, SP.EXPRESSION_PROPERTY, subject));
				subject = oldSubject;
			}

			@Override
			public void meet(GroupConcat node) throws RDFHandlerException {
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.GROUP_CONCAT_CLASS));
				if(node.isDistinct()) {
					handler.handleStatement(valueFactory.createStatement(subject, SP.DISTINCT_PROPERTY, BooleanLiteralImpl.TRUE));
				}
				Resource oldSubject = subject;
				super.meet(node);
				handler.handleStatement(valueFactory.createStatement(oldSubject, SP.EXPRESSION_PROPERTY, subject));
				subject = oldSubject;
			}

			@Override
			public void meet(Sample node) throws RDFHandlerException {
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, SP.SAMPLE_CLASS));
				if(node.isDistinct()) {
					handler.handleStatement(valueFactory.createStatement(subject, SP.DISTINCT_PROPERTY, BooleanLiteralImpl.TRUE));
				}
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

		final class GroupVisitor extends QueryModelVisitorBase<RDFHandlerException>
		{
			Group group;

			@Override
			public void meet(Order node) throws RDFHandlerException {
				node.getArg().visit(this);
			}

			@Override
			public void meet(Extension node) throws RDFHandlerException {
				node.getArg().visit(this);
			}

			@Override
			public void meet(Group node) throws RDFHandlerException {
				group = node;
				Set<String> groupNames = node.getGroupBindingNames();
				if(!groupNames.isEmpty()) {
					Resource groupByList = valueFactory.createBNode();
					handler.handleStatement(valueFactory.createStatement(subject, SP.GROUP_BY_PROPERTY, groupByList));
					ListContext groupByCtx = newList(groupByList);
					for(String groupName : groupNames) {
						Resource var = getVar(groupName);
						listEntry(var);
					}
					endList(groupByCtx);
				}
			}

			@Override
			public void meet(Filter node) throws RDFHandlerException {
				node.getArg().visit(this);
				if(group != null) {
					Resource havingList = valueFactory.createBNode();
					handler.handleStatement(valueFactory.createStatement(subject, SP.HAVING_PROPERTY, havingList));
					ListContext havingCtx = newList(havingList);
					listEntry();
					node.getCondition().visit(SPINVisitor.this);
					endList(havingCtx);
				}
			}

			@Override
			protected void meetNode(QueryModelNode node) {
				// stop
			}
		}

		final class OrderVisitor extends QueryModelVisitorBase<RDFHandlerException>
		{
			@Override
			public void meet(Order node) throws RDFHandlerException {
				Resource orderByList = valueFactory.createBNode();
				handler.handleStatement(valueFactory.createStatement(subject, SP.ORDER_BY_PROPERTY, orderByList));
				ListContext orderByCtx = newList(orderByList);
				for(OrderElem elem : node.getElements()) {
					elem.visit(this);
				}
				endList(orderByCtx);
			}

			@Override
			public void meet(OrderElem node) throws RDFHandlerException {
				URI asc = node.isAscending() ? SP.ASC_CLASS : SP.DESC_CLASS;
				listEntry();
				handler.handleStatement(valueFactory.createStatement(subject, RDF.TYPE, asc));
				SPINVisitor.this.meet(node.getExpr());
			}

			@Override
			protected void meetNode(QueryModelNode node) {
				// stop
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
		public void meet(Order node) {
			node.getArg().visit(this);
		}

		@Override
		public void meet(Extension node) {
			extension = node;
			List<ExtensionElem> elements = node.getElements();
			// NB: preserve ExtensionElem order
			extensionExprs = new LinkedHashMap<String,ValueExpr>(elements.size());
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
		Resource list;
		Resource subject;

		ListContext(Resource list, Resource subject) {
			this.list = list;
			this.subject = subject;
		}
	}
}
