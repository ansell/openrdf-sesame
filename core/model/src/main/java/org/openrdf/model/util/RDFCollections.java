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
package org.openrdf.model.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;

/**
 * Utilities for working with RDF Collections and converting to/from Java
 * {@link Collection} classes.
 * <P>
 * RDF Collections are represented using a Lisp-like structure: the list starts
 * with a head node, which is connected to the first collection member via the
 * {@link RDF#FIRST} relation. The head node is then connected to the rest of
 * the list via an {@link RDF#REST} relation. The last node in the list is
 * marked using the {@link RDF#NIL} node.
 * <p>
 * As an example, a list containing three literal values "A", "B", and "C" looks
 * like this as an RDF Collection:
 * 
 * <pre>
 *   (n1) -rdf:type--> rdf:List
 *     |
 *     +---rdf:first--> "A"
 *     |
 *     +---rdf:rest --> (n2) -rdf:first--> "B"
 *                        |
 *                        +---rdf:rest--> (n3) -rdf:first--> "C"
 *                                          |
 *                                          +---rdf:rest--> rdf:nil
 * </pre>
 *
 * @author Jeen Broekstra
 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF Schema
 *      1.1 section on Collection vocabulary</a>.
 */
public class RDFCollections {

	/**
	 * Converts the supplied {@link Collection} of {@link Value}s to an
	 * <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 * Collection</a>. The statements making up the new RDF Collection will be
	 * will be reported to the supplied {@link Consumer} function.
	 * 
	 * @param collection
	 *        a Java {@link Collection} of {@link Value} objects, which will be
	 *        converted to an RDF Collection.
	 * @param consumer
	 *        the {@link Consumer} function for the Statements of the RDF
	 *        Collection. May not be {@code null}.
	 * @param contexts
	 *        the context(s) in which to add the RDF Collection. This argument is
	 *        an optional vararg and can be left out.
	 */
	public static void asRDFCollection(Iterable<? extends Value> collection, Consumer<Statement> consumer,
			Resource... contexts)
	{
		asRDFCollection(collection, null, consumer, contexts);
	}

	/**
	 * Converts the supplied {@link Collection} of {@link Value}s to an
	 * <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 * Collection</a>, using the supplied {@code listStart} resource as the
	 * starting resource of the Collection. The statements making up the new RDF
	 * Collection will be reported to the supplied {@link Consumer} function.
	 * 
	 * @param collection
	 *        a Java {@link Collection} of {@link Value} objects, which will be
	 *        converted to an RDF Collection. May not be {@code null}.
	 * @param head
	 *        a {@link Resource} which will be used as the head of the list, that
	 *        is, the starting point of the created RDF Collection. May be
	 *        {@code null}, in which case a new resource is generated to
	 *        represent the list head.
	 * @param consumer
	 *        the {@link Consumer} function for the Statements of the RDF
	 *        Collection. May not be {@code null}.
	 * @param contexts
	 *        the context(s) in which to add the RDF Collection. This argument is
	 *        an optional vararg and can be left out.
	 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 *      Schema 1.1 section on Collection vocabulary</a>.
	 */
	public static void asRDFCollection(Iterable<? extends Value> collection, Resource head,
			Consumer<Statement> consumer, Resource... contexts)
	{
		Objects.requireNonNull(collection, "input collection may not be null");
		Objects.requireNonNull(consumer, "model may not be null");
		final ValueFactory vf = SimpleValueFactory.getInstance();

		Resource current = head != null ? head : vf.createBNode();

		Statements.create(vf, current, RDF.TYPE, RDF.LIST, consumer, contexts);

		Iterator<? extends Value> iter = collection.iterator();
		while (iter.hasNext()) {
			Value v = iter.next();
			Statements.create(vf, current, RDF.FIRST, v, consumer, contexts);
			if (iter.hasNext()) {
				Resource next = vf.createBNode();
				Statements.create(vf, current, RDF.REST, next, consumer, contexts);
				current = next;
			}
			else {
				Statements.create(vf, current, RDF.REST, RDF.NIL, consumer, contexts);
			}
		}
	}

