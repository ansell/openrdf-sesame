/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for objects of a specific abstract type (or interface) where the
 * actual (runtime) type of the object is specified by a key. The factory
 * requires all registered types to have a public constructor whose parameters
 * can be specified upon creation of the factory.
 */
public class KeyedObjectFactory<KEY, TYPE> {

	/*-----------*
	 * Variables *
	 **----------*/

	private Map<KEY, Class<? extends TYPE>> _types;

	private Map<KEY, Constructor<? extends TYPE>> _constructors;

	private Class[] _paramTypes;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public KeyedObjectFactory(Class... paramTypes) {
		_types = new HashMap<KEY, Class<? extends TYPE>>();
		_constructors = new HashMap<KEY, Constructor<? extends TYPE>>();
		_paramTypes = paramTypes;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Associates a type with a specific key. The specified type must be a public
	 * non-abstract class and it must have a public constructor whose parameters
	 * match the parameters specified when this factory was created.
	 * 
	 * @param key
	 *        The key to be associated with the type.
	 * @param type
	 *        The type class.
	 * @throws IllegalArgumentException
	 *         If the specified type is an interface or abstract class, if it
	 *         isn't public, or if it doesn't have the required public
	 *         constructor.
	 * @throws SecurityException
	 *         If the factory is not allowed to access the type's constructor.
	 */
	public void addType(KEY key, Class<? extends TYPE> type)
		throws SecurityException
	{
		int classModifiers = type.getModifiers();
		if (Modifier.isInterface(classModifiers)) {
			throw new IllegalArgumentException("supplied type is an interface: " + type.getName());
		}
		else if (Modifier.isAbstract(classModifiers)) {
			throw new IllegalArgumentException("supplied type is an abstract class: " + type.getName());
		}
		else if (!Modifier.isPublic(classModifiers)) {
			throw new IllegalArgumentException("supplied type is not a public class: " + type.getName());
		}

		try {
			// Register the zero-argument constructor of the parser
			Constructor<? extends TYPE> constructor = type.getConstructor(_paramTypes);

			if (!Modifier.isPublic(constructor.getModifiers())) {
				throw new IllegalArgumentException("required constructor is not public: "
						+ _getConstructorString(type));
			}

			_types.put(key, type);
			_constructors.put(key, constructor);
		}
		catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("required constructor not found: " + _getConstructorString(type));
		}
	}

	/**
	 * Removes the association between the specified key and the type that is
	 * currently associated with that key.
	 * 
	 * @param key
	 *        A key specifying which key-type association to remove.
	 */
	public void removeType(KEY key) {
		_types.remove(key);
		_constructors.remove(key);
	}

	/**
	 * Gets the type that is associated with the specified key.
	 * 
	 * @param key
	 *        The key to get the type for.
	 * @return The type that is associated with the specified key, or
	 *         <tt>null</tt> if no such type exists.
	 */
	public Class<? extends TYPE> getType(KEY key) {
		return _types.get(key);
	}

	/**
	 * Creates a new instance of the type associated with the specified key.
	 * 
	 * @param key
	 *        A key indicating the type of the instance that should be created.
	 * @throws NoSuchTypeException
	 *         If the specified key is not associated with any type.
	 * @throws TypeInstantiationException
	 *         If, for some reason, the factory failed to create an instance of
	 *         the requested type.
	 */
	public TYPE createInstance(KEY key, Object... initArgs)
		throws NoSuchTypeException, TypeInstantiationException
	{
		Constructor<? extends TYPE> constructor = _constructors.get(key);

		if (constructor == null) {
			throw new NoSuchTypeException("No type found for key: " + key);
		}

		try {
			return constructor.newInstance(initArgs);
		}
		catch (Exception e) {
			throw new TypeInstantiationException(e);
		}
	}

	/**
	 * Utility method to create a String representation of a constructor with
	 * arguments.
	 */
	private String _getConstructorString(Class<? extends TYPE> type) {
		StringBuilder sb = new StringBuilder(128);

		sb.append(type.getName());
		sb.append("(");
		for (int i = 0; i < _paramTypes.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(_paramTypes[i].getName());
		}
		sb.append(")");

		return sb.toString();
	}
}
