package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Things people have said about the game. These quotes could appear on the website (possibly randomly). It's better to have sources to the quotes if possible.
 * 
 * @author kyle-miller
 *
 */
public class Quotes extends OdpDomain {
	public static final String KIND = "Quotes";

	public Quotes() {
		super(new CachedEntity(KIND));
	}

	private Quotes(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final Quotes wrap(CachedEntity cachedEntity) {
		return new Quotes(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  The name of the person who we are quoting.
	 *  
	 * @param name
	 */
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	/**
	 *  What they said.
	 *  
	 * @param quote
	 */
	public void setQuote(String quote) {
		getCachedEntity().setProperty("quote", quote);
	}

	public String getQuote() {
		return (String) getCachedEntity().getProperty("quote");
	}

	/**
	 *  The url that points to the person actually saying it.
	 *  
	 * @param sourceUrl
	 */
	public void setSourceUrl(String sourceUrl) {
		getCachedEntity().setProperty("sourceUrl", sourceUrl);
	}

	public String getSourceUrl() {
		return (String) getCachedEntity().getProperty("sourceUrl");
	}

	/**
	 * A short name to give this quote for reference.
	 * 
	 * @param summary
	 */
	public void setSummary(String summary) {
		getCachedEntity().setProperty("summary", summary);
	}

	public String getSummary() {
		return (String) getCachedEntity().getProperty("summary");
	}

}
