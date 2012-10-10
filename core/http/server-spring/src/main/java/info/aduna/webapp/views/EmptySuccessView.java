/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
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
