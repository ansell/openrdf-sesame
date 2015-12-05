package org.openrdf.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
	 * added to the supplied {@link Model}.
	 * 
	 * @param collection
	 *        a Java {@link Collection} of {@link Value} objects, which will be
	 *        converted to an RDF Collection.
	 * @param m
	 *        the {@link Model} to which the RDF Collection will be added, and
	 *        which will be returned as the result. May not be {@code null}.
	 * @return the Model containing the newly created RDF Collection.
	 */
	public static Model asRDFCollection(Iterable<? extends Value> collection, Model m, Resource... contexts) {
		return asRDFCollection(collection, null, m, contexts);
	}

	/**
	 * Converts the supplied {@link Collection} of {@link Value}s to an
	 * <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 * Collection</a>, using the supplied {@code listStart} resource as the
	 * starting resource of the Collection. The statements making up the new RDF
	 * Collection will be added to the supplied {@link Model}.
	 * 
	 * @param collection
	 *        a Java {@link Collection} of {@link Value} objects, which will be
	 *        converted to an RDF Collection. May not be {@code null}.
	 * @param head
	 *        a {@link Resource} which will be used as the head of the list, that
	 *        is, the starting point of the created RDF Collection. May be
	 *        {@code null}, in which case a new resource is generated to
	 *        represent the list head.
	 * @param m
	 *        the {@link Model} to which the RDF Collection will be added, and
	 *        which will be returned as the result. May not be {@code null}.
	 * @return the Model containing the newly created RDF Collection.
	 * @see <a href="http://www.w3.org/TR/rdf-schema/#ch_collectionvocab">RDF
	 *      Schema 1.1 section on Collection vocabulary</a>.
	 */
	public static Model asRDFCollection(Iterable<? extends Value> collection, Resource head, Model m,
			Resource... contexts)
	{
		Objects.requireNonNull(collection, "input collection may not be null");
		Objects.requireNonNull(m, "model may not be null");
		final ValueFactory vf = SimpleValueFactory.getInstance();

		Resource current = head != null ? head : vf.createBNode();
		m.add(current, RDF.TYPE, RDF.LIST, contexts);

		Iterator<? extends Value> iter = collection.iterator();
		while (iter.hasNext()) {
			Value v = iter.next();
			m.add(current, RDF.FIRST, v, contexts);
			if (iter.hasNext()) {
				Resource next = vf.createBNode();
				m.add(current, RDF.REST, next, contexts);
				current = next;
			}
			else {
				m.add(current, RDF.REST, RDF.NIL, contexts);
			}
		}
		return m;
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
		List<Resource> visited = new ArrayList<>();

		while (!RDF.NIL.equals(current)) {
			if (visited.contains(current)) {
				// cycle detected, RDF Collection is not well-formed
				throw new ModelException("RDF Collection not well-formed: contains cyclical reference");
			}
			Models.object(m.filter(current, RDF.FIRST, null, contexts)).ifPresent(o -> collection.add(o));
			visited.add(current);
			current = Models.objectResource(m.filter(current, RDF.REST, null, contexts)).orElse(RDF.NIL);
		}

		return collection;
	}
}
