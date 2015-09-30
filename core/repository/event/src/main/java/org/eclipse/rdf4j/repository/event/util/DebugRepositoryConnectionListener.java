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
package org.eclipse.rdf4j.repository.event.util;

import java.io.PrintStream;
import java.util.Arrays;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.event.RepositoryConnectionListener;

/**
 * Utility class that prints all events to a PrintStream (default: System.err),
 * optionally with a stacktrace.
 * <p>
 * System.err is chosen as default because Thread.dumpStack() also prints to
 * System.err. Consequently, println's and stacktraces remain properly aligned.
 * When printing to System.out instead, environments such as Eclipse's Console
 * may mess up the order of println's and stacktraces, probably due to the use
 * of separate line buffers below the surface that get flushed to the UI at
 * different times.
 */
public class DebugRepositoryConnectionListener implements RepositoryConnectionListener {

	private boolean printing;

	private PrintStream stream;

	private boolean dumpingStack;

	public DebugRepositoryConnectionListener() {
		this(System.err);
	}

	public DebugRepositoryConnectionListener(PrintStream stream) {
		this.stream = stream;
		this.printing = stream != null;
		this.dumpingStack = false;
	}

	public boolean isPrinting() {
		return printing;
	}

	public void setPrinting(boolean printing) {
		this.printing = printing;
	}

	public PrintStream getStream() {
		return stream;
	}

	public void setStream(PrintStream stream) {
		this.stream = stream;
	}

	public boolean isDumpingStack() {
		return dumpingStack;
	}

	public void setDumpingStack(boolean dumpingStack) {
		this.dumpingStack = dumpingStack;
	}

	public void close(RepositoryConnection conn) {
		if (printing) {
			stream.println("CLOSE (" + getConnectionID(conn) + ")");
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	@Deprecated
	public void setAutoCommit(RepositoryConnection conn, boolean autoCommit) {
		if (printing) {
			stream.println("SETAUTOCOMMIT (" + getConnectionID(conn) + ") " + autoCommit);
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void commit(RepositoryConnection conn) {
		if (printing) {
			stream.println("COMMIT (" + getConnectionID(conn) + ")");
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void rollback(RepositoryConnection conn) {
		if (printing) {
			stream.println("ROLLBACK (" + getConnectionID(conn) + ")");
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void add(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
			Resource... contexts)
	{
		if (printing) {
			stream.println("ADD (" + getConnectionID(conn) + ") " + subject + ", " + predicate + ", " + object
					+ ", " + Arrays.toString(contexts));
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void remove(RepositoryConnection conn, Resource subject, IRI predicate, Value object,
			Resource... contexts)
	{
		if (printing) {
			stream.println("REMOVE (" + getConnectionID(conn) + ") " + subject + ", " + predicate + ", "
					+ object + ", " + Arrays.toString(contexts));
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void clear(RepositoryConnection conn, Resource... contexts) {
		if (printing) {
			stream.println("CLEAR (" + getConnectionID(conn) + ") " + Arrays.toString(contexts));
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void setNamespace(RepositoryConnection conn, String prefix, String name) {
		if (printing) {
			stream.println("SETNAMESPACE  (" + getConnectionID(conn) + ") " + prefix + ", " + name);
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void removeNamespace(RepositoryConnection conn, String prefix) {
		if (printing) {
			stream.println("REMOVENAMESPACE (" + getConnectionID(conn) + ") " + prefix);
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void clearNamespaces(RepositoryConnection conn) {
		if (printing) {
			stream.println("CLEARNAMESPACES (" + getConnectionID(conn) + ")");
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	protected String getConnectionID(RepositoryConnection conn) {
		String id = conn.toString();
		int length = id.length();
		int maxLength = 20;
		return length <= maxLength ? id : "..." + id.substring(length - maxLength);
	}

	public void execute(RepositoryConnection conn, QueryLanguage ql, String update, String baseURI,
			Update operation)
	{
		if (printing) {
			stream.println("EXECUTE (" + getConnectionID(conn) + ") " + update);
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}

	public void begin(RepositoryConnection conn) {
		if (printing) {
			stream.println("BEGIN (" + getConnectionID(conn) + ")");
		}
		if (dumpingStack) {
			Thread.dumpStack();
		}
	}
}
