package spribe.task.tests.players;

import spribe.task.api.core.ResponseWrapper;
import spribe.task.api.model.response.PlayersResponse;
import spribe.task.api.services.PlayersService;
import base.BaseTest;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


public class GetPlayersTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(GetPlayersTests.class);
    private final PlayersService playersService = new PlayersService();

    @Test(description = "Get all players returns 200 and matches schema Test")
    public void getAllPlayersReturns200AndMatchesSchemaTest() {

        ResponseWrapper<PlayersResponse> getAllResponse =
                playersService.getAll().expectStatus(200);

        getAllResponse.raw().then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/players-schema.json"));

        PlayersResponse body = getAllResponse.asBody();
        Assert.assertNotNull(body, "PlayersResponse body must not be null");
    }
}
