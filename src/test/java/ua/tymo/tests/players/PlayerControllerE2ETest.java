package ua.tymo.tests.players;

import ua.tymo.api.core.ResponseWrapper;
import ua.tymo.api.model.enums.Role;
import ua.tymo.api.model.request.Player;
import ua.tymo.api.model.response.PlayerResponse;
import ua.tymo.api.services.PlayersService;
import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import ua.tymo.util.TestDataGenerator;


public class PlayerControllerE2ETest extends BaseTest {

    private final PlayersService playersService = new PlayersService();

    @Test(description = "Full lifecycle test: create → get → update → delete Test")
    public void fullLifecycleCreateGetUpdateDeleteTest() {
        // Create
        Player newPlayer = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), newPlayer).expectStatus(200);
        int createdPlayerId = createResponse.asBody().getPlayerId();

        // Get
        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(createdPlayerId).expectStatus(200);
        Assert.assertEquals(getResponse.asBody().getLogin(), newPlayer.getLogin(), "Login should match after creation");

        // Update
        Player updatePayload = TestDataGenerator.generateUpdatePlayerWithNewPassword();
        ResponseWrapper<PlayerResponse> updateResponse =
                playersService.update(PlayersService.defaultSupervisor(), createdPlayerId, updatePayload)
                        .expectStatus(200);
        Assert.assertNotEquals(updateResponse.asBody().getPassword(), newPlayer.getPassword(),
                "Password should be updated");

        // Delete
        playersService.delete(PlayersService.defaultSupervisor(), createdPlayerId).expectStatus(204);

        // Verify deleted
        playersService.getById(createdPlayerId).expectStatus(404);
    }
}
