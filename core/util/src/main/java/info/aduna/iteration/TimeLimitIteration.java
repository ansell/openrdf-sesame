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

import java.util.NoSuchElementException;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Arjohn Kampman
 */
public abstract class TimeLimitIteration<E, X extends Exception> extends IterationWrapper<E, X> {

	private static Timer timer = null;

	private static synchronized Timer getTimer() {
		if (timer == null) {
			timer = new Timer("TimeLimitIteration", true);
		}
		return timer;
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final InterruptTask<E, X> interruptTask;
	
	private volatile boolean isInterrupted = false;

	public TimeLimitIteration(Iteration<? extends E, ? extends X> iter, long timeLimit) {
		super(iter);

		assert timeLimit > 0 : "time limit must be a positive number, is: " + timeLimit;

		interruptTask = new InterruptTask<E, X>(this);

		getTimer().schedule(interruptTask, timeLimit);
	}

	@Override
	public boolean hasNext()
		throws X
	{
		checkInterrupted();
		try {
			boolean result = super.hasNext();
			checkInterrupted();
			return result;
		}
		catch (NoSuchElementException e) {
			checkInterrupted();
			throw e;
		}
	}

	@Override
	public E next()
		throws X
	{
		checkInterrupted();
		try {
			return super.next();
		}
		catch (NoSuchElementException e) {
			checkInterrupted();
			throw e;
		}
	}

	@Override
	public void remove()
		throws X
	{
		checkInterrupted();
		super.remove();
	}

	@Override
	protected void handleClose()
		throws X
	{
		interruptTask.cancel();
		super.handleClose();
	}

	private final void checkInterrupted()
		throws X
	{
		if (isInterrupted) {
			throwInterruptedException();
		}
	}

	protected abstract void throwInterruptedException()
		throws X;

	void interrupt() {
		isInterrupted = true;
		if (!isClosed()) {
			try {
				close();
			}
			catch (Exception e) {
				logger.warn("Failed to close iteration", e);
			}
		}
	}
}
