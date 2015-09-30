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
package org.eclipse.rdf4j.sail;

import java.io.File;
import java.util.List;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.model.ValueFactory;

/**
 * Sail (Storage And Inference Layer) is an interface for RDF storage. RDF Sails
 * can store RDF statements and evaluate queries over them. Statements can be
 * stored in named contexts or in the null context. Contexts can be used to
 * group sets of statements that logically belong together, for example because
 * they come from the same source. Both URIs and blank nodes can be used as
 * context identifiers.
 * 
 * @author Arjohn Kampman
 */
public interface Sail {

	/**
	 * Sets the data directory for the Sail. The Sail can use this directory for
	 * storage of data, parameters, etc. This directory must be set before the
	 * Sail is {@link #initialize() initialized}.
	 * 
	 * @throws IllegalStateException
	 *         If this method is called when the Sail has already been
	 *         initialized.
	 */
	void setDataDir(File dataDir);

	/**
	 * Gets the Sail's data directory.
	 * 
	 * @see #setDataDir(File)
	 */
	File getDataDir();

	/**
	 * Initializes the Sail. Care should be taken that required initialization
	 * parameters have been set before this method is called. Please consult the
	 * specific Sail implementation for information about the relevant
	 * parameters.
	 * 
	 * @throws SailException
	 *         If the Sail could not be initialized.
	 * @throws IllegalStateException
	 *         If the Sail has already been initialized.
	 */
	void initialize()
		throws SailException;

	/**
	 * Shuts down the Sail, giving it the opportunity to synchronize any stale
	 * data. Care should be taken that all initialized Sails are being shut down
	 * before an application exits to avoid potential loss of data. Once shut
	 * down, a Sail can no longer be used until it is re-initialized.
	 * 
	 * @throws SailException
	 *         If the Sail object encountered an error or unexpected situation
	 *         internally.
	 */
	void shutDown()
		throws SailException;

	/**
	 * Checks whether this Sail object is writable, i.e. if the data contained in
	 * this Sail object can be changed.
	 */
	boolean isWritable()
		throws SailException;

	/**
	 * Opens a connection on the Sail which can be used to query and update data.
	 * Depending on how the implementation handles concurrent access, a call to
	 * this method might block when there is another open connection on this
	 * Sail.
	 * 
	 * @throws SailException
	 *         If no transaction could be started, for example because the Sail
	 *         is not writable.
	 * @throws IllegalStateException
	 *         If the Sail has not been initialized or has been shut down.
	 */
	SailConnection getConnection()
		throws SailException;

	/**
	 * Gets a ValueFactory object that can be used to create IRI-, blank node-,
	 * literal- and statement objects.
	 * 
	 * @return a ValueFactory object for this Sail object.
	 */
	ValueFactory getValueFactory();

	/**
	 * Retrieve the {@link IsolationLevel}s supported by this SAIL, ordered by
	 * increasing complexity.
	 * 
	 * @return a non-empty List of supported Isolation Levels, in order of
	 *         increasing complexity. Every SAIL supports at least one
	 *         {@link IsolationLevel}.
	 * @since 2.8.0
	 */
	List<IsolationLevel> getSupportedIsolationLevels();

	/**
	 * Retrieves the default {@link IsolationLevel} level on which transactions
	 * in this Sail operate.
	 * 
	 * @return the {@link IsolationLevel} that will be used with
	 *         {@link SailConnection#begin()}, for SAIL connections returned by
	 *         {@link #getConnection()}.
	 * @since 2.8.0
	 */
	IsolationLevel getDefaultIsolationLevel();
}
