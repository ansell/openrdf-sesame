/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryBase;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.RdbmsRuntimeException;
import org.openrdf.sail.rdbms.managers.BNodeManager;
import org.openrdf.sail.rdbms.managers.LiteralManager;
import org.openrdf.sail.rdbms.managers.PredicateManager;
import org.openrdf.sail.rdbms.managers.UriManager;
import org.openrdf.sail.rdbms.model.RdbmsBNode;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsStatement;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.IdCode;
import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Provides basic value creation both for traditional values as well as values
 * with an internal id. {@link RdbmsValue}s behaviour similar to the default
 * {@link Value} implementation with the addition that they also include an
 * internal id and a version associated with that id. The internal ids should
 * not be accessed directly, but rather either through this class or the
 * corresponding manager class.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsValueFactory extends ValueFactoryBase {
	public static int id_wait;
	@Deprecated
	public static final String NIL_LABEL = "nil";
	private ValueFactory vf;
	private BNodeManager bnodes;
	private UriManager uris;
	private LiteralManager literals;
	private PredicateManager predicates;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public void setBNodeManager(BNodeManager bnodes) {
		this.bnodes = bnodes;
	}

	public void setURIManager(UriManager uris) {
		this.uris = uris;
	}

	public void setLiteralManager(LiteralManager literals) {
		this.literals = literals;
	}

	public void setPredicateManager(PredicateManager predicates) {
		this.predicates = predicates;
	}

	public void setDelegate(ValueFactory vf) {
		this.vf = vf;
	}

	public void flush() throws RdbmsException {
		try {
			bnodes.flush();
			uris.flush();
			literals.flush();
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public RdbmsBNode createBNode(String nodeID) {
		RdbmsBNode resource = bnodes.findInCache(nodeID);
		if (resource == null) {
			try {
				BNode impl = vf.createBNode(nodeID);
				resource = bnodes.cache(new RdbmsBNode(impl));
			} catch (SQLException e) {
				throw new RdbmsRuntimeException(e);
			}
		}
		return resource;
	}

	public RdbmsLiteral createLiteral(String label) {
		return asRdbmsLiteral(vf.createLiteral(label));
	}

	public RdbmsLiteral createLiteral(String label, String language) {
		return asRdbmsLiteral(vf.createLiteral(label, language));
	}

	public RdbmsLiteral createLiteral(String label, URI datatype) {
		return asRdbmsLiteral(vf.createLiteral(label, datatype));
	}

	public RdbmsStatement createStatement(Resource subject, URI predicate,
			Value object) {
		return createStatement(subject, predicate, object, null);
	}

	public RdbmsStatement createStatement(Resource subject, URI predicate,
			Value object, Resource context) {
		RdbmsResource subj = asRdbmsResource(subject);
		RdbmsURI pred = asRdbmsURI(predicate);
		RdbmsValue obj = asRdbmsValue(object);
		RdbmsResource ctx = asRdbmsResource(context);
		return new RdbmsStatement(subj, pred, obj, ctx);
	}

	public RdbmsURI createURI(String uri) {
		RdbmsURI resource = uris.findInCache(uri);
		if (resource == null) {
			try {
				URI impl = vf.createURI(uri);
				resource = uris.cache(new RdbmsURI(impl));
			} catch (SQLException e) {
				throw new RdbmsRuntimeException(e);
			}
		}
		return resource;
	}

	public RdbmsURI createURI(String namespace, String localName) {
		return createURI(namespace + localName);
	}

	public RdbmsResource getRdbmsResource(long id, String stringValue) {
		IdCode code = IdCode.decode(id);
		if (code.isURI())
			return new RdbmsURI(id, uris.getIdVersion(), vf.createURI(stringValue));
		return new RdbmsBNode(id, bnodes.getIdVersion(), vf.createBNode(stringValue));
	}

	public RdbmsLiteral getRdbmsLiteral(long id, String label, String language,
			String datatype) {
		if (datatype == null && language == null)
			return new RdbmsLiteral(id, literals.getIdVersion(), vf.createLiteral(label));
		if (datatype == null)
			return new RdbmsLiteral(id, literals.getIdVersion(), vf.createLiteral(label, language));
		return new RdbmsLiteral(id, literals.getIdVersion(), vf.createLiteral(label, vf.createURI(datatype)));
	}

	public RdbmsResource asRdbmsResource(Resource node) {
		if (node == null)
			return null;
		if (node instanceof URI)
			return asRdbmsURI((URI) node);
		if (node instanceof RdbmsBNode) {
			try {
				return bnodes.cache((RdbmsBNode) node);
			} catch (SQLException e) {
				throw new RdbmsRuntimeException(e);
			}
		}
		return createBNode(((BNode) node).getID());
	}

	public RdbmsURI asRdbmsURI(URI uri) {
		if (uri == null)
			return null;
		if (uri instanceof RdbmsURI) {
			try {
				return uris.cache((RdbmsURI) uri);
			} catch (SQLException e) {
				throw new RdbmsRuntimeException(e);
			}
		}
		return createURI(uri.stringValue());
	}

	public RdbmsValue asRdbmsValue(Value value) {
		if (value == null)
			return null;
		if (value instanceof Literal)
			return asRdbmsLiteral((Literal) value);
		return asRdbmsResource((Resource) value);
	}

	public RdbmsLiteral asRdbmsLiteral(Literal literal) {
		try {
			if (literal instanceof RdbmsLiteral)
				return literals.cache((RdbmsLiteral) literal);
			RdbmsLiteral lit = literals.findInCache(literal);
			if (lit == null)
				lit = literals.cache(new RdbmsLiteral(literal));
			return lit;
		} catch (SQLException e) {
			throw new RdbmsRuntimeException(e);
		}
	}

	public RdbmsResource[] asRdbmsResource(Resource... contexts) {
		RdbmsResource[] ctxs = new RdbmsResource[contexts.length];
		for (int i = 0; i < ctxs.length; i++) {
			ctxs[i] = asRdbmsResource(contexts[i]);
		}
		return ctxs;
	}

	public RdbmsStatement asRdbmsStatement(Statement stmt) {
		if (stmt instanceof RdbmsStatement)
			return (RdbmsStatement) stmt;
		Resource s = stmt.getSubject();
		URI p = stmt.getPredicate();
		Value o = stmt.getObject();
		Resource c = stmt.getContext();
		return createStatement(s, p, o, c);
	}

	public long getInternalId(Value r) throws RdbmsException {
		if (r == null)
			return ValueTable.NIL_ID;
		RdbmsValue value = asRdbmsValue(r);
		long start = System.currentTimeMillis();
		try {
			if (value instanceof RdbmsURI) {
				Long id = predicates.getIdIfPredicate((RdbmsURI) value);
				if (id != null)
					return id;
				return uris.getInternalId((RdbmsURI) value);
			}
			if (value instanceof RdbmsBNode)
				return bnodes.getInternalId((RdbmsBNode) value);
			return literals.getInternalId((RdbmsLiteral) value);
		} catch (SQLException e) {
			throw new RdbmsException(e);
		} finally {
			long end = System.currentTimeMillis();
			id_wait += end - start;
		}
	}

	public long getPredicateId(RdbmsURI predicate) throws RdbmsException {
		try {
			return predicates.getIdOfPredicate(predicate);
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public Lock getIdReadLock() {
		return lock.readLock();
	}

	public Lock getIdWriteLock() {
		return lock.writeLock();
	}
}
