package com.bakdata.conquery.tasks;

import static com.bakdata.conquery.tasks.PermissionCleanupTask.*;

import com.bakdata.conquery.io.storage.MetaStorage;
import com.bakdata.conquery.models.auth.entities.User;
import com.bakdata.conquery.models.auth.permissions.Ability;
import com.bakdata.conquery.models.auth.permissions.AbilitySets;
import com.bakdata.conquery.models.auth.permissions.QueryPermission;
import com.bakdata.conquery.models.auth.permissions.WildcardPermission;
import com.bakdata.conquery.models.identifiable.ids.specific.DatasetId;
import com.bakdata.conquery.models.identifiable.ids.specific.ManagedExecutionId;
import com.bakdata.conquery.models.identifiable.ids.specific.UserId;
import com.bakdata.conquery.models.query.ManagedQuery;
import com.bakdata.conquery.models.query.concept.ConceptQuery;
import com.bakdata.conquery.models.query.concept.specific.CQAnd;
import com.bakdata.conquery.util.NonPersistentStoreFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionCleanupTaskTest {


    private MetaStorage storage = new MetaStorage(null, new NonPersistentStoreFactory(), null, null);

    @AfterEach
    public void teardownAfterEach() {
        storage.clear();
    }

    private ManagedQuery createManagedQuery() {
        final CQAnd root = new CQAnd();
        root.setChildren(new ArrayList<>());

        ConceptQuery query = new ConceptQuery(root);

        final ManagedQuery managedQuery = new ManagedQuery(query, null, new DatasetId("test"));

        managedQuery.setCreationTime(LocalDateTime.now().minusDays(1));

        storage.addExecution(managedQuery);

        return managedQuery;
    }

    @Test
    void doNotDeletePermissionValidReference() {
        assertThat(storage.getAllExecutions()).isEmpty();

        final ManagedQuery managedQuery = createManagedQuery();
        // Saving the Execution
        User user = new User("test", "test");
        storage.updateUser(user);
        user.addPermission(storage, QueryPermission.onInstance(AbilitySets.QUERY_CREATOR, managedQuery.getId()));

        deleteQueryPermissionsWithMissingRef(storage, storage.getAllUsers());

        assertThat(user.getPermissions()).containsOnly(QueryPermission.onInstance(AbilitySets.QUERY_CREATOR, managedQuery.getId()));

    }

    @Test
    void doDeletePermissionInvalidReference() {
        assertThat(storage.getAllExecutions()).isEmpty();

        final ManagedQuery managedQuery = createManagedQuery();
        // Removing the execution
        storage.removeExecution(managedQuery.getId());
        User user = new User("test", "test");
        storage.updateUser(user);
        user.addPermission(storage, QueryPermission.onInstance(AbilitySets.QUERY_CREATOR, managedQuery.getId()));

        deleteQueryPermissionsWithMissingRef(storage, storage.getAllUsers());

        assertThat(user.getPermissions()).isEmpty();

    }

    @Test
    void doDeletePartialPermissionWithInvalidReference() {
        assertThat(storage.getAllExecutions()).isEmpty();

        final ManagedQuery managedQuery1 = createManagedQuery();
        final ManagedQuery managedQuery2 = createManagedQuery();
        // Removing the second execution
        storage.removeExecution(managedQuery2.getId());
        User user = new User("test", "test");
        storage.updateUser(user);
        user.addPermission(
                storage,
                // Build a permission with multiple instances
                new WildcardPermission(List.of(
                        Set.of(QueryPermission.DOMAIN),
                        Set.of(Ability.READ.toString().toLowerCase()),
                        Set.of(managedQuery1.getId().toString(), managedQuery2.getId().toString())), Instant.now()));

        deleteQueryPermissionsWithMissingRef(storage, storage.getAllUsers());

        assertThat(user.getPermissions()).containsOnly(QueryPermission.onInstance(Ability.READ, managedQuery1.getId()));

    }


    @Test
    void doDeletePermissionsOfOwnedReference() {
        assertThat(storage.getAllExecutions()).isEmpty();

        // Created owned execution
        final ManagedQuery managedQueryOwned = createManagedQuery();
        // Setup user
        User user = new User("test", "test");
        storage.updateUser(user);
        user.addPermission(storage, QueryPermission.onInstance(AbilitySets.QUERY_CREATOR, managedQueryOwned.getId()));

        managedQueryOwned.setOwner(user.getId());
        storage.updateExecution(managedQueryOwned);

        // Created not owned execution
        final ManagedQuery managedQueryNotOwned = createManagedQuery();
        // Setup user
        user.addPermission(storage, QueryPermission.onInstance(Ability.READ, managedQueryNotOwned.getId()));

        // Set owner
        managedQueryNotOwned.setOwner(new UserId("test2"));
        storage.updateExecution(managedQueryNotOwned);

        deletePermissionsOfOwnedInstances(storage, QueryPermission.DOMAIN.toLowerCase(), ManagedExecutionId.Parser.INSTANCE, storage::getExecution);

        assertThat(user.getPermissions()).containsOnly(QueryPermission.onInstance(Ability.READ, managedQueryNotOwned.getId()));

    }

}