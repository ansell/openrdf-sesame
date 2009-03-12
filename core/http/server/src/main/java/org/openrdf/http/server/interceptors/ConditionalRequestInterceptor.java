/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.interceptors;

import static info.aduna.net.http.EntityHeaders.LAST_MODIFIED;
import static info.aduna.net.http.GeneralHeaders.DATE;
import static info.aduna.net.http.RequestHeaders.IF_MATCH;
import static info.aduna.net.http.RequestHeaders.IF_MODIFIED_SINCE;
import static info.aduna.net.http.RequestHeaders.IF_UNMODIFIED_SINCE;
import static info.aduna.net.http.ResponseHeaders.ETAG;
import static info.aduna.net.http.ResponseHeaders.SERVER;
import static info.aduna.net.http.ResponseHeaders.VARY;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_PRECONDITION_FAILED;
import static org.openrdf.http.protocol.Protocol.IF_NONE_MATCH;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import info.aduna.net.http.GeneralHeaders;

import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.ServerHTTPException;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;

/**
 * Interceptor for conditional requests. Handles modification dates and entity
 * tags ("etag's").
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ConditionalRequestInterceptor implements HandlerInterceptor {

	/*-----------*
	 * Statics *
	 *-----------*/

	private static final String BASE = ConditionalRequestInterceptor.class.getName() + "#";

	private static final String REPOSITORY_MODIFIED_KEY = BASE + "repository-modified";

	private static final String MANAGER_MODIFIED_KEY = BASE + "manager-modified";

	private static final String NOT_SAFE_KEY = BASE + "not-safe";

	public static void managerModified(HttpServletRequest request) {
		request.setAttribute(MANAGER_MODIFIED_KEY, Boolean.TRUE);
		notSafe(request);
	}

	public static void repositoryModified(HttpServletRequest request) {
		request.setAttribute(REPOSITORY_MODIFIED_KEY, Boolean.TRUE);
		notSafe(request);
	}

	public static void notSafe(HttpServletRequest request) {
		request.setAttribute(NOT_SAFE_KEY, Boolean.TRUE);
	}

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Logger logger = LoggerFactory.getLogger(ConditionalRequestInterceptor.class);

	/** Sequential counter for more accurate Not-Modified responses. */
	private final AtomicLong managerVersion = new AtomicLong((long)(Long.MAX_VALUE * Math.random()));

	private final Map<String, Long> repositoriesLastModified = new ConcurrentHashMap<String, Long>();

	private final ConcurrentMap<String, AtomicLong> repositoriesVersion = new ConcurrentHashMap<String, AtomicLong>();

	/*-----------*
	 * Variables *
	 *-----------*/

	private String serverName;

	private RepositoryManager repositoryManager;

	private int maxCacheAge;

	private volatile long managerLastModified = System.currentTimeMillis();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ConditionalRequestInterceptor() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	public void setMaxCacheAge(int maxCacheAge) {
		this.maxCacheAge = maxCacheAge;
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws HTTPException
	{
		if (notModified(request, response)) {
			response.setStatus(SC_NOT_MODIFIED);
			// Set etags on response:
			postHandle(request, response, null, null);
			return false;
		}

		if (!precondition(request, response)) {
			response.setStatus(SC_PRECONDITION_FAILED);
			response.setDateHeader(DATE, System.currentTimeMillis() / 1000 * 1000);
			return false;
		}

		// continue processing the request
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView)
	{
		long now = System.currentTimeMillis() / 1000 * 1000;
		long lastModified = lastModified(request); // update
		String eTag = eTag(request); // update
		response.setDateHeader(DATE, now);
		if (serverName != null) {
			response.setHeader(SERVER, serverName);
		}
		if (isSafe(request) && eTag != null && 0 < lastModified && lastModified < Long.MAX_VALUE) {
			response.setDateHeader(LAST_MODIFIED, lastModified);
			response.setHeader(ETAG, eTag);
			response.setHeader(GeneralHeaders.CACHE_CONTROL, getCacheControl(now, lastModified));
		}
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception)
		throws ServerHTTPException
	{
	}

	private boolean notModified(HttpServletRequest request, HttpServletResponse response) {
		long since = request.getDateHeader(IF_MODIFIED_SINCE);
		if (since != -1) {
			response.addHeader(VARY, IF_MODIFIED_SINCE);
			if (since >= lastModified(request)) {
				return true;
			}
		}
		String etag = request.getHeader(IF_NONE_MATCH);
		if (etag != null) {
			response.addHeader(VARY, IF_NONE_MATCH);
			if (etag.equals(eTag(request))) {
				return true;
			}
		}
		return false;
	}

	private boolean precondition(HttpServletRequest request, HttpServletResponse response) {
		String etag = request.getHeader(IF_MATCH);
		if (etag != null) {
			response.addHeader(VARY, IF_MATCH);
			if (!etag.equals(eTag(request))) {
				return false;
			}
		}
		etag = request.getHeader(IF_NONE_MATCH);
		if (etag != null) {
			response.addHeader(VARY, IF_NONE_MATCH);
			if (etag.equals(eTag(request))) {
				return false;
			}
		}
		long since = request.getDateHeader(IF_UNMODIFIED_SINCE);
		if (since != -1) {
			response.addHeader(VARY, IF_UNMODIFIED_SINCE);
			if (since < lastModified(request)) {
				return false;
			}
		}
		return true;
	}

	private boolean isSafe(HttpServletRequest request) {
		return request.getAttribute(NOT_SAFE_KEY) == null;
	}

	private String getCacheControl(long now, long lastModified) {
		long age = (now - lastModified) / 1000;
		if (maxCacheAge < 1 || age < 1) {
			return "no-cache";
		}
		// Modifications naturally occur near each other
		if (age < maxCacheAge) {
			return "max-age=" + age;
		}
		return "max-age=" + maxCacheAge;
	}

	private long lastModified(HttpServletRequest request) {
		long modified = managerLastModified(request);

		String id = ProtocolUtil.getRepositoryID(request);
		if (id == null) {
			try {
				for (String i : repositoryManager.getRepositoryIDs()) {
					long repositoryModified = repositoryLastModified(i, request);
					if (modified < repositoryModified) {
						modified = repositoryModified;
					}
				}
			}
			catch (StoreConfigException e) {
				logger.error(e.toString(), e);
				return Long.MAX_VALUE;
			}
		}
		else {
			long repositoryModified = repositoryLastModified(id, request);
			if (modified < repositoryModified) {
				modified = repositoryModified;
			}
		}
		return modified;
	}

	private long managerLastModified(HttpServletRequest request) {
		if (request.getAttribute(MANAGER_MODIFIED_KEY) == null) {
			return managerLastModified;
		}
		else {
			return managerLastModified = System.currentTimeMillis() / 1000 * 1000;
		}
	}

	private long repositoryLastModified(String id, HttpServletRequest request) {
		if (request.getAttribute(REPOSITORY_MODIFIED_KEY) == null) {
			if (repositoriesLastModified.containsKey(id)) {
				return repositoriesLastModified.get(id);
			}
		}
		long now = System.currentTimeMillis() / 1000 * 1000;
		repositoriesLastModified.put(id, now);
		return now;
	}

	private String eTag(HttpServletRequest request) {
		long version = managerVersion(request);

		String id = ProtocolUtil.getRepositoryID(request);
		if (id == null) {
			try {
				for (String i : repositoryManager.getRepositoryIDs()) {
					version += repositoryVersion(i, request);
				}
			}
			catch (StoreConfigException e) {
				logger.error(e.toString(), e);
				return null;
			}
		}
		else {
			version += repositoryVersion(id, request);
		}
		return "W/\"" + Long.toHexString(version) + "\"";
	}

	private long managerVersion(HttpServletRequest request) {
		if (request.getAttribute(MANAGER_MODIFIED_KEY) == null) {
			return managerVersion.longValue();
		}
		else {
			return managerVersion.incrementAndGet();
		}
	}

	private long repositoryVersion(String id, HttpServletRequest request) {
		AtomicLong seq = repositoriesVersion.get(id);
		if (seq == null) {
			long code = (long)(Long.MAX_VALUE * Math.random());
			AtomicLong o = repositoriesVersion.putIfAbsent(id, new AtomicLong(code));
			if (o == null) {
				return code;
			}
			else {
				return o.longValue();
			}
		}
		if (request.getAttribute(REPOSITORY_MODIFIED_KEY) == null) {
			return seq.longValue();
		}
		else {
			return seq.incrementAndGet();
		}
	}
}
