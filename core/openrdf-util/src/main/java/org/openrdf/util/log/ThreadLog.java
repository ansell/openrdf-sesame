/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Thread-based logging utility. The ThreadLog requires threads to register
 * themselves with the ThreadLog. In this registration procedure, a mapping is
 * made between the thread calling the registration method and an instance of
 * ThreadLog. This mapping is later used in all calls to logging-methods to
 * look-up whether these messages should be logged, and where they should be
 * logged.
 * <p>
 * Log messages are assigned a "level of importance". From high to low, these
 * levels are ERROR, WARNING, STATUS and TRACE. Messages can be suppressed based
 * on these levels. If a Thread registers itself with log level WARNING, only
 * errors and warning will be logged; status messages and traces will be
 * suppressed.
 */
public class ThreadLog {

	/*-----------*
	 * Constants *
	 *-----------*/

	public static final int NONE = 0;

	public static final int ERROR = 1;

	public static final int WARNING = 2;

	public static final int STATUS = 3;

	public static final int TRACE = 4;

	public static final int ALL = 5;

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/*------------------*
	 * Static variables *
	 *------------------*/

	static SimpleDateFormat _formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	static String[] _levelNames = { "NONE   ", "ERROR  ", "WARNING", "STATUS ", "TRACE  ", "ALL    " };

	/** Keeps track of which Threads maps to which ThreadLogs. */
	static InheritableThreadLocal<ThreadLog> _threadContext = new InheritableThreadLocal<ThreadLog>();

	static ThreadLog _defaultThreadLog;

	/** Maps (absolute) file paths to PrintWriters. */
	static HashMap<String, Writer> _writerTable = new HashMap<String, Writer>();

	/**
	 * set the ThreadLog to log warning and error messages to System.err by
	 * default
	 */
	static {
		setDefaultLog(null, WARNING);
	}

	/*----------------*
	 * Static methods *
	 *----------------*/

	/**
	 * Sets a default log file for all threads that have not registered
	 * themselves. Logging calls from any of these threads to ThreadLog will be
	 * logged to the specified log file if their priority is equal to or higher
	 * then the specified log level. If 'logFile' is equal to 'null', messages
	 * will be printed to System.err.
	 * 
	 * @param logFile
	 *        The file to log to, or <tt>null</tt> to log messages to
	 *        System.err.
	 * @param logLevel
	 *        One of the constants ERROR, WARNING, STATUS, TRACE, or ALL.
	 * @see #ERROR
	 * @see #WARNING
	 * @see #STATUS
	 * @see #TRACE
	 * @see #ALL
	 */
	public static void setDefaultLog(String logFile, int logLevel) {
		if (_defaultThreadLog == null) {
			_defaultThreadLog = new ThreadLog();
		}

		Writer logWriter = null;
		try {
			logWriter = _getLogWriter(logFile);
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();

			try {
				logWriter = _getLogWriter(null);
			}
			catch (IOException ignore) {
				// ignore
			}
		}

		_defaultThreadLog.setLogWriter(logWriter);
		_defaultThreadLog.setLogLev(logLevel);
	}

	/**
	 * Unsets the default log file for all threads that have not registered
	 * themselves.
	 */
	public static void unsetDefaultLog() {
		_defaultThreadLog = null;
	}

	public static int getDefaultLogLevel() {
		if (_defaultThreadLog == null) {
			_defaultThreadLog = new ThreadLog();
		}

		return _defaultThreadLog.getLogLev();
	}

	/**
	 * Registers the calling thread with the ThreadLog. Logging calls to
	 * ThreadLog will be logged to the specified log file if their priority is
	 * equal to or higher then the specified log level. If 'logFile' is equal to
	 * 'null', messages will be printed to System.err.
	 * 
	 * @param logFile
	 *        The file to log to, or <tt>null</tt> to log messages to
	 *        System.err.
	 * @param logLevel
	 *        One of the constants ERROR, WARNING, STATUS, TRACE, or ALL.
	 * @see #ERROR
	 * @see #WARNING
	 * @see #STATUS
	 * @see #TRACE
	 * @see #ALL
	 */
	public static void registerThread(String logFile, int logLevel) {
		ThreadLog threadLog = _createThreadLog();

		Writer logWriter = null;
		try {
			logWriter = _getLogWriter(logFile);
		}
		catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();

			try {
				logWriter = _getLogWriter(null);
			}
			catch (IOException ignore) {
				// ignore
			}
		}

