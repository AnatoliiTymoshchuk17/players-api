package ua.tymo.api.services;

import ua.tymo.api.core.RequestSpecFactory;
import ua.tymo.api.core.ResponseWrapper;
import ua.tymo.api.model.enums.Role;
import ua.tymo.api.model.request.Player;
import ua.tymo.api.model.response.PlayerResponse;
import ua.tymo.api.model.response.PlayersResponse;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Service-layer around player-controller endpoints.
 *  - create: GET /player/create/{editor} with query params
 *  - get by id: POST /player/get  with body { "playerId": id }
 *  - get all: GET /player/get/all
 *  - update: PATCH /player/update/{editor}/{id}
 *  - delete: DELETE /player/delete/{editor} with body { "playerId": id }
 */
public class PlayersService {

    private enum Route {
        CREATE("/player/create/{editor}"),
        GET_BY_ID("/player/get"),
        GET_ALL("/player/get/all"),
        UPDATE("/player/update/{editor}/{id}"),
        DELETE("/player/delete/{editor}");

        final String path;
        Route(String path) { this.path = path; }
    }

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
                .get(Route.CREATE.path);

        return new ResponseWrapper<>(resp, PlayerResponse.class);
    }

    @Step("Get player by id={id}")
    public ResponseWrapper<PlayerResponse> getById(Integer id) {
        Map<String, Object> body = new HashMap<>();
        body.put("playerId", id);

        Response resp = given()
                .spec(RequestSpecFactory.defaultSpec())
                .body(body)
                .post(Route.GET_BY_ID.path);

        return new ResponseWrapper<>(resp, PlayerResponse.class);
    }

    @Step("Get all players")
    public ResponseWrapper<PlayersResponse> getAll() {
        Response resp = given()
                .spec(RequestSpecFactory.defaultSpec())
                .get(Route.GET_ALL.path);

        return new ResponseWrapper<>(resp, PlayersResponse.class);
    }

    @Step("Update player id={id} as {editor}")
    public ResponseWrapper<PlayerResponse> update(String editor, Integer id, Player update) {
        Response resp = given()
                .spec(RequestSpecFactory.defaultSpec())
                .pathParam("editor", editor)
                .pathParam("id", id)
                .body(update)
                .patch(Route.UPDATE.path);

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
                .delete(Route.DELETE.path);

        return new ResponseWrapper<>(resp, PlayerResponse.class);
    }

    /** Helpers to read default editors from sys/env (fallbacks provided) */
    public static String defaultSupervisor() {
        String sys = System.getProperty("editor.supervisor");
        if (sys != null && !sys.isBlank()) return sys;
        String env = System.getenv("EDITOR_SUPERVISOR");
        if (env != null && !env.isBlank()) return env;
        return Role.SUPERVISOR.getValue();
    }

    public static String defaultAdmin() {
        String sys = System.getProperty("editor.admin");
        if (sys != null && !sys.isBlank()) return sys;
        String env = System.getenv("EDITOR_ADMIN");
        if (env != null && !env.isBlank()) return env;
        return Role.ADMIN.getValue();
    }
}
