package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class AspectCardDeck extends InitiumAspect
{
	
	public AspectCardDeck(InitiumObject object)
	{
		super(object);
	}

	@Override
	protected void initialize()
	{

	}

	@SuppressWarnings("unchecked")
	public List<Key> getCards()
	{
		List<Key> cards = (List<Key>)this.getProperty("cards");
		if (cards==null) cards = new ArrayList<Key>();
		return cards;
	}
	
	public void setCards(List<Key> cards)
	{
		this.setProperty("cards", cards);
	}
	
	public void addCard(Key card)
	{
		List<Key> cards = getCards();
		cards.add(card);
		setCards(cards);
	}

	private Key getContainerKey()
	{
		return (Key)this.entity.getProperty("containerKey");
	}
	
	private Key getLocationKey(CachedEntity entity)
	{
		
		Key locationKeyCandidate = null;
		if (entity.getKind().equals("Item"))
		{
			locationKeyCandidate = (Key)entity.getProperty("containerKey");
		}
		else if (entity.getKind().equals("Character"))
		{
			locationKeyCandidate = (Key)entity.getProperty("locationKey");
		}
		else if (entity.getKind().equals("Location"))
		{}
		else 
			throw new RuntimeException("Unhandled kind: "+entity.getKind());
		
		if (locationKeyCandidate.getKind().equals("Location"))
			return locationKeyCandidate;
		
		return getLocationKey(db.getEntity(locationKeyCandidate));
	}
	
	private void sendSystemMessage(String text)
	{
		Key locationKey = getLocationKey(db.getEntity(getContainerKey()));
		
		db.sendSystemMessageToLocation(locationKey, text);
	}
	
	public Key removeRandomCard()
	{
		List<Key> cards = getCards();
		Collections.shuffle(cards);
		
		Key card = cards.remove(0);
		
		setCards(cards);
		
		return card;
	}
	
	public void addAllCardsFromInventory(Key characterKey)
	{
		QueryHelper query = new QueryHelper(db.getDB());
		
		List<CachedEntity> inventory = query.getFilteredList("Item", "containerKey", characterKey);

		List<Key> cards = getCards();
		cards.clear();
		
		for(CachedEntity item:inventory)
		{
			Object itemClass = item.getProperty("itemClass");
			if (itemClass instanceof String)
			{
				if (((String)itemClass).equals("Playing Card"))
					cards.add(item.getKey());
					
			}
			else if (itemClass instanceof Key)
			{
				if (((Key)itemClass).getName().equals("Playing Card"))
					cards.add(item.getKey());
			}
		}
		
		setCards(cards);
	}
	
	
	
	static 
	{
		addCommand("CardsAddFromInventory", AspectCardDeck.CommandCardsAddFromInventory.class);
		addCommand("CardsTakeACard", AspectCardDeck.CommandCardsTakeACard.class);
	}
	
	
	
	public static class CommandCardsAddFromInventory extends Command
	{

		public CommandCardsAddFromInventory(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
		{
			Key cardDeckKey = KeyFactory.stringToKey(parameters.get("cardDeckKey"));
			InitiumObject cardDeck = db.getInitiumObject(cardDeckKey);
			
			if (cardDeck.isAspectPresent(AspectCardDeck.class)==false)
				throw new UserErrorMessage("Invalid card deck.");

			AspectCardDeck cardDeckAspect = (AspectCardDeck)cardDeck.getInitiumAspect("CardDeck");

			CachedEntity character = db.getCurrentCharacter();
			
			if (CommonChecks.checkItemIsAccessible(cardDeck.getEntity(), character)==false)
				throw new UserErrorMessage("The card deck you're trying to use is not within reach.");
			
			cardDeckAspect.addAllCardsFromInventory(character.getKey());
			
			
			////////
			// Now piece together the message we will add to the location chat...
			List<Key> cardKeys = cardDeckAspect.getCards();
			List<CachedEntity> cards = db.getEntities(cardKeys);
			
			List<String> cardNames = new ArrayList<String>();
			for(CachedEntity card:cards)
				cardNames.add((String)card.getProperty("name"));
			
			Collections.sort(cardNames);
			StringBuilder sb = new StringBuilder();
			sb.append(character.getProperty("name")).append(" has reset the deck. It contains the following cards: ");
			
			boolean firstTime = true;
			for(String n:cardNames)
			{
				if (firstTime)
					firstTime = false;
				else
					sb.append(", ");
				
				sb.append(n);
			}

			db.getDB().put(cardDeck.getEntity());;
			
			cardDeckAspect.sendSystemMessage(sb.toString());
			
		}
	}
	
	
	
	
	
	public static class CommandCardsTakeACard extends Command
	{

		public CommandCardsTakeACard(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
		{
		}
	}
	
	@Override
	public Integer getVersion() {
		return 1;
	}

	
	
}
