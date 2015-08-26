package org.openrdf.spin;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.SPIN;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

final class SPINWellKnownVars
{
	private static final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
	static final SPINWellKnownVars INSTANCE = new SPINWellKnownVars();

	private final BiMap<String,URI> stringToUri = HashBiMap.create();
	private final BiMap<URI,String> uriToString = stringToUri.inverse();

	public SPINWellKnownVars() {
		stringToUri.put("this", SPIN.THIS_CONTEXT_INSTANCE);
		stringToUri.put("arg1", SPIN.ARG1_INSTANCE);
		stringToUri.put("arg2", SPIN.ARG2_INSTANCE);
		stringToUri.put("arg3", SPIN.ARG3_INSTANCE);
		stringToUri.put("arg4", SPIN.ARG4_INSTANCE);
		stringToUri.put("arg5", SPIN.ARG5_INSTANCE);
	}

	public URI getURI(String name) {
		URI uri = stringToUri.get(name);
		if(uri == null && name.startsWith("arg")) {
			try {
				Integer.parseInt(name.substring("arg".length()));
				uri = valueFactory.createURI(SPIN.NAMESPACE, "_"+name);
			}
			catch(NumberFormatException nfe) {
				// ignore - not a well-known argN variable
			}
		}
		return uri;
	}

	public String getName(URI uri) {
		String name = uriToString.get(uri);
		if(name == null && SPIN.NAMESPACE.equals(uri.getNamespace()) && uri.getLocalName().startsWith("_arg")) {
			String lname = uri.getLocalName();
			try {
				Integer.parseInt(lname.substring("_arg".length()));
				name = lname.substring(1);
			}
			catch(NumberFormatException nfe) {
				// ignore - not a well-known argN variable
			}
		}
		return name;
	}
}
