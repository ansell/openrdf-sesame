/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
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
