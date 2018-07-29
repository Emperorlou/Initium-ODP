package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class ModifierService extends Service
{
	public enum ModifierType
	{
		strength,
		intelligence,
		dexterity,
		
		bludgeoningDamage,
		slashingDamage,
		piercingDamage,
		
		bludgeoningDamageReduction,
		slashingDamageReduction,
		piercingDamageReduction,
		
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
	
	public Double getAffectedValue(Double startingValue, CachedEntity modifierEntity, ModifierType modifierType)
	{
		Object rawModifiers = modifierEntity.getProperty("modifiers");
		if (rawModifiers==null) return startingValue;
		List<String> modifiers = (List<String>)rawModifiers;
		
		for(String modifierLine:modifiers)
		{
			if (getModifierTypeFrom(modifierLine) == modifierType)
			{
				startingValue = modifyValue(startingValue, modifierType, modifierLine);
			}
		}
		
		return startingValue; 
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
