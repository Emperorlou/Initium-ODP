public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		Long locationKey = (long)character.getProperty("locationKey");
		CachedEntity location = db.getEntity("location", locationKey);
		
		// check if player is house owner
		if (!GameUtils.equals(location.getProperty("ownerKey"), character.getProperty("userKey")))
			throw new UserErrorMessage("You cannot rename a house you do not own.");
		
		String newName = parameters.get("newName");
		
		if (newName == null || newName == "")
			throw new UserErrorMessage("Name cannot be blank.");
		else if (newName.length() > 40)
			throw new UserErrorMessage("Name is too long. Max length is 40 characters.");
		else if (!newName.matches("[A-Za-z0-9'\" .!$&()#?:;]+"))
			throw new UserErrorMessage("Name contains invalid characters.");
		else
		{
			location.setProperty("name", newName);
			ds.put(location);
		}
	}
