/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.HashSet;
import java.util.Set;

import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

import org.openrdf.query.BindingSet;

/**
 * An Iteration that returns the results of an Iteration (the left argument)
 * minus any results that are compatible with results of another Iteration (the right argument). 
 * This iteration effectively implements the SPARQL MINUS operator. 
 * 
 * @author Jeen
 */
public class IncompatibleBindingSetIteration<X extends Exception> extends FilterIteration<BindingSet, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Iteration<BindingSet, X> rightArg;

	private final boolean distinct;

	private boolean initialized;

	private Set<BindingSet> excludeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MinusIteration that returns the results of the left argument
	 * minus the results of the right argument. By default, duplicates are
	 * <em>not</em> filtered from the results.
	 * 
	 * @param leftArg
	 *        An Iteration containing the main set of elements.
	 * @param rightArg
	 *        An Iteration containing the set of elements that should be filtered
	 *        from the main set.
	 */
	public IncompatibleBindingSetIteration(Iteration<BindingSet, X> leftArg, Iteration<BindingSet, X> rightArg) {
		this(leftArg, rightArg, false);
	}

	/**
	 * Creates a new MinusIteration that returns the results of the left argument
	 * minus the results of the right argument.
	 * 
	 * @param leftArg
	 *        An Iteration containing the main set of elements.
	 * @param rightArg
	 *        An Iteration containing the set of elements that should be filtered
	 *        from the main set.
	 * @param distinct
	 *        Flag indicating whether duplicate elements should be filtered from
	 *        the result.
	 */
	public IncompatibleBindingSetIteration(Iteration<BindingSet, X> leftArg, Iteration<BindingSet, X> rightArg,
			boolean distinct)
	{
		super(leftArg);

		assert rightArg != null;

		this.rightArg = rightArg;
		this.distinct = distinct;
		this.initialized = false;
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	// implements LookAheadIteration.getNextElement()
	protected boolean accept(BindingSet object)
		throws X
	{
		if (!initialized) {
			// Build set of elements-to-exclude from right argument
			
			excludeSet = Iterations.addAll(rightArg, new HashSet<BindingSet>());
			initialized = true;
		}

		boolean compatible = false;
		
		for(BindingSet excluded: excludeSet) {
			
			// build set of shared variable names
			Set<String> intersection = new HashSet<String>(excluded.getBindingNames());
			intersection.retainAll(object.getBindingNames());

			if (! intersection.isEmpty()) {
				// check if shared bindings are compatible.
				// two bindingset are compatible if for every shared binding, the mapped value is equal
				boolean sharedBindingsCompatible = false;
				for (String bindingName: intersection) {
					if (excluded.getBinding(bindingName).equals(object.getBinding(bindingName))) {
						// found at least one compatible shared binding, continue checking.
						sharedBindingsCompatible = true;
					}
					else {
						// found an incompatible shared binding. BindingSet as a whole is incompatible,
						// stop checking.
						sharedBindingsCompatible = false;
						break;
					}
				}
				
				if (sharedBindingsCompatible) {
					// at least one compatible bindingset has been found in the exclude set, therefore
					// the object is compatible, therefore it should not be accepted.
					compatible = true;
					break;
				}
			}
		}

		return !compatible;
	}

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		Iterations.closeCloseable(rightArg);
	}
}
