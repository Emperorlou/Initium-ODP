package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

//This entity defines the permissions for a user of this editor. The email must correspond with the email the user used to sign in to the admin section.
public class EditorPermissions extends OdpDomain {

	public EditorPermissions() {
		super(new CachedEntity("EditorPermissions"));
	}

	public EditorPermissions(CachedEntity cachedEntity) {
		super(cachedEntity, "EditorPermissions");
	}

	// TRUE or FALSE if this email has admin priviledges. Admins have access to everything, regardless to what settings are setup here.
	public void setAdmin(Boolean admin) {
		getCachedEntity().setProperty("admin", admin);
	}

	public Boolean getAdmin() {
		return (Boolean) getCachedEntity().getProperty("admin");
	}

	// TRUE or FALSE if this individual has access to the Data Editor tab.
	public void setDataEditorAccess(Boolean dataEditorAccess) {
		getCachedEntity().setProperty("dataEditorAccess", dataEditorAccess);
	}

	public Boolean getDataEditorAccess() {
		return (Boolean) getCachedEntity().getProperty("dataEditorAccess");
	}

	// The email of the person who has limited permissions to use this editor.
	public void setEmail(String email) {
		getCachedEntity().setProperty("email", email);
	}

	public String getEmail() {
		return (String) getCachedEntity().getProperty("email");
	}

	// The full name of the individual these permissions apply to.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// TRUE or FALSE if this individual has access to the Schema Editor tab.
	public void setSchemaEditorAccess(Boolean schemaEditorAccess) {
		getCachedEntity().setProperty("schemaEditorAccess", schemaEditorAccess);
	}

	public Boolean getSchemaEditorAccess() {
		return (Boolean) getCachedEntity().getProperty("schemaEditorAccess");
	}

	// A comma delimited list of Schema Entity names that this individual has access to. Only these schema entities will show up and will be editable for this user.
	public void setSchemaEntityList(String schemaEntityList) {
		getCachedEntity().setProperty("schemaEntityList", schemaEntityList);
	}

	public String getSchemaEntityList() {
		return (String) getCachedEntity().getProperty("schemaEntityList");
	}

	// TRUE or FALSE if this individual has access to the Simulator tab.
	public void setSimulatorAccess(Boolean simulatorAccess) {
		getCachedEntity().setProperty("simulatorAccess", simulatorAccess);
	}

	public Boolean getSimulatorAccess() {
		return (Boolean) getCachedEntity().getProperty("simulatorAccess");
	}

}
