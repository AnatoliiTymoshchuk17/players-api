package ua.tymo.tests.players;

import ua.tymo.api.core.ResponseWrapper;
import ua.tymo.api.model.enums.Role;
import ua.tymo.api.model.error.ErrorBody;
import ua.tymo.api.model.request.Player;
import ua.tymo.api.model.response.PlayerResponse;
import ua.tymo.api.services.PlayersService;
import base.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import ua.tymo.util.TestDataGenerator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class DeletePlayerTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(DeletePlayerTests.class);
    private final PlayersService playersService = new PlayersService();
    private final List<Integer> createdPlayerIdsForCleanup = new CopyOnWriteArrayList<>();

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        for (Integer createdPlayerId : createdPlayerIdsForCleanup) {
            try {
                playersService.delete(PlayersService.defaultSupervisor(), createdPlayerId).raw();
            } catch (Exception e) {
                log.warn("Failed to cleanup player with id={}: {}", createdPlayerId, e.getMessage());
            }
        }
        createdPlayerIdsForCleanup.clear();
    }

    // ---------- Positive ----------

    @Test(description = "Supervisor can delete a user Test")
    public void supervisorCanDeleteUserTest() {
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();

        playersService.delete(PlayersService.defaultSupervisor(), createdUserId).expectStatus(204);

        playersService.getById(createdUserId).expectStatus(404);
    }

    @Test(description = "Admin can delete a user Test")
    public void adminCanDeleteUserTest() {
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);

        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);

        playersService.delete(adminToCreate.getLogin(), createdUserId).expectStatus(204);
        playersService.getById(createdUserId).expectStatus(404);
    }

    // ---------- Negative ----------

    @Test(description = "Admin cannot delete himself Test")
    public void adminCannotDeleteHimselfTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);

        ResponseWrapper<PlayerResponse> deleteResponse =
                playersService.delete(adminToCreate.getLogin(), createdAdminId)
                        .expectStatus(403);

        String errorTitleLower = deleteResponse.asError(ErrorBody.class).getTitle().toLowerCase();
        Assert.assertTrue(errorTitleLower.contains("cannot delete")
                        || errorTitleLower.contains("forbidden")
                        || errorTitleLower.contains("not allowed"),
                "Expected self-delete restriction for admin");
    }

    @Test(description = "User cannot delete admin Test")
    public void userCannotDeleteAdminTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);

        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);

        ResponseWrapper<PlayerResponse> deleteResponse =
                playersService.delete(userToCreate.getLogin(), createdAdminId)
                        .expectStatus(403);

        String errorTitleLower = deleteResponse.asError(ErrorBody.class).getTitle().toLowerCase();
        Assert.assertTrue(errorTitleLower.contains("only")
                        || errorTitleLower.contains("forbidden")
                        || errorTitleLower.contains("not allowed"),
                "Expected role restriction for user deleting admin");
    }

    @Test(description = "Deleting non-existent player should return 404 Test")
    public void deletingNonExistentPlayerShouldReturn404Test() {
        playersService.delete(PlayersService.defaultSupervisor(), 999_999_999).expectStatus(404);
    }

    @Test(description = "Deleting with non-existent editor should return 403 Test")
    public void deletingWithNonExistentEditorShouldReturn403Test() {
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);

        playersService.delete("non_existing_editor_login", createdUserId).expectStatus(403);
    }

    @Test(description = "Deleting supervisor should be forbidden Test")
    public void deletingSupervisorShouldBeForbiddenTest() {
        playersService.delete(PlayersService.defaultSupervisor(), 1).expectStatus(403);
    }

    @Test(description = "Deleting with boundary IDs should be rejected Test")
    public void deletingWithBoundaryIdsShouldBeRejectedTest() {
        log.info("Starting test: Deleting with boundary IDs should be rejected");
        playersService.delete(PlayersService.defaultSupervisor(), Integer.MAX_VALUE).expectStatus(400);
        playersService.delete(PlayersService.defaultSupervisor(), -1).expectStatus(400);
        log.info("Test completed: Boundary IDs rejected correctly");
    }

    @Test(description = "User cannot delete himself Test")
    public void userCannotDeleteHimselfTest() {
        log.info("Starting test: User cannot delete himself");
        
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        log.info("Creating user with login: {}", userToCreate.getLogin());
        
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);
        log.info("User created with ID: {}", createdUserId);

        log.info("User attempting to delete himself");
        ResponseWrapper<PlayerResponse> deleteResponse =
                playersService.delete(userToCreate.getLogin(), createdUserId)
                        .expectStatus(403);

        ErrorBody error = deleteResponse.asError(ErrorBody.class);
        String errorTitleLower = error.getTitle().toLowerCase();
        log.info("Received error: {}", error.getTitle());
        
        Assert.assertTrue(errorTitleLower.contains("cannot delete")
                        || errorTitleLower.contains("forbidden")
                        || errorTitleLower.contains("not allowed"),
                "Expected user cannot delete himself restriction, got: " + error.getTitle());
        
        log.info("Test completed: User correctly forbidden from deleting himself");
    }

    @Test(description = "Supervisor can delete admin Test")
    public void supervisorCanDeleteAdminTest() {
        log.info("Starting test: Supervisor can delete admin");
        
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        log.info("Creating admin with login: {}", adminToCreate.getLogin());
        
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        log.info("Admin created with ID: {}", createdAdminId);

        log.info("Supervisor attempting to delete admin");
        playersService.delete(PlayersService.defaultSupervisor(), createdAdminId).expectStatus(204);
        
        log.info("Verifying admin was deleted");
        playersService.getById(createdAdminId).expectStatus(404);
        log.info("Test completed: Supervisor successfully deleted admin");
    }

    @Test(description = "Admin can delete another admin Test")
    public void adminCanDeleteAnotherAdminTest() {
        log.info("Starting test: Admin can delete another admin");
        
        Player firstAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        Player secondAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        
        log.info("Creating first admin with login: {}", firstAdminToCreate.getLogin());
        int firstAdminId = playersService.create(PlayersService.defaultSupervisor(), firstAdminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(firstAdminId);
        
        log.info("Creating second admin with login: {}", secondAdminToCreate.getLogin());
        int secondAdminId = playersService.create(PlayersService.defaultSupervisor(), secondAdminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        log.info("Second admin created with ID: {}", secondAdminId);

        log.info("First admin attempting to delete second admin");
        playersService.delete(firstAdminToCreate.getLogin(), secondAdminId).expectStatus(204);
        
        log.info("Verifying second admin was deleted");
        playersService.getById(secondAdminId).expectStatus(404);
        log.info("Test completed: Admin successfully deleted another admin");
    }
}
