package spribe.task.tests.players;

import spribe.task.api.core.ResponseWrapper;
import spribe.task.api.model.enums.Role;
import spribe.task.api.model.error.ErrorBody;
import spribe.task.api.model.request.Player;
import spribe.task.api.model.response.PlayerResponse;
import spribe.task.api.services.PlayersService;
import base.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import spribe.task.util.TestDataGenerator;

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

        ResponseWrapper<PlayerResponse> deleteResponse = playersService.delete(PlayersService.defaultSupervisor(), createdUserId).expectStatus(204);
        Assert.assertNotNull(deleteResponse.raw(), "Delete response must be present");
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

        ResponseWrapper<PlayerResponse> deleteResponse = playersService.delete(adminToCreate.getLogin(), createdUserId).expectStatus(204);
        Assert.assertNotNull(deleteResponse.raw(), "Delete response must be present");
        
        ResponseWrapper<PlayerResponse> verifyResponse = playersService.getById(createdUserId).expectStatus(404);
        Assert.assertNotNull(verifyResponse.asError(ErrorBody.class), "Error body must be present after deletion");
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
        ResponseWrapper<PlayerResponse> response = playersService.delete(PlayersService.defaultSupervisor(), 999_999_999).expectStatus(404);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Deleting with non-existent editor should return 403 Test")
    public void deletingWithNonExistentEditorShouldReturn403Test() {
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);

        ResponseWrapper<PlayerResponse> response = playersService.delete("non_existing_editor_login", createdUserId).expectStatus(403);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Deleting supervisor should be forbidden Test")
    public void deletingSupervisorShouldBeForbiddenTest() {
        ResponseWrapper<PlayerResponse> response = playersService.delete(PlayersService.defaultSupervisor(), 1).expectStatus(403);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Deleting with boundary IDs should be rejected Test")
    public void deletingWithBoundaryIdsShouldBeRejectedTest() {
        ResponseWrapper<PlayerResponse> response1 = playersService.delete(PlayersService.defaultSupervisor(), Integer.MAX_VALUE).expectStatus(400);
        Assert.assertNotNull(response1.asError(ErrorBody.class), "Error body must be present for MAX_VALUE");
        
        ResponseWrapper<PlayerResponse> response2 = playersService.delete(PlayersService.defaultSupervisor(), -1).expectStatus(400);
        Assert.assertNotNull(response2.asError(ErrorBody.class), "Error body must be present for negative ID");
    }

    @Test(description = "User cannot delete himself Test")
    public void userCannotDeleteHimselfTest() {
        
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);

        ResponseWrapper<PlayerResponse> deleteResponse =
                playersService.delete(userToCreate.getLogin(), createdUserId)
                        .expectStatus(403);

        ErrorBody error = deleteResponse.asError(ErrorBody.class);
        String errorTitleLower = error.getTitle().toLowerCase();
        
        Assert.assertTrue(errorTitleLower.contains("cannot delete")
                        || errorTitleLower.contains("forbidden")
                        || errorTitleLower.contains("not allowed"),
                "Expected user cannot delete himself restriction, got: " + error.getTitle());
        
    }

    @Test(description = "Supervisor can delete admin Test")
    public void supervisorCanDeleteAdminTest() {
        
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();

        ResponseWrapper<PlayerResponse> deleteResponse = playersService.delete(PlayersService.defaultSupervisor(), createdAdminId).expectStatus(204);
        Assert.assertNotNull(deleteResponse.raw(), "Delete response must be present");
    }

    @Test(description = "Admin can delete another admin Test")
    public void adminCanDeleteAnotherAdminTest() {
        
        Player firstAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        Player secondAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        
        int firstAdminId = playersService.create(PlayersService.defaultSupervisor(), firstAdminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(firstAdminId);
        
        int secondAdminId = playersService.create(PlayersService.defaultSupervisor(), secondAdminToCreate)
                .expectStatus(200).asBody().getPlayerId();

        ResponseWrapper<PlayerResponse> deleteResponse = playersService.delete(firstAdminToCreate.getLogin(), secondAdminId).expectStatus(204);
        Assert.assertNotNull(deleteResponse.raw(), "Delete response must be present");
    }
}
