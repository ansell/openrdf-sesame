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
import java.util.Objects;
import java.util.function.Consumer;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Utility methods for {@link Statement} objects.
 *
 * @author Jeen Broekstra
 */
public class Statements {

	/**
	 * Creates one or more {@link Statement} objects with the given subject,
	 * predicate and object, one for each given context, and sends each created
	 * statement to the supplied {@link Consumer}. If no context is supplied,
	 * only a single statement (without any assigned context) is created.
	 * 
	 * @param vf
	 *        the {@link ValueFactory} to use for creating statements.
	 * @param subject
	 *        the subject of each statement. May not be null.
	 * @param predicate
	 *        the predicate of each statement. May not be null.
	 * @param object
	 *        the object of each statement. May not be null.
	 * @param consumer
	 *        the {@link Consumer} function for the produced statements.
	 * @param contexts
	 *        the context(s) for which to produce statements. This argument is
	 *        an optional vararg: leave it out completely to produce a single
	 *        statement without context.
	 */
	public static void consume(ValueFactory vf, Resource subject, IRI predicate, Value object,
			Consumer<Statement> consumer, Resource... contexts)
	{
		OpenRDFUtil.verifyContextNotNull(contexts);
		Objects.requireNonNull(consumer);

		if (contexts.length > 0) {
			for (Resource context : contexts) {
				consumer.accept(vf.createStatement(subject, predicate, object, context));
			}
		}
		else {
			consumer.accept(vf.createStatement(subject, predicate, object));
		}
	}

	/**
	 * Creates one or more {@link Statement} objects with the given subject,
	 * predicate and object, one for each given context. If no context is
	 * supplied, only a single statement (without any assigned context) is
	 * created.
	 * 
	 * @param vf
	 *        the {@link ValueFactory} to use for creating statements.
	 * @param subject
	 *        the subject of each statement. May not be null.
	 * @param predicate
	 *        the predicate of each statement. May not be null.
	 * @param object
	 *        the object of each statement. May not be null.
	 * @param collection
	 *        the collection of Statements to which the newly created Statements
	 *        will be added. May not be null.
	 * @return the input collection of Statements, with the newly created
	 *         Statements added.
	 * @param contexts
	 *        the context(s) for which to produce statements. This argument is
	 *        an optional vararg: leave it out completely to produce a single
	 *        statement without context.
	 */
	public static <C extends Collection<Statement>> C create(ValueFactory vf, Resource subject, IRI predicate,
			Value object, C collection, Resource... contexts)
	{
		Objects.requireNonNull(collection);
		consume(vf, subject, predicate, object, st -> collection.add(st), contexts);
		return collection;
	}
}
