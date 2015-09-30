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
package org.openrdf.sail.inferencer.fc;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.helpers.NotifyingSailWrapper;

public abstract class AbstractForwardChainingInferencer extends NotifyingSailWrapper {

	private static final IsolationLevels READ_COMMITTED = IsolationLevels.READ_COMMITTED;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AbstractForwardChainingInferencer() {
		super();
	}

	public AbstractForwardChainingInferencer(NotifyingSail baseSail) {
		super(baseSail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public IsolationLevel getDefaultIsolationLevel() {
		IsolationLevel level = super.getDefaultIsolationLevel();
		if (level.isCompatibleWith(READ_COMMITTED)) {
			return level;
		} else {
			List<IsolationLevel> supported = this.getSupportedIsolationLevels();
			return IsolationLevels.getCompatibleIsolationLevel(READ_COMMITTED, supported);
		}
	}

	@Override
	public List<IsolationLevel> getSupportedIsolationLevels() {
		List<IsolationLevel> supported = super.getSupportedIsolationLevels();
		List<IsolationLevel> levels = new ArrayList<IsolationLevel>(supported.size());
		for (IsolationLevel level : supported) {
			if (level.isCompatibleWith(READ_COMMITTED)) {
				levels.add(level);
			}
		}
		return levels;
	}
}
