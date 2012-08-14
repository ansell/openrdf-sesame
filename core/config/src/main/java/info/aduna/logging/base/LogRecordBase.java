/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.logging.base;

import info.aduna.logging.LogLevel;
import info.aduna.logging.LogRecord;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class LogRecordBase implements LogRecord {

	private LogLevel level;

	private String message;

	private List<String> stackTrace;

	private String threadName;

	private Date time;

	public LogLevel getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	public List<String> getStackTrace() {
		return stackTrace;
	}

	public String getThreadName() {
		return threadName;
	}

	public Date getTime() {
		return time;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setStackTrace(List<String> stackTrace) {
		this.stackTrace = stackTrace;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(level);
		result.append(" ");
		result.append(LogRecord.ISO8601_TIMESTAMP_FORMAT.format(time));
		result.append(" (");
		result.append(threadName);
		result.append("): ");
		result.append(message);
		Iterator<String> tracerator = stackTrace.iterator();
		if(tracerator.hasNext()) {
			result.append("\n\t");
			result.append(tracerator.next());
			if(tracerator.hasNext()) {
				result.append("\n\t");
				result.append(tracerator.next());
				result.append("\n\t... "+(stackTrace.size()-2)+" more lines");
			}
		}
		return result.toString();
	}
}
