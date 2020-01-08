package com.bakdata.conquery.resources.admin.ui;

import static com.bakdata.conquery.resources.ResourceConstants.ROLE_ID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.bakdata.conquery.models.identifiable.ids.specific.RoleId;
import com.bakdata.conquery.resources.admin.ui.model.UIView;
import com.bakdata.conquery.resources.hierarchies.HRoles;

import io.dropwizard.views.View;

@Produces(MediaType.TEXT_HTML)
public class RoleUIResource extends HRoles {

	@GET
	public View getRoles() {
		return new UIView<>("roles.html.ftl", processor.getUIContext(), processor.getAllRoles());
	}

	/**
	 * End point for retrieving information about a specific role.
	 *
	 * @param roleId
	 *            Unique id of the role.
	 * @return A view holding the information about the role.
	 */
	@Path("{" + ROLE_ID + "}")
	@GET
	public View getRole(@PathParam(ROLE_ID) RoleId roleId) {
		return new UIView<>("role.html.ftl", processor.getUIContext(), processor.getRoleContent(roleId));
	}
}
