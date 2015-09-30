/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
