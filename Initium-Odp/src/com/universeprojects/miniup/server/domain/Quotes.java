package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

//Things people have said about the game. These quotes could appear on the website (possibly randomly). It's better to have sources to the quotes if possible.
public class Quotes extends OdpDomain {

	public Quotes() {
		super(new CachedEntity("Quotes"));
	}

	public Quotes(CachedEntity cachedEntity) {
		super(cachedEntity, "Quotes");
	}

	// The name of the person who we are quoting.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// What they said.
	public void setQuote(String quote) {
		getCachedEntity().setProperty("quote", quote);
	}

	public String getQuote() {
		return (String) getCachedEntity().getProperty("quote");
	}

	// The url that points to the person actually saying it.
	public void setSourceUrl(String sourceUrl) {
		getCachedEntity().setProperty("sourceUrl", sourceUrl);
	}

	public String getSourceUrl() {
		return (String) getCachedEntity().getProperty("sourceUrl");
	}

	// A short name to give this quote for reference.
	public void setSummary(String summary) {
		getCachedEntity().setProperty("summary", summary);
	}

	public String getSummary() {
		return (String) getCachedEntity().getProperty("summary");
	}

}
