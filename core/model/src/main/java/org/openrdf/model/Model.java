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
package org.openrdf.model;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.model.util.ModelException;

/**
 * An RDF model, represented as a {@link java.util.Set} of {@link Statement}s
 * with predictable iteration order.
 * <p>
 * Additional utility functionality for working with Model objects is available
 * in the {@link org.openrdf.model.util.Models Models} utility class.
 * 
 * @since 2.7.0
 * @author James Leigh
 * @see org.openrdf.model.util.Models the Models utility class
 */
@SuppressWarnings("deprecation")
public interface Model extends Graph, Set<Statement>, Serializable {

	/**
	 * Returns an unmodifiable view of this model. This method provides
	 * "read-only" access to this model. Query operations on the returned model
	 * "read through" to this model, and attempts to modify the returned model,
	 * whether direct or via its iterator, result in an
	 * <tt>UnsupportedOperationException</tt>.
	 * <p>
	 * 
	 * @return an unmodifiable view of the specified set.
	 */
	public Model unmodifiable();

	/**
	 * Gets the map that contains the assigned namespaces.
	 * 
	 * @return Map of prefix to namespace
	 */
	public Set<Namespace> getNamespaces();

	/**
	 * Gets the namespace that is associated with the specified prefix, if any.
	 * 
	 * @param prefix
	 *        A namespace prefix.
	 * @return The namespace name that is associated with the specified prefix,
	 *         or {@link Optional#empty()} if there is no such namespace.
	 */
	public default Optional<Namespace> getNamespace(String prefix) {
		return getNamespaces().stream().filter(t -> t.getPrefix().equals(prefix)).findAny();
	}

	/**
	 * Sets the prefix for a namespace.
	 * 
	 * @param prefix
	 *        The new prefix.
	 * @param name
	 *        The namespace name that the prefix maps to.
	 * @return The {@link Namespace} object for the given namespace.
	 */
	public default Optional<Namespace> setNamespace(String prefix, String name) {
		Optional<Namespace> result = getNamespace(prefix);
		if (!result.isPresent() || !result.get().getName().equals(name)) {
			result = Optional.of(new SimpleNamespace(prefix, name));
			setNamespace(result.get());
		}
		return result;
	}

	/**
	 * Sets the prefix for a namespace.
	 * 
	 * @param namespace
	 *        A {@link Namespace} object to use in this Model.
	 */
	public void setNamespace(Namespace namespace);

	/**
	 * Removes a namespace declaration by removing the association between a
	 * prefix and a namespace name.
	 * 
	 * @param prefix
	 *        The namespace prefix of which the assocation with a namespace name
	 *        is to be removed.
	 * @return the previous namespace bound to the prefix or
	 *         {@link Optional#empty()}
	 */
	public Optional<Namespace> removeNamespace(String prefix);

	/**
	 * Determines if statements with the specified subject, predicate, object and
	 * (optionally) context exist in this model. The <tt>subject</tt>,
	 * <tt>predicate</tt> and <tt>object</tt> parameters can be <tt>null</tt> to
	 * indicate wildcards. The <tt>contexts</tt> parameter is a wildcard and
	 * accepts zero or more values. If no contexts are specified, statements will
	 * match disregarding their context. If one or more contexts are specified,
	 * statements with a context matching one of these will match. Note: to match
	 * statements without an associated context, specify the value <tt>null</tt>
	 * and explicitly cast it to type <tt>Resource</tt>.
	 * <p>
	 * Examples: <tt>model.contains(s1, null, null)</tt> is true if any
	 * statements in this model have subject <tt>s1</tt>,<br>
	 * <tt>model.contains(null, null, null, c1)</tt> is true if any statements in
	 * this model have context <tt>c1</tt>,<br>
	 * <tt>model.contains(null, null, null, (Resource)null)</tt> is true if any
	 * statements in this model have no associated context,<br>
	 * <tt>model.contains(null, null, null, c1, c2, c3)</tt> is true if any
	 * statements in this model have context <tt>c1</tt>, <tt>c2</tt> or
	 * <tt>c3</tt>.
	 * 
	 * @param subj
	 *        The subject of the statements to match, <tt>null</tt> to match
	 *        statements with any subject.
	 * @param pred
	 *        The predicate of the statements to match, <tt>null</tt> to match
	 *        statements with any predicate.
	 * @param obj
	 *        The object of the statements to match, <tt>null</tt> to match
	 *        statements with any object.
	 * @param contexts
	 *        The contexts of the statements to match. If no contexts are
	 *        specified, statements will match disregarding their context. If one
	 *        or more contexts are specified, statements with a context matching
	 *        one of these will match.
	 * @return <code>true</code> if statements match the specified pattern.
	 */
	public boolean contains(Resource subj, IRI pred, Value obj, Resource... contexts);

