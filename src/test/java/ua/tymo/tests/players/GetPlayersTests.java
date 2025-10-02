package ua.tymo.tests.players;

import ua.tymo.api.core.ResponseWrapper;
import ua.tymo.api.model.response.PlayersResponse;
import ua.tymo.api.services.PlayersService;
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
        log.info("Starting test: Get all players returns 200 and matches schema");
        
        log.info("Fetching all players from API");
        ResponseWrapper<PlayersResponse> getAllResponse =
                playersService.getAll().expectStatus(200);

        log.info("Validating response against JSON schema");
        getAllResponse.raw().then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/players-schema.json"));

        PlayersResponse body = getAllResponse.asBody();
        log.info("Response body deserialized successfully, contains {} players", 
                body.getPlayers() != null ? body.getPlayers().size() : 0);
        
        Assert.assertNotNull(body, "PlayersResponse body must not be null");
        log.info("Test completed: Response validated successfully");
    }
}
