/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.datatypes;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * This class provides utility functions for comparisons operating on
 * <code>xml:dateTime</code> datatypes as specified in <a
 * href="http://www.w3.org/TR/xmlschema-2/#dateTime">W3C, XML Schema Part 2:
 * Datatypes Second Edition</a> Known deviations from the standard: - the range
 * of years in this implementation is limited to Integer.MIN_VALUE to
 * Integer.MAX_VALUE for practical reasons - this implementation accepts some
 * dates that have impossible month, day-of-month combinations (such as
 * 2005-02-29, which was not a leap year)
 * 
 * @author Arjohn Kampman
 */
public class XMLDateTime implements Cloneable, Comparable<XMLDateTime> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/** The raw dateTime string that was used to initialize this object. */
	private String dateTimeString;

	/** Flag indicating whether the year is positive or negative. */
	private boolean isNegativeYear;

	/** year part of the dateTime object as String */
	private String year;

	/** month part of the dateTime object as String */
	private String months;

	/** day part of the dateTime object as String */
	private String days;

	/** hour part of the dateTime object as String */
	private String hours;

	/** minutes part of the dateTime object as String */
	private String minutes;

	/** seconds part of the dateTime object as String */
	private String seconds;

	/** fractional seconds part of the dateTime object as String */
	private String fractionalSeconds;

	/** Flag indicating whether the timezone, if any, is positive or negative. */
	private boolean isNegativeTimezone;

	/** hours part of the optional timezone as String */
	private String hoursTimezone;

	/** minutes part of the optional timezone as String */
	private String minutesTimezone;

	/** year part of the dateTime object as int */
	private int iYear;

	/** month part of the dateTime object as int */
	private int iMonths;

	/** day part of the dateTime object as int */
	private int iDays;

	/** hour part of the dateTime object as int */
	private int iHours;

	/** minute part of the dateTime object as int */
	private int iMinutes;

	/** second part of the dateTime object as int */
	private int iSeconds;

	/** fractional seconds part of the dateTime object as int */
	private double iFractionalSeconds;

	/** hours part of the optional timezone as int */
	private int iHoursTimezone;

	/** minutes part of the optional timezone as int */
	private int iMinutesTimezone;

	/** Flag indicating whether the values have been normalized. */
	private boolean isNormalized = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new DateTime object for the supplied xsd:dateTime string value.
	 * 
	 * @param dateTimeString
	 *        An xsd:dateTime value, for example
	 *        <tt>1999-05-31T13:20:00-05:00</tt>.
	 */
	public XMLDateTime(String dateTimeString) {
		this.dateTimeString = XMLDatatypeUtil.collapseWhiteSpace(dateTimeString);
		parseDateTimeString();
		setNumericFields();
		validateFieldValues();
	}

	/*---------*
	 * Methods *
	 *---------*/

	private void parseDateTimeString() {
		if (dateTimeString.length() < 19) {
			throw new IllegalArgumentException("String value too short to be a valid xsd:dateTime value: "
					+ dateTimeString);
		}

		String errMsg = "Invalid xsd:dateTime value: " + dateTimeString;

		StringTokenizer st = new StringTokenizer(dateTimeString, "+-:.TZ", true);
		try {
			year = st.nextToken();
			isNegativeYear = year.equals("-");
			if (isNegativeYear) {
				year = st.nextToken();
			}
			verifyTokenValue(st.nextToken(), "-", errMsg);
			months = st.nextToken();
			verifyTokenValue(st.nextToken(), "-", errMsg);
			days = st.nextToken();
			verifyTokenValue(st.nextToken(), "T", errMsg);
			hours = st.nextToken();
			verifyTokenValue(st.nextToken(), ":", errMsg);
			minutes = st.nextToken();
			verifyTokenValue(st.nextToken(), ":", errMsg);
			seconds = st.nextToken();

			String token = st.hasMoreTokens() ? st.nextToken() : null;

			if (".".equals(token)) {
				fractionalSeconds = st.nextToken();
				token = st.hasMoreTokens() ? st.nextToken() : null;
			}

			if ("+".equals(token) || "-".equals(token)) {
				isNegativeTimezone = "-".equals(token);
				hoursTimezone = st.nextToken();
				verifyTokenValue(st.nextToken(), ":", errMsg);
				minutesTimezone = st.nextToken();
			}
			else if ("Z".equals(token)) {
				isNegativeTimezone = false;
				hoursTimezone = minutesTimezone = "00";
			}

			if (st.hasMoreTokens()) {
				throw new IllegalArgumentException(errMsg);
			}
		}
		catch (NoSuchElementException e) {
			throw new IllegalArgumentException(errMsg);
		}
	}

	private void verifyTokenValue(String token, String expected, String errMsg) {
		if (!token.equals(expected)) {
			throw new IllegalArgumentException(errMsg);
		}
	}

	private void setNumericFields() {
		try {
			// FIXME: the following statement fails when the year is
			// outside the range of integers (comment by Arjohn)
			iYear = Integer.parseInt(year);
			iMonths = Integer.parseInt(months);
			iDays = Integer.parseInt(days);
			iHours = Integer.parseInt(hours);
			iMinutes = Integer.parseInt(minutes);
			iSeconds = Integer.parseInt(seconds);

			if (fractionalSeconds != null) {
				// FIXME: the following statement fails when the fractional
				// seconds are outside the range of doubles (comment by Arjohn)
				iFractionalSeconds = Double.parseDouble("0." + fractionalSeconds);
			}
			if (hoursTimezone != null) {
				iHoursTimezone = Integer.parseInt(hoursTimezone);
			}
			if (minutesTimezone != null) {
				iMinutesTimezone = Integer.parseInt(minutesTimezone);
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("All fields must be numbers: " + dateTimeString);
		}
	}

	private void validateFieldValues() {
		if (year.length() < 4) {
			throw new IllegalArgumentException("Year field requires at least 4 digits: " + dateTimeString);
		}
		if (months.length() != 2) {
			throw new IllegalArgumentException("Month field must be two digits: " + dateTimeString);
		}
		if (days.length() != 2) {
			throw new IllegalArgumentException("Days field must be two digits: " + dateTimeString);
		}
		if (hours.length() != 2) {
			throw new IllegalArgumentException("Hours field must be two digits: " + dateTimeString);
		}
		if (minutes.length() != 2) {
			throw new IllegalArgumentException("Minutes field must be two digits: " + dateTimeString);
		}
		if (seconds.length() != 2) {
			throw new IllegalArgumentException("Seconds field must be two digits: " + dateTimeString);
		}
		if (hoursTimezone != null) {
			if (hoursTimezone.length() != 2) {
				throw new IllegalArgumentException("Timezone-hours field must be two digits: " + dateTimeString);
			}
			if (minutesTimezone.length() != 2) {
				throw new IllegalArgumentException("Timezone-minutes field must be two digits: " + dateTimeString);
			}
		}

		if (year.length() > 4 && year.charAt(0) == '0') {
			throw new IllegalArgumentException("Leading zeros in years with more than 4 digits are prohibited: "
					+ dateTimeString);
		}
		if (iYear == 0) {
			throw new IllegalArgumentException("0000 is not a valid year: " + dateTimeString);
		}
		if (iHours > 24) {
			throw new IllegalArgumentException("Invalid hour value: " + dateTimeString);
		}
		if (iMinutes > 59) {
			throw new IllegalArgumentException("Invalid minute value: " + dateTimeString);
		}
		if (iSeconds > 59) {
			throw new IllegalArgumentException("Invalid second value: " + dateTimeString);
		}
		if (iHours == 24 && (iMinutes != 0 || iSeconds != 0)) {
			throw new IllegalArgumentException("Invalid time: " + dateTimeString);
		}
		if (iHoursTimezone > 14 || iMinutesTimezone > 59 || iHoursTimezone == 14 && iMinutesTimezone != 0) {
			throw new IllegalArgumentException("Invalid timezone: " + dateTimeString);
		}
	}

	/**
	 * Checks whether this object has already been normalized.
	 */
	public boolean isNormalized() {
		return isNormalized;
	}

	/**
	 * Normalizes this dateTime object.
	 */
	public void normalize() {
		if (isNormalized) {
			// Values already normalized
			return;
		}

		if (iHours == 24 || hoursTimezone != null && (iHoursTimezone != 0 || iMinutesTimezone != 0)) {
			// Normalize the timezone to Coordinated Universal Time (UTC)

			// Insert values into a GregorianCalendar object.
			// Note: GregorianCalendar uses 0-based months
			Calendar cal = new GregorianCalendar(iYear, iMonths - 1, iDays, iHours, iMinutes, iSeconds);
			if (isNegativeYear) {
				cal.set(Calendar.ERA, GregorianCalendar.BC);
			}

			// Add/subtract the timezone
			if (isNegativeTimezone) {
				cal.add(Calendar.HOUR_OF_DAY, iHoursTimezone);
				cal.add(Calendar.MINUTE, iMinutesTimezone);
			}
			else {
				cal.add(Calendar.HOUR_OF_DAY, -iHoursTimezone);
				cal.add(Calendar.MINUTE, -iMinutesTimezone);
			}

			// Get the updated fields
			if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
				isNegativeYear = true;
			}
			iYear = cal.get(Calendar.YEAR);
			iMonths = cal.get(Calendar.MONTH) + 1;
			iDays = cal.get(Calendar.DAY_OF_MONTH);
			iHours = cal.get(Calendar.HOUR_OF_DAY);
			iMinutes = cal.get(Calendar.MINUTE);
			iSeconds = cal.get(Calendar.SECOND);

			year = int2string(iYear, 4);
			months = int2string(iMonths, 2);
			days = int2string(iDays, 2);
			hours = int2string(iHours, 2);
			minutes = int2string(iMinutes, 2);
			seconds = int2string(iSeconds, 2);

			if (hoursTimezone != null) {
				iHoursTimezone = iMinutesTimezone = 0;
				hoursTimezone = minutesTimezone = "00";
				isNegativeTimezone = false;
			}
		}

		if (fractionalSeconds != null) {
			// Remove any trailing zeros
			int zeroCount = 0;
			for (int i = fractionalSeconds.length() - 1; i >= 0; i--) {
				if (fractionalSeconds.charAt(i) == '0') {
					zeroCount++;
				}
				else {
					break;
				}
			}

			if (zeroCount == fractionalSeconds.length()) {
				fractionalSeconds = null;
			}
			else if (zeroCount > 0) {
				fractionalSeconds = fractionalSeconds.substring(0, fractionalSeconds.length() - zeroCount);
			}
		}

		isNormalized = true;
	}

	/**
	 * Converts an integer to a string, enforcing the resulting string to have at
	 * least <tt>minDigits</tt> digits by prepending zeros if it has less than
	 * that amount of digits.
	 */
	private String int2string(int iValue, int minDigits) {
		String result = String.valueOf(iValue);

		int zeroCount = minDigits - result.length();
		if (zeroCount > 0) {
			StringBuilder sb = new StringBuilder(minDigits);
			for (int i = 0; i < zeroCount; i++) {
				sb.append('0');
			}
			sb.append(result);

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Returns the xsd:dateTime string-representation of this object.
	 * 
	 * @return An xsd:dateTime value, e.g. <tt>1999-05-31T13:20:00-05:00</tt>.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32);

		if (isNegativeYear) {
			sb.append('-');
		}
		sb.append(year);
		sb.append('-');
		sb.append(months);
		sb.append('-');
		sb.append(days);
		sb.append('T');
		sb.append(hours);
		sb.append(':');
		sb.append(minutes);
		sb.append(':');
		sb.append(seconds);

		if (fractionalSeconds != null) {
			sb.append('.');
			sb.append(fractionalSeconds);
		}

		if (hoursTimezone != null) {
			if (iHoursTimezone == 0 && iMinutesTimezone == 0) {
				sb.append("Z");
			}
			else {
				if (isNegativeTimezone) {
					sb.append('-');
				}
				else {
					sb.append('+');
				}
				sb.append(hoursTimezone);
				sb.append(':');
				sb.append(minutesTimezone);
			}
		}

		return sb.toString();
	}

	/**
	 * Compares this DateTime object to another DateTime object.
	 * 
	 * @throws ClassCastException
	 *         If <tt>other</tt> is not a DateTime object.
	 */
	public int compareTo(XMLDateTime otherDT) {
		XMLDateTime thisDT = this;

		if (thisDT.hoursTimezone != null && (thisDT.iHoursTimezone != 0 || thisDT.iMinutesTimezone != 0)) {
			// Create a normalized copy of this DateTime object
			thisDT = (XMLDateTime)thisDT.clone();
			thisDT.normalize();
		}

		if (otherDT.hoursTimezone != null && (otherDT.iHoursTimezone != 0 || otherDT.iMinutesTimezone != 0)) {
			// Create a normalized copy of this DateTime object
			otherDT = (XMLDateTime)otherDT.clone();
			otherDT.normalize();
		}

		if (thisDT.isNegativeYear && !otherDT.isNegativeYear) {
			return -1;
		}
		else if (!thisDT.isNegativeYear && otherDT.isNegativeYear) {
			return 1;
		}

		int result = 0;
		if (thisDT.iYear != otherDT.iYear) {
			result = thisDT.iYear - otherDT.iYear;
		}
		else if (thisDT.iMonths != otherDT.iMonths) {
			result = thisDT.iMonths - otherDT.iMonths;
		}
		else if (thisDT.iDays != otherDT.iDays) {
			result = thisDT.iDays - otherDT.iDays;
		}
		else if (thisDT.iHours != otherDT.iHours) {
			result = thisDT.iHours - otherDT.iHours;
		}
		else if (thisDT.iMinutes != otherDT.iMinutes) {
			result = thisDT.iMinutes - otherDT.iMinutes;
		}
		else if (thisDT.iSeconds != otherDT.iSeconds) {
			result = thisDT.iSeconds - otherDT.iSeconds;
		}
		else if (thisDT.iFractionalSeconds != otherDT.iFractionalSeconds) {
			result = (thisDT.iFractionalSeconds < otherDT.iFractionalSeconds) ? -1 : 1;
		}

		if (thisDT.isNegativeYear) {
			// Invert result for negative years
			result = -result;
		}

		return result;
	}

	// Overrides Object.clone();
	@Override
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
