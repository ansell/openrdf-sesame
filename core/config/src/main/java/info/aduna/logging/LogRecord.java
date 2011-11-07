/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public interface LogRecord {
	
	public static final SimpleDateFormat ISO8601_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	
	public LogLevel getLevel();
	public Date getTime();
	public String getThreadName();
	public String getMessage();
	public List<String> getStackTrace();
}
