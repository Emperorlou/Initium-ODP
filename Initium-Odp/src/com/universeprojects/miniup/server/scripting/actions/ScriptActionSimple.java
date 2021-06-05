package com.universeprojects.miniup.server.scripting.actions;

import java.util.Map;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.ODPDBAccess.CharacterMode;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.events.SimpleEvent;

public class ScriptActionSimple extends ScriptAction{

	public ScriptActionSimple(ODPDBAccess db, OperationBase operation, Map<String, String> parameters) {
		super(db, operation, parameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void validateCharacterState(CachedEntity character) throws UserErrorMessage
	{
		if(GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("You are incapacitated and cannot trigger any effects!");
		if(GameUtils.enumEquals(character.getProperty("mode"), CharacterMode.COMBAT))
			throw new UserErrorMessage("You cannot trigger this effect in combat!");
	}
	
	@Override
	protected void validateScriptState(CachedEntity trigger, CachedEntity script) throws UserErrorMessage
	{
		
		//If callback is NOT null and allowLong is false, throw an error.
		//If callback is null and allowLong is true, throw an error.
		if(isLongScript() == (script.getProperty("callback") == null))
			throw new UserErrorMessage("Invalid State");
		
		switch(trigger.getKind())
		{
			case "Item":
				if(GameUtils.enumEquals(script.getProperty("type"), ScriptType.directItem) == false)
					throw new UserErrorMessage("Item does not support this effect.");
				break;
			case "Location":
				if(GameUtils.enumEquals(script.getProperty("type"), ScriptType.directLocation) == false)
					throw new UserErrorMessage("Location does not support this effect.");
				break;
			case "Character":
				// What sort of validation do we allow here? Will likely be directCharacter
				// script type, for <secret>magic</secret>. Just allow for now.
				break;
			default:
				throw new RuntimeException("Unexpected trigger source for script " + script.getProperty("name"));
		}
	}
	
	/**
	 * Returns if the execution of this script should be a long operation.
	 * @return
	 */
	protected boolean isLongScript() {
		return false;
	}
	
	@Override
	protected ScriptEvent generateEvent(CachedEntity character, CachedEntity triggerEntity, 
			CachedEntity script, Map<String, String> parameters) throws UserErrorMessage
	{
		return new SimpleEvent(character, db);
	}

	@Override
	protected void processParameters(ScriptEvent event,
			Map<String, String> parameters) throws UserErrorMessage {
	}

}
