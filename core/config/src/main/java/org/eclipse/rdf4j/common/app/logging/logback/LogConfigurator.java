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
package org.eclipse.rdf4j.common.app.logging.logback;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ElementSelector;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.util.OptionHelper;

import org.eclipse.rdf4j.common.logging.LogReader;
import org.xml.sax.Attributes;

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
							org.eclipse.rdf4j.common.logging.LogReader.class, context);
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
		rs.addRule(new ElementSelector("configuration/appender/logreader"), new LogReaderAction());
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
