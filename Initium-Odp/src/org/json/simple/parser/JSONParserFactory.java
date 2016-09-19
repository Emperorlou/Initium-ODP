package org.json.simple.parser;

import com.google.appengine.labs.repackaged.com.google.common.annotations.GwtIncompatible;

public class JSONParserFactory {
	
	private static ParserHelper parserHelper;
	
	@GwtIncompatible
	public static void initServerParser() {
		parserHelper = new ServerParserHelper();
	}
	
//	public static void initGWTParser() {
//		parserHelper = new GWTParserHelper();
//	}
	
	public static JSONParser getParser() {
		return parserHelper.getParser();
	}
	
	@GwtIncompatible
	public static JSONServerParser getServerParser() {
		if(parserHelper == null) parserHelper = new ServerParserHelper();
		return (JSONServerParser) parserHelper.getParser();
	}

	
	private interface ParserHelper {
		JSONParser getParser();
	}
	
//	private static class GWTParserHelper implements ParserHelper {
//		JSONGWTParser gwtParser;
//
//		@Override
//		public JSONParser getParser() {
//			if(gwtParser == null)
//				gwtParser  = new JSONGWTParser();
//			return gwtParser;
//		}
//	}
	
	@GwtIncompatible
	private static class ServerParserHelper implements ParserHelper {
		final ThreadLocal<JSONServerParser> serverParser = new ThreadLocal<JSONServerParser>() {
			@Override
			protected JSONServerParser initialValue() {
				return new JSONServerParser();
			}
		};

		@Override
		public JSONParser getParser() {
			return serverParser.get();
		}
		
		
	}
}
