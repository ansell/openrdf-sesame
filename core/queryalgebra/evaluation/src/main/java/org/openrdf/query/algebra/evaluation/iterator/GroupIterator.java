/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.AggregateOperator;
import org.openrdf.query.algebra.AggregateOperatorBase;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.MathExpr.MathOp;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.MathUtil;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * @author David Huynh
 * @author Arjohn Kampman
 * @author Jeen Broekstra
 * @author James Leigh
 */
public class GroupIterator extends CloseableIteratorIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final ValueFactoryImpl vf = ValueFactoryImpl.getInstance();

	private final EvaluationStrategy strategy;

	private final BindingSet parentBindings;

	private final Group group;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GroupIterator(EvaluationStrategy strategy, Group group, BindingSet parentBindings)
		throws QueryEvaluationException
	{
		this.strategy = strategy;
		this.group = group;
		this.parentBindings = parentBindings;
		super.setIterator(createIterator());
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Iterator<BindingSet> createIterator()
		throws QueryEvaluationException
	{
		Collection<Entry> entries = buildEntries();
		Collection<BindingSet> bindingSets = new LinkedList<BindingSet>();

		for (Entry entry : entries) {
			QueryBindingSet sol = new QueryBindingSet(parentBindings);

			for (String name : group.getGroupBindingNames()) {
				BindingSet prototype = entry.getPrototype();
				if (prototype != null) {
					Value value = prototype.getValue(name);
					if (value != null) {
						// Potentially overwrites bindings from super
						sol.setBinding(name, value);
					}
				}
			}

			entry.bindSolution(sol);

			bindingSets.add(sol);
		}

		return bindingSets.iterator();
	}

	private Collection<Entry> buildEntries()
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter;
		iter = strategy.evaluate(group.getArg(), parentBindings);

		try {
			Map<Key, Entry> entries = new LinkedHashMap<Key, Entry>();

			if (!iter.hasNext()) {
				// no solutions, still need to process aggregates to produce a
				// zero-result.
				entries.put(new Key(new EmptyBindingSet()), new Entry(new EmptyBindingSet()));
			}

			while (iter.hasNext()) {
				BindingSet sol;
				try {
					sol = iter.next();
				}
				catch (NoSuchElementException e) {
					break; // closed
				}
				Key key = new Key(sol);
				Entry entry = entries.get(key);

				if (entry == null) {
					entry = new Entry(sol);
					entries.put(key, entry);
				}

				entry.addSolution(sol);
			}

			return entries.values();
		}
		finally {
			iter.close();
		}

	}

	/**
	 * A unique key for a set of existing bindings.
	 * 
	 * @author David Huynh
	 */
	protected class Key {

		private BindingSet bindingSet;

		private int hash;

		public Key(BindingSet bindingSet) {
			this.bindingSet = bindingSet;

			for (String name : group.getGroupBindingNames()) {
				Value value = bindingSet.getValue(name);
				if (value != null) {
					this.hash ^= value.hashCode();
				}
			}
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Key && other.hashCode() == hash) {
				BindingSet otherSolution = ((Key)other).bindingSet;

				for (String name : group.getGroupBindingNames()) {
					Value v1 = bindingSet.getValue(name);
					Value v2 = otherSolution.getValue(name);

					if (!ObjectUtil.nullEquals(v1, v2)) {
						return false;
					}
				}

				return true;
			}

			return false;
		}
	}

	private class Entry {

		private BindingSet prototype;

		private Map<String, Aggregate> aggregates;

		public Entry(BindingSet prototype)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			this.prototype = prototype;
			this.aggregates = new LinkedHashMap<String, Aggregate>();
			for (GroupElem ge : group.getGroupElements()) {
				Aggregate create = create(ge.getOperator());
				if (create != null) {
					aggregates.put(ge.getName(), create);
				}
			}
		}

		public BindingSet getPrototype() {
			return prototype;
		}

		public void addSolution(BindingSet bindingSet)
			throws QueryEvaluationException
		{
			for (Aggregate aggregate : aggregates.values()) {
				aggregate.processAggregate(bindingSet);
			}
		}

		public void bindSolution(QueryBindingSet sol)
			throws QueryEvaluationException
		{
			for (String name : aggregates.keySet()) {
				Value value = aggregates.get(name).getValue();
				if (value != null) {
					// Potentially overwrites bindings from super
					sol.setBinding(name, value);
				}
			}
		}

		private Aggregate create(AggregateOperator operator)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			if (operator instanceof Count) {
				return new CountAggregate((Count)operator);
			}
			else if (operator instanceof Min) {
				return new MinAggregate((Min)operator);
			}
			else if (operator instanceof Max) {
				return new MaxAggregate((Max)operator);
			}
			else if (operator instanceof Sum) {
				return new SumAggregate((Sum)operator);
			}
			else if (operator instanceof Avg) {
				return new AvgAggregate((Avg)operator);
			}
			else if (operator instanceof Sample) {
				return new SampleAggregate((Sample)operator);
			}
			else if (operator instanceof GroupConcat) {
				return new ConcatAggregate((GroupConcat)operator);
			}
			return null;
		}
	}

	private abstract class Aggregate {

		private final Set<Value> distinct;

		private final ValueExpr arg;

		public Aggregate(AggregateOperatorBase operator) {
			this.arg = operator.getArg();
			if (operator.isDistinct()) {
				distinct = new HashSet<Value>();
			}
			else {
				distinct = null;
			}
		}

		public abstract Value getValue()
			throws ValueExprEvaluationException;

		public abstract void processAggregate(BindingSet bindingSet)
			throws QueryEvaluationException;

		protected boolean distinct(Value value) {
			return distinct == null || distinct.add(value);
		}

		protected ValueExpr getArg() {
			return arg;
		}

		protected Value evaluate(BindingSet s)
			throws QueryEvaluationException
		{
			try {
				return strategy.evaluate(getArg(), s);
			}
			catch (ValueExprEvaluationException e) {
				return null; // treat missing or invalid expressions as null
			}
		}
	}

	private class CountAggregate extends Aggregate {

		private long count = 0;

		public CountAggregate(Count operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (getArg() != null) {
				Value value = evaluate(s);
				if (value != null && distinct(value)) {
					count++;
				}
			}
			else {
				count++;
			}
		}

		@Override
		public Value getValue() {
			return vf.createLiteral(Long.toString(count), XMLSchema.INTEGER);
		}
	}

	private class MinAggregate extends Aggregate {

		private final ValueComparator comparator = new ValueComparator();

		private Value min = null;

		public MinAggregate(Min operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (distinct(v)) {
				if (min == null) {
					min = v;
				}
				else if (comparator.compare(v, min) < 0) {
					min = v;
				}
			}
		}

		@Override
		public Value getValue() {
			return min;
		}
	}

	private class MaxAggregate extends Aggregate {

		private final ValueComparator comparator = new ValueComparator();

		private Value max = null;

		public MaxAggregate(Max operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (distinct(v)) {
				if (max == null) {
					max = v;
				}
				else if (comparator.compare(v, max) > 0) {
					max = v;
				}
			}
		}

		@Override
		public Value getValue() {
			return max;
		}
	}

	private class SumAggregate extends Aggregate {

		private Literal sum = vf.createLiteral("0", XMLSchema.INTEGER);

		public SumAggregate(Sum operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (distinct(v)) {
				if (v instanceof Literal) {
					Literal nextLiteral = (Literal)v;
					// check if the literal is numeric, if not, skip it. This is
					// strictly speaking not spec-compliant, but a whole lot more
					// useful.
					if (nextLiteral.getDatatype() != null
							&& XMLDatatypeUtil.isNumericDatatype(nextLiteral.getDatatype()))
					{
						sum = MathUtil.compute(sum, nextLiteral, MathOp.PLUS);
					}
				}
				else {
					throw new ValueExprEvaluationException("not a number: " + v);
				}
			}
		}

		@Override
		public Value getValue() {
			return sum;
		}
	}

	private class AvgAggregate extends Aggregate {

		private long count = 0;

		private Literal sum = vf.createLiteral("0", XMLSchema.INTEGER);

		public AvgAggregate(Avg operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (distinct(v)) {
				if (v instanceof Literal) {
					Literal nextLiteral = (Literal)v;
					// check if the literal is numeric, if not, skip it. This is
					// strictly speaking not spec-compliant, but a whole lot more
					// useful.
					if (nextLiteral.getDatatype() != null
							&& XMLDatatypeUtil.isNumericDatatype(nextLiteral.getDatatype()))
					{
						sum = MathUtil.compute(sum, nextLiteral, MathOp.PLUS);
					}
					count++;
				}
				else {
					throw new ValueExprEvaluationException("not a number: " + v);
				}
			}
		}

		@Override
		public Value getValue()
			throws ValueExprEvaluationException
		{
			if (count == 0)
				return vf.createLiteral(0.0d);
			Literal sizeLit = vf.createLiteral(count);
			return MathUtil.compute(sum, sizeLit, MathOp.DIVIDE);
		}
	}

	private class SampleAggregate extends Aggregate {

		private Value sample = null;

		public SampleAggregate(Sample operator) {
			super(operator);
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			if (sample == null) {
				sample = evaluate(s);
			}
		}

		@Override
		public Value getValue() {
			return sample;
		}
	}

	private class ConcatAggregate extends Aggregate {

		private StringBuilder concatenated = new StringBuilder();

		private String separator = " ";

		public ConcatAggregate(GroupConcat groupConcatOp)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			super(groupConcatOp);
			ValueExpr separatorExpr = groupConcatOp.getSeparator();
			if (separatorExpr != null) {
				Value separatorValue = strategy.evaluate(separatorExpr, parentBindings);
				separator = separatorValue.stringValue();
			}
		}

		@Override
		public void processAggregate(BindingSet s)
			throws QueryEvaluationException
		{
			Value v = evaluate(s);
			if (v != null) {
				concatenated.append(v.stringValue());
				concatenated.append(separator);
			}
		}

		@Override
		public Value getValue() {
			if (concatenated.length() == 0)
				return vf.createLiteral("");
			// remove separator at the end.
			int len = concatenated.length() - separator.length();
			return vf.createLiteral(concatenated.substring(0, len));
		}
	}
}