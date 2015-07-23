package org.openrdf.model.util;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.FilterIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.StatementSource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public class Statements {

	private Statements() {
	}

	public static <X extends Exception> CloseableIteration<? extends URI, X> getSubjectURIs(URI predicate,
			Value object, StatementSource<X> store)
		throws X
	{
		return new ConvertingIteration<Statement, URI, X>(
				new FilterIteration<Statement, X>(store.getStatements(null, predicate,
						object))
				{

					@Override
					protected boolean accept(Statement stmt)
						throws X
					{
						return (stmt.getSubject() instanceof URI);
					}
				})
		{

			@Override
			protected URI convert(Statement stmt)
				throws X
			{
				return (URI)stmt.getSubject();
			}
		};
	}

	public static <X extends Exception> CloseableIteration<? extends Resource, X> getObjectResources(Resource subject, URI predicate,
			StatementSource<X> store)
		throws X
	{
		return new ConvertingIteration<Statement, Resource, X>(
				new FilterIteration<Statement, X>(store.getStatements(subject, predicate,
						null))
				{

					@Override
					protected boolean accept(Statement stmt)
						throws X
					{
						return (stmt.getObject() instanceof Resource);
					}
				})
		{

			@Override
			protected Resource convert(Statement stmt)
				throws X
			{
				return (Resource)stmt.getObject();
			}
		};
	}

	public static <X extends Exception> CloseableIteration<? extends URI, X> getObjectURIs(Resource subject, URI predicate,
			StatementSource<X> store)
		throws X
	{
		return new ConvertingIteration<Statement, URI, X>(
				new FilterIteration<Statement, X>(store.getStatements(subject, predicate,
						null))
				{

					@Override
					protected boolean accept(Statement stmt)
						throws X
					{
						return (stmt.getObject() instanceof URI);
					}
				})
		{

			@Override
			protected URI convert(Statement stmt)
				throws X
			{
				return (URI)stmt.getObject();
			}
		};
	}

	public static <X extends Exception> CloseableIteration<? extends Literal, X> getObjectLiterals(Resource subject, URI predicate,
			StatementSource<X> store)
		throws X
	{
		return new ConvertingIteration<Statement, Literal, X>(
				new FilterIteration<Statement, X>(store.getStatements(subject, predicate,
						null))
				{

					@Override
					protected boolean accept(Statement stmt)
						throws X
					{
						return (stmt.getObject() instanceof Literal);
					}
				})
		{

			@Override
			protected Literal convert(Statement stmt)
				throws X
			{
				return (Literal)stmt.getObject();
			}
		};
	}
}
