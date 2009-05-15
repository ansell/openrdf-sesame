/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tracer implements InvocationHandler {

	private static Logger logger = LoggerFactory.getLogger(Tracer.class);

	private static int count;

	public static boolean isTraceEnabled() {
		try {
			return System.getProperty("org.openrdf.repository.trace") != null;
		}
		catch (SecurityException e) {
			// Thrown when not allowed to read system properties, for example
			// when running in applets
			return false;
		}
	}

	public static DataSource traceDataSource(DataSource ds) {
		try {
			String var = getVariableName(DataSource.class);
			File traceFile = File.createTempFile("sqltrace-" + Long.toHexString(System.currentTimeMillis()),
					".java");
			PrintWriter out = new PrintWriter(traceFile);
			logger.debug("Using trace file: {}", traceFile);
			return (DataSource)Tracer.trace(var, ds, DataSource.class, out);
		}
		catch (IOException e) {
			return ds;
		}
	}

	private static Object trace(String var, Object delegate, Class<?> type, PrintWriter out) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?>[] types = new Class<?>[] { type };
		return Proxy.newProxyInstance(cl, types, new Tracer(delegate, var, out));
	}

	private static String getVariableName(Class<?> type) {
		return type.getSimpleName().replaceAll("[a-z]", "").toLowerCase() + (++count);
	}

	private Object delegate;

	private String var;

	private PrintWriter out;

	public Tracer(Object delegate, String var, PrintWriter out) {
		this.delegate = delegate;
		this.var = var;
		this.out = out;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable
	{
		try {
			String returnVar = null;
			if (method.getReturnType().isInterface()) {
				returnVar = getVariableName(method.getReturnType());
			}
			if (out != null) {
				out.println(buildMethodCall(returnVar, method, args));
				out.flush();
			}
			Object result = method.invoke(delegate, args);
			Class<?> type = method.getReturnType();
			if (result != null && type.isInterface()) {
				return Tracer.trace(returnVar, result, type, out);
			}
			return result;
		}
		catch (InvocationTargetException exc) {
			String name = method.getName();
			String msg = exc.getCause().getMessage();
			String line = "/* ERROR in " + var + "." + name + ": " + msg;
			synchronized (out) {
				out.println(line);
				exc.getCause().printStackTrace(out);
				if (exc.getCause() instanceof SQLException) {
					SQLException se = (SQLException)exc.getCause();
					SQLException next = se.getNextException();
					if (next != null) {
						next.printStackTrace(out);
					}
				}
				out.println("*/");
			}
			out.flush();
			throw exc.getCause();
		}
	}

	private String buildMethodCall(String returnVar, Method method, Object[] args) {
		StringBuilder sb = new StringBuilder();
		if (method.getReturnType().isInterface()) {
			sb.append(method.getReturnType().getSimpleName()).append(" ");
			sb.append(returnVar);
			sb.append(" = ");
		}
		sb.append(var).append('.');
		sb.append(method.getName());
		sb.append("(");
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				Object arg = args[i];
				appendArg(arg, sb);
			}
		}
		sb.append(");");
		return sb.toString();
	}

	private void appendArg(Object arg, StringBuilder sb) {
		if (arg == null) {
			sb.append("null");
		}
		else if (arg instanceof String) {
			String str = (String)arg;
			sb.append("\"");
			str = str.replace("\\", "\\\\");
			str = str.replace("\n", "\\n");
			str = str.replace("\"", "\\\"");
			sb.append(str);
			sb.append("\"");
		}
		else if (arg instanceof Long) {
			sb.append(arg).append('l');
		}
		else if (arg.getClass().isArray()) {
			sb.append("new ");
			sb.append(arg.getClass().getComponentType().getSimpleName());
			sb.append("[]{");
			Object[] ar = (Object[])arg;
			for (int i = 0; i < ar.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				appendArg(ar[i], sb);
			}
			sb.append("}");
		}
		else {
			sb.append(arg);
		}
	}

}
