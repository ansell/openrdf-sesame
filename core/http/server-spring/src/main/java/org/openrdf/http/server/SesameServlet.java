package org.openrdf.http.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import org.openrdf.http.server.controllers.ContextController;
import org.openrdf.http.server.controllers.NamespaceController;
import org.openrdf.http.server.controllers.ProtocolController;
import org.openrdf.http.server.controllers.RepositoryController;
import org.openrdf.http.server.controllers.SizeController;
import org.openrdf.http.server.controllers.StatementController;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.repository.manager.RepositoryManager;

/**
 * This class contains all the boilerplate code to initiate the Servlet.
 * 
 * @author James Leigh
 */
public class SesameServlet implements Servlet {

	private static final String APPLICATION_CONFIG_CLASS = "contextClass";

	private static final String SERVLET_NAME = "Sesame Server";

	private Servlet delegate = new DispatcherServlet();

	private RepositoryManager manager;

	public SesameServlet(RepositoryManager manager) {
		this.manager = manager;
	}

	public void destroy() {
		delegate.destroy();
		manager.shutDown();
	}

	public ServletConfig getServletConfig() {
		return delegate.getServletConfig();
	}

	public String getServletInfo() {
		return SERVLET_NAME;
	}

	public void init(ServletConfig config)
		throws ServletException
	{
		synchronized (SesameApplication.class) {
			SesameApplication.staticManager = manager;
			delegate.init(new SesameServletConfig(config));
		}
	}

	public void service(ServletRequest req, ServletResponse res)
		throws ServletException, IOException
	{
		delegate.service(req, res);
	}

	private class SesameServletConfig implements ServletConfig {

		private ServletConfig config;

		public SesameServletConfig(ServletConfig config) {
			this.config = config;
		}

		public String getInitParameter(String name) {
			if (APPLICATION_CONFIG_CLASS.equals(name))
				return SesameApplication.class.getName();
			return config.getInitParameter(name);
		}

		@SuppressWarnings("unchecked")
		public Enumeration getInitParameterNames() {
			Vector names = new Vector();
			names.add(APPLICATION_CONFIG_CLASS);
			Enumeration e = config.getInitParameterNames();
			if (e != null && e.hasMoreElements()) {
				while (e.hasMoreElements()) {
					names.add(e.nextElement());
				}
			}
			return names.elements();
		}

		public ServletContext getServletContext() {
			return config.getServletContext();
		}

		public String getServletName() {
			return config.getServletName();
		}

	}

	public static class SesameApplication extends StaticWebApplicationContext {

		static RepositoryManager staticManager;

		public SesameApplication() {
			// RepositoryManager
			registerSingleton(RepositoryManager.class, staticManager);

			// Controllers
			registerPrototype(ContextController.class);
			registerPrototype(NamespaceController.class);
			registerPrototype(ProtocolController.class);
			registerPrototype(RepositoryController.class);
			registerPrototype(SizeController.class);
			registerPrototype(StatementController.class);

			// Exceptions
			registerPrototype(ExceptionWritter.class);

			// Views
			registerPrototype(ContentNegotiator.BEAN_NAME, ContentNegotiator.class);

			// Interceptors
			RepositoryInterceptor connections = new RepositoryInterceptor();
			connections.setRepositoryManager(staticManager);
			registerSingleton(connections);

			// Spring Processors
			registerPrototype(AutowiredAnnotationBeanPostProcessor.class);
			registerPrototype(CommonAnnotationBeanPostProcessor.class);
			AnnotationMethodHandlerAdapter methods = new AnnotationMethodHandlerAdapter();
			methods.setSupportedMethods(new String[] { "HEAD", "GET", "POST", "PUT", "DELETE" });
			registerSingleton(AnnotationMethodHandlerAdapter.class, methods);
			DefaultAnnotationHandlerMapping interceptors = new DefaultAnnotationHandlerMapping();
			interceptors.setInterceptors(new Object[] { connections });
			interceptors.setApplicationContext(this);
			registerSingleton(interceptors);
		}

		private void registerPrototype(Class<?> type) {
			registerPrototype(type.getName(), type);
		}

		private void registerSingleton(Object instance) {
			registerSingleton(instance.getClass(), instance);
		}

		private void registerSingleton(Class<?> type, Object instance) {
			getDefaultListableBeanFactory().registerSingleton(type.getName(), instance);
		}
	}
}
