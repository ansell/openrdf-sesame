/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.app.logging.logback;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.Pattern;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.util.OptionHelper;

import org.xml.sax.Attributes;

import info.aduna.logging.LogReader;

/**
 * @author alex
 */
public class LogConfigurator extends JoranConfigurator {

	Map<String, String> logReaderClassNames = new HashMap<String, String>();

	Map<String, Appender<?>> appenders = new HashMap<String, Appender<?>>();

	String defaultAppender = null;

	public LogReader getDefaultLogReader() {
		if (defaultAppender == null) {
			if (appenders.keySet().iterator().hasNext()) {
				defaultAppender = appenders.keySet().iterator().next();
			}
		}
		return this.getLogReader(defaultAppender);
	}

	public LogReader getLogReader(String appenderName) {
		if (appenderName != null) {
			String className = logReaderClassNames.get(appenderName);
			if (className != null) {
				try {
					LogReader logReader = (LogReader)OptionHelper.instantiateByClassName(className,
							info.aduna.logging.LogReader.class, context);
					logReader.setAppender(appenders.get(appenderName));
					return logReader;
				}
				catch (Exception ex) {
					System.err.println("Could not create logreader of type " + className + " !");
					ex.printStackTrace();
				}
			}
			else {
				System.err.println("Could not find logreader for appender " + appenderName + " !");
			}
		}
		return null;
	}

	@Override
	public void addInstanceRules(RuleStore rs)
	{
		// parent rules already added
		super.addInstanceRules(rs);
		rs.addRule(new Pattern("configuration/appender/logreader"), new LogReaderAction());
	}

	public class LogReaderAction extends Action {

		String className;

		boolean def = false;

		@Override
		public void begin(InterpretationContext ec, String name, Attributes attributes)
		{
			className = attributes.getValue(CLASS_ATTRIBUTE);
			def = (attributes.getValue("default") != null)
					&& attributes.getValue("default").equalsIgnoreCase("true");
			ec.pushObject(className);
		}

		@Override
		public void end(InterpretationContext ec, String arg1)
		{
			Object o = ec.peekObject();
			if (o != className) {
				addWarn("The object on the top the of the stack is not the logreader classname pushed earlier.");
			}
			else {
				ec.popObject();
				Appender<?> appender = (Appender<?>)ec.peekObject();
				logReaderClassNames.put(appender.getName(), className);
				appenders.put(appender.getName(), appender);
				if (def) {
					defaultAppender = appender.getName();
				}
			}
		}

	}
}
