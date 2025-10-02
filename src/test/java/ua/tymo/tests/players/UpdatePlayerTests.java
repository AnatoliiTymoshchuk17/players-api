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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ua.tymo.util.TestDataGenerator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UpdatePlayerTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(UpdatePlayerTests.class);
    private final PlayersService playersService = new PlayersService();
    private final List<Integer> createdPlayerIdsForCleanup = new CopyOnWriteArrayList<>();

    private int createdUserId;

    @BeforeMethod(alwaysRun = true)
    public void createBaseUser() {
        Player baseUserToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        createdUserId = playersService.create(PlayersService.defaultSupervisor(), baseUserToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);
    }

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

    @Test(description = "Supervisor can update age Test")
    public void supervisorCanUpdateAgeTest() {
        Player partialUpdatePayload = new Player();
        partialUpdatePayload.setAge(30);

        playersService.update(PlayersService.defaultSupervisor(), createdUserId, partialUpdatePayload)
                .expectStatus(200);

        ResponseWrapper<PlayerResponse> getAfterUpdateResponse =
                playersService.getById(createdUserId).expectStatus(200);

        Assert.assertEquals(getAfterUpdateResponse.asBody().getAge(), Integer.valueOf(30), "Age should be updated");
    }

    @Test(description = "Supervisor can update gender Test")
    public void supervisorCanUpdateGenderTest() {
        Player partialUpdatePayload = new Player();
        partialUpdatePayload.setGender("female");

        playersService.update(PlayersService.defaultSupervisor(), createdUserId, partialUpdatePayload)
                .expectStatus(200);

        ResponseWrapper<PlayerResponse> getAfterUpdateResponse =
                playersService.getById(createdUserId).expectStatus(200);

        Assert.assertEquals(getAfterUpdateResponse.asBody().getGender(), "female", "Gender should be updated");
    }

    @Test(description = "Supervisor can update login Test")
    public void supervisorCanUpdateLoginTest() {
        Player partialUpdatePayload = TestDataGenerator.generateUpdatePlayerWithNewLogin();

        playersService.update(PlayersService.defaultSupervisor(), createdUserId, partialUpdatePayload)
                .expectStatus(200);

        ResponseWrapper<PlayerResponse> getAfterUpdateResponse =
                playersService.getById(createdUserId).expectStatus(200);

        Assert.assertEquals(getAfterUpdateResponse.asBody().getLogin(),
                partialUpdatePayload.getLogin(), "Login should be updated");
    }

    @Test(description = "Supervisor can update screenName Test")
    public void supervisorCanUpdateScreenNameTest() {
        Player partialUpdatePayload = TestDataGenerator.generateUpdatePlayerWithNewScreenName();

        playersService.update(PlayersService.defaultSupervisor(), createdUserId, partialUpdatePayload)
                .expectStatus(200);

        ResponseWrapper<PlayerResponse> getAfterUpdateResponse =
                playersService.getById(createdUserId).expectStatus(200);

        Assert.assertEquals(getAfterUpdateResponse.asBody().getScreenName(),
                partialUpdatePayload.getScreenName(), "ScreenName should be updated");
    }

    @Test(description = "Supervisor can update password Test")
    public void supervisorCanUpdatePasswordTest() {
        Player partialUpdatePayload = TestDataGenerator.generateUpdatePlayerWithNewPassword();

        playersService.update(PlayersService.defaultSupervisor(), createdUserId, partialUpdatePayload)
                .expectStatus(200);

        ResponseWrapper<PlayerResponse> getAfterUpdateResponse =
                playersService.getById(createdUserId).expectStatus(200);

        Assert.assertEquals(getAfterUpdateResponse.asBody().getPassword(),
                partialUpdatePayload.getPassword(), "Password should be updated");
    }

    @Test(description = "Supervisor can update multiple fields at once Test")
    public void supervisorCanUpdateMultipleFieldsAtOnceTest() {
        Player partialUpdatePayload = new Player();
        partialUpdatePayload.setAge(35);
        partialUpdatePayload.setGender("female");
        partialUpdatePayload.setLogin("updatedLogin123");

        playersService.update(PlayersService.defaultSupervisor(), createdUserId, partialUpdatePayload)
                .expectStatus(200);

        ResponseWrapper<PlayerResponse> getAfterUpdateResponse =
                playersService.getById(createdUserId).expectStatus(200);

        Assert.assertEquals(getAfterUpdateResponse.asBody().getAge(), Integer.valueOf(35));
        Assert.assertEquals(getAfterUpdateResponse.asBody().getGender(), "female");
        Assert.assertEquals(getAfterUpdateResponse.asBody().getLogin(), "updatedLogin123");
    }

    @Test(description = "Admin can update user Test")
    public void adminCanUpdateUserTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);

        Player partialUpdatePayload = new Player();
        partialUpdatePayload.setAge(40);

        playersService.update(adminToCreate.getLogin(), createdUserId, partialUpdatePayload)
                .expectStatus(200);

        Assert.assertEquals(playersService.getById(createdUserId).expectStatus(200)
                .asBody().getAge(), Integer.valueOf(40));
    }

    @Test(description = "User can update his own profile Test")
    public void userCanUpdateHisOwnProfileTest() {
        ResponseWrapper<PlayerResponse> getCurrent =
                playersService.getById(createdUserId).expectStatus(200);
        String currentLogin = getCurrent.asBody().getLogin();

        Player partialUpdatePayload = new Player();
        partialUpdatePayload.setAge(28);

        playersService.update(currentLogin, createdUserId, partialUpdatePayload)
                .expectStatus(200);

        Assert.assertEquals(playersService.getById(createdUserId).expectStatus(200)
                .asBody().getAge(), Integer.valueOf(28));
    }

    // ---------- Negative ----------

    @Test(description = "Update with invalid age should be rejected Test")
    public void updateWithInvalidAgeShouldBeRejectedTest() {
        Player invalidUpdatePayload = new Player();
        invalidUpdatePayload.setAge(15);

        ResponseWrapper<PlayerResponse> updateResponse =
                playersService.update(PlayersService.defaultSupervisor(), createdUserId, invalidUpdatePayload)
                        .expectStatus(403);

        String errorTitleLower = updateResponse.asError(ErrorBody.class).getTitle().toLowerCase();
        Assert.assertTrue(errorTitleLower.contains("older than 16") || errorTitleLower.contains("younger than 60")
                || errorTitleLower.contains("age"), "Expected age validation error");
    }

    @Test(description = "Update with invalid gender should be rejected Test")
    public void updateWithInvalidGenderShouldBeRejectedTest() {
        Player invalidUpdatePayload = new Player();
        invalidUpdatePayload.setGender("invalid_gender");

        playersService.update(PlayersService.defaultSupervisor(), createdUserId, invalidUpdatePayload)
                .expectStatus(400);
    }

    @Test(description = "Update with invalid password should be rejected Test")
    public void updateWithInvalidPasswordShouldBeRejectedTest() {
        Player invalidUpdatePayload = new Player();
        invalidUpdatePayload.setPassword("short");

        playersService.update(PlayersService.defaultSupervisor(), createdUserId, invalidUpdatePayload)
                .expectStatus(400);
    }

    @Test(description = "Update non-existent user should return 404 Test")
    public void updateNonExistentUserShouldReturn404Test() {
        Player partialUpdatePayload = new Player();
        partialUpdatePayload.setAge(25);

        playersService.update(PlayersService.defaultSupervisor(), 999_999_999, partialUpdatePayload)
                .expectStatus(404);
    }

    @Test(description = "Update with non-existent editor should return 403 Test")
    public void updateWithNonExistentEditorShouldReturn403Test() {
        log.info("Starting test: Update with non-existent editor should return 403");
        Player partialUpdatePayload = new Player();
        partialUpdatePayload.setAge(25);

        playersService.update("non_existent_editor_login", createdUserId, partialUpdatePayload)
                .expectStatus(403);
        log.info("Test completed: Non-existent editor correctly rejected");
    }

    @Test(description = "User cannot update other users Test")
    public void userCannotUpdateOtherUsersTest() {
        log.info("Starting test: User cannot update other users");
        
        // Create first user
        Player firstUserToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        log.info("Creating first user with login: {}", firstUserToCreate.getLogin());
        int firstUserId = playersService.create(PlayersService.defaultSupervisor(), firstUserToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(firstUserId);
        
        // Create second user (the target)
        Player secondUserToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        log.info("Creating second user with login: {}", secondUserToCreate.getLogin());
        int secondUserId = playersService.create(PlayersService.defaultSupervisor(), secondUserToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(secondUserId);

        // First user tries to update second user
        Player updatePayload = new Player();
        updatePayload.setAge(50);
        
        log.info("First user attempting to update second user");
        ResponseWrapper<PlayerResponse> updateResponse =
                playersService.update(firstUserToCreate.getLogin(), secondUserId, updatePayload)
                        .expectStatus(403);

        ErrorBody error = updateResponse.asError(ErrorBody.class);
        log.info("Received error: {}", error.getTitle());
        Assert.assertTrue(error.getTitle().toLowerCase().contains("forbidden")
                        || error.getTitle().toLowerCase().contains("not allowed")
                        || error.getTitle().toLowerCase().contains("only"),
                "Expected permission restriction, got: " + error.getTitle());
        
        log.info("Test completed: User correctly forbidden from updating other users");
    }

    @Test(description = "User cannot update admin Test")
    public void userCannotUpdateAdminTest() {
        log.info("Starting test: User cannot update admin");
        
        // Create admin
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        log.info("Creating admin with login: {}", adminToCreate.getLogin());
        int adminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(adminId);
        
        // Create user
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        log.info("Creating user with login: {}", userToCreate.getLogin());
        int userId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(userId);

        // User tries to update admin
        Player updatePayload = new Player();
        updatePayload.setAge(55);
        
        log.info("User attempting to update admin");
        ResponseWrapper<PlayerResponse> updateResponse =
                playersService.update(userToCreate.getLogin(), adminId, updatePayload)
                        .expectStatus(403);

        ErrorBody error = updateResponse.asError(ErrorBody.class);
        log.info("Received error: {}", error.getTitle());
        Assert.assertTrue(error.getTitle().toLowerCase().contains("forbidden")
                        || error.getTitle().toLowerCase().contains("not allowed")
                        || error.getTitle().toLowerCase().contains("only"),
                "Expected role restriction for user updating admin, got: " + error.getTitle());
        
        log.info("Test completed: User correctly forbidden from updating admin");
    }
}
