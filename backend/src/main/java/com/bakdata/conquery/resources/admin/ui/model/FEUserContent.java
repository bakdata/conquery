package com.bakdata.conquery.resources.admin.ui.model;

import java.util.Collection;

import com.bakdata.conquery.models.auth.entities.Role;
import com.bakdata.conquery.models.auth.entities.User;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * This class provides the corresponding FreeMarker template with the data needed.
 */
@Getter
@SuperBuilder
public class FEUserContent extends FEPermissionOwnerContent<User> implements FERoleOwner {

	public final Collection<Role> roles;
	public final Collection<Role> availableRoles;
}