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

public class CreatePlayerTests extends BaseTest {

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

    @Test(description = "Supervisor can create a user with valid data Test")
    public void supervisorCanCreateUserWithValidDataTest() {
        Player playerToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerToCreate)
                        .expectStatus(200);

        PlayerResponse createdPlayerBody = createResponse.asBody();
        Assert.assertEquals(createdPlayerBody.getLogin(), playerToCreate.getLogin(), "Login must match");
        Assert.assertEquals(createdPlayerBody.getRole(), Role.USER.getValue(), "Role must be 'user'");

        createdPlayerIdsForCleanup.add(createdPlayerBody.getPlayerId());
    }

    @Test(description = "Admin can create another user Test")
    public void adminCanCreateAnotherUserTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);

        Player playerToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(adminToCreate.getLogin(), playerToCreate)
                        .expectStatus(200);

        Assert.assertEquals(createResponse.asBody().getRole(), Role.USER.getValue(), "Role must be 'user'");
        createdPlayerIdsForCleanup.add(createResponse.asBody().getPlayerId());
    }

    @Test(description = "Supervisor can create an admin Test")
    public void supervisorCanCreateAdminTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                        .expectStatus(200);

        Assert.assertEquals(createResponse.asBody().getRole(), Role.ADMIN.getValue(), "Role must be 'admin'");
        createdPlayerIdsForCleanup.add(createResponse.asBody().getPlayerId());
    }

    // ---------- Negative (validation & permissions) ----------

    @Test(description = "User cannot create another user Test")
    public void userCannotCreateAnotherUserTest() {
        Player existingUser = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), existingUser)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);

        Player newUserToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(existingUser.getLogin(), newUserToCreate)
                        .expectStatus(403);

        String errorTitleLower = createResponse.asError(ErrorBody.class).getTitle().toLowerCase();
        Assert.assertTrue(errorTitleLower.contains("not allowed")
                        || errorTitleLower.contains("forbidden")
                        || errorTitleLower.contains("only those with role"),
                "Expected role restriction error message");
    }

    @Test(description = "Age below 16 should be rejected Test")
    public void ageBelowSixteenShouldBeRejectedTest() {
        Player playerWithInvalidAge = TestDataGenerator.generatePlayerWithInvalidAgeYoung();

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithInvalidAge)
                        .expectStatus(400);

        Assert.assertTrue(createResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("age"),
                "Expected age validation error");
    }

    @Test(description = "Age above 60 should be rejected Test")
    public void ageAboveSixtyShouldBeRejectedTest() {
        Player playerWithInvalidAge = TestDataGenerator.generatePlayerWithInvalidAgeOld();

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithInvalidAge)
                        .expectStatus(400);

        Assert.assertTrue(createResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("age"),
                "Expected age validation error");
    }

    @Test(description = "Invalid role should be rejected Test")
    public void invalidRoleShouldBeRejectedTest() {
        Player playerWithInvalidRole = TestDataGenerator.generatePlayerWithInvalidRole();

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithInvalidRole)
                        .expectStatus(400);

        Assert.assertTrue(createResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("role"),
                "Expected role validation error");
    }

    @Test(description = "Duplicate login should be rejected Test")
    public void duplicateLoginShouldBeRejectedTest() {
        Player firstPlayer = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int firstPlayerId = playersService.create(PlayersService.defaultSupervisor(), firstPlayer)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(firstPlayerId);

        Player playerWithDuplicateLogin =
                TestDataGenerator.generatePlayerWithDuplicateLogin(firstPlayer.getLogin());

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithDuplicateLogin)
                        .expectStatus(400);

        Assert.assertTrue(createResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("login"),
                "Expected unique login error");
    }

    @Test(description = "Duplicate screenName should be rejected Test")
    public void duplicateScreenNameShouldBeRejectedTest() {
        Player firstPlayer = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int firstPlayerId = playersService.create(PlayersService.defaultSupervisor(), firstPlayer)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(firstPlayerId);

        Player playerWithDuplicateScreenName =
                TestDataGenerator.generatePlayerWithDuplicateScreenName(firstPlayer.getScreenName());

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithDuplicateScreenName)
                        .expectStatus(400);

        Assert.assertTrue(createResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("screen"),
                "Expected unique screenName error");
    }

    @Test(description = "Password too short should be rejected Test")
    public void passwordTooShortShouldBeRejectedTest() {
        Player playerWithShortPassword = TestDataGenerator.generatePlayerWithInvalidPasswordShort();

        playersService.create(PlayersService.defaultSupervisor(), playerWithShortPassword)
                .expectStatus(400);
    }

    @Test(description = "Password too long should be rejected Test")
    public void passwordTooLongShouldBeRejectedTest() {
        Player playerWithLongPassword = TestDataGenerator.generatePlayerWithInvalidPasswordLong();

        playersService.create(PlayersService.defaultSupervisor(), playerWithLongPassword)
                .expectStatus(400);
    }

    @Test(description = "Password without numbers should be rejected Test")
    public void passwordWithoutNumbersShouldBeRejectedTest() {
        Player playerWithoutNumbers = TestDataGenerator.generatePlayerWithInvalidPasswordNoNumbers();

        playersService.create(PlayersService.defaultSupervisor(), playerWithoutNumbers)
                .expectStatus(400);
    }

    @Test(description = "Password without letters should be rejected Test")
    public void passwordWithoutLettersShouldBeRejectedTest() {
        Player playerWithoutLetters = TestDataGenerator.generatePlayerWithInvalidPasswordNoLetters();

        playersService.create(PlayersService.defaultSupervisor(), playerWithoutLetters)
                .expectStatus(400);
    }

    @Test(description = "Invalid gender should be rejected Test")
    public void invalidGenderShouldBeRejectedTest() {
        Player playerWithInvalidGender = TestDataGenerator.generatePlayerWithInvalidGender();

        playersService.create(PlayersService.defaultSupervisor(), playerWithInvalidGender)
                .expectStatus(400);
    }
}
