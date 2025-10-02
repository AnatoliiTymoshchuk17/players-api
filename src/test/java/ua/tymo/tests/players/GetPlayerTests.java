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

import java.util.ArrayList;
import java.util.List;


public class GetPlayerTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(GetPlayerTests.class);
    private final PlayersService playersService = new PlayersService();
    private final List<Integer> createdPlayerIdsForCleanup = new ArrayList<>();

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

    @Test(description = "Get player by id returns correct data Test")
    public void getPlayerByIdReturnsCorrectDataTest() {
        log.info("Starting test: Get player by id returns correct data");
        
        Player playerToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        log.info("Creating player with login: {}", playerToCreate.getLogin());
        
        PlayerResponse createdPlayer = playersService.create(PlayersService.defaultSupervisor(), playerToCreate)
                .expectStatus(200).asBody();
        createdPlayerIdsForCleanup.add(createdPlayer.getPlayerId());
        log.info("Player created with ID: {}", createdPlayer.getPlayerId());

        log.info("Retrieving player by ID: {}", createdPlayer.getPlayerId());
        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(createdPlayer.getPlayerId()).expectStatus(200);

        PlayerResponse retrievedPlayer = getResponse.asBody();
        log.info("Verifying retrieved player data matches created player");
        
        Assert.assertEquals(retrievedPlayer.getPlayerId(), createdPlayer.getPlayerId(),
                "Player ID should match");
        Assert.assertEquals(retrievedPlayer.getAge(), playerToCreate.getAge(),
                "Age should match");
        Assert.assertEquals(retrievedPlayer.getGender(), playerToCreate.getGender(),
                "Gender should match");
        Assert.assertEquals(retrievedPlayer.getLogin(), playerToCreate.getLogin(),
                "Login should match");
        Assert.assertEquals(retrievedPlayer.getPassword(), playerToCreate.getPassword(),
                "Password should match");
        Assert.assertEquals(retrievedPlayer.getRole(), playerToCreate.getRole(),
                "Role should match");
        Assert.assertEquals(retrievedPlayer.getScreenName(), playerToCreate.getScreenName(),
                "Screen name should match");
        
        log.info("Test completed successfully: All player fields match");
    }

    // ---------- Negative ----------

    @Test(description = "Get non-existent player by id should return 404 Test")
    public void getNonExistentPlayerByIdShouldReturn404Test() {
        log.info("Starting test: Get non-existent player should return 404");
        int nonExistentId = 999_999_999;
        
        log.info("Attempting to get player with non-existent ID: {}", nonExistentId);
        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(nonExistentId).expectStatus(404);

        ErrorBody error = getResponse.asError(ErrorBody.class);
        String errorTitleLower = error.getTitle().toLowerCase();
        log.info("Received error: {}", error.getTitle());
        
        Assert.assertTrue(errorTitleLower.contains("does not exist")
                        || errorTitleLower.contains("not found"),
                "Expected 'not found' error message, got: " + error.getTitle());
        log.info("Test completed: Correct error message received");
    }

    @Test(description = "Get player with null id should return 400 Test")
    public void getPlayerWithNullIdShouldReturn400Test() {
        log.info("Starting test: Get player with null ID should return 400");
        playersService.getById(null).expectStatus(400);
        log.info("Test completed: 400 status received for null ID");
    }

    @Test(description = "Get player with very large id should return 400 Test")
    public void getPlayerWithVeryLargeIdShouldReturn400Test() {
        log.info("Starting test: Get player with very large ID should return 400");
        log.info("Using ID: {}", Integer.MAX_VALUE);
        playersService.getById(Integer.MAX_VALUE).expectStatus(400);
        log.info("Test completed: 400 status received for very large ID");
    }
}
