package com.bakdata.conquery.resources.admin.ui.model;

import java.util.Collection;

import com.bakdata.conquery.models.auth.entities.Group;
import com.bakdata.conquery.models.auth.entities.Role;
import com.bakdata.conquery.models.auth.entities.User;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class FEAuthOverview {

	private Collection<OverviewRow> overview;

	@Builder
	@Getter
	public static class OverviewRow {

		private User user;
		private Collection<Group> groups;
		private Collection<Role> effectiveRoles;
	}
}
