package com.bakdata.conquery.resources.admin.ui.model;

import java.util.List;

import com.bakdata.conquery.models.auth.subjects.Role;
import com.bakdata.conquery.models.auth.subjects.User;

import lombok.Getter;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder
public class FERoleContent extends FEPermissionOwnerContent<Role>{
	private List<User> users;
}