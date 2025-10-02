package ua.tymo.tests.players;

import ua.tymo.api.core.ResponseWrapper;
import ua.tymo.api.model.enums.Role;
import ua.tymo.api.model.request.Player;
import ua.tymo.api.model.response.PlayerResponse;
import ua.tymo.api.services.PlayersService;
import base.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import ua.tymo.util.TestDataGenerator;


public class PlayerControllerE2ETest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(PlayerControllerE2ETest.class);
    private final PlayersService playersService = new PlayersService();

    @Test(description = "Full lifecycle test: create → get → update → delete Test")
    public void fullLifecycleCreateGetUpdateDeleteTest() {
        log.info("=== Starting E2E test: Full player lifecycle ===");
        
        // Create
        log.info("STEP 1: Creating new player");
        Player newPlayer = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        log.info("Generated player with login: {}", newPlayer.getLogin());
        
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), newPlayer).expectStatus(200);
        int createdPlayerId = createResponse.asBody().getPlayerId();
        log.info("Player created successfully with ID: {}", createdPlayerId);

        // Get
        log.info("STEP 2: Retrieving created player by ID");
        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(createdPlayerId).expectStatus(200);
        Assert.assertEquals(getResponse.asBody().getLogin(), newPlayer.getLogin(), 
                "Login should match after creation");
        log.info("Player retrieved successfully, login verified: {}", getResponse.asBody().getLogin());

        // Update
        log.info("STEP 3: Updating player password");
        Player updatePayload = TestDataGenerator.generateUpdatePlayerWithNewPassword();
        log.info("New password: {}", updatePayload.getPassword());
        
        ResponseWrapper<PlayerResponse> updateResponse =
                playersService.update(PlayersService.defaultSupervisor(), createdPlayerId, updatePayload)
                        .expectStatus(200);
        Assert.assertNotEquals(updateResponse.asBody().getPassword(), newPlayer.getPassword(),
                "Password should be updated");
        log.info("Player updated successfully, password changed from '{}' to '{}'", 
                newPlayer.getPassword(), updateResponse.asBody().getPassword());

        // Delete
        log.info("STEP 4: Deleting player");
        playersService.delete(PlayersService.defaultSupervisor(), createdPlayerId).expectStatus(204);
        log.info("Player deleted successfully");

        // Verify deleted
        log.info("STEP 5: Verifying player is deleted");
        playersService.getById(createdPlayerId).expectStatus(404);
        log.info("Delete verified - player no longer exists");
        
        log.info("=== E2E test completed successfully ===");
    }
}
