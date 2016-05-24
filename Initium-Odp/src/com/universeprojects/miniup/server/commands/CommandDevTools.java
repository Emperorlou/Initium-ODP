package com.universeprojects.miniup.server.commands;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;


/**
 * Several Dev tool extensions
 * 
 * Usage notes:
 * Checks if caller is a Dev, and if so, executes the tool
 * 
 * Parameters:
 * 		tool - the name of the tool
 * 		[...] - params for the tool
 *  
 * Valid tools:
 * 		ResetInstanceLocation - Fixes any issues with an instance
 * 			locId - locationID of the location to reset
 * 				default: character location
 * 			entireInstance - boolean whether parentLocations need to be reset as well
 * 							 if true, will also kick all players to the start of the instance.
 * 				default: false  
 * 		
 * 		RestockTutorial - Restocks the tutorial item store
 * 			amount - number of items to restock to
 * 				default: 100
 * 		
 * 		Teleport - TP directly to a location
 * 			locId - locationID to be teleported to
 *		 
 * 		UpdateItems - Updates the fields of all previously generated items with that name
 * 			itemName - name of the items to be updated
 * 			[<fieldName>] - the new values  
 * 
 * @author NJ
 *
 */
public class CommandDevTools extends Command {

	public CommandDevTools(HttpServletRequest request, HttpServletResponse response) 
	{
		super(request, response);
	}
	
	/**
	 * All these helper function are to do with item generation and field types.
	 * Far from ideal, but since they're not in the ODP, the only way.
	 * Made private so the hacky nature of this is contained to this command only.
	 * So if the functionally later gets added to the ODP, this can quickly be replaced.
	 */
	
	private enum dataType {
		DD, Double, Long, String
	}
	
	// requires valid fieldName
	private dataType getDataType(String fieldName) {
		switch (fieldName) {
		case "weaponDamage":
			return dataType.DD;
		case "strengthRequirement": case "transportMovementSpeed": case "weaponDamageCriticalMultiplier":
			return dataType.Double;
		case "blockChance": case "damageReduction": case "dexterityPenalty": case "dogecoins": case "durability": case "maxSpace": case "maxWeight":
		case "space": case "warmth": case "weaponDamageCriticalChance": case "weaponRange": case "weaponRangeIncrement": case "weatherDamage": case "weight":
			return dataType.Long;
		default:
			return dataType.String;
		}
	}
	
	private Object resolve(Object value)
	{
		if (value==null) return null;
		String text = value.toString().trim();
		if (text.equals("")) return null;
		
		// Hacky as all hell, but better than reinventing the wheel...
		// Uses the URL for the Test button in the editor.
		InputStream is = null;
		String data = null;
		try {
			HttpURLConnection conn = (HttpURLConnection)(new URL("https://www.playinitium.com/admin/editor/gef?type=testCurve&curve="+text)).openConnection();
			
			// authenticate to the editor
			Cookie[] cookies = request.getCookies();
			String cookieString = null;
			for (Cookie cookie : cookies)
			{
				if (cookieString==null)
					cookieString = cookie.getName()+"="+cookie.getValue();
				else
					cookieString += "; "+cookie.getName()+"="+cookie.getValue();
			}
			if (cookieString!=null)
				conn.setRequestProperty("Cookie", cookieString);
			conn.connect();
			
			// Make sure we get an OK response
			int respCode = conn.getResponseCode();
			if (respCode==302)
				throw new RuntimeException("Could not authenticate. Make sure you are logged into the editor.");
			else if (respCode!=200)
				throw new RuntimeException("Could not load resolver. Response code: "+respCode);
			
			// Actually get the data
			is = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			data = br.readLine();
			if (data==null || (data=data.trim()).equals(""))
				throw new RuntimeException("No data returned.");
			else if (data.startsWith("DD"))
				return data;
			else
				return new Double(data.trim());
		} catch (NumberFormatException nfe) {
			// The URL returned an error message, so add that to the exception. 
			throw new RuntimeException("Unable to resolve the formula: "+text+"\n"+data);
		} catch (Exception e) {
			throw new RuntimeException("Unable to resolve the formula: "+text+"\n"+e.getMessage());
		} finally {
			try {
				if (is!=null) is.close();
			} catch (Exception e) {
			}
		}
	}
	
