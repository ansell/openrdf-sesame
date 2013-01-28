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
package info.aduna.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Interceptor that inserts some commonly used values into the model.
 * 
 * The inserted values are:
 *  - path, equal to request.getContextPath() (e.g. /context) - basePath, equal
 * to the fully qualified context path (e.g. http://www.example.com/context/) -
 * currentYear, equal to the current year
 * 
 * @author Herko ter Horst
 */
public class MessageHandlerInterceptor implements HandlerInterceptor {

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex)
	{
		// nop
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView mav)
	{
		HttpSession session = request.getSession();

		if (session != null) {
			Message message = (Message)session.getAttribute(Message.ATTRIBUTE_KEY);
			if (message != null && !mav.getModelMap().containsKey(Message.ATTRIBUTE_KEY)) {
				mav.addObject(Message.ATTRIBUTE_KEY, message);
			}

			boolean shouldRemove = true;
			if (mav.hasView() && mav.getView() instanceof RedirectView) {
				shouldRemove = false;
			}
			if (mav.getViewName() != null && mav.getViewName().startsWith("redirect:")) {
				shouldRemove = false;
			}

			if (shouldRemove) {
				session.removeAttribute(Message.ATTRIBUTE_KEY);
			}
		}
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception
	{
		return true;
	}

}
