package com.universeprojects.miniup.server;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.JSONParserFactory;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;



public class ServletEventServer extends HttpServlet
{


	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServletEventServer() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ODPDBAccess db = ODPDBAccess.getInstance(request);
		
		JSONParser parser = JSONParserFactory.getServerParser();
		
		String rawBody = IOUtils.toString(request.getReader());
		try {
			JSONObject body = (JSONObject) parser.parse(rawBody);
			JSONObject respBody = new JSONObject();
			respBody.put("success", false);
			switch (request.getParameter("type")) {
			case "auth":
				String token = (String) body.get("Auth-Token");
				try
				{
					Key characterKey = null;
					CachedEntity character = null;
					Key userOrCharacter = db.decodeAndCheckChatAuthToken(token);
					
					if (userOrCharacter.getKind().equals("User"))
					{
						CachedEntity user = db.getEntity(userOrCharacter);
						characterKey = (Key)user.getProperty("characterKey");
					}
					else
						characterKey = userOrCharacter;
					
					if (characterKey==null || characterKey.getKind().equals("Character")==false)
						throw new SecurityException();
					
					character = db.getEntity(characterKey);
					
					if (character==null)
						throw new SecurityException();
					
					Long groupId = null;
					if (CommonChecks.checkCharacterIsMemberOfHisGroup(character))
						groupId = ((Key)character.getProperty("groupKey")).getId();
					Long locationId = ((Key)character.getProperty("locationKey")).getId();
					String partyCode = ((String)character.getProperty("partyCode"));
					
					
					respBody.put("success", true);
					respBody.put("accountId", userOrCharacter.toString());
					respBody.put("groupId", groupId);
					respBody.put("locationId", locationId);
					respBody.put("partyId", partyCode);
					
				}
				catch (SecurityException e)
				{
					respBody.put("success", false);
				}
				
				break;
			case "message":
				String accountId = (String) body.get("accountId");
				String contents = (String) body.get("contents");
				String channel = (String) body.get("channel");

				Key accountKey = GameUtils.parseKey(accountId);
				CachedEntity character = null;
				if (accountKey.getKind().equals("Character"))
					character = db.getEntity(accountKey);
				else if (accountKey.getKind().equals("User"))
				{
					CachedEntity user = db.getEntity(accountKey);
					character = db.getEntity((Key)user.getProperty("characterKey"));
				}
				
				String characterName = (String)character.getProperty("name");
				
				// do something to format the contents, and maybe other checks depending on the channel
				respBody.put("success", true);

				contents = characterName+": " + contents;
				respBody.put("id", channel);
						
				respBody.put("formattedMsg", contents);
				break;
			}
			System.out.println("responding with: " + respBody.toJSONString());
			response.setHeader("content-type", "application/json");
			response.getWriter().append(respBody.toJSONString());
		} catch (ParseException e) {
			throw new RuntimeException("Parse exception", e);
		}
	}

	
	
//	public static
}