	/**
	 * Converts the supplied {@link Collection} of {@link Value}s to an
	 * <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 * Collection</a>. The statements making up the new RDF Collection will be
	 * added to the supplied statement collection.
	 * 
	 * @param collection
	 *        a Java {@link Collection} of {@link Value} objects, which will be
	 *        converted to an RDF Collection. May not be {@code null}.
	 * @param rdfCollection
	 *        a {@link Collection} of {@link Statement} objects (for example a
	 *        {@link Model}) to which the RDF Collection statements will be
	 *        added. May not be {@code null}.
	 * @param contexts
	 *        the context(s) in which to add the RDF Collection. This argument is
	 *        an optional vararg and can be left out.
	 * @return the input {@link Collection} of {@link Statement}s, with the new
	 *         Statements forming the RDF {@link Collection} added.
	 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 *      Schema 1.1 section on Collection vocabulary</a>.
	 */
	public static <C extends Collection<Statement>> C asRDFCollection(Iterable<? extends Value> collection,
			C rdfCollection, Resource... contexts)
	{
		return asRDFCollection(collection, null, rdfCollection, contexts);
	}

	/**
	 * Converts the supplied {@link Collection} of {@link Value}s to an
	 * <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 * Collection</a>, using the supplied {@code listStart} resource as the
	 * starting resource of the Collection. The statements making up the new RDF
	 * Collection will be added to the supplied statement collection.
	 * 
	 * @param collection
	 *        a Java {@link Collection} of {@link Value} objects, which will be
	 *        converted to an RDF Collection. May not be {@code null}.
	 * @param head
	 *        a {@link Resource} which will be used as the head of the list, that
	 *        is, the starting point of the created RDF Collection. May be
	 *        {@code null}, in which case a new resource is generated to
	 *        represent the list head.
	 * @param rdfCollection
	 *        a {@link Collection} of {@link Statement} objects (for example a
	 *        {@link Model}) to which the RDF Collection statements will be
	 *        added. May not be {@code null}.
	 * @param contexts
	 *        the context(s) in which to add the RDF Collection. This argument is
	 *        an optional vararg and can be left out.
	 * @return the input {@link Collection} of {@link Statement}s, with the new
	 *         Statements forming the RDF {@link Collection} added.
	 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 *      Schema 1.1 section on Collection vocabulary</a>.
	 */
	public static <C extends Collection<Statement>> C asRDFCollection(Iterable<? extends Value> collection,
			Resource head, C rdfCollection, Resource... contexts)
	{
		Objects.requireNonNull(rdfCollection);
		asRDFCollection(collection, head, st -> rdfCollection.add(st), contexts);
		return rdfCollection;
	}

	/**
	 * Reads an RDF Collection from the supplied {@link Model} and converts it to
	 * a Java {@link Collection} of {@link Value}s. If the model contains more
	 * than one RDF Collection, any one Collection is read and returned. This
	 * method expects the RDF Collection to be well-formed. If the collection is
	 * not well-formed the results depend on the nature of the
	 * non-wellformedness: the method may return part of the collection, or may
	 * throw a {@link ModelException}.
	 * 
	 * @param m
	 *        the Model containing the collection to read.
	 * @param collection
	 *        the Java {@link Collection} to add the collection items to.
	 * @param contexts
	 *        the context(s) from which to read the RDF Collection. This argument
	 *        is an optional vararg and can be left out.
	 * @return the supplied Java {@link Collection}, filled with the items from
	 *         the RDF Collection (if any).
	 * @throws ModelException
	 *         if a problem occurs reading the RDF Collection, for example if the
	 *         Collection is not well-formed.
	 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 *      Schema 1.1 section on Collection vocabulary</a>.
	 */
	public static <C extends Collection<Value>> C readCollection(Model m, C collection, Resource... contexts)
		throws ModelException
	{
		return readCollection(m, null, collection, contexts);
	}

	/**
	 * Reads an RDF Collection from the supplied {@link Model} and sends each
	 * collection member to the supplied {@link Consumer} function. If the model
	 * contains more than one RDF Collection, any one Collection is read and
	 * returned. This method expects the RDF Collection to be well-formed. If the
	 * collection is not well-formed the results depend on the nature of the
	 * non-wellformedness: the method may report only part of the collection, or
	 * may throw a {@link ModelException}.
	 * 
	 * @param m
	 *        the Model containing the collection to read.
	 * @param consumer
	 *        the Java {@link Consumer} function to which the collection items
	 *        are reported.
	 * @param contexts
	 *        the context(s) from which to read the RDF Collection. This argument
	 *        is an optional vararg and can be left out.
	 * @throws ModelException
	 *         if a problem occurs reading the RDF Collection, for example if the
	 *         Collection is not well-formed.
	 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 *      Schema 1.1 section on Collection vocabulary</a>.
	 */
	public static void readCollection(final Model m, Consumer<Value> consumer, Resource... contexts)
		throws ModelException
	{
		readCollection(m, null, consumer, contexts);
	}

