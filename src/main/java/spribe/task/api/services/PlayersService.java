package spribe.task.api.services;

import spribe.task.api.core.RequestSpecFactory;
import spribe.task.api.core.ResponseWrapper;
import spribe.task.api.model.request.Player;
import spribe.task.api.model.response.PlayerResponse;
import spribe.task.api.model.response.PlayersResponse;
import spribe.task.common.env.ConfigFactoryProvider;
import spribe.task.common.env.TestConfig;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Service-layer around player-controller endpoints.
 * Endpoints are configurable via TestConfig for different environments.
 */
public class PlayersService {

    private final TestConfig config = ConfigFactoryProvider.config();

    @Step("Create player as {editor}")
    public ResponseWrapper<PlayerResponse> create(String editor, Player payload) {
        Map<String, Object> query = new HashMap<>();
        query.put("age", payload.getAge());
        query.put("gender", payload.getGender());
        query.put("login", payload.getLogin());
        query.put("password", payload.getPassword());
        query.put("role", payload.getRole());
        query.put("screenName", payload.getScreenName());

        Response resp = given()
                .spec(RequestSpecFactory.defaultSpec())
                .pathParam("editor", editor)
                .queryParams(query)
                .get(config.endpointPlayerCreate());

        return new ResponseWrapper<>(resp, PlayerResponse.class);
    }

    @Step("Get player by id={id}")
    public ResponseWrapper<PlayerResponse> getById(Integer id) {
        Map<String, Object> body = new HashMap<>();
        body.put("playerId", id);

        Response resp = given()
                .spec(RequestSpecFactory.defaultSpec())
                .body(body)
                .post(config.endpointPlayerGet());

        return new ResponseWrapper<>(resp, PlayerResponse.class);
    }

    @Step("Get all players")
    public ResponseWrapper<PlayersResponse> getAll() {
        Response resp = given()
                .spec(RequestSpecFactory.defaultSpec())
                .get(config.endpointPlayerGetAll());

        return new ResponseWrapper<>(resp, PlayersResponse.class);
    }

    @Step("Update player id={id} as {editor}")
    public ResponseWrapper<PlayerResponse> update(String editor, Integer id, Player update) {
        Response resp = given()
                .spec(RequestSpecFactory.defaultSpec())
                .pathParam("editor", editor)
                .pathParam("id", id)
                .body(update)
                .patch(config.endpointPlayerUpdate());

        return new ResponseWrapper<>(resp, PlayerResponse.class);
    }

    @Step("Delete player id={id} as {editor}")
    public ResponseWrapper<PlayerResponse> delete(String editor, Integer id) {
        Map<String, Object> body = new HashMap<>();
        body.put("playerId", id);

        Response resp = given()
                .spec(RequestSpecFactory.defaultSpec())
                .pathParam("editor", editor)
                .body(body)
                .delete(config.endpointPlayerDelete());

        return new ResponseWrapper<>(resp, PlayerResponse.class);
    }

    /**
     * Helper to get default supervisor login from configuration.
     */
    public static String defaultSupervisor() {
        return ConfigFactoryProvider.config().supervisorLogin();
    }

    /**
     * Helper to get default admin login from configuration.
     */
    public static String defaultAdmin() {
        return ConfigFactoryProvider.config().adminLogin();
    }
}
