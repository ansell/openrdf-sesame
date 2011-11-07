/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.logging.base;

import java.util.Date;
import java.util.List;

import ch.qos.logback.core.Appender;

import info.aduna.logging.LogLevel;
import info.aduna.logging.LogReader;
import info.aduna.logging.LogRecord;

public abstract class LogReaderBase implements LogReader {
	
	public abstract boolean hasNext();

	public abstract LogRecord next();

	private int limit = 0;
	private int offset = 0;	

	private Appender<?> appender;	
		
	public final void remove() {
		throw new UnsupportedOperationException("Removing log records is not supported.");
	}
	
	public void setAppender(Appender<?> appender) {
		this.appender = appender;
	}
	
	public Appender<?> getAppender() {
		return this.appender;
	}	

	public Date getEndDate() {		
		return null;
	}

	public LogLevel getLevel() {
		return null;
	}

	public Date getStartDate() {
		return null;
	}

	public void setEndDate(Date date) {
		throw new UnsupportedOperationException("Date ranges are not supported by this LogReader implementation!");
	}

	public void setLevel(LogLevel level) {
		throw new UnsupportedOperationException("Level filter is not supported by this LogReader implementation!");
	}

	public void setStartDate(Date date) {
		throw new UnsupportedOperationException("Date ranges are not supported by this LogReader implementation!");
	}

	public boolean supportsDateRanges() {
		return false;
	}
	
	public Date getMaxDate() {		
		return null;
	}

	public Date getMinDate() {
		return null;
	}

	public boolean supportsLevelFilter() {
		return false;
	}

	public String getThread() {
		return null;
	}

	public void setThread(String threadname) {
		throw new UnsupportedOperationException("Thread filter is not supported by this LogReader implementation!");
	}

	public boolean supportsThreadFilter() {
		return false;
	}
	
	/**
	 * @return Returns the limit.
	 */
	public int getLimit() {
		return limit;
	}
	
	/**
	 * @param limit The limit to set.
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	/**
	 * @return Returns the offset.
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * @param offset The offset to set.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	public List<String> getThreadNames() {		
		return null;
	}
	
}
