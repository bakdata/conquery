package com.bakdata.conquery.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.bakdata.conquery.integration.IntegrationTest;
import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.models.auth.entities.Role;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.auth.permissions.Ability;
import com.bakdata.conquery.models.auth.permissions.DatasetPermission;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.util.support.StandaloneSupport;

public class PermissionRoleHandlingTest extends IntegrationTest.Simple implements ProgrammaticIntegrationTest {

	private final Role mandator1 = new Role("company", "company");
	private final User user1 = new User("user", "user");

	/**
	 * This is a longer test that plays through different scenarios of permission
	 * and role adding/deleting. Creating many objects here to avoid side effects.
	 */
	@Override
	public void execute(StandaloneSupport conquery) throws Exception {
		MasterMetaStorage storage = conquery.getMasterMetaStorage();
		Dataset dataset1 = new Dataset();
		dataset1.setLabel("dataset1");
		
		try {

			storage.addRole(mandator1);
			storage.addUser(user1);

			user1.addRole(storage, mandator1);

			user1.addPermission(storage, DatasetPermission.onInstance(Ability.READ, dataset1.getId()));
			mandator1.addPermission(storage, DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()));

			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isTrue();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isTrue();

			// Delete permission from mandator
			mandator1.removePermission(storage, DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()));
			assertThat(mandator1.getPermissions()).isEmpty();

			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isTrue();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isFalse();

			// Add permission to user
			user1.addPermission(storage, DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()));

			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isTrue();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isTrue();

			// Delete permission from mandator
			user1.removePermission(storage, DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()));

			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isTrue();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isFalse();

			// Add permission to mandator, remove mandator from user
			mandator1.addPermission(storage, DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()));
			user1.removeRole(storage, mandator1);

			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isTrue();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isFalse();

			// Add mandator back to user
			user1.addRole(storage, mandator1);

			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isTrue();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isTrue();

			// Delete all permissions from mandator and user
			user1.removePermission(storage, DatasetPermission.onInstance(Ability.READ, dataset1.getId()));
			mandator1.removePermission(storage, DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()));

			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isFalse();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isFalse();
		}
		finally {
			storage.removeUser(user1.getId());
			storage.removeRole(mandator1.getId());
		}
	}

}
