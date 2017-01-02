package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//This is used to describe an entity and it is used for matching/validation purposes. For example it could be used to describe an item required for a recipe, or it could be used to describe a tool that is required for construction or resource collection.
public class EntityRequirement extends OdpDomain {

	public EntityRequirement() {
		super(new CachedEntity("EntityRequirement"));
	}

	public EntityRequirement(CachedEntity cachedEntity) {
		super(cachedEntity, "EntityRequirement");
	}

	// A definition entity that was used to generate the entity we require. For example, an ItemDef that was used to generate an Item.
	public void setDefinitionEntityKey(Key definitionEntityKey) {
		getCachedEntity().setProperty("definitionEntityKey", definitionEntityKey);
	}

	public Key getDefinitionEntityKey() {
		return (Key) getCachedEntity().getProperty("definitionEntityKey");
	}

	// A list of expressions that tell us the requirements for the field values on the entity in question.
	public void setFieldFilters(List<String> fieldFilters) { // TODO - type
		getCachedEntity().setProperty("fieldFilters", fieldFilters);
	}

	@SuppressWarnings("unchecked")
	public List<String> getFieldFilters() {
		return (List<String>) getCachedEntity().getProperty("fieldFilters");
	}

	// A generalized name for the entity requirement that portrays the spirit of this requirement in a single phrase.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// A list of aspects the entity needs to have to pass the requirement.
	public void setRequiredAspects(List<String> requiredAspects) { // TODO - type
		getCachedEntity().setProperty("requiredAspects", requiredAspects);
	}

	@SuppressWarnings("unchecked")
	public List<String> getRequiredAspects() {
		return (List<String>) getCachedEntity().getProperty("requiredAspects");
	}

	public enum EntityType {
		Item,
	}

	public void setEntityType(EntityType entityType) {
		getCachedEntity().setProperty("entityType", entityType);
	}

	public EntityType getEntityType() {
		return (EntityType) getCachedEntity().getProperty("entityType");
	}

	public enum Type {
		Recipe, Tool,
	}

	public void setType(Type type) {
		getCachedEntity().setProperty("type", type);
	}

	public Type getType() {
		return (Type) getCachedEntity().getProperty("type");
	}

}
