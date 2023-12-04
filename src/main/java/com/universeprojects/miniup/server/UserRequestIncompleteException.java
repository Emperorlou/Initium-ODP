package com.universeprojects.miniup.server;


public class UserRequestIncompleteException extends Exception
{
	private static final long serialVersionUID = 45051881954949867L;

	final public String playerMessage;
	final public String pagePopupUrl;
	final public String pagePopupTitle;
	final public String userRequestId;
	final public String urlParameters;
	
	public UserRequestIncompleteException(String pagePopupTitle, String pagePopupUrl, String jsFunctionCall, String playerMessage, String userRequestId)
	{
		if (userRequestId==null) throw new RuntimeException("userRequestId cannot be null.");
		if (pagePopupUrl==null) throw new RuntimeException("pagePopupUrl cannot be null.");
		
		this.playerMessage = playerMessage;
		this.pagePopupUrl = pagePopupUrl;
		this.pagePopupTitle = pagePopupTitle;
		this.userRequestId = userRequestId;
		this.urlParameters = jsFunctionCall;
	}
	
}
