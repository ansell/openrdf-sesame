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
package info.aduna.logging.base;

import java.util.Date;
import java.util.List;

import ch.qos.logback.core.Appender;

import info.aduna.logging.LogLevel;
import info.aduna.logging.LogReader;
import info.aduna.logging.LogRecord;

public abstract class AbstractLogReader implements LogReader {
	
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