	/**
	 * Adds one or more statements to the model. This method creates a statement
	 * for each specified context and adds those to the model. If no contexts are
	 * specified, a single statement with no associated context is added. If this
	 * Model is a filtered Model then null (if context empty) values are
	 * permitted and will use the corresponding filtered values.
	 * 
	 * @param subj
	 *        The statement's subject.
	 * @param pred
	 *        The statement's predicate.
	 * @param obj
	 *        The statement's object.
	 * @param contexts
	 *        The contexts to add statements to.
	 * @throws IllegalArgumentException
	 *         If This Model cannot store the given statement, because it is
	 *         filtered out of this view.
	 * @throws UnsupportedOperationException
	 *         If this Model cannot accept any statements, because it is filtered
	 *         to the empty set.
	 */
	public boolean add(Resource subj, IRI pred, Value obj, Resource... contexts);

	/**
	 * Removes statements with the specified context exist in this model.
	 * 
	 * @param context
	 *        The context of the statements to remove.
	 * @return <code>true</code> if one or more statements have been removed.
	 */
	public boolean clear(Resource... context);

	/**
	 * Removes statements with the specified subject, predicate, object and
	 * (optionally) context exist in this model. The <tt>subject</tt>,
	 * <tt>predicate</tt> and <tt>object</tt> parameters can be <tt>null</tt> to
	 * indicate wildcards. The <tt>contexts</tt> parameter is a wildcard and
	 * accepts zero or more values. If no contexts are specified, statements will
	 * be removed disregarding their context. If one or more contexts are
	 * specified, statements with a context matching one of these will be
	 * removed. Note: to remove statements without an associated context, specify
	 * the value <tt>null</tt> and explicitly cast it to type <tt>Resource</tt>.
	 * <p>
	 * Examples: <tt>model.remove(s1, null, null)</tt> removes any statements in
	 * this model have subject <tt>s1</tt>,<br>
	 * <tt>model.remove(null, null, null, c1)</tt> removes any statements in this
	 * model have context <tt>c1</tt>,<br>
	 * <tt>model.remove(null, null, null, (Resource)null)</tt> removes any
	 * statements in this model have no associated context,<br>
	 * <tt>model.remove(null, null, null, c1, c2, c3)</tt> removes any statements
	 * in this model have context <tt>c1</tt>, <tt>c2</tt> or <tt>c3</tt>.
	 * 
	 * @param subj
	 *        The subject of the statements to remove, <tt>null</tt> to remove
	 *        statements with any subject.
	 * @param pred
	 *        The predicate of the statements to remove, <tt>null</tt> to remove
	 *        statements with any predicate.
	 * @param obj
	 *        The object of the statements to remove, <tt>null</tt> to remove
	 *        statements with any object.
	 * @param contexts
	 *        The contexts of the statements to remove. If no contexts are
	 *        specified, statements will be removed disregarding their context.
	 *        If one or more contexts are specified, statements with a context
	 *        matching one of these will be removed.
	 * @return <code>true</code> if one or more statements have been removed.
	 */
	public boolean remove(Resource subj, IRI pred, Value obj, Resource... contexts);

