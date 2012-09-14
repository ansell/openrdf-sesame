/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.lang.ref.WeakReference;
import java.util.TimerTask;


/**
 * TimerTask that keeps a weak reference to the supplied iteration and when activated, interrupts it.
 * 
 * @author Jeen Broekstra
 */
class InterruptTask<E, X extends Exception> extends TimerTask {

	private WeakReference<TimeLimitIteration<E, X>> iterationRef;
	
	public InterruptTask(TimeLimitIteration<E, X> iteration) {
		this.iterationRef = new WeakReference<TimeLimitIteration<E, X>>(iteration);
	}
	
	@Override
	public void run() {
			TimeLimitIteration<E, X> iteration = iterationRef.get();
			if (iteration != null) {
				iteration.interrupt();
			}
	}
}
