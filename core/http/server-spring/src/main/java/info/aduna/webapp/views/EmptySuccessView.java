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
package info.aduna.webapp.views;

import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

/**
 * 
 * @author Herko ter Horst
 */
public class EmptySuccessView implements View {

	private static final EmptySuccessView INSTANCE = new EmptySuccessView();
	
	public static EmptySuccessView getInstance() {
		return INSTANCE;
	}
	
	private EmptySuccessView() {}
	
	public String getContentType() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		// Indicate success with a 204 NO CONTENT response
		response.setStatus(SC_NO_CONTENT);
		response.getOutputStream().close();
	}

}
