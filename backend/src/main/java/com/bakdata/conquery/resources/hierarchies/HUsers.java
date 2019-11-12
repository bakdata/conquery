package com.bakdata.conquery.resources.hierarchies;

import javax.inject.Inject;
import javax.ws.rs.Path;

import com.bakdata.conquery.resources.admin.rest.AdminProcessor;

import lombok.Setter;

@Setter
@Path("users")
public class HUsers extends HAuthorized {

	@Inject
	protected AdminProcessor processor;
}
