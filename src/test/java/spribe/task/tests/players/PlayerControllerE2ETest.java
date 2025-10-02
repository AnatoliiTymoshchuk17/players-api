package spribe.task.tests.players;

import org.testng.Assert;
import spribe.task.api.core.ResponseWrapper;
import spribe.task.api.model.enums.Role;
import spribe.task.api.model.error.ErrorBody;
import spribe.task.api.model.request.Player;
import spribe.task.api.model.response.PlayerResponse;
import spribe.task.api.services.PlayersService;
import base.BaseTest;
import org.testng.annotations.Test;
import spribe.task.util.TestDataGenerator;


public class PlayerControllerE2ETest extends BaseTest {

    private final PlayersService playersService = new PlayersService();

    @Test(description = "Full lifecycle test: create → get → update → delete Test")
    public void fullLifecycleCreateGetUpdateDeleteTest() {
        // Create
        Player newPlayer = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), newPlayer)
                        .expectStatus(200);

        int createdPlayerId = createResponse.asBody().getPlayerId();
        Assert.assertEquals(createResponse.asBody().getLogin(), newPlayer.getLogin(),
                "Login should match");

        // Get
        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(createdPlayerId).expectStatus(200);
        Assert.assertEquals(getResponse.asBody().getLogin(), newPlayer.getLogin(),
                "Retrieved login should match created");

        // Update
        Player updatePayload = TestDataGenerator.generateUpdatePlayerWithNewPassword();
        
        ResponseWrapper<PlayerResponse> updateResponse =
                playersService.update(PlayersService.defaultSupervisor(), createdPlayerId, updatePayload)
                        .expectStatus(200);
        Assert.assertNotEquals(updateResponse.asBody().getPassword(), newPlayer.getPassword(),
                "Password should be updated");

        // Delete
        ResponseWrapper<PlayerResponse> deleteResponse = playersService.delete(PlayersService.defaultSupervisor(), createdPlayerId).expectStatus(204);
        Assert.assertNotNull(deleteResponse.raw(), "Delete response must be present");

        // Verify deleted
        ResponseWrapper<PlayerResponse> verifyDeleteResponse = playersService.getById(createdPlayerId).expectStatus(404);
        Assert.assertNotNull(verifyDeleteResponse.asError(ErrorBody.class), "Error body must be present after deletion");
    }
}

