/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.AggregateOperator;
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
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;

/**
 * @author David Huynh
 * @author Arjohn Kampman
 * @author Jeen Broekstra
 */
public class GroupIterator extends CloseableIteratorIteration<BindingSet, QueryEvaluationException> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final BindingSet parentBindings;

	private final Group group;

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean ordered = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public GroupIterator(EvaluationStrategy strategy, Group group, BindingSet parentBindings)
		throws QueryEvaluationException
	{
		this.strategy = strategy;
		this.group = group;
		this.parentBindings = parentBindings;

		// TODO figure out if the supplied group has an order imposed
		ordered = true;

		super.setIterator(createIterator());
	}

	/*---------*
	 * Methods *
	 *---------*/

	private Iterator<BindingSet> createIterator()
		throws QueryEvaluationException
	{
		Collection<BindingSet> bindingSets;
		Collection<Entry> entries;

		if (ordered) {
			bindingSets = new ArrayList<BindingSet>();
			entries = buildOrderedEntries();
		}
		else {
			bindingSets = new HashSet<BindingSet>();
			entries = buildUnorderedEntries();
		}

		for (Entry entry : entries) {
			QueryBindingSet sol = new QueryBindingSet(parentBindings);

			for (String name : group.getGroupBindingNames()) {
				Value value = entry.getPrototype().getValue(name);
				if (value != null) {
					// Potentially overwrites bindings from super
					sol.setBinding(name, value);
				}
			}

			for (GroupElem ge : group.getGroupElements()) {
				Value value = processAggregate(entry.getSolutions(), ge.getOperator());
				if (value != null) {
					// Potentially overwrites bindings from super
					sol.setBinding(ge.getName(), value);
				}
			}

			bindingSets.add(sol);
		}

		return bindingSets.iterator();
	}

	private Collection<Entry> buildOrderedEntries()
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter = strategy.evaluate(group.getArg(),
				parentBindings);

		try {
			List<Entry> orderedEntries = new ArrayList<Entry>();
			Map<Key, Entry> entries = new HashMap<Key, Entry>();

			while (iter.hasNext()) {
				BindingSet bindingSet = iter.next();
				Key key = new Key(bindingSet);
				Entry entry = entries.get(key);

				if (entry == null) {
					entry = new Entry(bindingSet);
					entries.put(key, entry);
					orderedEntries.add(entry);
				}

				entry.addSolution(bindingSet);
			}

			return orderedEntries;
		}
		finally {
			iter.close();
		}
	}

	private Collection<Entry> buildUnorderedEntries()
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> iter = strategy.evaluate(group.getArg(),
				parentBindings);

		try {
			Map<Key, Entry> entries = new HashMap<Key, Entry>();

			while (iter.hasNext()) {
				BindingSet sol = iter.next();
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

	private Value processAggregate(Collection<BindingSet> bindingSets, AggregateOperator operator)
		throws QueryEvaluationException
	{
		if (operator instanceof Count) {
			Count countOp = (Count)operator;

			ValueExpr arg = countOp.getArg();

			if (arg != null) {
				Collection<Value> values = createValueCollection(arg, bindingSets);
				return new LiteralImpl(Integer.toString(values.size()), XMLSchema.INTEGER);
			}
			else {
				return new LiteralImpl(Integer.toString(bindingSets.size()), XMLSchema.INTEGER);
			}
		}
		else if (operator instanceof Min) {
			Min minOp = (Min)operator;

			Collection<Value> values = createValueCollection(minOp.getArg(), bindingSets);

			Value result = null;

			ValueComparator comparator = new ValueComparator();

			for (Value v : values) {
				if (result == null) {
					result = v;
				}
				else if (comparator.compare(v, result) < 0) {
					result = v;
				}
			}
			return result;
		}

		else if (operator instanceof Max) {
			Max maxOp = (Max)operator;

			Collection<Value> values = createValueCollection(maxOp.getArg(), bindingSets);

			Value result = null;

			ValueComparator comparator = new ValueComparator();

			for (Value v : values) {
				if (result == null) {
					result = v;
				}
				else if (comparator.compare(v, result) > 0) {
					result = v;
				}
			}
			return result;
		}
		else if (operator instanceof Sum) {

			Sum sumOp = (Sum)operator;

			Collection<Value> values = createValueCollection(sumOp.getArg(), bindingSets);

			return calculateSum(values);

		}
		else if (operator instanceof Avg) {

			Avg avgOp = (Avg)operator;

			Collection<Value> values = createValueCollection(avgOp.getArg(), bindingSets);

			int size = values.size();
			if (size == 0) {
				return new LiteralImpl("0.0", XMLSchema.DOUBLE);
			}

			double sum = calculateSum(values).doubleValue();
			double avg = sum / size;

			return new LiteralImpl(String.valueOf(avg), XMLSchema.DOUBLE);
		}
		else if (operator instanceof Sample) {

			Sample sampleOp = (Sample)operator;

			// just get a single value and return it.
			Value value = strategy.evaluate(sampleOp.getArg(), bindingSets.iterator().next());

			return value;
		}
		else if (operator instanceof GroupConcat) {
			GroupConcat groupConcatOp = (GroupConcat)operator;
			Collection<Value> values = createValueCollection(groupConcatOp.getArg(), bindingSets);

			String separator = " ";
			ValueExpr separatorExpr = groupConcatOp.getSeparator();

			if (separatorExpr != null) {
				Value separatorValue = strategy.evaluate(separatorExpr, parentBindings);
				separator = separatorValue.stringValue();
			}

			StringBuilder concatenated = new StringBuilder();
			for (Value v : values) {
				concatenated.append(v.stringValue());
				concatenated.append(separator);
			}

			if (values.size() > 0) {
				// remove separator at the end.
				concatenated.delete(concatenated.lastIndexOf(separator), concatenated.length());
			}

			return new LiteralImpl(concatenated.toString(), XMLSchema.STRING);
		}

		return null;
	}

	private Literal calculateSum(Collection<Value> values)
		throws ValueExprEvaluationException
	{
		double sum = 0;

		// by default, the result datatype is xsd:integer.
		URI resultDatatype = XMLSchema.INTEGER;

		for (Value v : values) {
			if (v instanceof Literal) {
				Literal l = (Literal)v;
				URI datatype = l.getDatatype();

				if (datatype == null || !XMLDatatypeUtil.isNumericDatatype(datatype)) {
					throw new ValueExprEvaluationException("Not a number: " + l);
				}

				// check if the result datatype should be double, float, or decimal.
				if (datatype.equals(XMLSchema.DOUBLE)) {
					resultDatatype = XMLSchema.DOUBLE;
				}
				else if (datatype.equals(XMLSchema.FLOAT)) {
					if (!resultDatatype.equals(XMLSchema.DOUBLE)) {
						resultDatatype = XMLSchema.FLOAT;
					}
				}
				else if (datatype.equals(XMLSchema.DECIMAL)) {
					if (!(resultDatatype.equals(XMLSchema.FLOAT) || resultDatatype.equals(XMLSchema.DOUBLE))) {
						resultDatatype = XMLSchema.DECIMAL;
					}
				}

				try {
					sum += l.doubleValue();
				}
				catch (NumberFormatException e) {
					throw new ValueExprEvaluationException("Not a valid number: " + l);
				}
			}
			else {
				throw new ValueExprEvaluationException("Not a number: " + v);
			}
		}

		String sumString = String.valueOf(sum);
		if (XMLSchema.INTEGER.equals(resultDatatype)) {
			sumString = String.valueOf(((int)sum));
		}

		return new LiteralImpl(sumString, resultDatatype);
	}

	private Collection<Value> createValueCollection(ValueExpr arg, Collection<BindingSet> bindingSets)
		throws QueryEvaluationException
	{
		Collection<Value> values = null;

		if (bindingSets instanceof Set) {
			values = new HashSet<Value>();
		}
		else {
			values = new ArrayList<Value>();
		}

		for (BindingSet s : bindingSets) {
			Value value = strategy.evaluate(arg, s);
			if (value != null) {
				values.add(value);
			}
		}

		return values;
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

		private Collection<BindingSet> bindingSets;

		public Entry(BindingSet prototype) {
			this.prototype = prototype;
			if (ordered) {
				this.bindingSets = new ArrayList<BindingSet>();
			}
			else {
				this.bindingSets = new HashSet<BindingSet>();
			}

		}

		public BindingSet getPrototype() {
			return prototype;
		}

		public void addSolution(BindingSet bindingSet) {
			bindingSets.add(bindingSet);
		}

		public Collection<BindingSet> getSolutions() {
			return bindingSets;
		}
	}
}
