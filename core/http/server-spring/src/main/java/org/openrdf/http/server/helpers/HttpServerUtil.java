/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpServerUtil {

	public static final String ACCEPT_HEADER_NAME = "Accept";

	private static final String _UNKNOWN = "__UNKNOWN__";

	/**
	 * Extracts the MIME type from the specified content type string. This method
	 * parses the content type string and returns just the MIME type, ignoring
	 * any parameters that are included.
	 * 
	 * @param contentType
	 *        A content type string, e.g. <tt>application/xml; charset=utf-8</tt>.
	 * @return The MIME type part of the specified content type string, or
	 *         <tt>null</tt> if the specified content type string was
	 *         <tt>null</tt>.
	 */
	public static String getMIMEType(String contentType) {
		if (contentType == null) {
			return null;
		}

		return HeaderElement.parse(contentType).getValue();
	}

	/**
	 * Selects from a set of MIME types, the MIME type that has the highest
	 * quality score when matched with the Accept headers in the supplied
	 * request.
	 * 
	 * @param mimeTypes
	 *        The set of available MIME types.
	 * @param request
	 *        The request to match the MIME types against.
	 * @return The MIME type that best matches the types that the client finds
	 *         acceptable, or <tt>null</tt> in case no acceptable MIME type
	 *         could be found.
	 */
	public static String selectPreferredMIMEType(Iterator<String> mimeTypes, HttpServletRequest request) {
		List<HeaderElement> acceptElements = getHeaderElements(request, ACCEPT_HEADER_NAME);

		if (acceptElements.isEmpty()) {
			// Client does not specify any requirements, return first MIME type
			// from the list
			if (mimeTypes.hasNext()) {
				return mimeTypes.next();
			}
			else {
				return null;
			}
		}

		String result = null;
		double highestQuality = 0.0;

		while (mimeTypes.hasNext()) {
			String mimeType = mimeTypes.next();
			HeaderElement acceptType = matchAcceptHeader(mimeType, acceptElements);

			if (acceptType != null) {
				// quality defaults to 1.0
				double quality = 1.0;

				String qualityStr = acceptType.getParameterValue("q");
				if (qualityStr != null) {
					try {
						quality = Double.parseDouble(qualityStr);
					}
					catch (NumberFormatException e) {
						// Illegal quality value, assume it has a different meaning
						// and ignore it
					}
				}

				if (quality > highestQuality) {
					result = mimeType;
					highestQuality = quality;
				}
			}
		}

		return result;
	}

	/**
	 * Gets the elements of the request header with the specified name.
	 * 
	 * @param request
	 *        The request to get the header from.
	 * @param headerName
	 *        The name of the header to get the elements of.
	 * @return A List of {@link HeaderElement} objects.
	 */
	public static List<HeaderElement> getHeaderElements(HttpServletRequest request, String headerName) {
		List<HeaderElement> elemList = new ArrayList<HeaderElement>(8);

		@SuppressWarnings("unchecked")
		Enumeration<String> headerValues = request.getHeaders(headerName);
		while (headerValues.hasMoreElements()) {
			String value = headerValues.nextElement();

			List<String> subValues = splitHeaderString(value, ',');

			for (String subValue : subValues) {
				// Ignore any empty header elements
				subValue = subValue.trim();
				if (subValue.length() > 0) {
					elemList.add(HeaderElement.parse(subValue));
				}
			}
		}

		return elemList;
	}

	/**
	 * Splits the supplied string into sub parts using the specified splitChar as
	 * a separator, ignoring occurrences of this character inside quoted strings.
	 * 
	 * @param s
	 *        The header string to split into sub parts.
	 * @param splitChar
	 *        The character to use as separator.
	 * @return A <tt>List</tt> of <tt>String</tt>s.
	 */
	public static List<String> splitHeaderString(String s, char splitChar) {
		List<String> result = new ArrayList<String>(8);

		boolean parsingQuotedString = false;
		int i, startIdx = 0;

		for (i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == splitChar && !parsingQuotedString) {
				result.add(s.substring(startIdx, i));
				startIdx = i + 1;
			}
			else if (c == '"') {
				parsingQuotedString = !parsingQuotedString;
			}
		}

		if (startIdx < s.length()) {
			result.add(s.substring(startIdx));
		}

		return result;
	}

	/**
	 * Tries to match the specified MIME type spec against the list of Accept
	 * header elements, returning the applicable header element if available.
	 * 
	 * @param mimeTypeSpec
	 *        The MIME type to determine the quality for, e.g. "text/plain" or
	 *        "application/xml; charset=utf-8".
	 * @param acceptElements
	 *        A List of {@link HeaderElement} objects.
	 * @return The Accept header element that matches the MIME type spec most
	 *         closely, or <tt>null</tt> if no such header element could be
	 *         found.
	 */
	public static HeaderElement matchAcceptHeader(String mimeTypeSpec, List<HeaderElement> acceptElements) {
		HeaderElement mimeTypeElem = HeaderElement.parse(mimeTypeSpec);

		while (mimeTypeElem != null) {
			for (HeaderElement acceptElem : acceptElements) {
				if (matchesAcceptHeader(mimeTypeElem, acceptElem)) {
					return acceptElem;
				}
			}

			// No match found, generalize the MIME type spec and try again
			mimeTypeElem = generalizeMIMEType(mimeTypeElem);
		}

		return null;
	}

	private static boolean matchesAcceptHeader(HeaderElement mimeTypeElem, HeaderElement acceptElem) {
		if (!mimeTypeElem.getValue().equals(acceptElem.getValue())) {
			return false;
		}

		// Values match, check parameters
		if (mimeTypeElem.getParameterCount() > acceptElem.getParameterCount()) {
			return false;
		}

		for (int i = 0; i < mimeTypeElem.getParameterCount(); i++) {
			if (!mimeTypeElem.getParameter(i).equals(acceptElem.getParameter(i))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Generalizes a MIME type element. The following steps are taken for
	 * generalization:
	 * <ul>
	 * <li>If the MIME type element has one or more parameters, the last
	 * parameter is removed.
	 * <li>Otherwise, if the MIME type element's subtype is not equal to '*'
	 * then it is set to this value.
	 * <li>Otherwise, if the MIME type element's type is not equal to '*' then
	 * it is set to this value.
	 * <li>Otherwise, the MIME type is equal to "*&slash;*" and cannot be
	 * generalized any further; <tt>null</tt> is returned.
	 * </ul>
	 * <p>
	 * Example generalizations:
	 * </p>
	 * <table>
	 * <tr>
	 * <th>input</th>
	 * <th>result</th>
	 * </tr>
	 * <tr>
	 * <td>application/xml; charset=utf-8</td>
	 * <td>application/xml</td>
	 * </tr>
	 * <tr>
	 * <td>application/xml</td>
	 * <td>application/*</td>
	 * </tr>
	 * <tr>
	 * <td>application/*</td>
	 * <td>*&slash;*</td>
	 * </tr>
	 * <tr>
	 * <td>*&slash;*</td>
	 * <td><tt>null</tt></td>
	 * </tr>
	 * </table>
	 * 
	 * @param mimeTypeElem
	 *        The MIME type element that should be generalized.
	 * @return The generalized MIME type element, or <tt>null</tt> if it could
	 *         not be generalized any further.
	 */
	private static HeaderElement generalizeMIMEType(HeaderElement mimeTypeElem) {
		int parameterCount = mimeTypeElem.getParameterCount();
		if (parameterCount > 0) {
			// remove last parameter
			mimeTypeElem.removeParameter(parameterCount - 1);
		}
		else {
			String mimeType = mimeTypeElem.getValue();

			int slashIdx = mimeType.indexOf('/');
			if (slashIdx > 0) {
				String type = mimeType.substring(0, slashIdx);
				String subType = mimeType.substring(slashIdx + 1);

				if (!subType.equals("*")) {
					// generalize subtype
					mimeTypeElem.setValue(type + "/*");
				}
				else if (!type.equals("*")) {
					// generalize type
					mimeTypeElem.setValue("*/*");
				}
				else {
					// Cannot generalize any further
					mimeTypeElem = null;
				}
			}
			else {
				// invalid MIME type
				mimeTypeElem = null;
			}
		}

		return mimeTypeElem;
	}

	/**
	 * Gets the trimmed value of a request parameter as a String.
	 * 
	 * @return The trimmed value, or null if the parameter does not exist.
	 */
	public static String getPostDataParameter(Map<String, Object> formData, String name) {
		String result = null;

		try {
			Object param = formData.get(name);
			if (param instanceof String[]) {
				String[] paramArray = (String[])param;
				if (paramArray.length > 0) {
					result = paramArray[0];
				}
			}
			else if (param instanceof String) {
				result = (String)param;
			}

			if (result != null) {
				result = result.trim();
			}
		}
		catch (ClassCastException cce) {
			// ignore, return null
		}

		return result;
	}

	/**
	 * Get the parameters of this request. Request parameters are extra
	 * information sent with the request. For HTTP servlets, parameters are
	 * contained in the query string or posted form data. The values are either
	 * of type String[] or of type FilePart.
	 * 
	 * @return the parameters of this request
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getPostData(HttpServletRequest request) {
		Map<String, Object> result = null;

		String contentType = request.getContentType();
		if (contentType != null && contentType.startsWith("multipart/form-data")) {
			result = parseMultipartFormData(request);
		}
		else {
			result = request.getParameterMap();
		}

		return result;
	}

	/**
	 * Parse the contents of a multipart/form-data encoded POST request.
	 * 
	 * @param request
	 *        the request to parse
	 * @return a map containing all names and values from the request. Values may
	 *         be either Strings or FileParts.
	 */
	public static Map<String, Object> parseMultipartFormData(HttpServletRequest request) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		int contentLength = request.getContentLength();
		String contentType = request.getContentType();
		byte formData[] = new byte[0];

		try {
			InputStream inputStream = request.getInputStream();
			if (contentLength < 0) {
				contentLength = 100 * 1024; // set a reasonable expected
				// contentlength of 100KB
			}
			formData = new byte[contentLength];
			contentLength = inputStream.read(formData);
			inputStream.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		if (contentType != null) {
			int boundaryPos = contentType.indexOf("boundary=") + "boundary=".length();
			String boundaryString = "--" + contentType.substring(boundaryPos);
			// Remove any trailing blanks that might follow the boundary
			byte[] boundary = boundaryString.trim().getBytes();
			int boundaryLength = boundary.length;

			// Split the data into parts separated by the boundaries:
			ArrayList<byte[]> parts = new ArrayList<byte[]>();
			int partStart = 0;
			int partEnd = 0;
			boolean firstBoundary = true;

			while (true) {
				boundaryPos = findIndexOf(boundary, formData, partStart);
				if (boundaryPos != -1) {
					if (firstBoundary) {
						firstBoundary = false;
					}
					else {
						partEnd = boundaryPos;

						byte[] part = new byte[partEnd - partStart];
						System.arraycopy(formData, partStart, part, 0, partEnd - partStart);
						part = trimReturns(part);
						parts.add(part);
					}
					partStart = boundaryPos + boundaryLength;
				}
				else {
					break;
				}
			}

			for (int i = 0; i < parts.size(); i++) {
				byte[] part = parts.get(i);

				String name = null;
				String filename = null;
				String fileContentType = null;

				name = findAttribute("name=\"", "\"", part).trim();

				// We recognize a file-part by the existence of a "filename"
				// field.
				filename = findAttribute("filename=\"", "\"", part).trim();

				if (!filename.equals(_UNKNOWN)) {
					int slashPos = filename.lastIndexOf("/");
					int backslashPos = filename.lastIndexOf("\\");

					if (slashPos >= 0 || backslashPos >= 0) {
						int lastPos = Math.max(slashPos, backslashPos);
						filename = filename.substring(lastPos + 1);
					}

					fileContentType = findAttribute("Content-Type: ", "\n", part);
					if (fileContentType == null) {
						fileContentType = _UNKNOWN;
					}
					else {
						fileContentType = fileContentType.trim();
					}
				}

				int bodyStartCR = findIndexOf("\n\n".getBytes(), part, 0);
				int bodyStartCRLF = findIndexOf("\r\n\r\n".getBytes(), part, 0);

				int bodyStart;
				byte[] bodyArray = null;

				if (bodyStartCR < 0 && bodyStartCRLF < 0) {
					bodyArray = new byte[0]; // Used for these type of
					// form-fields: xxx=''
					bodyStart = -1;
				}
				else if (bodyStartCR < 0) {
					bodyStart = bodyStartCRLF;
				}
				else if (bodyStartCRLF < 0) {
					bodyStart = bodyStartCR;
				}
				else {
					bodyStart = Math.min(bodyStartCR, bodyStartCRLF);
				}

				if (bodyStart >= 0) {
					int bodyEnd = part.length;

					if (bodyStart == bodyStartCR) {
						bodyStart += "\n\n".getBytes().length; // skip \n\n at
						// the start
					}
					else {
						bodyStart += "\r\n\r\n".getBytes().length; // skip
						// \r\n\r\n
					}

					bodyArray = new byte[bodyEnd - bodyStart];
					System.arraycopy(part, bodyStart, bodyArray, 0, bodyEnd - bodyStart);
				}

				// if a filename attribute exists, we assume a file was uploaded
				if (!filename.equals(_UNKNOWN)) {
					result.put(name, new FilePart(filename, fileContentType, bodyArray));
				}
				else {
					result.put(name, new String(bodyArray));
				}
			}
		}

		return result;
	}

	/**
	 * Sets headers on the supplied response that prevent all kinds of browsers
	 * to cache it.
	 */
	public static void setNoCacheHeaders(HttpServletResponse response) {
		//
		// according to http://vancouver-webpages.com/META/FAQ.html
		// 
		response.setHeader("Cache-Control", "must-revalidate");
		response.setHeader("Cache-Control", "max-age=1");
		response.setHeader("Cache-Control", "no-cache");
		response.setIntHeader("Expires", 0);
		response.setHeader("Pragma", "no-cache");
	}

	/**
	 * Finds the first occurrence of 'toFind' in 'source', starting at offset
	 * 'offset'.
	 */
	private static int findIndexOf(byte[] toFind, byte[] source, int offset) {
		int toFindLength = toFind.length;
		int maxI = source.length - toFindLength;

		for (int i = offset; i <= maxI; i++) {
			int j;
			for (j = 0; j < toFindLength; j++) {
				if (toFind[j] != source[i + j]) {
					break;
				}
			}
			if (j == toFindLength) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Finds an attribute's value in a byte-array. The returned 'value' is
	 * defined as the bytes between 'startPattern' and 'endPattern'.
	 */
	private static String findAttribute(String startPattern, String endPattern, byte[] sourceArray) {
		String result = null;

		String source = new String(sourceArray);

		int indexOfStartPattern = source.indexOf(startPattern);
		if (indexOfStartPattern < 0) {
			result = _UNKNOWN;
		}
		else {
			int indexOfEndPattern = source.indexOf(endPattern, indexOfStartPattern + startPattern.length());
			if (indexOfEndPattern < 0) {
				result = _UNKNOWN;
			}
			else {
				result = source.substring(indexOfStartPattern + startPattern.length(), indexOfEndPattern);
			}
		}

		return result;
	}

	/**
	 * Remove one \n or \r\n (and combination thereof) from the beginning and end
	 * of the specified byte array.
	 */
	private static byte[] trimReturns(byte[] source) {
		byte[] result = source;
		int srcLength = source.length;

		int resultStart = 0;
		int resultLength = source.length;

		// Trim return from start of source:
		if (resultLength >= 2 && source[0] == '\r' && source[1] == '\n') {
			resultStart = 2;
			resultLength -= 2;
		}
		else if (resultLength >= 1 && source[0] == '\n') {
			resultStart = 1;
			resultLength -= 1;
		}

		// Trim return from end of source:
		if (resultLength >= 2 && source[srcLength - 2] == '\r' && source[srcLength - 1] == '\n') {
			resultLength -= 2;
		}
		else if (resultLength >= 1 && source[srcLength - 1] == '\n') {
			resultLength -= 1;
		}

		if (resultLength != srcLength) {
			result = new byte[resultLength];
			System.arraycopy(source, resultStart, result, 0, resultLength);
		}

		return result;
	}

	/**
	 * @return true if the string is either null or equal to ""
	 */
	public static boolean isEmpty(String string) {
		boolean result = false;
		if (string == null || string.trim().equals("")) {
			result = true;
		}
		return result;
	}

	/**
	 * @return true if the string is !isEmpty and equal to "true"
	 */
	public static boolean isTrue(String string) {
		boolean result = false;
		if (!isEmpty(string) && (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("on"))) {
			result = true;
		}
		return result;
	}

	/**
	 * @return true if the string is !isEmpty and equal to "false"
	 */
	public static boolean isFalse(String string) {
		boolean result = false;
		if (!isEmpty(string) && (string.equalsIgnoreCase("false") || string.equalsIgnoreCase("off"))) {
			result = true;
		}
		return result;
	}
}
