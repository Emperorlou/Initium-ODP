package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.aspects.AspectSlottable;
import com.universeprojects.miniup.server.aspects.AspectSlotted;

public class ModifierService extends Service
{
	public enum ModifierType
	{
		strength,
		intelligence,
		dexterity,
		
		physicalDamage,
		bludgeoningDamage,
		slashingDamage,
		piercingDamage,
		
		bludgeoningDamageReduction,
		slashingDamageReduction,
		piercingDamageReduction,
		
		movementSpeed,	
		skillExecutionSpeed,
		avoidDetection,		
	}
	public ModifierService(ODPDBAccess db)
	{
		super(db);
	}

	public ModifierType getModifierType(String name)
	{
		try
		{
			return ModifierType.valueOf(name);
		} catch(Exception e)
		{
			return null;
		}
	}
	
	public String getFullName(ModifierType modifierType)
	{
		String name = modifierType.name();
		
		name = name.replaceAll("([A-Z])", " $1");
		
		name = name.toLowerCase();
		
		return name;
	}
	
	public String getFullValue(ModifierType modifierType, String modifierLine)
	{
		String modifier = modifierLine.substring(modifierType.toString().length());
		
		modifier = modifier.replace(" ", "");
		
		if (modifier.contains("-")==false && modifier.contains("+")==false)
			modifier = "+"+modifier;
		
		return modifier;
	}
	
	public List<String> getFullModifierLines(CachedEntity modifierEntity)
	{
		Object rawModifiers = modifierEntity.getProperty("modifiers");
		if (rawModifiers==null) return null;
		return getFullModifierLines((List<String>)rawModifiers);
	}
 	
	public List<String> getFullModifierLines(EmbeddedEntity modifierEntity)
	{
		Object rawModifiers = modifierEntity.getProperty("modifiers");
		return getFullModifierLines((List<String>)rawModifiers);
	}

	
	
	public List<String> getFullModifierLines(List<String> rawModifiers)
	{
		if (rawModifiers==null) return null;
		List<String> modifiers = new ArrayList<>((List<String>)rawModifiers);
		
		for(int i = 0; i<modifiers.size(); i++)
		{
			try
			{
				String modifierLine = modifiers.get(i);
				if(modifierLine == null || modifierLine.length() == 0)
					continue;
				
				ModifierType modifierType = getModifierTypeFrom(modifierLine);
				String fullValue = getFullValue(modifierType, modifierLine);
				String fullName = getFullName(modifierType);
				
				if (fullValue.contains("%"))
					modifiers.set(i, fullValue+" "+fullName);
				else
					modifiers.set(i, fullValue+" to "+fullName);
			}
			catch(Exception e)
			{
				modifiers.add("Error: "+e.getMessage());
			}
		}
		
		return modifiers;
	}
	
	@SuppressWarnings("unchecked")
	public Double getAffectedValue(Double startingValue, CachedEntity modifierEntity, ModifierType modifierType)
	{
		Object rawModifiers = modifierEntity.getProperty("modifiers");
		List<String>modifiers = new ArrayList<>();
		
		//if rawmodifiers is null, it will just add nothing to the list. I think?
		modifiers.addAll((List<String>)rawModifiers);
		
		InitiumObject obj = new InitiumObject(db, modifierEntity);
		
		if(obj.hasAspect(AspectSlotted.class)) {
			AspectSlotted slottedAspect = obj.getAspect(AspectSlotted.class);
			Map<InitiumObject, AspectSlottable> items = slottedAspect.getSlottedItems();
			
			for(Map.Entry<InitiumObject, AspectSlottable> entry:items.entrySet()) {
				modifiers.addAll(entry.getValue().getStoredModifiers());
			}
		}
		
		if(modifiers.size() == 0) return startingValue;
		
		Double buffValue = 0d;
		for(String modifierLine:modifiers)
		{
			if (getModifierTypeFrom(modifierLine) == modifierType)
			{
				buffValue += modifyValue(startingValue, modifierType, modifierLine) - startingValue;
			}
		}
		
		return startingValue + buffValue; 
	}
	
	public Long getAffectedValue(Long startingValue, CachedEntity modifierEntity, ModifierType modifierType)
	{
		Double dblValue = startingValue.doubleValue();
		
		return Math.round(getAffectedValue(dblValue, modifierEntity, modifierType)); 
	}
	
	private double modifyValue(double startingValue, ModifierType modifierType, String modifierLine)
	{
		String modifier = modifierLine.substring(modifierType.toString().length());
		
		modifier = modifier.trim().replace("+", "");
		if (modifier.endsWith("%"))
		{
			modifier = modifier.replace("%", "");
			Double multiplier = Double.parseDouble(modifier);
			startingValue *= (multiplier/100d+1);
		}
		else
		{
			Double delta = Double.parseDouble(modifier);
			startingValue += delta;
		}
		
		return startingValue;
	}
	
	private ModifierType getModifierTypeFrom(String modifierLine)
	{
		ModifierType selected = null;
		for(ModifierType type:ModifierType.values())
		{
			if (modifierLine.startsWith(type.toString()))
			{
				if (selected!=null && selected.toString().length()>type.toString().length())
					continue;
						
				selected = type;
			}
		}
		
		return selected;
	}
	
}