		threadLog.setLogWriter(logWriter);
		threadLog.setLogLev(logLevel);

		Thread currentThread = Thread.currentThread();
		threadLog.setThreadName(currentThread.getName());
	}

	/**
	 * Changes the log level for the calling thread.
	 * 
	 * @param logLevel
	 *        One of the constants ERROR, WARNING, STATUS, TRACE, or ALL.
	 * @see #ERROR
	 * @see #WARNING
	 * @see #STATUS
	 * @see #TRACE
	 */
	public static void setLogLevel(int logLevel) {
		ThreadLog threadLog = null;

		synchronized (_threadContext) {
			threadLog = _threadContext.get();
		}

		if (threadLog != null) {
			threadLog.setLogLev(logLevel);
		}
	}
	
	public static int getLogLevel() {
		ThreadLog threadLog = _getThreadLog();
		
		if (threadLog != null) {
			return threadLog._logLevel;
		}
		else {
			return NONE;
		}
	}

	public static boolean logLevelEnabled(int logLevel) {
		return getLogLevel() >= logLevel;
	}
	
	/**
	 * Creates a ThreadLog for the thread calling this method. If the thread has
	 * already created a ThreadLog before, this existing ThreadLog will be
	 * returned.
	 */
	static ThreadLog _createThreadLog() {
		ThreadLog threadLog = null;

		synchronized (_threadContext) {
			threadLog = _threadContext.get();
			if (threadLog == null) {
				threadLog = new ThreadLog();
				_threadContext.set(threadLog);
			}
		}

		return threadLog;
	}

	/**
	 * Gets a (possibly shared) Writer to the specified logFile.
	 */
	static Writer _getLogWriter(String logFile)
		throws IOException
	{
		Writer logWriter = null;

		String absPath = null;
		File file = null;
		if (logFile != null) {
			file = new File(logFile);
			absPath = file.getAbsolutePath();
		}

		synchronized (_writerTable) {
			logWriter = _writerTable.get(absPath);
			if (logWriter == null) {
				// Create a new log writer
				if (absPath != null) {
					// Check if parent directory exists yet
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					logWriter = new FileWriter(absPath, true);
				}
				else {
					logWriter = new OutputStreamWriter(System.err);
				}
				_writerTable.put(absPath, logWriter);
			}
		}

		return logWriter;
	}

	/**
	 * Deregisters the calling thread with the ThreadLog. Logging calls to
	 * ThreadLog on the calling thread will no longer be logged.
	 */
	public static void deregisterThread() {
		synchronized (_threadContext) {
			_threadContext.set(null);
		}
	}

	/**
	 * Gets the ThreadLog for the thread calling this method. If no ThreadLog is
	 * available, null will be returned.
	 */
	static ThreadLog _getThreadLog() {
		ThreadLog threadLog = null;

		synchronized (_threadContext) {
			threadLog = _threadContext.get();
		}

		if (threadLog == null) {
			threadLog = _defaultThreadLog;
		}

		return threadLog;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/** Writer for the log file. */
	Writer _logWriter;

	/** Log level */
	int _logLevel;

	/**
	 * Name of the thread.
	 */
	String _threadName;

	/*--------------*
	 * Constructors *
	 *--------------*/

	ThreadLog() {
		this(null, ALL);
	}

	ThreadLog(Writer logWriter) {
		this(logWriter, ALL);
	}

	ThreadLog(Writer logWriter, int logLevel) {
		setLogWriter(logWriter);
		setLogLev(logLevel);
		_threadName = null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	void setLogWriter(Writer logWriter) {
		_logWriter = logWriter;
	}

	void setLogLev(int logLevel) {
		_logLevel = logLevel;
	}

	int getLogLev() {
		return _logLevel;
	}

	void setThreadName(String threadName) {
		_threadName = threadName;
	}

	String getThreadName() {
		return _threadName;
	}

	void doLog(String msg, Object arg, int level) {
		// First check log level
		if (_logLevel < level) {
			return;
		}

		// Create log message
		String logMsg = _createLogMessage(msg, arg, level, _threadName);

		// Write log message
		// Synchronize on _logWriter to prevent mixed lines
		try {
			synchronized (_logWriter) {
				_logWriter.write(logMsg);
				_logWriter.flush();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String _createLogMessage(String msg, Object arg, int level, String threadName) {
		StringBuilder logMsg = new StringBuilder(128);

		logMsg.append(_formatter.format(new Date()));
		if (threadName != null) {
			logMsg.append(" [");
			logMsg.append(threadName);
			logMsg.append("]");
		}
		logMsg.append(" [");
		logMsg.append(_levelNames[level]);
		logMsg.append("] ");

		logMsg.append(msg);
		if (arg != null) {
			logMsg.append(": ");

			if (arg instanceof Throwable) {
				Throwable throwable = (Throwable)arg;
				logMsg.append(throwable.getMessage());
				logMsg.append(LINE_SEPARATOR);

				java.io.StringWriter stackTrace = new java.io.StringWriter();
				java.io.PrintWriter pw = new java.io.PrintWriter(stackTrace);
				throwable.printStackTrace(pw);
				logMsg.append(stackTrace.toString());
			}
			else {
				String argString = arg.toString();
				if (argString.contains("\n") || argString.contains("\r")) {
					// argument formatted with newlines, do not append on same line
					logMsg.append(LINE_SEPARATOR);
				}
				logMsg.append(argString);
			}
		}
		logMsg.append(LINE_SEPARATOR);

		return logMsg.toString();
	}

	/*------------------------*
	 * Static utility methods *
	 *------------------------*/

	/**
	 * Logs an error.
	 * 
	 * @param msg
	 *        A indicative message for the error.
	 */
	public static void error(String msg) {
		_log(msg, null, ERROR);
	}

	/**
	 * Logs an error.
	 * 
	 * @param msg
	 *        A indicative message for the error.
	 * @param arg
	 *        An argument related to the error. In case <tt>arg</tt> is an
	 *        instance of <tt>java.lang.Throwable</tt>, the message and stack
	 *        trace of the argument will be logged.
	 */
	public static void error(String msg, Object arg) {
		_log(msg, arg, ERROR);
	}

	/**
	 * Logs a warning.
	 * 
	 * @param msg
	 *        A indicative message for the warning.
	 */
	public static void warning(String msg) {
		_log(msg, null, WARNING);
	}

	/**
	 * Logs a warning.
	 * 
	 * @param msg
	 *        A indicative message for the warning.
	 * @param arg
	 *        An argument related to the warning. In case <tt>arg</tt> is an
	 *        instance of <tt>java.lang.Throwable</tt>, the message and stack
	 *        trace of the argument will be logged.
	 */
	public static void warning(String msg, Object arg) {
		_log(msg, arg, WARNING);
	}

	/**
	 * Logs a message.
	 * 
	 * @param msg
	 *        A indicative message for the message.
	 */
	public static void log(String msg) {
		_log(msg, null, STATUS);
	}

	/**
	 * Logs a message.
	 * 
	 * @param msg
	 *        A indicative message for the message.
	 * @param arg
	 *        An argument related to the message. In case <tt>arg</tt> is an
	 *        instance of <tt>java.lang.Throwable</tt>, the message and stack
	 *        trace of the argument will be logged.
	 */
	public static void log(String msg, Object arg) {
		_log(msg, arg, STATUS);
	}

	/**
	 * Logs a trace message.
	 * 
	 * @param msg
	 *        A indicative message for the trace message.
	 */
	public static void trace(String msg) {
		_log(msg, null, TRACE);
	}

	/**
	 * Logs a trace message.
	 * 
	 * @param msg
	 *        A indicative message for the trace message.
	 * @param arg
	 *        An argument related to the trace message. In case <tt>arg</tt> is
	 *        an instance of <tt>java.lang.Throwable</tt>, the message and
	 *        stack trace of the argument will be logged.
	 */
	public static void trace(String msg, Object arg) {
		_log(msg, arg, TRACE);
	}

	/**
	 * Logs a message on the specified level.
	 * 
	 * @param msg
	 *        A indicative message for the trace message.
	 * @param arg
	 *        An argument related to the trace message. In case <tt>arg</tt> is
	 *        an instance of <tt>java.lang.Throwable</tt>, the message and
	 *        stack trace of the argument will be logged.
	 * @param level
	 *        One of the constants ERROR, WARNING, STATUS, TRACE, or ALL.
	 * @see #ERROR
	 * @see #WARNING
	 * @see #STATUS
	 * @see #TRACE
	 * @see #ALL
	 */
	protected static void _log(String msg, Object arg, int level) {
		ThreadLog threadLog = _getThreadLog();
		if (threadLog != null) {
			threadLog.doLog(msg, arg, level);
		}
	}
}