	// Views

	/**
	 * Returns a view of the statements with the specified subject, predicate,
	 * object and (optionally) context. The <tt>subject</tt>, <tt>predicate</tt>
	 * and <tt>object</tt> parameters can be <tt>null</tt> to indicate wildcards.
	 * The <tt>contexts</tt> parameter is a wildcard and accepts zero or more
	 * values. If no contexts are specified, statements will match disregarding
	 * their context. If one or more contexts are specified, statements with a
	 * context matching one of these will match. Note: to match statements
	 * without an associated context, specify the value <tt>null</tt> and
	 * explicitly cast it to type <tt>Resource</tt>.
	 * <p>
	 * The returned model is backed by this Model, so changes to this Model are
	 * reflected in the returned model, and vice-versa. If this Model is modified
	 * while an iteration over the returned model is in progress (except through
	 * the iterator's own <tt>remove</tt> operation), the results of the
	 * iteration are undefined. The model supports element removal, which removes
	 * the corresponding statement from this Model, via the
	 * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. The statements passed
	 * to the <tt>add</tt> and <tt>addAll</tt> operations must match the
	 * parameter pattern.
	 * <p>
	 * Examples: <tt>model.filter(s1, null, null)</tt> matches all statements
	 * that have subject <tt>s1</tt>,<br>
	 * <tt>model.filter(null, null, null, c1)</tt> matches all statements that
	 * have context <tt>c1</tt>,<br>
	 * <tt>model.filter(null, null, null, (Resource)null)</tt> matches all
	 * statements that have no associated context,<br>
	 * <tt>model.filter(null, null, null, c1, c2, c3)</tt> matches all statements
	 * that have context <tt>c1</tt>, <tt>c2</tt> or <tt>c3</tt>.
	 * 
	 * @param subj
	 *        The subject of the statements to match, <tt>null</tt> to match
	 *        statements with any subject.
	 * @param pred
	 *        The predicate of the statements to match, <tt>null</tt> to match
	 *        statements with any predicate.
	 * @param obj
	 *        The object of the statements to match, <tt>null</tt> to match
	 *        statements with any object.
	 * @param contexts
	 *        The contexts of the statements to match. If no contexts are
	 *        specified, statements will match disregarding their context. If one
	 *        or more contexts are specified, statements with a context matching
	 *        one of these will match.
	 * @return The statements that match the specified pattern.
	 */
	public Model filter(Resource subj, IRI pred, Value obj, Resource... contexts);

	/**
	 * Returns a {@link Set} view of the subjects contained in this model. The
	 * set is backed by the model, so changes to the model are reflected in the
	 * set, and vice-versa. If the model is modified while an iteration over the
	 * set is in progress (except through the iterator's own <tt>remove</tt>
	 * operation), the results of the iteration are undefined. The set supports
	 * element removal, which removes the corresponding statement from the model,
	 * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the
	 * <tt>add</tt> or <tt>addAll</tt> operations if the parameters <tt>pred</tt>
	 * or <tt>obj</tt> are null.
	 * 
	 * @return a set view of the subjects contained in this model
	 */
	public default Set<Resource> subjects() {
		Set<Resource> subjects = stream().map(st -> st.getSubject()).collect(Collectors.toSet());
		return subjects;
	};

	/**
	 * Gets the subject of the statement(s). If the model contains one or more
	 * statements, all these statements should have the same subject. A
	 * {@link ModelException} is thrown if this is not the case.
	 * 
	 * @return The subject of the matched statement(s), or
	 *         {@link Optional#empty()} if no matching statements were found.
	 * @throws ModelException
	 *         If the statements matched by the specified parameters have more
	 *         than one unique subject.
	 * @since 2.8.0
	 */
	public default Optional<Resource> subjectResource()
		throws ModelException
	{
		Set<Resource> result = stream().map(st -> st.getSubject()).distinct().limit(2).collect(
				Collectors.toSet());
		if (result.isEmpty()) {
			return Optional.empty();
		}
		else if (result.size() > 1) {
			throw new ModelException("Did not find a unique subject resource");
		}
		else {
			return Optional.of(result.iterator().next());
		}
	}

