package com.universeprojects.miniup.server.longoperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ODPInventionService;

public class LongOperationTeachIdea extends LongOperation{

	public LongOperationTeachIdea(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage {
		super(db, requestParameters);
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
		int value = 0;
		
		CachedEntity character = db.getCurrentCharacter();
		ODPInventionService ois = db.getInventionService(character, db.getKnowledgeService(character.getKey()));
		
		List<Key> ideaKeys = null;
		
		String ideaIds = parameters.get("ideaIds");
		if(ideaIds != null) {
			ideaKeys = new ArrayList<Key>();
			for(String ideaId : ideaIds.split(","))
				ideaKeys.add(KeyFactory.createKey("ConstrucItemIdea", Long.parseLong(ideaId)));
		}
		else throw new UserErrorMessage("No ideas were selected.");
		
		List<Key> studentKeys = null;
		
		String characterIds = parameters.get("characterIds");
		if(characterIds != null) {
			studentKeys = new ArrayList<Key>();
			for(String charId : characterIds.split(","))
				studentKeys.add(KeyFactory.createKey("Character", Long.parseLong(charId)));
		}
		else throw new UserErrorMessage("No students were selected.");
		
		db.pool.loadEntities(ideaKeys);
				
		List<CachedEntity> ideaEnts = db.pool.get(ideaKeys);
		List<Key> ideaDefKeys = new ArrayList<Key>();
		
		for(CachedEntity idea : ideaEnts) {
			db.pool.addToQueue(idea.getProperty("_definitionKey"));
			ideaDefKeys.add((Key) idea.getProperty("_definitionKey"));
		}
		
		List<CachedEntity> ideaDefs = db.pool.get(ideaDefKeys);
		
		for(CachedEntity ideaDef : ideaDefs) {
			//value += ois.getIdeaTeachingTime(character, null, ideaDef);
		}
		
		setDataProperty("ideaDefKeys", ideaDefKeys);
		setDataProperty("studentKeys", studentKeys);
		
		return value;
	}

	@Override
	String doComplete() throws UserErrorMessage, UserRequestIncompleteException, ContinuationException {
		CachedEntity character = db.getCurrentCharacter();
		
		List<Key> studentKeys = (List<Key>) getDataProperty("studentKeys");
		List<Key> ideaDefKeys = (List<Key>) getDataProperty("ideaDefKeys");
		
		db.pool.addToQueue(studentKeys);
		db.pool.addToQueue(ideaDefKeys);
		
		List<CachedEntity> students = db.pool.get(studentKeys);
		List<CachedEntity> ideaDefs = db.pool.get(ideaDefKeys);
		
		QueryHelper query = new QueryHelper(db.getDB());
		
		for(CachedEntity ideaDef : ideaDefs) {
			nextStudent:
			for(CachedEntity student : students) {
				//make sure the student is still valid. Ensure that they're still in learn mode, and that they're still in the same location.
				
				if(GameUtils.equals(student.getProperty("locationKey"), character.getProperty("locationKey")) == false) continue;
				if(GameUtils.equals(student.getProperty("mode"), "LEARNING") == false) continue; //not sure if this is how we want to do this.
				
				//get all ideas that this character has. If they dont already have this idea, give it to them.
				
				List<CachedEntity> currentIdeas = query.getFilteredList("ConstructItemIdea", "characterKey", student.getKey());
				
				for(CachedEntity currentIdea : currentIdeas) {
					if(GameUtils.equals(currentIdea.getProperty("_definitionKey"), ideaDef.getKey())) {
						continue nextStudent;
					}
				}
				
				//we are good to teach this idea. How?
				
			}
		}
		
		return "Teaching complete.";
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		return "doTeach(null)";
	}

}
