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
package info.aduna.logging.file.logback;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;

import info.aduna.logging.LogLevel;
import info.aduna.logging.LogRecord;
import info.aduna.logging.base.AbstractLogReader;
import info.aduna.logging.base.SimpleLogRecord;

public class FileLogReader extends AbstractLogReader {

	private File logFile = null;

	private RandomAccessFile log = null;

	private long fileLength;

	private long byteOffset;

	private LogRecord next = null;

	private int count = 0;

	public FileLogReader() {
	}

	public FileLogReader(File logFile) {
		this.logFile = logFile;
	}

	public void setAppender(Appender<?> appender) {
		super.setAppender(appender);
		if (appender instanceof FileAppender) {
			this.logFile = new File(((FileAppender<?>)appender).getFile());
		}
		else {
			throw new RuntimeException("FileLogReader appender must be an instance of FileAppender!");
		}
		this.next = null;
	}

	public void init()
		throws Exception
	{
		if (logFile == null) {
			throw new RuntimeException("Log file is undefined for this FileLogReader!");
		}
		if (log != null) {
			log.close();
		}
		log = new RandomAccessFile(logFile, "r");
		fileLength = log.length();
		byteOffset = fileLength - 1;
		count = 0;
		next = getNext();
		if (getOffset() > 0) {
			doSkip(getOffset());
		}
	}

	private void doSkip(int offset) {
		while (this.hasNext() && (count < offset)) {
			this.next();
		}
	}

	public boolean isMoreAvailable() {
		return next != null;
	}

	public boolean hasNext() {
		if (getLimit() == 0) {
			return isMoreAvailable();
		}
		return isMoreAvailable() && (count < (getOffset() + getLimit()));
	}

	public LogRecord next() {
		LogRecord result = next;
		try {
			next = getNext();
			count++;
		}
		catch (IOException ioe) {
			throw new RuntimeException("Unable to get next log record.", ioe);
		}
		if (!hasNext()) {
			try {
				destroy();
			}
			catch (IOException e) {
				// too bad
			}
		}
		return result;
	}

	private LogRecord getNext()
		throws IOException
	{
		SimpleLogRecord result = null;

		StringBuilder message = new StringBuilder();

		List<String> stackTrace = new LinkedList<String>();

		while (result == null && byteOffset > 0) {
			List<Byte> bytesRead = new LinkedList<Byte>();
			if (byteOffset < 0) {
				System.err.println("Subzero byteOffset with: ");
				System.err.println("\tMessage: " + message);
				System.err.println("\tStacktrace: " + stackTrace.size());
			}
			// find start of previous line
			byte currentByte;
			do {
				log.seek(byteOffset--);
				currentByte = log.readByte();
				if(currentByte != '\n' && currentByte != '\r') {
					bytesRead.add(0, currentByte);
				}
			}
			while (byteOffset > 0 && currentByte != '\n' && currentByte != '\r');

			// if at start of file, retrieve the byte we just read in the do/while
			if (byteOffset < 1) {
				byteOffset = 0;
				log.seek(0);
			}

			// read the line
			byte[] lineBytes = new byte[bytesRead.size()];
			int index = 0;
			Iterator<Byte> byteIt = bytesRead.iterator();
			while(byteIt.hasNext()) {
				lineBytes[index] = byteIt.next();
				index++;
			}	
			String lastLine = new String(lineBytes, "UTF-8");

			if (lastLine != null) {
				// is this a log line?
				Matcher matcher = StackTracePatternLayout.DEFAULT_PARSER_PATTERN.matcher(lastLine);
				if (matcher.matches()) {
					try {
						LogLevel level = LogLevel.valueOf(matcher.group(1).trim());
						Date timestamp = LogRecord.ISO8601_TIMESTAMP_FORMAT.parse(matcher.group(2).trim());
						String threadName = matcher.group(3);
						message.insert(0, matcher.group(4));

						result = new SimpleLogRecord();
						result.setLevel(level);
						result.setTime(timestamp);
						result.setThreadName(threadName);
						result.setMessage(message.toString());
						result.setStackTrace(stackTrace);

						message = new StringBuilder();
						stackTrace = new ArrayList<String>();
					}
					catch (ParseException pe) {
						throw new IOException("Unable to parse timestamp in log record");
					}
				}
				// it may be a message line or a stacktrace line
				else {
					if (!lastLine.trim().equals("")) {
						if (lastLine.startsWith("\t")) {
							stackTrace.add(0, lastLine.trim());
						}
						else {
							message.insert(0, lastLine);
						}
					}
				}
			}
		}

		return result;
	}

	public void destroy()
		throws IOException
	{
		if (log != null) {
			log.close();
		}
		log = null;
	}

}
