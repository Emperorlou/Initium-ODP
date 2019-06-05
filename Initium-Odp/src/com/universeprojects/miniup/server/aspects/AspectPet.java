package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumEntityPool;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class AspectPet extends ItemAspect
{
	public enum PetStatus
	{
		Satisfied, Hungry, Starving, Dead
	}

	public AspectPet(InitiumObject object)
	{
		super(object);
	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter)
	{
		if (CommonChecks.checkItemIsAccessible(object.getEntity(), currentCharacter))
		{
			ItemPopupEntry ipe = new ItemPopupEntry("Feed", "Feed this pet food that you have in your inventory or in your current location.",
					"viewPetFoodOptions(event, " + object.getKey().getId() + ")");
			return Arrays.asList(new ItemPopupEntry[]
			{
					ipe
			});
		}
		else
			return null;
	}

	@Override
	public String getPopupTag()
	{
		return null;
	}

	@Override
	protected boolean update()
	{
		boolean changed = super.update();

		if (determinePetStatus()==true)
			changed = true;

		return changed;
	}

	private boolean determinePetStatus()
	{
		String oldPetIcon = getInitiumObject().getIcon();
		
		// Get the satisfied value as a percentage, lower than 0% means hungry and not satisfied
		long currentMs = System.currentTimeMillis();
		long outOfFoodMs = getOutOfFoodDate().getTime();
		long remainingSatisfiedTimeMs = outOfFoodMs-currentMs;
		long maxSatisfiedMs = getHoursSatisfied().longValue()*3600000L;
		
		double satisfiedPercent = (double)remainingSatisfiedTimeMs/(double)maxSatisfiedMs*100;
		
		if (satisfiedPercent>=0)
			getInitiumObject().setIcon(getMainImage());
		else if (satisfiedPercent>=-20)
			getInitiumObject().setIcon(getHungryImage());
		else if (satisfiedPercent>=-50)
			getInitiumObject().setIcon(getStarvingImage());
		else 
			doDeath();
			
		return !GameUtils.equals(oldPetIcon, getInitiumObject().getIcon());
	}

	public void doDeath()
	{
		InitiumObject petObject = getInitiumObject();
		
		petObject.setIcon(getDeadImage());
		
		AspectBuffable buffable = petObject.getAspect(AspectBuffable.class);
		if (buffable!=null)
			buffable.clearAllBuffs();
		
		petObject.setProperty("modifiers", null);
		
	}
	
	public PetStatus getPetStatus()
	{
		if (GameUtils.equals(getInitiumObject().getIcon(), getMainImage()))
			return PetStatus.Satisfied;
		else if (GameUtils.equals(getInitiumObject().getIcon(), getHungryImage()))
			return PetStatus.Hungry;
		else if (GameUtils.equals(getInitiumObject().getIcon(), getStarvingImage()))
			return PetStatus.Starving;
		else if (GameUtils.equals(getInitiumObject().getIcon(), getDeadImage()))
			return PetStatus.Dead;

		return PetStatus.Satisfied;
	}

	@SuppressWarnings("unchecked")
	public List<Key> getBuffs()
	{
		return (List<Key>) getProperty("buffs");
	}

	public List<List<String>> getFieldFilters()
	{
		return db.getValueFromFieldTypeFieldFilter2DCollection(entity, "foodMatcher");
	}

	public String getDeadImage()
	{
		return (String) getProperty("deadImage");
	}

	public String getStarvingImage()
	{
		return (String) getProperty("starvingImage");
	}

	public String getHungryImage()
	{
		return (String) getProperty("hungryImage");
	}

	public String getMainImage()
	{
		return (String) getProperty("mainImage");
	}

	public Date getLastUpdate()
	{
		return (Date) getProperty("lastUpdate");
	}

	public Double getMaxFood()
	{
		return (Double) getProperty("maxFood");
	}

	public Double getHoursSatisfied()
	{
		return (Double) getProperty("hoursSatisfied");
	}

	public Long getFeedingImageMs()
	{
		return (Long) getProperty("feedingImageMs");
	}

	public String getFeedingImage()
	{
		return (String) getProperty("feedingImage");
	}

	public Date getOutOfFoodDate()
	{
		return (Date) getProperty("outOfFoodDate");
	}
	
	
	public void addOutOfFoodDateTime(int ms)
	{
		Date date = getOutOfFoodDate();
		if (date==null) date = new Date();
		long dateMs = date.getTime();
		
		dateMs+=ms;
		
		setOutOfFoodDate(new Date(dateMs));
	}
	
	public void setOutOfFoodDate(Date date)
	{
		setProperty("outOfFoodDate", date);
	}
	
	public void setCurrentFood(Double food)
	{
		setProperty("currentFood", food);
	}

	public void setBuffs(List<Key> buffs)
	{
		setProperty("buffs", buffs);
	}

	public void setDeadImage(String deadImage)
	{
		setProperty("deadImage", deadImage);
	}

	public void setStarvingImage(String starvingImage)
	{
		setProperty("starvingImage", starvingImage);
	}

	public void setHungryImage(String hungryImage)
	{
		setProperty("hungryImage", hungryImage);
	}

	public void setMainImage(String mainImage)
	{
		setProperty("mainImage", mainImage);
	}

	public void setLastUpdate(Date lastUpdate)
	{
		setProperty("lastUpdate", lastUpdate);
	}

	public void setMaxFood(Double maxFood)
	{
		setProperty("maxFood", maxFood);
	}

	public void setHoursSatisfied(Double hoursSatisfied)
	{
		setProperty("hoursSatisfied", hoursSatisfied);
	}

	public void setFeedingImageMs(Long feedingImageMs)
	{
		setProperty("feedingImageMs", feedingImageMs);
	}

	public void setFeedingImage(String feedingImage)
	{
		setProperty("feedingImage", feedingImage);
	}
	
	
	
	public double getRemainingFoodSupplies()
	{
		long currentMs = System.currentTimeMillis();
		long outOfFoodMs = getOutOfFoodDate().getTime();
		long remainingSatisfiedTimeMs = outOfFoodMs-currentMs;
		long maxSatisfiedMs = getHoursSatisfied().longValue()*3600000L;
		
		double satisfiedPercent = (double)remainingSatisfiedTimeMs/(double)maxSatisfiedMs;
		return getMaxFood() * satisfiedPercent;
	}

	public boolean isReadyToEat()
	{
		return getDesiredFoodKg()>0;
	}

	public double getSatisfiedMsTimeFor1Kg()
	{
		return getHoursSatisfied()/getMaxFood()*3600000;
	}
	
	public Double getDesiredFoodKg()
	{
		Double maxFood = getMaxFood();
		Double currentFood = getRemainingFoodSupplies();
		if (maxFood==null) maxFood = 2d;
		
		return maxFood-currentFood;
	}

	/**
	 * 
	 * @param food
	 * @return How much durability is restored
	 */
	public boolean feed(CachedEntity food)
	{
		Double desiredFood = getDesiredFoodKg();

		if (desiredFood<=0) return false;
		
		if (food.getProperty("weight")==null) return true;
		
		Double weight = ((Long) food.getProperty("weight")).doubleValue();
		if (weight.equals(0d)) return true;
		weight/=1000;

		if (weight>desiredFood) weight = desiredFood;
		
		// Determine how long we will be satisfied now
		double satisfiedMs = getSatisfiedMsTimeFor1Kg()*weight;
		addOutOfFoodDateTime((int)satisfiedMs);
		
		return true;
	}

	public void filterFoodItems(List<CachedEntity> food)
	{
		for (int i = food.size() - 1; i >= 0; i--)
		{
			if (food.get(i) == null)
				food.remove(i);
			else if (db.validateFieldFilter(object.getEntity(), "Pet:foodMatcher", food.get(i)) == false)
				food.remove(i);
			else if (CommonChecks.checkItemIsEquipped(food.get(i).getKey(), db.getCurrentCharacter())) food.remove(i);
		}
	}

	static
	{
		addCommand("PetFeed", AspectPet.CommandPetFeed.class);
	}

	public static class CommandPetFeed extends Command
	{

		public CommandPetFeed(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
		{
			Key petKey = KeyFactory.createKey("Item", Long.parseLong(parameters.get("petId")));
			InitiumObject pet = db.getInitiumObject(petKey);

			if (pet.isAspectPresent(AspectPet.class) == false) throw new UserErrorMessage("Something isn't right. Try doing the feed thing again?");

			if (CommonChecks.checkItemIsAccessible(pet.getEntity(), db.getCurrentCharacter()) == false) throw new UserErrorMessage("You're not near this pet.");

			AspectPet petAspect = (AspectPet) pet.getInitiumAspect("Pet");

			CachedEntity character = db.getCurrentCharacter();

			CachedDatastoreService ds = db.getDB();
			InitiumEntityPool pool = new InitiumEntityPool(ds, db.getGridMapService());
			List<Key> eatenKeys = new ArrayList<>();
			ds.beginBulkWriteMode();
			try
			{
				petAspect.update();

				if (petAspect.isReadyToEat() == false) throw new UserErrorMessage(pet.getName() + " is not ready to eat.");
				String[] itemIdStrs = (String[]) request.getParameterMap().get("itemId[]");
				if (itemIdStrs == null || itemIdStrs.length == 0) throw new UserErrorMessage("There is nothing here to feed your pet.");

				List<Key> itemKeys = new ArrayList<>();
				List<Long> itemIds = new ArrayList<>();
				for (String itemIdStr : itemIdStrs)
				{
					Long itemId = Long.parseLong(itemIdStr);
					itemIds.add(itemId);
					Key key = KeyFactory.createKey("Item", itemId);
					pool.addToQueue(key);
					itemKeys.add(key);
				}

				pool.loadEntities();

				List<CachedEntity> food = pool.get(itemKeys);

				// Make sure each food item we're going to have is valid
				petAspect.filterFoodItems(food);

				// Chop off any excess food (max 20 items at a time)
				if (food.size() > 20) food = food.subList(0, 20);

				boolean fedSomething = false;
				for (CachedEntity foodItem : food)
				{
					if (CommonChecks.checkItemIsAccessible(foodItem, character) == false)
						throw new UserErrorMessage("The '" + foodItem.getProperty("name") + "' is not accessible to the character in use.");


					boolean fedThis = petAspect.feed(foodItem);
					if (fedThis==false) continue;

					fedSomething = true;

					eatenKeys.add(foodItem.getKey());

				}
				if (fedSomething == false) throw new UserErrorMessage("Nothing was fed to the pet.");

				ds.delete(eatenKeys);

				boolean iconChanged = petAspect.update();

				if (iconChanged)
					pet.liveUpdateIcon(this);
				
				ds.put(pet.getEntity());

				String feedingImage = petAspect.getFeedingImage();
				Long feedingImageMs = petAspect.getFeedingImageMs();
				String regularImage = pet.getIcon();

				if (feedingImageMs != null && feedingImage != null)
					addJavascriptToResponse("doFeedPetAnimation(" + petKey.getId() + ", '" + feedingImage + "', " + feedingImageMs + ", '" + regularImage + "');");
				
				if (fedSomething)
					addJavascriptToResponse("updateNeededPetFood('"+GameUtils.formatNumber(petAspect.getDesiredFoodKg())+"');");
				// db.getGridMapService().putLocationDataIfChanged(ds);
				//
				// CachedEntity createCampfireSkillDef =
				// db.getEntity("ConstructItemIdeaDef", 4988291556573184L);//
				// This is the create campfire skill
				// if
				// (inventionService.getKnowledgeService().increaseKnowledgeFor(createCampfireSkillDef,
				// 1, 20))
				// db.sendGameMessage(ds, character, "Your Create Fire knowledge
				// increased by 1.");

			}
			finally
			{
				ds.commitBulkWrite();
			}

			// Delete all HTML of an item
			for (Key deletedKey : eatenKeys)
				if (deletedKey.getKind().equals("Item")) deleteHtml(".deletable-Item" + deletedKey.getId());

			db.getGridMapService().updateChangedTileGraphics(this);
		}

	}

}
