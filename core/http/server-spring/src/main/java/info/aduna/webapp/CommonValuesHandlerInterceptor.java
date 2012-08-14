/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor that inserts some commonly used values into the model.
 * 
 * The inserted values are:
 * 
 * - path, equal to request.getContextPath() (e.g. /context)
 * - basePath, equal to the fully qualified context path (e.g. http://www.example.com/context/)
 * - currentYear, equal to the current year
 * 
 * @author Herko ter Horst
 */
public class CommonValuesHandlerInterceptor implements HandlerInterceptor {

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		// nop
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mav) {
		mav.addObject("path", request.getContextPath());
		mav.addObject("basePath", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ request.getContextPath() + "/");
		mav.addObject("currentYear", Calendar.getInstance().get(Calendar.YEAR));
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

}
