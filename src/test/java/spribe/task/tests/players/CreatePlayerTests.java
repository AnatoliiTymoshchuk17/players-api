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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CreatePlayerTests extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(CreatePlayerTests.class);
    private final PlayersService playersService = new PlayersService();
    private final List<Integer> createdPlayerIdsForCleanup = new CopyOnWriteArrayList<>();

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

    @Test(description = "Supervisor can create a user with valid data Test")
    public void supervisorCanCreateUserWithValidDataTest() {
        Player playerToCreate = TestDataGenerator.generateValidPlayer("user");

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerToCreate)
                        .expectStatus(200);

        PlayerResponse createdPlayerBody = createResponse.asBody();
        
        SoftAssert soft = new SoftAssert();
        soft.assertEquals(createdPlayerBody.getLogin(), playerToCreate.getLogin(), "Login must match");
        soft.assertEquals(createdPlayerBody.getRole(), Role.USER.getValue(), "Role must be 'user'");
        soft.assertNotNull(createdPlayerBody.getPlayerId(), "Player ID must not be null");

        createdPlayerIdsForCleanup.add(createdPlayerBody.getPlayerId());
        soft.assertAll();
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

        PlayerResponse createdUser = createResponse.asBody();
        Assert.assertEquals(createdUser.getRole(), Role.USER.getValue(), "Role must be 'user'");
        createdPlayerIdsForCleanup.add(createdUser.getPlayerId());
        Assert.assertNotNull(createdUser.getPlayerId(), "Player ID must not be null");
    }

    @Test(description = "Supervisor can create an admin Test")
    public void supervisorCanCreateAdminTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                        .expectStatus(200);

        PlayerResponse createdAdmin = createResponse.asBody();
        Assert.assertEquals(createdAdmin.getRole(), Role.ADMIN.getValue(), "Role must be 'admin'");
        createdPlayerIdsForCleanup.add(createdAdmin.getPlayerId());
        Assert.assertNotNull(createdAdmin.getPlayerId(), "Player ID must not be null");
    }

    @Test(description = "Admin can create another admin Test")
    public void adminCanCreateAnotherAdminTest() {
        
        // Create first admin
        Player firstAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int firstAdminId = playersService.create(PlayersService.defaultSupervisor(), firstAdminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(firstAdminId);

        // First admin creates second admin
        Player secondAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(firstAdminToCreate.getLogin(), secondAdminToCreate)
                        .expectStatus(200);

        PlayerResponse secondAdmin = createResponse.asBody();
        Assert.assertEquals(secondAdmin.getRole(), Role.ADMIN.getValue(), 
                "Role must be 'admin'");
        createdPlayerIdsForCleanup.add(secondAdmin.getPlayerId());
        Assert.assertNotNull(secondAdmin.getPlayerId(), "Second admin ID must not be null");
    }

    // ---------- Negative (validation & permissions) ----------

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

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithShortPassword)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Password too long should be rejected Test")
    public void passwordTooLongShouldBeRejectedTest() {
        Player playerWithLongPassword = TestDataGenerator.generatePlayerWithInvalidPasswordLong();

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithLongPassword)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Password without numbers should be rejected Test")
    public void passwordWithoutNumbersShouldBeRejectedTest() {
        Player playerWithoutNumbers = TestDataGenerator.generatePlayerWithInvalidPasswordNoNumbers();

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithoutNumbers)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Password without letters should be rejected Test")
    public void passwordWithoutLettersShouldBeRejectedTest() {
        Player playerWithoutLetters = TestDataGenerator.generatePlayerWithInvalidPasswordNoLetters();

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithoutLetters)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Invalid gender should be rejected Test")
    public void invalidGenderShouldBeRejectedTest() {
        Player playerWithInvalidGender = TestDataGenerator.generatePlayerWithInvalidGender();

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithInvalidGender)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Cannot create user with supervisor role Test")
    public void cannotCreateUserWithSupervisorRoleTest() {
        
        Player playerWithSupervisorRole = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithSupervisorRole.setRole(Role.SUPERVISOR.getValue());

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithSupervisorRole)
                        .expectStatus(400);

        ErrorBody error = createResponse.asError(ErrorBody.class);
        Assert.assertTrue(error.getTitle().toLowerCase().contains("role")
                        || error.getTitle().toLowerCase().contains("admin")
                        || error.getTitle().toLowerCase().contains("user"),
                "Expected role validation error for supervisor role, got: " + error.getTitle());
        
    }

    // ---------- Required Fields Validation ----------

    @Test(description = "Create with missing login should be rejected Test")
    public void createWithMissingLoginShouldBeRejectedTest() {
        
        Player playerWithMissingLogin = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingLogin.setLogin(null);

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithMissingLogin)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Create with missing password should be rejected Test")
    public void createWithMissingPasswordShouldBeRejectedTest() {
        
        Player playerWithMissingPassword = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingPassword.setPassword(null);

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithMissingPassword)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Create with missing age should be rejected Test")
    public void createWithMissingAgeShouldBeRejectedTest() {
        
        Player playerWithMissingAge = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingAge.setAge(null);

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithMissingAge)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Create with missing gender should be rejected Test")
    public void createWithMissingGenderShouldBeRejectedTest() {
        
        Player playerWithMissingGender = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingGender.setGender(null);

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithMissingGender)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Create with missing role should be rejected Test")
    public void createWithMissingRoleShouldBeRejectedTest() {
        
        Player playerWithMissingRole = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingRole.setRole(null);

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithMissingRole)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    @Test(description = "Create with missing screenName should be rejected Test")
    public void createWithMissingScreenNameShouldBeRejectedTest() {
        
        Player playerWithMissingScreenName = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingScreenName.setScreenName(null);

        ResponseWrapper<PlayerResponse> response = playersService.create(PlayersService.defaultSupervisor(), playerWithMissingScreenName)
                .expectStatus(400);
        Assert.assertNotNull(response.asError(ErrorBody.class), "Error body must be present");
    }

    // ---------- Boundary Value Tests ----------

    @Test(description = "Create with age 16 should succeed Test")
    public void createWithAge16ShouldSucceedTest() {
        
        Player playerWithBoundaryAge = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithBoundaryAge.setAge(16);

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithBoundaryAge)
                        .expectStatus(200);

        PlayerResponse created = createResponse.asBody();
        Assert.assertEquals(created.getAge(), Integer.valueOf(16), "Age should be 16");
        createdPlayerIdsForCleanup.add(created.getPlayerId());
        Assert.assertNotNull(created.getPlayerId(), "Player ID must not be null");
    }

    @Test(description = "Create with age 60 should succeed Test")
    public void createWithAge60ShouldSucceedTest() {
        
        Player playerWithBoundaryAge = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithBoundaryAge.setAge(60);

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithBoundaryAge)
                        .expectStatus(200);

        PlayerResponse created = createResponse.asBody();
        Assert.assertEquals(created.getAge(), Integer.valueOf(60), "Age should be 60");
        createdPlayerIdsForCleanup.add(created.getPlayerId());
        Assert.assertNotNull(created.getPlayerId(), "Player ID must not be null");
    }

    @Test(description = "Create with password 7 characters should succeed Test")
    public void createWithPassword7CharsShouldSucceedTest() {
        
        Player playerWithBoundaryPassword = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithBoundaryPassword.setPassword("Pass123"); // exactly 7 chars

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithBoundaryPassword)
                        .expectStatus(200);

        PlayerResponse created = createResponse.asBody();
        Assert.assertEquals(created.getPassword().length(), 7, "Password length should be 7");
        createdPlayerIdsForCleanup.add(created.getPlayerId());
        Assert.assertNotNull(created.getPlayerId(), "Player ID must not be null");
    }

    @Test(description = "Create with password 15 characters should succeed Test")
    public void createWithPassword15CharsShouldSucceedTest() {
        
        Player playerWithBoundaryPassword = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithBoundaryPassword.setPassword("Password1234567"); // exactly 15 chars

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithBoundaryPassword)
                        .expectStatus(200);

        PlayerResponse created = createResponse.asBody();
        Assert.assertEquals(created.getPassword().length(), 15, "Password length should be 15");
        createdPlayerIdsForCleanup.add(created.getPlayerId());
        Assert.assertNotNull(created.getPlayerId(), "Player ID must not be null");
    }

    @Test(description = "Admin cannot create supervisor Test")
    public void adminCannotCreateSupervisorTest() {
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        int createdAdminId = playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdAdminId);

        Player supervisorToCreate = TestDataGenerator.generateValidPlayer(Role.SUPERVISOR.getValue());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(adminToCreate.getLogin(), supervisorToCreate).expectStatus(403);

        ErrorBody error = createResponse.asError(ErrorBody.class);
        Assert.assertTrue(error.getTitle().toLowerCase().contains("only")
                        || error.getTitle().toLowerCase().contains("cannot"),
                "Expected restriction on creating supervisor by admin");
    }

    @Test(description = "User cannot create another user Test")
    public void userCannotCreateAnotherUserTest() {
        Player userToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        int createdUserId = playersService.create(PlayersService.defaultSupervisor(), userToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(createdUserId);

        Player anotherUserToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(userToCreate.getLogin(), anotherUserToCreate).expectStatus(403);

        Assert.assertTrue(createResponse.asError(ErrorBody.class).getTitle().toLowerCase().contains("only those with role"),
                "Expected role restriction for user creating another user");
    }

}
