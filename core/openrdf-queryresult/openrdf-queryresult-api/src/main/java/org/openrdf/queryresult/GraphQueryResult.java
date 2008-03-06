package org.openrdf.queryresult;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.Statement;

public interface GraphQueryResult extends Iterable<Statement>, Closeable {
	
	/**
	 * Retrieves relevant namespaces from the query result.
	 * 
	 * @return a Map<String, String> object containing (prefix, namespace) pairs.
	 */
	public Map<String, String> getNamespaces();
	
	/**
	 * Returns the next statement in the result.
	 * 
	 * @return the next statement.
	 */
	public Statement nextStatement();
	
	/**
	 * Creates an iterator over the statements of this query result. Note that,
	 * unlike normal Collection classes, <em>a query result can only be iterated
	 * over once</em>.
	 */
	public Iterator<Statement> iterator();
	
	/**
	 * Checks if the result is guaranteed to contain no duplicate solutions.
	 * 
	 * @return true if the result is guaranteed to contain no duplicate
	 *         solutions, false otherwise.
	 */
	public boolean isDistinct();
	
	/**
	 * Closes the query result and frees any resources (such as open connections)
	 * it keeps hold of.
	 */
	public void close();

}