	/**
	 * Reads an RDF Collection starting with the supplied list head from the
	 * supplied {@link Model} and sends each collection member to the supplied
	 * {@link Consumer} function. This method expects the RDF Collection to be
	 * well-formed. If the collection is not well-formed the results depend on
	 * the nature of the non-wellformedness: the method may report only part of
	 * the collection, or may throw a {@link ModelException}.
	 * 
	 * @param m
	 *        the Model containing the collection to read.
	 * @param head
	 *        the {@link Resource} that represents the list head, that is the
	 *        start resource of the RDF Collection to be read. May be
	 *        {@code null} in which case the method attempts to find a list head.
	 * @param consumer
	 *        the Java {@link Consumer} function to which the collection items
	 *        are reported.
	 * @param contexts
	 *        the context(s) from which to read the RDF Collection. This argument
	 *        is an optional vararg and can be left out.
	 * @throws ModelException
	 *         if a problem occurs reading the RDF Collection, for example if the
	 *         Collection is not well-formed.
	 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 *      Schema 1.1 section on Collection vocabulary</a>.
	 */
	public static void readCollection(final Model m, Resource head, Consumer<Value> consumer,
			Resource... contexts)
				throws ModelException
	{
		Objects.requireNonNull(consumer, "consumer may not be null");
		Objects.requireNonNull(m, "input model may not be null");

		if (head == null) {
			Optional<Statement> startStatement = m.filter(null, RDF.FIRST, null, contexts).stream().filter(
					st -> {
						return !m.contains(null, RDF.REST, st.getSubject(), contexts);
					}).findAny();
			if (startStatement.isPresent()) {
				head = startStatement.get().getSubject();
			}
			else {
				throw new ModelException("could not determine start of collection");
			}
		}

		Resource current = head;
		final Set<Resource> visited = new HashSet<>();

		while (!RDF.NIL.equals(current)) {
			if (visited.contains(current)) {
				// cycle detected, RDF Collection is not well-formed
				throw new ModelException("RDF Collection not well-formed: contains cyclical reference");
			}
			Models.object(m.filter(current, RDF.FIRST, null, contexts)).ifPresent(o -> consumer.accept(o));
			visited.add(current);
			current = Models.objectResource(m.filter(current, RDF.REST, null, contexts)).orElse(RDF.NIL);
		}
	}

	/**
	 * Reads an RDF Collection starting with the supplied list head from the
	 * supplied {@link Model} and convert it to a Java {@link Collection} of
	 * {@link Value}s. This method expects the RDF Collection to be well-formed.
	 * If the collection is not well-formed the results depend on the nature of
	 * the non-wellformedness: the method may return part of the collection, or
	 * may throw a {@link ModelException}.
	 * 
	 * @param m
	 *        the Model containing the collection to read.
	 * @param head
	 *        the {@link Resource} that represents the list head, that is the
	 *        start resource of the RDF Collection to be read. May be
	 *        {@code null} in which case the method attempts to find a list head.
	 * @param collection
	 *        the Java {@link Collection} to add the collection items to.
	 * @param contexts
	 *        the context(s) from which to read the RDF Collection. This argument
	 *        is an optional vararg and can be left out.
	 * @return the supplied Java {@link Collection}, filled with the items from
	 *         the RDF Collection (if any).
	 * @throws ModelException
	 *         if a problem occurs reading the RDF Collection, for example if the
	 *         Collection is not well-formed.
	 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 *      Schema 1.1 section on Collection vocabulary</a>.
	 */
	public static <C extends Collection<Value>> C readCollection(final Model m, Resource head, C collection,
			Resource... contexts)
				throws ModelException
	{
		Objects.requireNonNull(collection, "collection may not be null");

		readCollection(m, head, v -> collection.add(v), contexts);

		return collection;
	}
}
