package ua.tymo.tests.players;

import ua.tymo.api.core.ResponseWrapper;
import ua.tymo.api.model.response.PlayersResponse;
import ua.tymo.api.services.PlayersService;
import base.BaseTest;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.Assert;
import org.testng.annotations.Test;


public class GetPlayersTests extends BaseTest {

    private final PlayersService playersService = new PlayersService();

    @Test(description = "Get all players returns 200 and matches schema Test")
    public void getAllPlayersReturns200AndMatchesSchemaTest() {
        ResponseWrapper<PlayersResponse> getAllResponse =
                playersService.getAll().expectStatus(200);

        getAllResponse.raw().then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/players-schema.json"));

        Assert.assertNotNull(getAllResponse.asBody(), "PlayersResponse body must not be null");
    }
}
