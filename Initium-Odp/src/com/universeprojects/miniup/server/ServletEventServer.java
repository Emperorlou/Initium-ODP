package com.universeprojects.miniup.server;


import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.json.shared.parser.JSONParser;
import com.universeprojects.json.shared.parser.JSONParserFactory;
import com.universeprojects.json.shared.parser.ParseException;
import com.universeprojects.miniup.CommonChecks;



public class ServletEventServer extends HttpServlet
{
	Random rnd = new Random();
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
					
					String groupId = "";
					if (CommonChecks.checkCharacterIsMemberOfHisGroup(character)) {
						Long gId = ((Key)character.getProperty("groupKey")).getId();
						groupId = "G" + gId.toString();
					}
						
					Long locationId = ((Key)character.getProperty("locationKey")).getId();
					String partyCode = ((String)character.getProperty("partyCode"));
					
					String partyId = "";
					if (partyCode != null) {
						partyId = "P" + partyCode;
					}
					
					respBody.put("success", true);
					respBody.put("accountId", userOrCharacter.toString());
					respBody.put("groupId", groupId);
					respBody.put("locationId", "L" + locationId);
					respBody.put("partyId", partyId);
					
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
				String target = (String) body.get("target");

				Key accountKey = GameUtils.parseKey(accountId);
				CachedEntity character = null;
				if (accountKey.getKind().equals("Character"))
					character = db.getEntity(accountKey);
				else if (accountKey.getKind().equals("User"))
				{
					CachedEntity user = db.getEntity(accountKey);
					character = db.getEntity((Key)user.getProperty("characterKey"));
				} else {
					// accountId was invalid
					break;
				}
				String characterName = (String)character.getProperty("name");
				
				if (contents==null || contents.equals("") || contents.trim().equals(""))
					return;
				if (contents.length()>2000)
					return;
				

				List<CachedEntity> buffs = db.getBuffsFor(character.getKey());
				int drunkLevel = 0;
				for(CachedEntity buff:buffs)
					if ("Drunk".equals(buff.getProperty("name")))
						drunkLevel++;

				String message = contents;
				for(int i = 0; i<drunkLevel; i++)
					message = doDrunkText(message);
				
				
				// Just for fun
				message = message.replace("kill", "krill");
				
				// Clean the message so it doesn't contain any threatening characters
				message = message.replace(">", "&gt;");
				message = message.replace("<", "&lt;");

				message = message.replaceAll("\\&lt;(http.*?)\\|(.*?)\\&gt;", "<a href='$1' target='_blank'>$2</a>");
				message = message.replaceAll("\\&lt;(http.*?)\\&gt;", "$1");
				message = message.replaceAll("\\[(.*?)\\]\\((http.*?)\\)", "<a href='$2' target='_blank'>$1</a>");
				
				
				
				
				if (message.startsWith("/competition"))
				{
					message = "<a href='https://initiumcompetition.appspot.com' target='_blank'>[View the official competition page]</a> "+message.substring(12);
				}
				
				if (message.startsWith("/guide"))
				{
					message = "<a href='http://initium.wikia.com/wiki/Starter_Guide' target='_blank'>[View the player-made starter guide]</a> "+message.substring(6);
				}
				
				if (message.startsWith("/faq"))
				{
					message = "<a href='http://initium.wikia.com/wiki/Staub%27s_FAQ_Guide' target='_blank'>[View the player-made FAQ]</a> "+message.substring(4);
				}
				
					
				if (message.startsWith("/changelog"))
				{
					message = "<a onclick='viewChangelog()'>[View the change log]</a> "+message.substring(10);
				}
				
				if (message.startsWith("/exchange"))
				{
					message = "<a onclick='viewExchange()'>[Visit the Global Premium Membership Token Exchange page]</a> "+message.substring(9);
				}
				
				if (message.startsWith("/profile"))
				{
					message = "<a onclick='viewProfile()'>[Visit your Profile Page]</a> "+message.substring(8);
				}
				
				if (message.startsWith("/merchant"))
				{
					message = "<a onclick='viewStore("+character.getKey().getId()+")'>[Visit Store]</a> "+message.substring(9);
				}
				
				if (message.startsWith("/store"))
				{
					message = "<a onclick='viewStore("+character.getKey().getId()+")'>[Visit Store]</a> "+message.substring(6);
				}
				
				if (message.startsWith("/groups"))
				{
					message = "<a href='http://initium.wikia.com/wiki/Category:Player_Groups' target='_blank'>[Visit the player-made groups list]</a> "+message.substring(7);
				}
					
				if (message.startsWith("/group"))
				{
					Key groupKey = (Key)character.getProperty("groupKey");
					CachedEntity group = db.getEntity(groupKey);
					if (group!=null)
					{
						message = "<a onclick='viewGroup("+group.getKey().getId()+")'>[Visit the '"+group.getProperty("name")+"' group page]</a> "+message.substring(6);
					}
				}
				
				
				
				
				
				if (message.startsWith("/customize"))
				{
					message = "<a onclick='customizeItemOrderPage()'>[Visit the customize item order page]</a> "+message.substring(10);
				}
					
