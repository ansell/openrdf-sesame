package org.openrdf.repository.sail.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Defines constants for the HTTPRepository schema which is used by
 * {@link ProxyRepositoryFactory}s to initialize {@link ProxyRepository}s.
 * 
 * @author Dale Visser
 */
public class ProxyRepositorySchema {
	/**
	 * The HTTPRepository schema namespace (
	 * <tt>http://www.openrdf.org/config/repository/http#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/proxy#";

	/** <tt>http://www.openrdf.org/config/repository/proxy#proxiedID</tt> */
	public final static URI PROXIED_ID;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PROXIED_ID = factory.createURI(NAMESPACE, "proxiedID");
	}
}
