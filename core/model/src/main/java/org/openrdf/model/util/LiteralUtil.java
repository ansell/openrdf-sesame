/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

import info.aduna.i18n.languagetag.LanguageTag;
import info.aduna.i18n.languagetag.LanguageTagSyntaxException;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;

/**
 * Various utility methods related to {@link Literal}.
 * 
 * @author Arjohn Kampman
 */
public class LiteralUtil {

	/**
	 * Gets the label of the supplied literal. The fallback value is returned in
	 * case the supplied literal is <tt>null</tt>.
	 * 
	 * @param l
	 *        The literal to get the label for.
	 * @param fallback
	 *        The value to fall back to in case the supplied literal is
	 *        <tt>null</tt>.
	 * @return Either the literal's label, or the fallback value.
	 */
	public static String getLabel(Literal l, String fallback) {
		return l != null ? l.getLabel() : fallback;
	}

	/**
	 * Returns the result of
	 * {@link #getLabel(Literal, String) getLabel((Literal)v, fallback} in case
	 * the supplied value is a literal, returns the fallback value otherwise.
	 */
	public static String getLabel(Value v, String fallback) {
		return v instanceof Literal ? getLabel((Literal)v, fallback) : fallback;
	}

	/**
	 * Gets the byte value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#byteValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the byte value for.
	 * @param fallback
	 *        The value to fall back to in case no byte value could gotten from
	 *        the literal.
	 * @return Either the literal's byte value, or the fallback value.
	 */
	public static byte getByteValue(Literal l, byte fallback) {
		try {
			return l.byteValue();
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, byte) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static byte getByteValue(Value v, byte fallback) {
		if (v instanceof Literal) {
			return getByteValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the short value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#shortValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the short value for.
	 * @param fallback
	 *        The value to fall back to in case no short value could gotten from
	 *        the literal.
	 * @return Either the literal's short value, or the fallback value.
	 */
	public static short getShortValue(Literal l, short fallback) {
		try {
			return l.shortValue();
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, short) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static short getShortValue(Value v, short fallback) {
		if (v instanceof Literal) {
			return getShortValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the int value of the supplied literal. The fallback value is returned
	 * in case {@link Literal#intValue()} throws a {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the int value for.
	 * @param fallback
	 *        The value to fall back to in case no int value could gotten from
	 *        the literal.
	 * @return Either the literal's int value, or the fallback value.
	 */
	public static int getIntValue(Literal l, int fallback) {
		try {
			return l.intValue();
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, int) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static int getIntValue(Value v, int fallback) {
		if (v instanceof Literal) {
			return getIntValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the long value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#longValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the long value for.
	 * @param fallback
	 *        The value to fall back to in case no long value could gotten from
	 *        the literal.
	 * @return Either the literal's long value, or the fallback value.
	 */
	public static long getLongValue(Literal l, long fallback) {
		try {
			return l.longValue();
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, long) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static long getLongValue(Value v, long fallback) {
		if (v instanceof Literal) {
			return getLongValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the integer value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#integerValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the integer value for.
	 * @param fallback
	 *        The value to fall back to in case no integer value could gotten
	 *        from the literal.
	 * @return Either the literal's integer value, or the fallback value.
	 */
	public static BigInteger getIntegerValue(Literal l, BigInteger fallback) {
		try {
			return l.integerValue();
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, BigInteger) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static BigInteger getIntegerValue(Value v, BigInteger fallback) {
		if (v instanceof Literal) {
			return getIntegerValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the decimal value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#decimalValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the decimal value for.
	 * @param fallback
	 *        The value to fall back to in case no decimal value could gotten
	 *        from the literal.
	 * @return Either the literal's decimal value, or the fallback value.
	 */
	public static BigDecimal getDecimalValue(Literal l, BigDecimal fallback) {
		try {
			return l.decimalValue();
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, BigDecimal) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static BigDecimal getDecimalValue(Value v, BigDecimal fallback) {
		if (v instanceof Literal) {
			return getDecimalValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the float value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#floatValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the float value for.
	 * @param fallback
	 *        The value to fall back to in case no float value could gotten from
	 *        the literal.
	 * @return Either the literal's float value, or the fallback value.
	 */
	public static float getFloatValue(Literal l, float fallback) {
		try {
			return l.floatValue();
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, float) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static float getFloatValue(Value v, float fallback) {
		if (v instanceof Literal) {
			return getFloatValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the double value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#doubleValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the double value for.
	 * @param fallback
	 *        The value to fall back to in case no double value could gotten from
	 *        the literal.
	 * @return Either the literal's double value, or the fallback value.
	 */
	public static double getDoubleValue(Literal l, double fallback) {
		try {
			return l.doubleValue();
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, double) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static double getDoubleValue(Value v, double fallback) {
		if (v instanceof Literal) {
			return getDoubleValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the boolean value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#booleanValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the boolean value for.
	 * @param fallback
	 *        The value to fall back to in case no boolean value could gotten
	 *        from the literal.
	 * @return Either the literal's boolean value, or the fallback value.
	 */
	public static boolean getBooleanValue(Literal l, boolean fallback) {
		try {
			return l.booleanValue();
		}
		catch (IllegalArgumentException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, boolean) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static boolean getBooleanValue(Value v, boolean fallback) {
		if (v instanceof Literal) {
			return getBooleanValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Gets the calendar value of the supplied literal. The fallback value is
	 * returned in case {@link Literal#calendarValue()} throws a
	 * {@link NumberFormatException}.
	 * 
	 * @param l
	 *        The literal to get the calendar value for.
	 * @param fallback
	 *        The value to fall back to in case no calendar value could gotten
	 *        from the literal.
	 * @return Either the literal's calendar value, or the fallback value.
	 */
	public static XMLGregorianCalendar getCalendarValue(Literal l, XMLGregorianCalendar fallback) {
		try {
			return l.calendarValue();
		}
		catch (IllegalArgumentException e) {
			return fallback;
		}
	}

	/**
	 * Returns the result of
	 * {@link #getByteValue(Literal, XMLGregorianCalendar) getByteValue((Literal)value, fallback)}
	 * in case the supplied value is a literal, returns the fallback value
	 * otherwise.
	 */
	public static XMLGregorianCalendar getCalendarValue(Value v, XMLGregorianCalendar fallback) {
		if (v instanceof Literal) {
			return getCalendarValue((Literal)v, fallback);
		}
		else {
			return fallback;
		}
	}

	/**
	 * Determine the Locale from a literal's language tag, as specified by RFC
	 * 3166. Note that RFC 3166 isn't fully covered by the current (JSE 6)
	 * implementation of java.util.Locale. Therefore, this method will only
	 * return a specific locale for language tags that comply with the Locale
	 * API, i.e. those that contain an ISO639 language, an optional ISO3166
	 * country and an optional variant. In all other cases (i.e. if an error
	 * occurs or the language tag represents an IANA-registred language tag), the
	 * fallback value will be returned.
	 * 
	 * @param l
	 *        the literal
	 * @param fallback
	 *        a fallback value for the locale
	 * @return the Locale, or the fallback if a suitable Locale could not be
	 *         constructed for the language tag.
	 * @see http://www.ietf.org/rfc/rfc3066.txt
	 */
	public static Locale getLocale(Literal l, Locale fallback) {
		Locale result = fallback;

		try {
			String lang = l.getLanguage();
			if(lang != null) {
				LanguageTag tag = new LanguageTag(lang);
				result = tag.toLocale();
			}
		}
		catch (LanguageTagSyntaxException e) {
			result = fallback;
		}

		return result;
	}
}
