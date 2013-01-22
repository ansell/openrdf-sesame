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

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import info.aduna.io.IOUtil;

/**
 * 
 * @author Herko ter Horst
 */
public class SimpleCustomResponseView implements View {

	public static final String SC_KEY = "sc";

	public static final String CONTENT_KEY = "content";

	public static final String CONTENT_LENGTH_KEY = "contentLength";

	public static final String CONTENT_TYPE_KEY = "contentType";

	private static final int DEFAULT_SC = HttpServletResponse.SC_OK;

	private static final SimpleCustomResponseView INSTANCE = new SimpleCustomResponseView();

	public static SimpleCustomResponseView getInstance() {
		return INSTANCE;
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		int sc = DEFAULT_SC;
		if (model.containsKey(SC_KEY)) {
			sc = (Integer)model.get(SC_KEY);
		}
		String contentType = (String)model.get(CONTENT_TYPE_KEY);
		Integer contentLength = (Integer)model.get(CONTENT_LENGTH_KEY);
		InputStream content = (InputStream)model.get(CONTENT_KEY);

		try {
			response.setStatus(sc);

			ServletOutputStream out = response.getOutputStream();
			if (content != null) {
				if (contentType != null) {
					response.setContentType(contentType);
				}
				if (contentLength != null) {
					response.setContentLength(contentLength);
				}
				IOUtil.transfer(content, out);
			}
			else {
				response.setContentLength(0);
			}
			out.close();
		}
		finally {
			if (content != null) {
				content.close();
			}
		}
	}
}
