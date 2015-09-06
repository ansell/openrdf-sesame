package org.openrdf.spin;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

final class SPINWellKnownFunctions
{
	private static final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
	private static final FunctionRegistry functionRegistry = FunctionRegistry.getInstance();
	static final SPINWellKnownFunctions INSTANCE = new SPINWellKnownFunctions();

	private final BiMap<String,URI> stringToUri = HashBiMap.create(64);
	private final BiMap<URI,String> uriToString = stringToUri.inverse();

	public SPINWellKnownFunctions() {
		stringToUri.put(FN.SUBSTRING.stringValue(), valueFactory.createURI(SP.NAMESPACE, "substr"));
		stringToUri.put(FN.SUBSTRING_BEFORE.stringValue(), valueFactory.createURI(SP.NAMESPACE, "strbefore"));
		stringToUri.put(FN.SUBSTRING_AFTER.stringValue(), valueFactory.createURI(SP.NAMESPACE, "strafter"));
		stringToUri.put(FN.STARTS_WITH.stringValue(), valueFactory.createURI(SP.NAMESPACE, "strstarts"));
		stringToUri.put(FN.ENDS_WITH.stringValue(), valueFactory.createURI(SP.NAMESPACE, "strends"));
		stringToUri.put(FN.STRING_LENGTH.stringValue(), valueFactory.createURI(SP.NAMESPACE, "strlen"));
		stringToUri.put(FN.CONCAT.stringValue(), valueFactory.createURI(SP.NAMESPACE, "concat"));
		stringToUri.put(FN.CONTAINS.stringValue(), valueFactory.createURI(SP.NAMESPACE, "contains"));
		stringToUri.put(FN.LOWER_CASE.stringValue(), valueFactory.createURI(SP.NAMESPACE, "lcase"));
		stringToUri.put(FN.UPPER_CASE.stringValue(), valueFactory.createURI(SP.NAMESPACE, "ucase"));
		stringToUri.put(FN.REPLACE.stringValue(), valueFactory.createURI(SP.NAMESPACE, "replace"));
		stringToUri.put(FN.NUMERIC_ABS.stringValue(), valueFactory.createURI(SP.NAMESPACE, "abs"));
		stringToUri.put(FN.NUMERIC_CEIL.stringValue(), valueFactory.createURI(SP.NAMESPACE, "ceil"));
		stringToUri.put(FN.NUMERIC_FLOOR.stringValue(), valueFactory.createURI(SP.NAMESPACE, "floor"));
		stringToUri.put(FN.NUMERIC_ROUND.stringValue(), valueFactory.createURI(SP.NAMESPACE, "round"));
		stringToUri.put(FN.YEAR_FROM_DATETIME.stringValue(), valueFactory.createURI(SP.NAMESPACE, "year"));
		stringToUri.put(FN.MONTH_FROM_DATETIME.stringValue(), valueFactory.createURI(SP.NAMESPACE, "month"));
		stringToUri.put(FN.DAY_FROM_DATETIME.stringValue(), valueFactory.createURI(SP.NAMESPACE, "day"));
		stringToUri.put(FN.HOURS_FROM_DATETIME.stringValue(), valueFactory.createURI(SP.NAMESPACE, "hours"));
		stringToUri.put(FN.MINUTES_FROM_DATETIME.stringValue(), valueFactory.createURI(SP.NAMESPACE, "minutes"));
		stringToUri.put(FN.SECONDS_FROM_DATETIME.stringValue(), valueFactory.createURI(SP.NAMESPACE, "seconds"));
		stringToUri.put(FN.TIMEZONE_FROM_DATETIME.stringValue(), valueFactory.createURI(SP.NAMESPACE, "timezone"));
		stringToUri.put(FN.ENCODE_FOR_URI.stringValue(), valueFactory.createURI(SP.NAMESPACE, "encode_for_uri"));
		stringToUri.put("NOW", valueFactory.createURI(SP.NAMESPACE, "now"));
		stringToUri.put("RAND", valueFactory.createURI(SP.NAMESPACE, "rand"));
		stringToUri.put("STRDT", valueFactory.createURI(SP.NAMESPACE, "strdt"));
		stringToUri.put("STRLANG", valueFactory.createURI(SP.NAMESPACE, "strlang"));
		stringToUri.put("TZ", valueFactory.createURI(SP.NAMESPACE, "tz"));
		stringToUri.put("UUID", valueFactory.createURI(SP.NAMESPACE, "uuid"));
		stringToUri.put("STRUUID", valueFactory.createURI(SP.NAMESPACE, "struuid"));
		stringToUri.put("MD5", valueFactory.createURI(SP.NAMESPACE, "md5"));
		stringToUri.put("SHA1", valueFactory.createURI(SP.NAMESPACE, "sha1"));
		stringToUri.put("SHA256", valueFactory.createURI(SP.NAMESPACE, "sha256"));
		stringToUri.put("SHA384", valueFactory.createURI(SP.NAMESPACE, "sha384"));
		stringToUri.put("SHA512", valueFactory.createURI(SP.NAMESPACE, "sha512"));
	}

	public URI getURI(String name) {
		URI uri = stringToUri.get(name);
		if(uri == null && functionRegistry.has(name)) {
			uri = valueFactory.createURI(name);
		}
		return uri;
	}

	public String getName(URI uri) {
		String name = uriToString.get(uri);
		if(name == null && functionRegistry.has(uri.stringValue())) {
			name = uri.stringValue();
		}
		return name;
	}
}