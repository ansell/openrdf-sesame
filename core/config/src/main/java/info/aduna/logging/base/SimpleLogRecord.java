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

import info.aduna.logging.LogLevel;
import info.aduna.logging.LogRecord;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SimpleLogRecord implements LogRecord {

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
