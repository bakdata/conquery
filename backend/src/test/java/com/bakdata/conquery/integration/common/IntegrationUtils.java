package com.bakdata.conquery.integration.common;

import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.models.auth.permissions.ConqueryPermission;
import com.bakdata.conquery.models.auth.subjects.Mandator;
import com.bakdata.conquery.models.auth.subjects.PermissionOwner;
import com.bakdata.conquery.models.auth.subjects.User;
import com.bakdata.conquery.models.exceptions.JSONException;
import com.bakdata.conquery.models.identifiable.ids.specific.MandatorId;
import com.bakdata.conquery.models.identifiable.ids.specific.PermissionOwnerId;
import com.bakdata.conquery.models.identifiable.ids.specific.UserId;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IntegrationUtils {


	/**
	 * Load the constellation of roles, users and permissions into the provided storage.
	 */
	public static void importPermissionConstellation(MasterMetaStorage storage,
			Mandator [] roles,
			RequiredUser [] rUsers,
			ConqueryPermission [] permissions) throws JSONException {
				
		for(Mandator role: roles) {
			storage.addMandator(role);
		}
		
		for(RequiredUser rUser: rUsers) {
			User user = rUser.getUser();
			MandatorId [] rolesInjected = rUser.getRolesInjected();
			
			for(MandatorId mandatorId : rolesInjected) {
				user.addMandatorLocal(storage.getMandator(mandatorId));
			}
			storage.addUser(user);
		}
		
		for(ConqueryPermission permission: permissions) {
			PermissionOwnerId<?> ownerId = permission.getOwnerId();
			PermissionOwner<?> owner =null;
			if(ownerId instanceof UserId) {
				owner = storage.getUser((UserId) ownerId);
			} else if(ownerId instanceof MandatorId) {
				owner = storage.getMandator((MandatorId) ownerId);
			}
			
			owner.addPermission(storage, permission);
		}
	}
	


	public static void clearAuthStorage(MasterMetaStorage storage) {
		// Clear MasterStorage
		for(Mandator mandator : storage.getAllMandators()) {
			storage.removeMandator(mandator.getId());
		}
		for(User user : storage.getAllUsers()) {
			storage.removeUser(user.getId());
		}
		for(ConqueryPermission permission : storage.getAllPermissions()) {
			storage.removePermission(permission.getId());
		}
	}
}