				if (message.startsWith("/wiki"))
				{
					message = "<a href='http://initium.wikia.com/wiki/Initium_Wiki' target='_blank'>[Visit the player-made wiki]</a> "+message.substring(5);
				}
					
				if (message.startsWith("/app"))
				{
					message = "<a href='http://initium-resources.appspot.com/android.apk'>[Android App Download]</a> &#8226; <a href='http://initium-resources.appspot.com/windows.xap'>[Windows Phone App Download]</a> &#8226; [iOS App Coming Soon...] "+message.substring(4);
				}
				
				if (message.startsWith("/about"))
				{
					message = "<a href='about.jsp'>[Visit Initium About Page]</a> "+message.substring(6);
				}
				
				if (message.startsWith("/quickstart"))
				{
					message = "<a href='quickstart.jsp'>[Visit Quick Start Page]</a> "+message.substring(11);
				}
				
				if (message.startsWith("/mechanics"))
				{
					message = "<a href='odp/mechanics.jsp'>[Visit Game Mechanics Page]</a> "+message.substring(10);
				}
				
				if (message.startsWith("/premium"))
				{
					message = "For information on becoming a premium member <a onclick='viewProfile()'>[Visit Your Profile Page]</a> "+message.substring(8);
				}

				if (message.startsWith("/map"))
				{
					message = "<a href='http://i.imgur.com/OU61I5e.jpg' target='_blank'>[Visit the player-made world map]</a> "+message.substring(4);
				}
				
				CachedDatastoreService ds = db.getDB();
				if (message.startsWith("/ignore "))
				{
					message = message.substring(8);
					
					// Now message only contains the character name, lets try to find it
					CachedEntity c = db.getCharacterByName(message);
					if (c==null)
					{
						//throw new UserErrorMessage("Unable to find a character by the name of '"+message+"' in the database.");
					}
					else
					{
						if ("NPC".equals(c.getProperty("type")))
							return;
						else
							message = "<a onclick='ignoreAPlayer("+c.getId()+", \""+message.replace("'", "\\'")+"\");'>[Click here to ignore '"+message+"']</a>";
					}
					
					
				}
				
				message = textReplace(ds, message);
				
				JSONObject payload = new JSONObject();
				Date d = new Date();

