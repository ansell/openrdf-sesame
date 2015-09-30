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
package org.openrdf.sail;

/**
 * An interface for {@link Sail}s that notify registered
 * {@link SailChangedListener}s of changes in the data in the Sail.
 * 
 * @author James Leigh
 */
public interface NotifyingSail extends Sail {

	/**
	 * Opens a connection on the Sail which can be used to query and update data.
	 * Depending on how the implementation handles concurrent access, a call to
	 * this method might block when there is another open connection on this
	 * Sail.
	 * 
	 * @throws SailException
	 *         If no transaction could be started, for example because the Sail
	 *         is not writable.
	 */
	public NotifyingSailConnection getConnection()
		throws SailException;

	/**
	 * Adds the specified SailChangedListener to receive events when the data in
	 * this Sail object changes.
	 */
	public void addSailChangedListener(SailChangedListener listener);

	/**
	 * Removes the specified SailChangedListener so that it no longer receives
	 * events from this Sail object.
	 */
	public void removeSailChangedListener(SailChangedListener listener);
}
