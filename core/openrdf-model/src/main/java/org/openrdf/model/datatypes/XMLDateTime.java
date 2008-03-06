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
 * Datatypes Second Edition</a>
 * 
 * Known deviations from the standard: - the range of years in this
 * implementation is limited to Integer.MIN_VALUE to Integer.MAX_VALUE for
 * practical reasons - this implementation accepts some dates that have
 * impossible month, day-of-month combinations (such as 2005-02-29, which was
 * not a leap year)
 * 
 * @author Arjohn Kampman
 */
public class XMLDateTime implements Cloneable, Comparable<XMLDateTime> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/** The raw dateTime string that was used to initialize this object. */
	private String _dateTimeString;

	/** Flag indicating whether the year is positive or negative. */
	private boolean _isNegativeYear;

	/** year part of the dateTime object as String */
	private String _year;

	/** month part of the dateTime object as String */
	private String _months;

	/** day part of the dateTime object as String */
	private String _days;

	/** hour part of the dateTime object as String */
	private String _hours;

	/** minutes part of the dateTime object as String */
	private String _minutes;

	/** seconds part of the dateTime object as String */
	private String _seconds;

	/** fractional seconds part of the dateTime object as String */
	private String _fractionalSeconds;

	/** Flag indicating whether the timezone, if any, is positive or negative. */
	private boolean _isNegativeTimezone;

	/** hours part of the optional timezone as String */
	private String _hoursTimezone;

	/** minutes part of the optional timezone as String */
	private String _minutesTimezone;

	/** year part of the dateTime object as int */
	private int _iYear;

	/** month part of the dateTime object as int */
	private int _iMonths;

	/** day part of the dateTime object as int */
	private int _iDays;

	/** hour part of the dateTime object as int */
	private int _iHours;

	/** minute part of the dateTime object as int */
	private int _iMinutes;

	/** second part of the dateTime object as int */
	private int _iSeconds;

	/** fractional seconds part of the dateTime object as int */
	private double _iFractionalSeconds;

	/** hours part of the optional timezone as int */
	private int _iHoursTimezone;

	/** minutes part of the optional timezone as int */
	private int _iMinutesTimezone;

	/** Flag indicating whether the values have been normalized. */
	private boolean _isNormalized = false;

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
		_dateTimeString = XMLDatatypeUtil.collapseWhiteSpace(dateTimeString);
		parseDateTimeString();
		setNumericFields();
		validateFieldValues();
	}

	/*---------*
	 * Methods *
	 *---------*/

	private void parseDateTimeString() {
		if (_dateTimeString.length() < 19) {
			throw new IllegalArgumentException("String value too short to be a valid xsd:dateTime value: "
					+ _dateTimeString);
		}

		String errMsg = "Invalid xsd:dateTime value: " + _dateTimeString;

		StringTokenizer st = new StringTokenizer(_dateTimeString, "+-:.TZ", true);
		try {
			_year = st.nextToken();
			_isNegativeYear = _year.equals("-");
			if (_isNegativeYear) {
				_year = st.nextToken();
			}
			verifyTokenValue(st.nextToken(), "-", errMsg);
			_months = st.nextToken();
			verifyTokenValue(st.nextToken(), "-", errMsg);
			_days = st.nextToken();
			verifyTokenValue(st.nextToken(), "T", errMsg);
			_hours = st.nextToken();
			verifyTokenValue(st.nextToken(), ":", errMsg);
			_minutes = st.nextToken();
			verifyTokenValue(st.nextToken(), ":", errMsg);
			_seconds = st.nextToken();

			String token = st.hasMoreTokens() ? st.nextToken() : null;

			if (".".equals(token)) {
				_fractionalSeconds = st.nextToken();
				token = st.hasMoreTokens() ? st.nextToken() : null;
			}

			if ("+".equals(token) || "-".equals(token)) {
				_isNegativeTimezone = "-".equals(token);
				_hoursTimezone = st.nextToken();
				verifyTokenValue(st.nextToken(), ":", errMsg);
				_minutesTimezone = st.nextToken();
			}
			else if ("Z".equals(token)) {
				_isNegativeTimezone = false;
				_hoursTimezone = _minutesTimezone = "00";
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
			_iYear = Integer.parseInt(_year);
			_iMonths = Integer.parseInt(_months);
			_iDays = Integer.parseInt(_days);
			_iHours = Integer.parseInt(_hours);
			_iMinutes = Integer.parseInt(_minutes);
			_iSeconds = Integer.parseInt(_seconds);

			if (_fractionalSeconds != null) {
				// FIXME: the following statement fails when the fractional
				// seconds are outside the range of doubles (comment by Arjohn)
				_iFractionalSeconds = Double.parseDouble("0." + _fractionalSeconds);
			}
			if (_hoursTimezone != null) {
				_iHoursTimezone = Integer.parseInt(_hoursTimezone);
			}
			if (_minutesTimezone != null) {
				_iMinutesTimezone = Integer.parseInt(_minutesTimezone);
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("All fields must be numbers: " + _dateTimeString);
		}
	}

	private void validateFieldValues() {
		if (_year.length() < 4) {
			throw new IllegalArgumentException("Year field requires at least 4 digits: " + _dateTimeString);
		}
		if (_months.length() != 2) {
			throw new IllegalArgumentException("Month field must be two digits: " + _dateTimeString);
		}
		if (_days.length() != 2) {
			throw new IllegalArgumentException("Days field must be two digits: " + _dateTimeString);
		}
		if (_hours.length() != 2) {
			throw new IllegalArgumentException("Hours field must be two digits: " + _dateTimeString);
		}
		if (_minutes.length() != 2) {
			throw new IllegalArgumentException("Minutes field must be two digits: " + _dateTimeString);
		}
		if (_seconds.length() != 2) {
			throw new IllegalArgumentException("Seconds field must be two digits: " + _dateTimeString);
		}
		if (_hoursTimezone != null) {
			if (_hoursTimezone.length() != 2) {
				throw new IllegalArgumentException("Timezone-hours field must be two digits: " + _dateTimeString);
			}
			if (_minutesTimezone.length() != 2) {
				throw new IllegalArgumentException("Timezone-minutes field must be two digits: "
						+ _dateTimeString);
			}
		}

		if (_year.length() > 4 && _year.charAt(0) == '0') {
			throw new IllegalArgumentException("Leading zeros in years with more than 4 digits are prohibited: "
					+ _dateTimeString);
		}
		if (_iYear == 0) {
			throw new IllegalArgumentException("0000 is not a valid year: " + _dateTimeString);
		}
		if (_iHours > 24) {
			throw new IllegalArgumentException("Invalid hour value: " + _dateTimeString);
		}
		if (_iMinutes > 59) {
			throw new IllegalArgumentException("Invalid minute value: " + _dateTimeString);
		}
		if (_iSeconds > 59) {
			throw new IllegalArgumentException("Invalid second value: " + _dateTimeString);
		}
		if (_iHours == 24 && (_iMinutes != 0 || _iSeconds != 0)) {
			throw new IllegalArgumentException("Invalid time: " + _dateTimeString);
		}
		if (_iHoursTimezone > 14 || _iMinutesTimezone > 59 || _iHoursTimezone == 14 && _iMinutesTimezone != 0) {
			throw new IllegalArgumentException("Invalid timezone: " + _dateTimeString);
		}
	}

	/**
	 * Checks whether this object has already been normalized.
	 */
	public boolean isNormalized() {
		return _isNormalized;
	}

	/**
	 * Normalizes this dateTime object.
	 */
	public void normalize() {
		if (_isNormalized) {
			// Values already normalized
			return;
		}

		if (_iHours == 24 || _hoursTimezone != null && (_iHoursTimezone != 0 || _iMinutesTimezone != 0)) {
			// Normalize the timezone to Coordinated Universal Time (UTC)

			// Insert values into a GregorianCalendar object.
			// Note: GregorianCalendar uses 0-based months
			Calendar cal = new GregorianCalendar(_iYear, _iMonths - 1, _iDays, _iHours, _iMinutes, _iSeconds);
			if (_isNegativeYear) {
				cal.set(Calendar.ERA, GregorianCalendar.BC);
			}

			// Add/subtract the timezone
			if (_isNegativeTimezone) {
				cal.add(Calendar.HOUR_OF_DAY, _iHoursTimezone);
				cal.add(Calendar.MINUTE, _iMinutesTimezone);
			}
			else {
				cal.add(Calendar.HOUR_OF_DAY, -_iHoursTimezone);
				cal.add(Calendar.MINUTE, -_iMinutesTimezone);
			}

			// Get the updated fields
			if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
				_isNegativeYear = true;
			}
			_iYear = cal.get(Calendar.YEAR);
			_iMonths = cal.get(Calendar.MONTH) + 1;
			_iDays = cal.get(Calendar.DAY_OF_MONTH);
			_iHours = cal.get(Calendar.HOUR_OF_DAY);
			_iMinutes = cal.get(Calendar.MINUTE);
			_iSeconds = cal.get(Calendar.SECOND);

			_year = int2string(_iYear, 4);
			_months = int2string(_iMonths, 2);
			_days = int2string(_iDays, 2);
			_hours = int2string(_iHours, 2);
			_minutes = int2string(_iMinutes, 2);
			_seconds = int2string(_iSeconds, 2);

			if (_hoursTimezone != null) {
				_iHoursTimezone = _iMinutesTimezone = 0;
				_hoursTimezone = _minutesTimezone = "00";
				_isNegativeTimezone = false;
			}
		}

		if (_fractionalSeconds != null) {
			// Remove any trailing zeros
			int zeroCount = 0;
			for (int i = _fractionalSeconds.length() - 1; i >= 0; i--) {
				if (_fractionalSeconds.charAt(i) == '0') {
					zeroCount++;
				}
				else {
					break;
				}
			}

			if (zeroCount == _fractionalSeconds.length()) {
				_fractionalSeconds = null;
			}
			else if (zeroCount > 0) {
				_fractionalSeconds = _fractionalSeconds.substring(0, _fractionalSeconds.length() - zeroCount);
			}
		}

		_isNormalized = true;
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
	public String toString() {
		StringBuilder sb = new StringBuilder(32);

		if (_isNegativeYear) {
			sb.append('-');
		}
		sb.append(_year);
		sb.append('-');
		sb.append(_months);
		sb.append('-');
		sb.append(_days);
		sb.append('T');
		sb.append(_hours);
		sb.append(':');
		sb.append(_minutes);
		sb.append(':');
		sb.append(_seconds);

		if (_fractionalSeconds != null) {
			sb.append('.');
			sb.append(_fractionalSeconds);
		}

		if (_hoursTimezone != null) {
			if (_iHoursTimezone == 0 && _iMinutesTimezone == 0) {
				sb.append("Z");
			}
			else {
				if (_isNegativeTimezone) {
					sb.append('-');
				}
				else {
					sb.append('+');
				}
				sb.append(_hoursTimezone);
				sb.append(':');
				sb.append(_minutesTimezone);
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

		if (thisDT._hoursTimezone != null && (thisDT._iHoursTimezone != 0 || thisDT._iMinutesTimezone != 0)) {
			// Create a normalized copy of this DateTime object
			thisDT = (XMLDateTime)thisDT.clone();
			thisDT.normalize();
		}

		if (otherDT._hoursTimezone != null && (otherDT._iHoursTimezone != 0 || otherDT._iMinutesTimezone != 0))
		{
			// Create a normalized copy of this DateTime object
			otherDT = (XMLDateTime)otherDT.clone();
			otherDT.normalize();
		}

		if (thisDT._isNegativeYear && !otherDT._isNegativeYear) {
			return -1;
		}
		else if (!thisDT._isNegativeYear && otherDT._isNegativeYear) {
			return 1;
		}

		int result = 0;
		if (thisDT._iYear != otherDT._iYear) {
			result = thisDT._iYear - otherDT._iYear;
		}
		else if (thisDT._iMonths != otherDT._iMonths) {
			result = thisDT._iMonths - otherDT._iMonths;
		}
		else if (thisDT._iDays != otherDT._iDays) {
			result = thisDT._iDays - otherDT._iDays;
		}
		else if (thisDT._iHours != otherDT._iHours) {
			result = thisDT._iHours - otherDT._iHours;
		}
		else if (thisDT._iMinutes != otherDT._iMinutes) {
			result = thisDT._iMinutes - otherDT._iMinutes;
		}
		else if (thisDT._iSeconds != otherDT._iSeconds) {
			result = thisDT._iSeconds - otherDT._iSeconds;
		}
		else if (thisDT._iFractionalSeconds != otherDT._iFractionalSeconds) {
			result = (thisDT._iFractionalSeconds < otherDT._iFractionalSeconds) ? -1 : 1;
		}

		if (thisDT._isNegativeYear) {
			// Invert result for negative years
			result = -result;
		}

		return result;
	}

	// Overrides Object.clone();
	public Object clone() {
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
