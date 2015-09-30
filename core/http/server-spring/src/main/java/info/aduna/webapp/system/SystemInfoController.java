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
package info.aduna.webapp.system;

import info.aduna.app.AppConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class SystemInfoController implements Controller {

	private String view;

	private AppConfiguration config;

	private ServerInfo server;

	public SystemInfoController() {
		server = new ServerInfo();
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView result = new ModelAndView();
		result.setViewName(view);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("appConfig", config);
		model.put("server", server);
		model.put("memory", new MemoryInfo());
		model.put("javaProps", getJavaPropStrings());
		model.put("envVars", getEnvVarStrings());
		result.addAllObjects(model);

		return result;
	}

	public AppConfiguration getConfig() {
		return config;
	}

	public void setConfig(AppConfiguration config) {
		this.config = config;
	}

	public static class ServerInfo {
		private String os;

		private String java;

		private String user;

		public ServerInfo() {
			os = System.getProperty("os.name") + " "
					+ System.getProperty("os.version") + " ("
					+ System.getProperty("os.arch") + ")";
			java = System.getProperty("java.vendor") + " "
					+ System.getProperty("java.vm.name") + " "
					+ System.getProperty("java.version");
			user = System.getProperty("user.name");
		}

		public String getOs() {
			return os;
		}

		public String getJava() {
			return java;
		}

		public String getUser() {
			return user;
		}
	}

	public static class MemoryInfo {

		private int maximum;
		private int used;
		private float percentageInUse;

		public MemoryInfo() {
			Runtime runtime = Runtime.getRuntime();
			long usedMemory = runtime.totalMemory() - runtime.freeMemory();
			long maxMemory = runtime.maxMemory();

			// Memory usage (percentage)
			percentageInUse = (float) ((float) usedMemory / (float) maxMemory);

			// Memory usage in MB
			used = (int) (usedMemory / 1024 / 1024);
			maximum = (int) (maxMemory / 1024 / 1024);
		}

		public int getMaximum() {
			return maximum;
		}

		public int getUsed() {
			return used;
		}

		public float getPercentageInUse() {
			return percentageInUse;
		}
	}
	
	private Map getJavaPropStrings() {
		Properties sysProps = System.getProperties();
		ArrayList keyList = new ArrayList(sysProps.keySet());
		Collections.sort(keyList);
		Map result = new LinkedHashMap(keyList.size());
		Iterator<String> sysPropNames = keyList.iterator();
		while (sysPropNames.hasNext()) {
			String name = sysPropNames.next();
			if (!name.startsWith("aduna")) {
				result.put(name, sysProps.get(name));
			}
		}
		return result;
	}
	
	private Map getEnvVarStrings() {
		Map<String, String> envProps = System.getenv();
		ArrayList keyList = new ArrayList(envProps.keySet());
		Collections.sort(keyList);
		Map result = new LinkedHashMap(keyList.size());
		Iterator<String> envPropNames = keyList.iterator();
		while (envPropNames.hasNext()) {
			String name = envPropNames.next();
			result.put(name, envProps.get(name));
		}
		return result;
	}
}