	/**
	 * Utility method that casts the return value of {@link #subjectResource()}
	 * to a IRI, or throws a ModelException if that value is not an IRI.
	 * 
	 * @return The subject of the matched statement(s), or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelException
	 *         If such an exception is thrown by {@link #subjectResource()} or if
	 *         its return value is not a IRI.
	 * @since 2.8.0
	 */
	public default Optional<IRI> subjectIRI()
		throws ModelException
	{
		Optional<Resource> subjectResource = subjectResource();
		if (subjectResource.isPresent()) {
			if (subjectResource.get() instanceof IRI) {
				return Optional.of((IRI)subjectResource.get());
			}
			else {
				throw new ModelException("Did not find a unique subject URI");
			}
		}
		else {
			return Optional.empty();
		}
	}

	/**
	 * Provided for backward-compatibility purposes only, this method executes
	 * {@link #subjectIRI} instead.
	 * 
	 * @deprecated use {@link #subjectIRI()} instead.
	 */
	@Deprecated
	public default Optional<IRI> subjectURI()
		throws ModelException
	{
		return subjectIRI();
	}

	/**
	 * Utility method that casts the return value of {@link #subjectResource()}
	 * to a BNode, or throws a ModelException if that value is not a BNode.
	 * 
	 * @return The subject of the matched statement(s), or <tt>null</tt> if no
	 *         matching statements were found.
	 * @throws ModelException
	 *         If such an exception is thrown by {@link #subjectResource()} or if
	 *         its return value is not a BNode.
	 * @since 2.8.0
	 */
	public default Optional<BNode> subjectBNode()
		throws ModelException
	{
		Optional<Resource> subjectResource = subjectResource();
		if (subjectResource.isPresent()) {
			if (subjectResource.get() instanceof BNode) {
				return Optional.of((BNode)subjectResource.get());
			}
			else {
				throw new ModelException("Did not find a unique subject URI");
			}
		}
		else {
			return Optional.empty();
		}
	}

	/**
	 * Returns a {@link Set} view of the predicates contained in this model. The
	 * set is backed by the model, so changes to the model are reflected in the
	 * set, and vice-versa. If the model is modified while an iteration over the
	 * set is in progress (except through the iterator's own <tt>remove</tt>
	 * operation), the results of the iteration are undefined. The set supports
	 * element removal, which removes the corresponding statement from the model,
	 * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the
	 * <tt>add</tt> or <tt>addAll</tt> operations if the parameters <tt>subj</tt>
	 * or <tt>obj</tt> are null.
	 * 
	 * @return a set view of the predicates contained in this model
	 */
	public default Set<IRI> predicates() {
		Set<IRI> predicates = stream().map(st -> st.getPredicate()).collect(Collectors.toSet());
		return predicates;
	};

	/**
	 * Returns a {@link Set} view of the objects contained in this model. The set
	 * is backed by the model, so changes to the model are reflected in the set,
	 * and vice-versa. If the model is modified while an iteration over the set
	 * is in progress (except through the iterator's own <tt>remove</tt>
	 * operation), the results of the iteration are undefined. The set supports
	 * element removal, which removes the corresponding statement from the model,
	 * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the
	 * <tt>add</tt> or <tt>addAll</tt> operations if the parameters <tt>subj</tt>
	 * or <tt>pred</tt> are null.
	 * 
	 * @return a set view of the objects contained in this model
	 */
	public default Set<Value> objects() {
		Set<Value> objects = stream().map(st -> st.getObject()).collect(Collectors.toSet());
		return objects;
	}

