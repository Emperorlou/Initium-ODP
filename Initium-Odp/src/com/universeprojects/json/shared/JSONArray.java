/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package com.universeprojects.json.shared;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * A JSON array. JSONObject supports java.util.List interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
@SuppressWarnings("rawtypes")
public class JSONArray extends ArrayList<Object> implements List<Object>, JSONAware, JSONStreamAware {
	private static final long serialVersionUID = 3957988303675231981L;

	/**
	 * Encode a list into JSON text and write it to oucom.universeprojects.json.shared list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
	 * 
	 * @see com.universeprojects.json.shared.JSONValue#writeJSONString(Object, Writer)
	 * 
	 * @param list
	 * @param out
	 */
	public static void writeJSONString(List list, Writer out, final String indent) throws IOException{
		if(list == null){
			out.write("null");
			return;
		}

		String innerIndent = indent;
		if(indent != null) {
			for(int i=0;i<JSONValue.INDENT;i++)
				innerIndent += " ";
		}

		boolean first = true;
		Iterator iter=list.iterator();

		out.write('[');
		while(iter.hasNext()){
			if(first)
				first = false;
			else
				out.write(',');

			if(indent != null) {
				out.write("\n");
				out.write(innerIndent);
			}
			Object value=iter.next();
			if(value == null){
				out.write("null");
				continue;
			}

			JSONValue.writeJSONString(value, out, innerIndent);
		}
		if(indent != null) {
			out.write("\n");
			out.write(indent);
		}
		out.write(']');
	}

	@Override
	public void writeJSONString(Writer out, String indent) throws IOException{
		writeJSONString(this, out, indent);
	}

	/**
com.universeprojects.json.sharedist to JSON text. The result is a JSON array.
	 * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
	 * 
	 * @see com.universeprojects.json.shared.JSONValue#toJSONString(Object)
	 * 
	 * @param list
	 * @return JSON text, or "null" if list is null.
	 */
	public static String toJSONString(List list){
		if(list == null)
			return "null";

		boolean first = true;
		StringBuffer sb = new StringBuffer();
		Iterator iter=list.iterator();

		sb.append('[');
		while(iter.hasNext()){
			if(first)
				first = false;
			else
				sb.append(',');

			Object value=iter.next();
			if(value == null){
				sb.append("null");
				continue;
			}
			sb.append(JSONValue.toJSONString(value));
		}
		sb.append(']');
		return sb.toString();
	}

	@Override
	public String toJSONString(){
		return toJSONString(this);
	}

	@Override
	public String toString() {
		return toJSONString();
	}



}
