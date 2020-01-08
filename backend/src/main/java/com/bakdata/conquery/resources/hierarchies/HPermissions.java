package com.bakdata.conquery.resources.hierarchies;

import static com.bakdata.conquery.resources.ResourceConstants.OWNER_ID;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.bakdata.conquery.models.identifiable.ids.specific.PermissionOwnerId;
import com.bakdata.conquery.resources.admin.rest.AdminProcessor;

import lombok.Setter;

@Setter
@Path("permissions/{" + OWNER_ID + "}")
public abstract class HPermissions extends HAdmin {
	
	@Inject
	protected AdminProcessor processor;
	@PathParam(OWNER_ID)
	protected PermissionOwnerId<?> ownerId;

}
