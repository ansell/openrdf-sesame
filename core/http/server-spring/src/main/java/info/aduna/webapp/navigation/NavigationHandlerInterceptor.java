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