	/**
	 * Returns a {@link Set} view of the contexts contained in this model. The
	 * set is backed by the model, so changes to the model are reflected in the
	 * set, and vice-versa. If the model is modified while an iteration over the
	 * set is in progress (except through the iterator's own <tt>remove</tt>
	 * operation), the results of the iteration are undefined. The set supports
	 * element removal, which removes the corresponding statement from the model,
	 * via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the
	 * <tt>add</tt> or <tt>addAll</tt> operations if the parameters <tt>subj</tt>
	 * , <tt>pred</tt> or <tt>obj</tt> are null.
	 * 
	 * @return a set view of the contexts contained in this model
	 */
	public default Set<Resource> contexts() {
		Set<Resource> subjects = stream().map(st -> st.getContext()).collect(Collectors.toSet());
		return subjects;
	};

	/**
	 * Gets the object of the statement(s). If the model contains one or more
	 * statements, all these statements should have the same object. A
	 * {@link ModelException} is thrown if this is not the case.
	 * 
	 * @return The object of the matched statement(s), or
	 *         {@link Optional#empty()} if no matching statements were found.
	 * @throws ModelException
	 *         If the statements matched by the specified parameters have more
	 *         than one unique object.
	 */
	public default Optional<Value> objectValue()
		throws ModelException
	{
		Set<Value> result = stream().map(st -> st.getObject()).distinct().limit(2).collect(Collectors.toSet());
		if (result.isEmpty()) {
			return Optional.empty();
		}
		else if (result.size() > 1) {
			throw new ModelException("Did not find a unique object value");
		}
		else {
			return Optional.of(result.iterator().next());
		}
	};

	/**
	 * Utility method that casts the return value of {@link #objectValue()} to a
	 * Literal, or throws a ModelUtilException if that value is not a Literal.
	 * 
	 * @return The object of the matched statement(s), or
	 *         {@link Optional#empty()} if no matching statements were found.
	 * @throws ModelException
	 *         If such an exception is thrown by {@link #objectValue()} or if its
	 *         return value is not a Literal.
	 */
	public default Optional<Literal> objectLiteral()
		throws ModelException
	{
		Optional<Value> objectValue = objectValue();
		if (objectValue.isPresent()) {
			if (objectValue.get() instanceof Literal) {
				return Optional.of((Literal)objectValue.get());
			}
			else {
				throw new ModelException("Did not find a unique object literal");
			}
		}
		else {
			return Optional.empty();
		}
	}

	/**
	 * Utility method that casts the return value of {@link #objectValue()} to a
	 * Resource, or throws a ModelUtilException if that value is not a Resource.
	 * 
	 * @return The object of the matched statement(s), or
	 *         {@link Optional#empty()} if no matching statements were found.
	 * @throws ModelException
	 *         If such an exception is thrown by {@link #objectValue()} or if its
	 *         return value is not a Resource.
	 */
	public default Optional<Resource> objectResource()
		throws ModelException
	{
		Optional<Value> objectValue = objectValue();
		if (objectValue.isPresent()) {
			if (objectValue.get() instanceof Resource) {
				return Optional.of((Resource)objectValue.get());
			}
			else {
				throw new ModelException("Did not find a unique object resource");
			}
		}
		else {
			return Optional.empty();
		}
	}

	/**
	 * Utility method that casts the return value of {@link #objectValue()} to an
	 * IRI, or throws a ModelUtilException if that value is not an IRI.
	 * 
	 * @return The object of the matched statement(s), or
	 *         {@link Optional#empty()} if no matching statements were found.
	 * @throws ModelException
	 *         If such an exception is thrown by {@link #objectValue()} or if its
	 *         return value is not an IRI.
	 */
	public default Optional<IRI> objectIRI()
		throws ModelException
	{
		Optional<Value> objectValue = objectValue();
		if (objectValue.isPresent()) {
			if (objectValue.get() instanceof IRI) {
				return Optional.of((IRI)objectValue.get());
			}
			else {
				throw new ModelException("Did not find a unique object URI");
			}
		}
		else {
			return Optional.empty();
		}
	}

