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
import org.testng.asserts.SoftAssert;
import spribe.task.util.TestDataGenerator;

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
        
        Player playerToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        
        PlayerResponse createdPlayer = playersService.create(PlayersService.defaultSupervisor(), playerToCreate)
                .expectStatus(200).asBody();
        createdPlayerIdsForCleanup.add(createdPlayer.getPlayerId());

        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(createdPlayer.getPlayerId()).expectStatus(200);

        PlayerResponse retrievedPlayer = getResponse.asBody();
        
        SoftAssert soft = new SoftAssert();
        soft.assertEquals(retrievedPlayer.getPlayerId(), createdPlayer.getPlayerId(),
                "Player ID should match");
        soft.assertEquals(retrievedPlayer.getAge(), playerToCreate.getAge(),
                "Age should match");
        soft.assertEquals(retrievedPlayer.getGender(), playerToCreate.getGender(),
                "Gender should match");
        soft.assertEquals(retrievedPlayer.getLogin(), playerToCreate.getLogin(),
                "Login should match");
        soft.assertEquals(retrievedPlayer.getPassword(), playerToCreate.getPassword(),
                "Password should match");
        soft.assertEquals(retrievedPlayer.getRole(), playerToCreate.getRole(),
                "Role should match");
        soft.assertEquals(retrievedPlayer.getScreenName(), playerToCreate.getScreenName(),
                "Screen name should match");
        soft.assertAll();
    }

    // ---------- Negative ----------

    @Test(description = "Get non-existent player by id should return 404 Test")
    public void getNonExistentPlayerByIdShouldReturn404Test() {
        int nonExistentId = 999_999_999;
        
        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(nonExistentId).expectStatus(404);

        ErrorBody error = getResponse.asError(ErrorBody.class);
        String errorTitleLower = error.getTitle().toLowerCase();
        
        Assert.assertTrue(errorTitleLower.contains("does not exist")
                        || errorTitleLower.contains("not found"),
                "Expected 'not found' error message, got: " + error.getTitle());
    }

    @Test(description = "Get player with null id should return 400 Test")
    public void getPlayerWithNullIdShouldReturn400Test() {
        playersService.getById(null).expectStatus(400);
    }

    @Test(description = "Get player with very large id should return 400 Test")
    public void getPlayerWithVeryLargeIdShouldReturn400Test() {
        playersService.getById(Integer.MAX_VALUE).expectStatus(400);
    }
}
