package com.bakdata.conquery.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.bakdata.conquery.integration.IntegrationTest;
import com.bakdata.conquery.io.xodus.MasterMetaStorage;
import com.bakdata.conquery.models.auth.entities.Role;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.auth.permissions.Ability;
import com.bakdata.conquery.models.auth.permissions.DatasetPermission;
import com.bakdata.conquery.models.auth.permissions.SuperPermission;
import com.bakdata.conquery.models.datasets.Dataset;
import com.bakdata.conquery.util.support.StandaloneSupport;

public class SuperPermissionTest implements ProgrammaticIntegrationTest, IntegrationTest.Simple  {

	private final Role mandator1 = new Role("company", "company");
	private final User user1 = new User("user", "user");
	

	@Override
	public void execute(StandaloneSupport conquery) throws Exception {
		Dataset dataset1 = new Dataset();
		dataset1.setLabel("dataset1");
		MasterMetaStorage storage = conquery.getStandaloneCommand().getMaster().getStorage();
		
		try {
			user1.addRole(storage, mandator1);
			// Add SuperPermission to User
			user1.addPermission(storage,  SuperPermission.onDomain());
		
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isTrue();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isTrue();
		
			// Add SuperPermission to mandator and remove from user
			user1.removePermission(storage, SuperPermission.onDomain());
			mandator1.addPermission(storage, SuperPermission.onDomain());
		
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isTrue();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isTrue();
		
			// Add SuperPermission to mandator and remove from user
			mandator1.removePermission(storage, SuperPermission.onDomain());
		
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.READ, dataset1.getId()))).isFalse();
			assertThat(user1.isPermitted(DatasetPermission.onInstance(Ability.DOWNLOAD, dataset1.getId()))).isFalse();
		}
		finally {
			storage.removeUser(user1.getId());
			storage.removeRole(mandator1.getId());
		}

	}

}