	/**
	 * Provided for backward-compatibility purposes only, this method executes
	 * {@link #objectIRI} instead.
	 * 
	 * @deprecated use {@link #objectIRI()} instead.
	 */
	@Deprecated
	public default Optional<IRI> objectURI()
		throws ModelException
	{
		return objectIRI();
	}

	/**
	 * Utility method that returns the string value of {@link #objectValue()}.
	 * 
	 * @return The object string value of the matched statement(s), or
	 *         {@link Optional#empty()} if no matching statements were found.
	 * @throws ModelException
	 *         If the statements matched by the specified parameters have more
	 *         than one unique object.
	 */
	public default Optional<String> objectString()
		throws ModelException
	{
		Optional<Value> objectValue = objectValue();
		if (objectValue.isPresent()) {
			return Optional.of(objectValue.get().toString());
		}
		else {
			return Optional.empty();
		}
	}

	/**
	 * Utility method that finds a single literal object in the model and returns
	 * it if it exists. If multiple literal objects exist in the model it throws
	 * a ModelException.
	 * 
	 * @return A unique literal appearing as the object of the matched
	 *         statement(s), or {@link Optional#empty()} if no matching
	 *         statements were found.
	 * @throws ModelException
	 *         If there is more than one unique object literal in the model.
	 */
	public default Optional<Literal> anObjectLiteral()
		throws ModelException
	{
		Set<Literal> result = stream().filter(st -> st.getObject() instanceof Literal).map(
				st -> (Literal)st.getObject()).distinct().limit(2).collect(Collectors.toSet());
		if (result.isEmpty()) {
			return Optional.empty();
		}
		else if (result.size() > 1) {
			throw new ModelException("Did not find a unique object literal");
		}
		else {
			return Optional.of(result.iterator().next());
		}
	}

	/**
	 * Utility method that finds a single resource object, including both URI and
	 * BNodes, in the model and returns it if it exists. If multiple resource
	 * objects exist in the model it throws a ModelException.
	 * 
	 * @return A unique resource appearing as an object of the matched
	 *         statement(s), or {@link Optional#empty()} if no matching
	 *         statements were found.
	 * @throws ModelException
	 *         If there is more than one unique object resource in the model.
	 */

	public default Optional<Resource> anObjectResource()
		throws ModelException
	{
		Set<Resource> result = stream().filter(st -> st.getObject() instanceof Resource).map(
				st -> (Resource)st.getObject()).distinct().limit(2).collect(Collectors.toSet());
		if (result.isEmpty()) {
			return Optional.empty();
		}
		else if (result.size() > 1) {
			throw new ModelException("Did not find a unique object resource");
		}
		else {
			return Optional.of(result.iterator().next());
		}
	}

	/**
	 * Utility method that finds a single IRI object in the model and returns it
	 * if it exists. If multiple IRI objects exist in the model it throws a
	 * ModelException.
	 * 
	 * @return A unique IRI appearing as an object of the matched statement(s),
	 *         or {@link Optional#empty()} if no matching statements were found.
	 * @throws ModelException
	 *         If there is more than one unique object URI in the model.
	 */
	public default Optional<IRI> anObjectURI()
		throws ModelException
	{
		Set<IRI> result = stream().filter(st -> st.getObject() instanceof IRI).map(st -> (IRI)st.getObject()).distinct().limit(
				2).collect(Collectors.toSet());

		if (result.isEmpty()) {
			return Optional.empty();
		}
		else if (result.size() > 1) {
			throw new ModelException("Did not find a unique object URI");
		}
		else {
			return Optional.of(result.iterator().next());
		}
	}
}
