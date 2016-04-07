package com.universeprojects.miniup.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;



/**
 *
 * @author Administrator
 */
public class WebUtils
{
	public static SimpleDateFormat dateParser_LR = new SimpleDateFormat("HH:mm:ss MMM dd, yyyy z");
	public static SimpleDateFormat dateParser_Simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}

	public static String getBaseUrl(HttpServletRequest request)
	{
		String url = request.getRequestURL().toString();
		int index = url.indexOf("/", 9);
		url = url.substring(0, index);
		return url;
	}


	public static String encode(String text)
	{
		try
		{
			if (text==null)
				return "";
			return URLEncoder.encode(text, "UTF-8");
		}
		catch(UnsupportedEncodingException uee)
		{
			throw new RuntimeException("WebUtils.encode failed! Error: "+uee.getMessage());
		}
	}

	/**
	 * This method will take a string that may contain html and return a string
	 * that has all HTML tags removed and all <br> tags converted to char #13.
	 * 
	 * @param htmlString
	 * @return
	 */
	public static String htmlToTextOnly(String htmlString)
	{
		if (htmlString==null) return "";

		htmlString = htmlString.replaceAll("<br>", "\n");
		htmlString = htmlString.replaceAll("<.*>", "");
		return htmlString;
	}

	/**
	 * This method will take a string that may contain html and return a string
	 * that contains the html code equivalents. In other words, in an HTML browser
	 * the html tags will appear as text as oppose to being used as html tags.
	 * @param htmlString
	 * @return
	 */
	public static String htmlSafe(String htmlString)
	{
		if (htmlString==null) return "";
		htmlString = htmlString.replaceAll("\\&", "&amp;");
		htmlString = htmlString.replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\n", "<br>").replaceAll("\"", "&quot;");
		htmlString = htmlString.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		htmlString = htmlString.replaceAll("'", "`");

		return htmlString;
	}


	public static String textToHtml(String text)
	{
		if (text==null) return "";
		text = text.replace("  ", "&nbsp;&nbsp");
		text = text.replace("\n", "<br/>");
		return text;
	}

	public static String centsToDollars(Integer cents, boolean excludeCentsIfPossible)
	{
		if (cents==null)
			cents = 0;

		return centsToDollars((long)cents, excludeCentsIfPossible);

	}

	public static String percent2Decimals(double value)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		return df.format(value);
	}

	public static String centsToDollars(Long cents, boolean excludeCentsIfPossible)
	{
		if (cents==null)
			cents = 0l;

		if (cents<10)
		{
			String str = "0.0"+cents;
			if (excludeCentsIfPossible && str.endsWith(".00"))
				str = str.substring(0, str.length()-3);

			return str;
		}
		else if (cents<100)
		{
			String str = "0."+cents;
			if (excludeCentsIfPossible && str.endsWith(".00"))
				str = str.substring(0, str.length()-3);

			return str;
		}
		else
		{
			String str = Long.toString(cents);
			str = str.substring(0, str.length()-2)+"."+str.substring(str.length()-2);
			int pos = 2+4;
			while(str.length()-pos>0)
			{
				str = str.substring(0, str.length()-pos)+","+str.substring(str.length()-pos);
				pos=pos+4;
			}

			if (excludeCentsIfPossible && str.endsWith(".00"))
				str = str.substring(0, str.length()-3);
			return str;

		}
	}

	public static String prettyInt(Integer value)
	{
		if (value==null)
			value = 0;
		String str = Integer.toString(value);
		int pos = 3;
		while(str.length()-pos>0)
		{
			str = str.substring(0, str.length()-pos)+","+str.substring(str.length()-pos);
			pos=pos+4;
		}
		return str;

	}

	public static String prettyLong(Long value)
	{
		if (value==null)
			value = 0l;
		String str = Long.toString(value);
		int pos = 3;
		while(str.length()-pos>0)
		{
			str = str.substring(0, str.length()-pos)+","+str.substring(str.length()-pos);
			pos=pos+4;
		}
		return str;

	}

	public static String urlsToHTML(String text)
	{
		// The next 2 lines would make it so any something.something would be given an http:// at the beginning and turned into an url
		//        text = text.replaceAll("((\\s|^)[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?)", " http://$1");
		//        text = text.replaceAll("http:// ", "http://");
		return text.replaceAll("(^|\\s)((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?)", "<a href='$2' target='_blank'>$2<\\/a>");
	}

	public static Integer getIntParam(HttpServletRequest request, String parameterName)
	{
		return Convert.StrToInteger(request.getParameter(parameterName));
	}

	public static Long getLongParam(HttpServletRequest request, String parameterName)
	{
		return Convert.StrToLong(request.getParameter(parameterName));
	}

	public static Integer getIntParam(HttpServletRequest request, String parameterName, boolean disallowNull)
	{
		Integer val = Convert.StrToInteger(request.getParameter(parameterName));
		if (val==null && disallowNull==true)
			return Integer.valueOf(0);
		else
			return val;
	}

	public static Double getDoubleParam(HttpServletRequest request, String parameterName)
	{
		return getDoubleParam(request, parameterName, false);
	}

	public static Double getDoubleParam(HttpServletRequest request, String parameterName, boolean disallowNull)
	{
		Double val = Convert.StrToDouble(request.getParameter(parameterName));
		if (val==null && disallowNull==true)
			return Double.valueOf(0);
		else
			return val;
	}

	public static String getStrParam(HttpServletRequest request, String parameterName)
	{
		return request.getParameter(parameterName);
	}

	public static String getStrParam(HttpServletRequest request, String parameterName, boolean disallowNull)
	{
		String val = request.getParameter(parameterName);
		if (val == null && disallowNull == true)
			return "";
		else
			return val;
	}

	public static Boolean getBoolParam(HttpServletRequest request, String parameterName)
	{
		return getBoolParam(request, parameterName, true);
	}

	public static Boolean getBoolParam(HttpServletRequest request, String parameterName, boolean disallowNull)
	{
		if (disallowNull==false)
			if (request.getParameter(parameterName)==null)
				return null;

		return Convert.StrToBoolean(request.getParameter(parameterName));
	}

	/**
	 * This takes a string that will be used in javascript and escapes all
	 * the necessary characters to make the result javascript safe.
	 *
	 * Line breaks are turned into textual \n as javascript will re-interpret that as a line break.
	 * @param promptText
	 * @return
	 */
	public static String jsSafe(String promptText)
	{
		promptText = promptText.replaceAll("'", "\\'");
		promptText = promptText.replaceAll("\"", "\\\"");
		promptText = promptText.replaceAll("\n", "\\\\n");
		return promptText;

	}


	/**
	 * Redirects the output to the client to the appropriate internal address for the webserver.
	 * @param url
	 * @param currentServlet
	 * @param request
	 * @param response
	 * @throws javax.servlet.ServletException
	 * @throws java.io.IOException
	 */
	public static void askForRedirectClientTo(String url, HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException
	{
		try
		{
			response.sendRedirect(url);
		}
		catch(IOException ioe)
		{
			throw new IOException("Exception when trying to redirect to "+url);
		}
		catch(IllegalStateException ex)
		{
			throw new IOException("Exception when trying to redirect to "+url);
		}
	}

	public static void forceRedirectClientTo(String url, HttpServletRequest request, HttpServletResponse response, String userMessage) throws ServletException, IOException {
		request.setAttribute("userMessage", userMessage);
		RequestDispatcher rd = request.getRequestDispatcher(url);

		
		rd.forward(request, response);
	}

	public static void forceRedirectClientTo(String url,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher(url);
		
		
		rd.forward(request, response);
	}

	public static void include(String url, HttpServletRequest request, HttpServletResponse response, String userMessage) throws ServletException, IOException {
		request.setAttribute("userMessage", userMessage);
		RequestDispatcher rd = request.getRequestDispatcher(url);

		
		rd.include(request, response);
		response.setHeader("Location", "");
	}

	public static void include(String url,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher(url);
		
		rd.include(request, response);
		response.setHeader("Location", "");
	}

	public static String StringToEncryptedForUrl(String string)
	{
		if (string==null) return null;
		string = new String(Base64.encodeBase64(string.getBytes()));
		string = string.replace("=", "_");
		string = string.replace("+", "-");
		string = string.replace("/", "*");
		return string;
	}

	public static String EncryptedForUrlToString(String encryptedString)
	{
		if (encryptedString==null) return null;
		encryptedString = encryptedString.replace("_", "=");
		encryptedString = encryptedString.replace("-", "+");
		encryptedString = encryptedString.replace("*", "/");
		try
		{
			byte[] bytes = Base64.decodeBase64(encryptedString.getBytes());
			String str = new String(bytes);
			str = str.replace("\0", "");
			return new String(bytes);
		}
		catch(IllegalArgumentException iae)
		{
			throw new IllegalArgumentException(iae.getMessage()+" ("+encryptedString+")");
		}
	}


	public static String constructQueryString(HttpServletRequest request) {
		if (request==null)
			throw new IllegalArgumentException("'Request' argument cannot be null.");
		if (request.getParameterMap().isEmpty())
			return "";

		StringBuffer sb = new StringBuffer();
		Set<?> keys = request.getParameterMap().keySet();
		for(Object key:keys)
		{
			String name = key.toString();
			if (sb.length()>0)
				sb.append("&");
			sb.append(name);
			sb.append("=");
			sb.append(WebUtils.encode(request.getParameter(name)));
		}
		return sb.toString();
	}

	
	public static String getClientIpAddr(HttpServletRequest request) {  
        String ip = request.getHeader("X-Forwarded-For");  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("WL-Proxy-Client-IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_CLIENT_IP");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
        }  
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
            ip = request.getRemoteAddr();  
        }  
        return ip;  
    } 	
}