				String nicknameStyled = GameUtils.renderCharacter(db.getEntity((Key)character.getProperty("userKey")), character);
				String nicknameMeStyled = GameUtils.renderCharacter(db.getEntity((Key)character.getProperty("userKey")), character, true, true);
				payload.put("createdDate", d.getTime());
				payload.put("characterId", character.getKey().getId());
				payload.put("nickname", characterName);
				payload.put("nicknameStyled", nicknameStyled);
				payload.put("nicknameMeStyled", nicknameMeStyled);
				switch(channel) {
				case "public":
					respBody.put("id", "public");
					payload.put("message", message);
					payload.put("code", "GlobalChat");
					respBody.put("success", true);
					break;
				case "location":
					Long locationId = ((Key)character.getProperty("locationKey")).getId();
					respBody.put("id", "L"+locationId);
					payload.put("message", message);
					payload.put("code", "LocationChat");
					respBody.put("success", true);
					break;
				case "group":
					String groupId = "";
					if (CommonChecks.checkCharacterIsMemberOfHisGroup(character)) {
						Long gId = ((Key)character.getProperty("groupKey")).getId();
						groupId = "G" + gId.toString();
						respBody.put("id", groupId);
						payload.put("message", message);
						payload.put("code", "GroupChat");
						respBody.put("success", true);
					}
					break;
				case "party":
					String partyCode = ((String)character.getProperty("partyCode"));
					String partyId = "";
					if (partyCode != null) {
						partyId = "P" + partyCode;
						respBody.put("id", partyId);
						payload.put("message", message);
						payload.put("code", "PartyChat");
						respBody.put("success", true);
					}
					break;
				case "private":
					CachedEntity toCharacter = null;
					// Handle sending to Character ID or Name
					if (target.startsWith("#"))
					{
						try
						{
							toCharacter = db.getCharacterById(Long.parseLong(target.substring(1)));
							target = (String)toCharacter.getProperty("name");
						}
						catch(Exception e)
						{
							//ignore exceptions here
						}
						if (toCharacter==null) {
							// TODO: handle invalid target
						}
					}
					else
					{
						toCharacter = db.getCharacterByName(target);
						if (toCharacter==null) {
							// TODO: handle invalid target
						}
					}
					
					payload.put("message", "To " + target + " -> " + message);
					respBody.put("id", toCharacter.getKey().getId() + "/" + character.getKey().getId());
					payload.put("code", "PrivateChat");
					respBody.put("success", true);
					break;
				default:
					// TODO decide if we need a default case
					
					break;
				}
				respBody.put("payload", payload);
				break;
			}
			System.out.println("responding with: " + respBody.toJSONString());
			response.setHeader("content-type", "application/json");
			response.getWriter().append(respBody.toJSONString());
		} catch (ParseException e) {
			throw new RuntimeException("Parse exception", e);
		}
	}

	public String doDrunkText(String originalText)
	{
		int type = rnd.nextInt(9);
		
		if (type==0)
		{
			// Double up on some letters...
			int place = rnd.nextInt(originalText.length());
			String chr = originalText.charAt(place)+"";
			
			return new StringBuilder(originalText).insert(place, chr).toString();			
		}
		else if (type==1)
		{
			// Remove a letter
			int place = rnd.nextInt(originalText.length());
			
			return new StringBuilder(originalText).replace(place,place+1, "").toString();			
		}
		else if (type==2)
		{
			int place = rnd.nextInt(originalText.length());
			
			String firstHalf = originalText.substring(0, place);
			String secondHalf = originalText.substring(place, originalText.length());
			// Replace th with sh
			return firstHalf+secondHalf.replaceFirst("th ", "sh ");			
		}
		else if (type==3)
		{
			int place = rnd.nextInt(originalText.length());
			
			String firstHalf = originalText.substring(0, place);
			String secondHalf = originalText.substring(place, originalText.length());
			// Replace a with eh
			return firstHalf+secondHalf.replaceFirst(" a ", " eh ");			
		}
		else if (type==4)
		{
			int place = rnd.nextInt(originalText.length());
			
			String firstHalf = originalText.substring(0, place);
			String secondHalf = originalText.substring(place, originalText.length());
			// Replace a space with uhhhh
			return firstHalf+secondHalf.replaceFirst(" ", " ..uhhhh.. ");			
		}
		else if (type==5)
		{
			int place = rnd.nextInt(originalText.length());
			
			String firstHalf = originalText.substring(0, place);
			String secondHalf = originalText.substring(place, originalText.length());
			// Replace a space with uhhhh
			return firstHalf+secondHalf.replaceFirst("o", "u");			
		}
		else if (type==6)
		{
			return originalText.toUpperCase();
		}
		else if (type==7)
		{
			return originalText+" ..hic";
		}
		else if (type==8)
		{
			if (rnd.nextInt(3)==0)
			{
				int ver = rnd.nextInt(9);
				if (ver==0)
					return "/me tries to speak but pukes instead";
				else if (ver==1)
					return "/me wanted to say something but fell instead";
				else if (ver==2)
					return "/me fell asleep briefly just as he was about to say something";
				else if (ver==3)
					return "/me was going to say something but started heaving";
				else if (ver==4)
					return "/me opened their mouth to speak but forgot what they were going to say";
				else if (ver==5)
					return "/me passes out for a moment";
				else if (ver==6)
					return "/me was about to say something but accidentally fell into /r/initium";
				else if (ver==7)
					return "/me starts rambling excitedly. Something about premium membership..";
				else if (ver==8)
					return "/me runs off, stark naked, into a near by field screaming \""+originalText.replace("/", "")+"\"!!";
				else if (ver==9)
					return "/me finds himself almost too drunk to say \""+originalText.replace("/", "").toUpperCase()+"\"";
				else if (ver==10)
					return "/me finds a wild dog and tells him \""+originalText.replace("/", "")+"\"";
			}
			else
				return originalText;
		}
		
		return originalText;
	}
	
	private String textReplace(CachedDatastoreService ds, String text)
	{
		text = text.replaceAll("(((?<!')https?):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)", "<a href='$1' target='_blank'>$1</a>");
		//&#106;&#97;&#118;&#97;&#115;&#99;&#114;&#105;&#112;
		text = text.replaceAll("[Jj(&#106;)][Aa(&#97;)][Vv(&#118;)][Aa(&#97;)][Ss(&#115;)][Cc(&#99;)][Rr(&#114;)][Ii(&#105;)][Pp(&#112;)][Tt]\\s*[:]", "");	// Wreck any attempts to execute javascript in-line

		text = text.replaceAll("(^| )/(r/[A-Za-z0-9_-]+)", "$1<a href='http://reddit.com/$2' target='_blank'>/$2</a>");
		text = text.replaceAll("(^| )/(u/[A-Za-z0-9_-]+)", "$1<a href='http://reddit.com/$2' target='_blank'>/$2</a>");
		
		Pattern p = Pattern.compile("Item\\((\\d+)\\)");
		Matcher m = p.matcher(text);
		int matchCount = 0;
		while (m.find()) {
			
			String itemIdStr = m.group(1);
			CachedEntity item;
			try 
			{
				Long itemId = Long.parseLong(itemIdStr);
				item = ds.get(KeyFactory.createKey("Item", itemId));
				String itemText = "<div class='chat-embedded-item'>"+GameUtils.renderItem(item)+"</div>";
				text = text.replace("Item("+itemIdStr+")", itemText);
			} catch (Exception e) 
			{
				String itemText = "[Item "+itemIdStr+" not found]";
				text = text.replace("Item("+itemIdStr+")", itemText);
			}
			
			matchCount++;
			if (matchCount>5)
				break;
		}
		
		if (text.toLowerCase().startsWith("/roll "))
			text = "{{"+text.substring(6)+"}}";
		text = GameUtils.resolveFormulas(text, false, false);
		
		if (text.contains("dice1.png") && text.length()>3000)
			text = "ERROR: Dice roll formula was too complex.";
		
		return text;
	}
//	public static
}
