package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Allows the player to switch between owned characters
 * 
 * @author jenga201
 * @author Evan
 */
public class CommandSwitchCharacter extends Command {

	public CommandSwitchCharacter(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();

		// Up front authentication validation
		CachedEntity user = db.getCurrentUser();
		if (user == null) {
			throw new UserErrorMessage(
					"You cannot switch users if you're not logged into a user account. Are you just using a throwaway? If so, try converting the throwaway to a full account first.");
		}
		
		CachedEntity currentCharacter = db.getCurrentCharacter();

		// Get characterId to switch to
		Long targetCharacterId = null;
		
		//This allows for hotkeys to loop through characters, either in alphabetical order or characters in the same party.
		try {
			int direction;
			direction = Integer.parseInt(parameters.get("direction"));
			
			//This method will return all characters that we can switch to. Filters out zambies and dead characters right off the bat.
			
			switch(direction) {
			
			case 1:
			case 2:
				List<CachedEntity> characters = db.getAlphabetSortedValidCharactersByUser(user.getKey());
				
				for(int i = 0; i < characters.size(); i++) {
					if(!GameUtils.equals(characters.get(i).getKey(), currentCharacter.getKey())) continue;
					
					int dir = direction == 1 ? -1 : 1;
					targetCharacterId = characters.get((i+ characters.size() + dir) % characters.size()).getId();
					break;
				}
				break;
			case 3:
			case 4:
				//here, we need to grab all the partied characters. then, filter by userkey.
				List<CachedEntity> party = db.getParty(ds, currentCharacter);
				if(party == null) throw new UserErrorMessage("You aren't in a party.");
				
				//now we narrow down the list of characters in the party to the ones that are
				//associated with the same user.
				List<CachedEntity> userCharsInParty = new ArrayList<>();
				for(CachedEntity c:party) {
					if(GameUtils.equals(c.getProperty("userKey"), currentCharacter.getProperty("userKey"))) {
						userCharsInParty.add(c);
					}
				}

				//Iterate through characters in the same party and userke
				if(direction == 3) {
					//now that we've narrowed down our characters, we search for the index of our character and then increment the index.
					if(userCharsInParty.size() == 1) throw new UserErrorMessage("You don't have any more characters in this party.");
					
					//sort the party, only if we need to.
					Collections.sort(userCharsInParty, new Comparator<CachedEntity>()
					{
						@Override
						public int compare(CachedEntity o1, CachedEntity o2)
						{
							return ((String)o1.getProperty("name")).compareTo((String)o2.getProperty("name"));
						}
					});
					
					//this loop locates where we are in the party, and then
					for(int i = 0; i < userCharsInParty.size(); i++) {
						if(GameUtils.equals(userCharsInParty.get(i), currentCharacter)) {
							
							//if we're at the end index, go back to 0.
							if(i - 1 == userCharsInParty.size()) {
								targetCharacterId = userCharsInParty.get(0).getId();
								break;
							}
							
							//if we're not at the end index, go to the next one.
							targetCharacterId = userCharsInParty.get(i+1).getId();	
							break;
						}
					}
					break;
				}
				
				//Switch to the party leader, if the leader is associated with the same userkey as the active user.
				if(direction == 4) {
					CachedEntity leader = db.getPartyLeader(ds, (String)currentCharacter.getProperty("partyCode"), party);
					
					if(leader != null) {
						targetCharacterId = leader.getId();
						break;
					}
					throw new UserErrorMessage("The party leader isn't on this account.");
				}
				
			//if the direction wasn't 1-4, break.
			default: break;
			}
		} 
		catch(NumberFormatException e) {
			//silently fail if no direction is specified.
		}
		
		//if we couldn't find a character ID based on the direction specified, we grab the parameter.
		if(targetCharacterId == null) {
			try {
				targetCharacterId = Long.parseLong(parameters.get("characterId"));
			} catch (NumberFormatException e) {
				throw new UserErrorMessage("This character doesn't exist.");
			}
		}
		
		// Get target character entity and validate
		CachedEntity targetCharacter = db.getEntity("Character", targetCharacterId);
		if (targetCharacter == null) {
			throw new UserErrorMessage("This character doesn't exist.");
		}

		// Compare userKey between current character and target character
		CachedEntity character = db.getCurrentCharacter();
		if (!GameUtils.equals(character.getProperty("userKey"), targetCharacter.getProperty("userKey"))) {
			throw new UserErrorMessage("The character you are trying to switch to does not belong to you.");
		}

		// Don't switch to characters with zombie status
		if (CommonChecks.checkCharacterIsZombie(targetCharacter)) {
			throw new UserErrorMessage("You cannot switch to this character, it is now a zombie.");
		}

		// Set and save new character
		user.setProperty("characterKey", targetCharacter.getKey());
		try {
			ds.put(user);
		} catch (Exception e) {
			throw new UserErrorMessage("Error while switching character: " + e.getMessage());
		}

		// Set the cached currentCharacter to target
		request.setAttribute("characterEntity", targetCharacter);
		
		// Update the verifyCode to the new character
		StringBuilder js = new StringBuilder();
		try {
			js.append("window.verifyCode = '" + db.getVerifyCode() + "';");
		} catch (NotLoggedInException e) {
			js.append("window.verifyCode = '';");
		}

		js.append("window.chatIdToken = '" + db.getChatIdToken(targetCharacter.getKey()) + "';");
		js.append("window.messager.idToken = window.chatIdToken;");
		js.append("window.characterId = " + targetCharacter.getId() + ";");

		// Consolidating this to quick refresh the page
		CachedEntity location = ds.getIfExists((Key) targetCharacter.getProperty("locationKey"));
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), targetCharacter, location, this);
		mpus.updateFullPage_shortcut();
		
		js.append("window.newChatIdToken= '"+db.getChatToken(targetCharacter.getKey())+"';");
		js.append("messager.reconnect();");
		js.append("$('.chat_messages').html('');");
		
		addJavascriptToResponse(js.toString());
	}
}
