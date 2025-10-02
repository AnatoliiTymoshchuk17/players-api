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


public class GetPlayerTests extends BaseTest {

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

    @Test(description = "Get player by id returns correct data Test")
    public void getPlayerByIdReturnsCorrectDataTest() {
        Player playerToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        PlayerResponse createdPlayer = playersService.create(PlayersService.defaultSupervisor(), playerToCreate)
                .expectStatus(200).asBody();
        createdPlayerIdsForCleanup.add(createdPlayer.getPlayerId());

        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(createdPlayer.getPlayerId()).expectStatus(200);

        PlayerResponse retrievedPlayer = getResponse.asBody();
        Assert.assertEquals(retrievedPlayer.getPlayerId(), createdPlayer.getPlayerId());
        Assert.assertEquals(retrievedPlayer.getAge(), playerToCreate.getAge());
        Assert.assertEquals(retrievedPlayer.getGender(), playerToCreate.getGender());
        Assert.assertEquals(retrievedPlayer.getLogin(), playerToCreate.getLogin());
        Assert.assertEquals(retrievedPlayer.getPassword(), playerToCreate.getPassword());
        Assert.assertEquals(retrievedPlayer.getRole(), playerToCreate.getRole());
        Assert.assertEquals(retrievedPlayer.getScreenName(), playerToCreate.getScreenName());
    }

    // ---------- Negative ----------

    @Test(description = "Get non-existent player by id should return 404 Test")
    public void getNonExistentPlayerByIdShouldReturn404Test() {
        ResponseWrapper<PlayerResponse> getResponse =
                playersService.getById(999_999_999).expectStatus(404);

        String errorTitleLower = getResponse.asError(ErrorBody.class).getTitle().toLowerCase();
        Assert.assertTrue(errorTitleLower.contains("does not exist")
                        || errorTitleLower.contains("not found"),
                "Expected not found error");
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
