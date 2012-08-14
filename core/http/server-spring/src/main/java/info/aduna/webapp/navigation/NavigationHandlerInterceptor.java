/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.navigation;

import info.aduna.webapp.navigation.NavigationModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor that inserts the navigation model for the current Spring view
 * into the model.
 * 
 * @author Herko ter Horst
 */
public class NavigationHandlerInterceptor implements HandlerInterceptor {

	private NavigationModel navigationModel;

	public NavigationModel getNavigationModel() {
		return navigationModel;
	}

	public void setNavigationModel(NavigationModel navigationModel) {
		this.navigationModel = navigationModel;
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex)
	{
		// nop
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView mav)
	{
		NavigationModel sessionNavigationModel = (NavigationModel)request.getSession().getAttribute(
				NavigationModel.NAVIGATION_MODEL_KEY);
		if (sessionNavigationModel == null) {
			sessionNavigationModel = navigationModel;
		}

		if (mav != null && sessionNavigationModel != null) {
			mav.addObject("view", sessionNavigationModel.findView(request.getRequestURI().substring(
					request.getContextPath().length())));
		}
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		HttpSession session = request.getSession(true);
		if (session.getAttribute(NavigationModel.NAVIGATION_MODEL_KEY) == null) {
			session.setAttribute(NavigationModel.NAVIGATION_MODEL_KEY, getNavigationModel().clone());
		}

		return true;
	}

}
