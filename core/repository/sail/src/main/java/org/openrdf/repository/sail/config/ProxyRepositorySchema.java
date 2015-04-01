package org.openrdf.repository.sail.config;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Defines constants for the HTTPRepository schema which is used by
 * {@link ProxyRepositoryFactory}s to initialize
 * {@link org.openrdf.repository.sail.ProxyRepository}s.
 * 
 * @author Dale Visser
 */
public class ProxyRepositorySchema {

	/**
	 * The {@link org.openrdf.repository.sail.ProxyRepository} schema namespace (
	 * <tt>http://www.openrdf.org/config/repository/proxy#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/proxy#";

	/** <tt>http://www.openrdf.org/config/repository/proxy#proxiedID</tt> */
	public final static IRI PROXIED_ID;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		PROXIED_ID = factory.createIRI(NAMESPACE, "proxiedID");
	}
}