	private Double resolveDouble(Object value)
	{
		Object retVal = resolve(value);
		if (retVal == null)
			return null;
		if (retVal instanceof Double)
			return (Double)retVal;
		throw new RuntimeException("The formula didn't resolve to a valid double: "+value.toString().trim());
	}
	
	private Long resolveLong(Object value)
	{
		Object retVal = resolve(value);
		if (retVal == null)
			return null;
		if (retVal instanceof Double)
			return Math.round((Double)retVal);
		throw new RuntimeException("The formula didn't resolve to a valid long: "+value.toString().trim());
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();

		// Verify that caller is a Dev
		CachedEntity character = db.getCurrentCharacter(request);
		String nameClass = (String)character.getProperty("nameClass");
		if (nameClass==null || nameClass.contains("content-dev-nickname")==false)
			throw new UserErrorMessage("This command is only available to Dev characters.");
		
		// Verify parameter sanity
		String tool = parameters.get("tool");
		if (tool==null || (tool=tool.trim()).equals(""))
			throw new RuntimeException("DevTools invalid call format, 'tool' is not a valid tool.");
		
		CachedDatastoreService ds = getDS();

		switch (tool) {
		// tool=ResetInstanceLocation, locId=<xxx>, entireInstance=false|true
		case "ResetInstanceLocation":
		{
			// Verify parameter sanity
			String paramLocId = parameters.get("locId");
			CachedEntity location;
			if (paramLocId==null || paramLocId.trim().equals(""))
			{
				// Default to character location
				location = db.getEntity((Key)character.getProperty("locationKey"));
				if (location==null)
					throw new UserErrorMessage("Could not retrieve the character location.");
			}
			else
			{
				location = db.getLocationById(tryParseId(parameters, "locId"));
				if (location==null)
					throw new RuntimeException("DevTools invalid call format, 'locId' is not a valid id.");
			}
			
			// If we're not in an instance, we can't reset it
			if ("TRUE".equals(location.getProperty("instanceModeEnabled"))==false)
				throw new UserErrorMessage("This location is not an instance.");
			
			String paramEntireInstance = parameters.get("entireInstance");
			// Default to false
			boolean entireInstance = false;
			if (paramEntireInstance!=null && paramEntireInstance.trim().equals("")==false)
			{
				if (paramEntireInstance.toLowerCase().equals("true"))
					entireInstance = true;
				else if (paramEntireInstance.toLowerCase().equals("false")==false)
					throw new RuntimeException("DevTools invalid call format, 'entireInstance' must be true or false.");
			}
			
			// add all locations to reset to a list
			// the point of this is to be able to get the instance start beforehand
			// this instance start is stored in location
			List<CachedEntity> locList = new ArrayList<CachedEntity>();
			locList.add(location);
			while (entireInstance)
			{
				location = db.getEntity((Key)location.getProperty("parentLocationKey"));
				// if this location is no longer an instance, we found the start
				if (location==null || "TRUE".equals(location.getProperty("instanceModeEnabled"))==false)
					break;
				locList.add(location);
			}
			// In the event the last location didn't have a parent, use the last known one
			if (location==null)
				location = locList.get(locList.size()-1);
			
			// walk the locations and reset them
			for (CachedEntity loc : locList)
			{
				Filter filter = new FilterPredicate("locationKey", FilterOperator.EQUAL, loc.getKey());
				List<CachedEntity> charList = ds.fetchAsList("Character", filter, 10000);
				for (CachedEntity c : charList)
				{
					if ("NPC".equals(c.getProperty("type")))
					{
						// Delete both the monster and its items
						List<CachedEntity> itemList = db.getItemContentsFor(c.getKey());
						ds.delete(c);
						for (CachedEntity i : itemList)
							ds.delete(i);
					}
					else
					{
						boolean changed = false;
						// If in combat, reset to normal
						if ("COMBAT".equals(c.getProperty("mode")))
						{
							c.setProperty("mode", "NORMAL");
							c.setProperty("combatType", null);
							c.setProperty("combatant", null);
							changed = true;
						}
						// If entireInstance, move to start
						if (entireInstance)
						{
							c.setProperty("locationKey", location.getKey());
							changed = true;
						}
						if (changed) ds.put(c);
					}
				}
				// And finally trigger the respawn.
				loc.setProperty("instanceRespawnDate", new Date());
				ds.put(loc);
			}
			setPopupMessage("Successfully reset "+locList.size()+" location(s).");
			
		} break;
		
		// tool=RestockTutorial, amount=<xxx>
		case "RestockTutorial":
		{
			// Verify parameter sanity
			String paramAmount = parameters.get("amount");
			// Default to 100
			Long amount = 100l;
			if (paramAmount!=null && paramAmount.trim().equals("")==false)
			{
				try {
					amount = Long.parseLong(paramAmount);
				} catch (Exception e) {
					throw new RuntimeException("DevTools invalid call format, 'amount' is not a valid number.");
				}
				if (amount<=0)
					throw new RuntimeException("DevTools invalid call format, 'amount' is not a valid number.");
				if (amount>1000)
					throw new UserErrorMessage("Amount can not be greater than 1000.");
			}
			
			// Get the itemDef & character (hardcoded by Id)
			CachedEntity itemDef = db.getItemDefinitionById(6335380444938240l);
			if (itemDef==null)
				throw new UserErrorMessage("Unable to retrieve the ItemDef.");
			character = db.getCharacterById(5137880555978752l);
			if (character==null)
				throw new UserErrorMessage("Unable to retrieve the store character.");
			
			// Get the current sale items
			List<CachedEntity> saleItems = db.getSaleItemsFor(character.getKey());
			int itemsRemaining = saleItems.size(); 
			
			// Remove sold entries
			for(CachedEntity s : saleItems)
			{
				if ("Sold".equals(s.getProperty("status")))
				{
					ds.delete(s);
					itemsRemaining--;
				}
			}
			
			// Generate the needed items and set them for sale
			for (int i=itemsRemaining; i<amount; i++){
				// Not ideal, but since item generation isn't in the ODP, the only way...
				CachedEntity item = new CachedEntity("Item");
				Map<String, Object> itemProps = itemDef.getProperties();
				for (Map.Entry<String, Object> prop : itemProps.entrySet())
				{
					switch (getDataType(prop.getKey())) {
					case DD:
						item.setProperty(prop.getKey(), (String)resolve(prop.getValue()));
						break;
					case Double:
						item.setProperty(prop.getKey(), resolveDouble(prop.getValue()));
						break;
					case Long:
						item.setProperty(prop.getKey(), resolveLong(prop.getValue()));
						break;
					case String:
						item.setProperty(prop.getKey(), prop.getValue());
					}
				}
				item.setProperty("_definitionKey", itemDef.getKey());
				item.setProperty("containerKey", character.getKey());
				item.setProperty("maxDurability", item.getProperty("durability"));
				item.setProperty("movedTimestamp", new Date());
				
				ds.put(item);
				db.newSaleItem(ds, character, item, 1l);
			}
			setPopupMessage("Successfully restocked "+(amount-itemsRemaining)+" new items, making a total of "+amount+".");
		} break;
		
		// tool=Teleport, locId=<xxx>
		case "Teleport":
		{
			// Verify parameter sanity
			CachedEntity location = db.getLocationById(tryParseId(parameters, "locId"));
			if (location==null)
				throw new RuntimeException("DevTools invalid call format, 'locId' is not a valid id.");
			
			// Set new location
			character.setProperty("locationKey", location.getKey());
			ds.put(character);
			setJavascriptResponse(JavascriptResponse.FullPageRefresh);
		} break;
		
		// tool=UpdateItems, itemName=<name>, [<fieldName>=<newValue>]
		case "UpdateItems":
		{
			// Verify parameter sanity
			String itemName = parameters.get("itemName");
			if (itemName==null || (itemName=itemName.trim()).equals(""))
				throw new RuntimeException("DevTools invalid call format, 'itemName' is not a valid name.");
			
			// cmd, tool and itemName are already confirmed to exist, check whether there's at least 1 more param
			if (parameters.size()==3)
				throw new RuntimeException("DevTools invalid call format, No fieldNames specified.");
			
			// As we can't really lock down the DB to do it all in 1 transaction, get the items in 20 item chunks.
			Filter filter = new FilterPredicate("name", FilterOperator.EQUAL, itemName);
			Cursor cursor = null;
			final int chunkSize = 20;
			int counter = -1;
			List<CachedEntity> itemList;
			do {
				ds.beginTransaction();
				try {
					counter++;
					itemList = ds.fetchAsList("Item", filter, chunkSize, cursor);
					// On first run, check if any items exist at all. 
					if (counter==0 && itemList.isEmpty())
						throw new UserErrorMessage("No items exist by that name.");
					
					// save cursor for next iteration
					cursor = ds.getLastQueryCursor();
					
					// Walk all items and update the fields
					for (CachedEntity item : itemList) {
						int retries = 0;
						
						// On ConcurrentModificationException retry here (max 5 times)
						// Basically goto workaround
						retry: while (true) {
							String fieldName = null;
							try {
								boolean changed = false;
								for (Map.Entry<String, String> param : parameters.entrySet())
								{
									// Skip cmd, tool and itemName param
									// Note that paramKey should never be null or ""
									fieldName = param.getKey();
									switch (fieldName) {
									case "": case "cmd": case "tool": case "itemName":
										continue;
									}
									
									// Set the new value
									// Nothing other than dataType.String should probably ever be set though
									// as all items would otherwise have the exact same stat
									// but the functionality is there just in case.
									Object newVal = null;
									String strVal = param.getValue();
									if (strVal!=null && (strVal=strVal.trim()).equals("")==false)
									{
										switch (getDataType(fieldName)) {
										case DD:
											newVal = strVal;
											break;
										case Double:
											newVal = Double.parseDouble(strVal);
											break;
										case Long:
											newVal = Long.parseLong(strVal);
											break;
										case String:
											newVal = strVal;
										}
									}
									
									//No point in updating the DB if the value is the same
									if (GameUtils.equals(newVal, item.getProperty(fieldName))==false) {
										item.setProperty(fieldName, newVal);
										changed = true;
									}
								}
								if (changed) ds.put(item);
								
								// Needed because of the goto workaround
								break retry;
								
							} catch (NumberFormatException nfe) {
								throw new RuntimeException("DevTools invalid call format, '"+fieldName+"' is not a valid number.");
							// Item was changed elsewhere, refetch and retry.
							} catch (ConcurrentModificationException cme) {
								if (retries==5)
									throw new UserErrorMessage("The database is currently experiencing heavy load for that item.\nOperation aborted, please try again later.");
								retries++;
								
								// retry with fresh copy
								ds.refetch(item);
								continue retry;
							}
						}
					}
					ds.commit();
				} finally {
					ds.rollbackIfActive();
				}
			} while (itemList.size()==chunkSize);
			setPopupMessage("Successfully updated "+(counter*chunkSize+itemList.size())+" items.");
		} break;
		
		// Unknown tool
		default:
			throw new RuntimeException("DevTools invalid call format, 'tool' is not a valid tool.");
		}
		
	}

}
