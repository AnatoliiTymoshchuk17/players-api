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


public class AdminEditorTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(AdminEditorTests.class);
    private final PlayersService playersService = new PlayersService();
    private final List<Integer> createdPlayerIdsForCleanup = new CopyOnWriteArrayList<>();

    @AfterMethod(alwaysRun = true)
    public void cleanup() {
        for (Integer id : createdPlayerIdsForCleanup) {
            try {
                playersService.delete(PlayersService.defaultSupervisor(), id).raw();
            } catch (Exception e) {
                log.warn("Failed to cleanup player with id={}: {}", id, e.getMessage());
            }
        }
        createdPlayerIdsForCleanup.clear();
    }

    @Test(description = "Admin cannot delete himself Test")
    public void adminCannotDeleteHimselfTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);

        ResponseWrapper<PlayerResponse> deleteResponse =
                playersService.delete(adminToCreate.getLogin(), createdAdminId).expectStatus(403);

        Assert.assertTrue(deleteResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("cannot delete"),
                "Expected 'cannot delete' restriction for admin self-delete");
    }

    @Test(description = "Admin can update another admin Test")
    public void adminCanUpdateAnotherAdminTest() {
        Player firstAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        Player secondAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());

        int firstAdminId = playersService.create(PlayersService.defaultSupervisor(), firstAdminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        int secondAdminId = playersService.create(PlayersService.defaultSupervisor(), secondAdminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(firstAdminId);
        createdPlayerIdsForCleanup.add(secondAdminId);

        Player updatePayload = new Player();
        updatePayload.setAge(45);

        ResponseWrapper<PlayerResponse> updateResponse =
                playersService.update(firstAdminToCreate.getLogin(), secondAdminId, updatePayload).expectStatus(200);

        Assert.assertEquals(updateResponse.asBody().getAge(), Integer.valueOf(45),
                "Expected updated age for second admin");
    }

    @Test(description = "User cannot delete admin Test")
    public void userCannotDeleteAdminTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());

        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);
        createdPlayerIdsForCleanup.add(createdUserId);

        ResponseWrapper<PlayerResponse> deleteResponse =
                playersService.delete(userToCreate.getLogin(), createdAdminId).expectStatus(403);

        Assert.assertTrue(deleteResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("only"),
                "Expected role restriction for user deleting admin");
    }
}
