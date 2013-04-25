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
