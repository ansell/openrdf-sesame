/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event.util;

import java.io.PrintStream;
import java.util.Arrays;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;

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

	public void add(RepositoryConnection conn, Resource subject, URI predicate, Value object,
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

	public void remove(RepositoryConnection conn, Resource subject, URI predicate, Value object,
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
}
