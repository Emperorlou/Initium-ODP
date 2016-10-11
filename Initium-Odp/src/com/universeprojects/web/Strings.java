package com.universeprojects.web;

import java.util.Arrays;
import java.util.Collection;

/**
 * Various convenience methods for working with Strings
 */
public class Strings {

	/**
	 * @return TRUE if the given string is null, empty, or blank
	 */
	public static boolean isEmpty(String str) {
		return (str == null || "".equals(str.trim()));
	}

	/**
	 * @return FALSE if the given string is null, empty, or blank
     */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
     * @return a default string, if the given string is null, empty, or blank
     */
	public static String emptyToDefault(String str, String defaultValue) {
		return Strings.isEmpty(str) ? defaultValue : str;
	}

	/**
	 * @return the given string in quotes, for example: "ABC"
     */
	public static String inQuotes(String str) {
		return "\"" + str + "\"";
	}

	/**
	 * @return the given string in single quotes, for example: 'ABC'
	 */
	public static String inSingleQuotes(String str) {
		return "'" + str + "'";
	}

	/**
	 * @return the given string in parentheses, for example: (ABC)
	 */
	public static String inParentheses(String str) {
		return "(" + str + ")";
	}

	/**
	 * Joins an array of strings into a single string, using the specified delimiter
     */
	public static String join(Object[] items, String delimiter) {
		return join(Arrays.asList(items), delimiter);
	}

	/**
	 * Joins a collection of strings into a single string, using the specified delimiter
	 */
	public static String join(Collection<? extends Object> items, String delimiter) {
		if (items == null) {
			throw new IllegalArgumentException("Collection reference can't be null");
		}
		if (delimiter == null) {
			throw new IllegalArgumentException("Delimiter can't be null");
		}

		StringBuilder sb = new StringBuilder();
		boolean first = true;
    	for (Object obj : items) {
    		if (!first) {
				sb.append(delimiter);
			}
    		sb.append(obj != null ? obj.toString() : "null");
    		first = false;
    	}
    	
    	return sb.toString();
	}

}
