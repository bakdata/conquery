package com.bakdata.conquery.models.auth.entities;

import com.bakdata.conquery.io.storage.MetaStorage;
import com.bakdata.conquery.models.identifiable.ids.specific.RoleId;

public class Role extends PermissionOwner<RoleId> {
	

	public Role(String name, String label) {
		super(name, label);
	}

	@Override
	public RoleId createId() {
		return new RoleId(name);
	}
	
	@Override
	protected void updateStorage(MetaStorage storage) {
		storage.updateRole(this);
		
	}

}
