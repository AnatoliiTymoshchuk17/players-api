package ua.tymo.tests.players;

import ua.tymo.api.core.ResponseWrapper;
import ua.tymo.api.model.enums.Role;
import ua.tymo.api.model.error.ErrorBody;
import ua.tymo.api.model.request.Player;
import ua.tymo.api.model.response.PlayerResponse;
import ua.tymo.api.services.PlayersService;
import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import ua.tymo.util.TestDataGenerator;

import java.util.ArrayList;
import java.util.List;


public class DeletePlayerTests extends BaseTest {

    private final PlayersService playersService = new PlayersService();
    private final List<Integer> createdPlayerIdsForCleanup = new ArrayList<>();

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        for (Integer createdPlayerId : createdPlayerIdsForCleanup) {
            try {
                playersService.delete(PlayersService.defaultSupervisor(), createdPlayerId).raw();
            } catch (Exception ignored) {}
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
        playersService.delete(PlayersService.defaultSupervisor(), Integer.MAX_VALUE).expectStatus(400);
        playersService.delete(PlayersService.defaultSupervisor(), -1).expectStatus(400);
    }
}
