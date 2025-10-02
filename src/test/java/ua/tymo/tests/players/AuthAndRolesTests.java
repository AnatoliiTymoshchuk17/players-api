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


public class AuthAndRolesTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(AuthAndRolesTests.class);
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

    @Test(description = "Admin cannot create supervisor Test")
    public void adminCannotCreateSupervisorTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);

        Player supervisorToCreate = TestDataGenerator.generateValidPlayer(Role.SUPERVISOR.getValue());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(adminToCreate.getLogin(), supervisorToCreate).expectStatus(403);

        ErrorBody error = createResponse.asError(ErrorBody.class);
        Assert.assertTrue(error.getTitle().toLowerCase().contains("only")
                        || error.getTitle().toLowerCase().contains("cannot"),
                "Expected restriction on creating supervisor by admin");
    }

    @Test(description = "User cannot create another user Test")
    public void userCannotCreateAnotherUserTest() {
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);

        Player anotherUserToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(userToCreate.getLogin(), anotherUserToCreate).expectStatus(403);

        Assert.assertTrue(createResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("only those with role"),
                "Expected role restriction for user creating another user");
    }
}
