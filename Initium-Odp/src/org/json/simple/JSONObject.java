/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package org.json.simple;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.appengine.labs.repackaged.com.google.common.annotations.GwtIncompatible;

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
@SuppressWarnings("rawtypes")
public class JSONObject extends LinkedHashMap<Object,Object> implements Map<Object,Object>, JSONAware, JSONStreamAware{
	private static final long serialVersionUID = -503443796854799292L;

	/**
	 * Encode a map into JSON text and write it to out.
	 * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
	 * 
	 * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
	 * 
	 * @param map
	 * @param out
	 */
	@GwtIncompatible
	public static void writeJSONString(Map map, Writer out, final String indent) throws IOException {
		if(map == null){
			out.write("null");
			return;
		}
		String innerIndent = indent;
		if(indent != null) {
			for(int i=0;i<JSONValue.INDENT;i++)
				innerIndent += " ";
		}

		boolean first = true;
		Iterator iter=map.entrySet().iterator();

		out.write('{');
		while(iter.hasNext()){
			if(first)
				first = false;
			else
				out.write(',');
			if(indent != null) {
				out.write("\n");
				out.write(innerIndent);
			}
			Map.Entry entry=(Map.Entry)iter.next();

			out.write('\"');
			out.write(escape(String.valueOf(entry.getKey())));
			out.write('\"');
			out.write(':');
			JSONValue.writeJSONString(entry.getValue(), out, innerIndent);
		}
		if(indent != null) {
			out.write("\n");
			out.write(indent);
		}
		out.write('}');
	}

	@GwtIncompatible
	@Override
	public void writeJSONString(Writer out, String indent) throws IOException{
		writeJSONString(this, out, indent);
	}

	/**
	 * Convert a map to JSON text. The result is a JSON object.
	 * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see org.json.simple.JSONValue#toJSONString(Object)
	 * 
	 * @param map
	 * @return JSON text, or "null" if map is null.
	 */
	public static String toJSONString(Map map){
		if(map == null)
			return "null";

		StringBuffer sb = new StringBuffer();
		boolean first = true;
		Iterator iter=map.entrySet().iterator();

		sb.append('{');
		while(iter.hasNext()){
			if(first)
				first = false;
			else
				sb.append(',');

			Map.Entry entry=(Map.Entry)iter.next();
			toJSONString(String.valueOf(entry.getKey()),entry.getValue(), sb);
		}
		sb.append('}');
		return sb.toString();
	}

	@Override
	public String toJSONString(){
		return toJSONString(this);
	}

	public static JSONObject exceptionToJSONObject(Throwable ex) {
		JSONObject json = new JSONObject();
		if(ex.getMessage() != null)
			json.put("message", ex.getMessage());
		JSONArray arr = new JSONArray();
		for(StackTraceElement el : ex.getStackTrace()) {
			JSONObject elObj = new JSONObject();
			elObj.put("class", el.getClassName());
			elObj.put("method", el.getMethodName());
			elObj.put("file", el.getFileName());
			elObj.put("line", el.getLineNumber());
			arr.add(elObj);
		}
		if(ex.getCause() != null)
			json.put("cause", exceptionToJSONObject(ex.getCause()));
		json.put("stackTrace", arr);
		return json;
	}

	private static String toJSONString(String key,Object value, StringBuffer sb){
		sb.append('\"');
		if(key == null)
			sb.append("null");
		else
			JSONValue.escape(key, sb);
		sb.append('\"').append(':');

		sb.append(JSONValue.toJSONString(value));

		return sb.toString();
	}

	@Override
	public String toString(){
		return toJSONString();
	}

	public static String toString(String key,Object value){
		StringBuffer sb = new StringBuffer();
		toJSONString(key, value, sb);
		return sb.toString();
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * It's the same as JSONValue.escape() only for compatibility here.
	 * 
	 * @see org.json.simple.JSONValue#escape(String)
	 * 
	 * @param s
	 * @return
	 */
	public static String escape(String s){
		return JSONValue.escape(s);
	}
}
