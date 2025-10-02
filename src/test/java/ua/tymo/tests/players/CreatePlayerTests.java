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
import org.testng.annotations.Test;
import ua.tymo.util.TestDataGenerator;

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
        log.info("Starting test: Supervisor can create an admin");
        Player adminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), adminToCreate)
                        .expectStatus(200);

        Assert.assertEquals(createResponse.asBody().getRole(), Role.ADMIN.getValue(), "Role must be 'admin'");
        createdPlayerIdsForCleanup.add(createResponse.asBody().getPlayerId());
        log.info("Test completed: Admin created successfully");
    }

    @Test(description = "Admin can create another admin Test")
    public void adminCanCreateAnotherAdminTest() {
        log.info("Starting test: Admin can create another admin");
        
        // Create first admin
        Player firstAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        log.info("Creating first admin with login: {}", firstAdminToCreate.getLogin());
        int firstAdminId = playersService.create(PlayersService.defaultSupervisor(), firstAdminToCreate)
                .expectStatus(200).asBody().getPlayerId();
        createdPlayerIdsForCleanup.add(firstAdminId);

        // First admin creates second admin
        Player secondAdminToCreate = TestDataGenerator.generateValidPlayer(Role.ADMIN.getValue());
        log.info("First admin creating second admin with login: {}", secondAdminToCreate.getLogin());
        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(firstAdminToCreate.getLogin(), secondAdminToCreate)
                        .expectStatus(200);

        Assert.assertEquals(createResponse.asBody().getRole(), Role.ADMIN.getValue(), 
                "Role must be 'admin'");
        createdPlayerIdsForCleanup.add(createResponse.asBody().getPlayerId());
        log.info("Test completed: Admin successfully created another admin");
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
        log.info("Starting test: Invalid gender should be rejected");
        Player playerWithInvalidGender = TestDataGenerator.generatePlayerWithInvalidGender();

        playersService.create(PlayersService.defaultSupervisor(), playerWithInvalidGender)
                .expectStatus(400);
        log.info("Test completed: Invalid gender rejected");
    }

    @Test(description = "Cannot create user with supervisor role Test")
    public void cannotCreateUserWithSupervisorRoleTest() {
        log.info("Starting test: Cannot create user with supervisor role");
        
        Player playerWithSupervisorRole = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithSupervisorRole.setRole(Role.SUPERVISOR.getValue());
        log.info("Attempting to create user with role: {}", Role.SUPERVISOR.getValue());

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithSupervisorRole)
                        .expectStatus(400);

        ErrorBody error = createResponse.asError(ErrorBody.class);
        log.info("Received error: {}", error.getTitle());
        Assert.assertTrue(error.getTitle().toLowerCase().contains("role")
                        || error.getTitle().toLowerCase().contains("admin")
                        || error.getTitle().toLowerCase().contains("user"),
                "Expected role validation error for supervisor role, got: " + error.getTitle());
        
        log.info("Test completed: Supervisor role correctly rejected");
    }

    // ---------- Required Fields Validation ----------

    @Test(description = "Create with missing login should be rejected Test")
    public void createWithMissingLoginShouldBeRejectedTest() {
        log.info("Starting test: Create with missing login should be rejected");
        
        Player playerWithMissingLogin = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingLogin.setLogin(null);
        log.info("Attempting to create user without login");

        playersService.create(PlayersService.defaultSupervisor(), playerWithMissingLogin)
                .expectStatus(400);
        
        log.info("Test completed: Missing login correctly rejected");
    }

    @Test(description = "Create with missing password should be rejected Test")
    public void createWithMissingPasswordShouldBeRejectedTest() {
        log.info("Starting test: Create with missing password should be rejected");
        
        Player playerWithMissingPassword = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingPassword.setPassword(null);
        log.info("Attempting to create user without password");

        playersService.create(PlayersService.defaultSupervisor(), playerWithMissingPassword)
                .expectStatus(400);
        
        log.info("Test completed: Missing password correctly rejected");
    }

    @Test(description = "Create with missing age should be rejected Test")
    public void createWithMissingAgeShouldBeRejectedTest() {
        log.info("Starting test: Create with missing age should be rejected");
        
        Player playerWithMissingAge = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingAge.setAge(null);
        log.info("Attempting to create user without age");

        playersService.create(PlayersService.defaultSupervisor(), playerWithMissingAge)
                .expectStatus(400);
        
        log.info("Test completed: Missing age correctly rejected");
    }

    @Test(description = "Create with missing gender should be rejected Test")
    public void createWithMissingGenderShouldBeRejectedTest() {
        log.info("Starting test: Create with missing gender should be rejected");
        
        Player playerWithMissingGender = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingGender.setGender(null);
        log.info("Attempting to create user without gender");

        playersService.create(PlayersService.defaultSupervisor(), playerWithMissingGender)
                .expectStatus(400);
        
        log.info("Test completed: Missing gender correctly rejected");
    }

    @Test(description = "Create with missing role should be rejected Test")
    public void createWithMissingRoleShouldBeRejectedTest() {
        log.info("Starting test: Create with missing role should be rejected");
        
        Player playerWithMissingRole = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingRole.setRole(null);
        log.info("Attempting to create user without role");

        playersService.create(PlayersService.defaultSupervisor(), playerWithMissingRole)
                .expectStatus(400);
        
        log.info("Test completed: Missing role correctly rejected");
    }

    @Test(description = "Create with missing screenName should be rejected Test")
    public void createWithMissingScreenNameShouldBeRejectedTest() {
        log.info("Starting test: Create with missing screenName should be rejected");
        
        Player playerWithMissingScreenName = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithMissingScreenName.setScreenName(null);
        log.info("Attempting to create user without screenName");

        playersService.create(PlayersService.defaultSupervisor(), playerWithMissingScreenName)
                .expectStatus(400);
        
        log.info("Test completed: Missing screenName correctly rejected");
    }

    // ---------- Boundary Value Tests ----------

    @Test(description = "Create with age 16 should succeed Test")
    public void createWithAge16ShouldSucceedTest() {
        log.info("Starting test: Create with age 16 (boundary min) should succeed");
        
        Player playerWithBoundaryAge = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithBoundaryAge.setAge(16);
        log.info("Creating user with boundary age: 16");

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithBoundaryAge)
                        .expectStatus(200);

        Assert.assertEquals(createResponse.asBody().getAge(), Integer.valueOf(16), 
                "Age should be 16");
        createdPlayerIdsForCleanup.add(createResponse.asBody().getPlayerId());
        log.info("Test completed: Age 16 accepted successfully");
    }

    @Test(description = "Create with age 60 should succeed Test")
    public void createWithAge60ShouldSucceedTest() {
        log.info("Starting test: Create with age 60 (boundary max) should succeed");
        
        Player playerWithBoundaryAge = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithBoundaryAge.setAge(60);
        log.info("Creating user with boundary age: 60");

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithBoundaryAge)
                        .expectStatus(200);

        Assert.assertEquals(createResponse.asBody().getAge(), Integer.valueOf(60), 
                "Age should be 60");
        createdPlayerIdsForCleanup.add(createResponse.asBody().getPlayerId());
        log.info("Test completed: Age 60 accepted successfully");
    }

    @Test(description = "Create with password 7 characters should succeed Test")
    public void createWithPassword7CharsShouldSucceedTest() {
        log.info("Starting test: Create with password 7 chars (boundary min) should succeed");
        
        Player playerWithBoundaryPassword = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithBoundaryPassword.setPassword("Pass123"); // exactly 7 chars
        log.info("Creating user with boundary password length: 7");

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithBoundaryPassword)
                        .expectStatus(200);

        Assert.assertEquals(createResponse.asBody().getPassword().length(), 7, 
                "Password length should be 7");
        createdPlayerIdsForCleanup.add(createResponse.asBody().getPlayerId());
        log.info("Test completed: Password with 7 chars accepted successfully");
    }

    @Test(description = "Create with password 15 characters should succeed Test")
    public void createWithPassword15CharsShouldSucceedTest() {
        log.info("Starting test: Create with password 15 chars (boundary max) should succeed");
        
        Player playerWithBoundaryPassword = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
        playerWithBoundaryPassword.setPassword("Password1234567"); // exactly 15 chars
        log.info("Creating user with boundary password length: 15");

        ResponseWrapper<PlayerResponse> createResponse =
                playersService.create(PlayersService.defaultSupervisor(), playerWithBoundaryPassword)
                        .expectStatus(200);

        Assert.assertEquals(createResponse.asBody().getPassword().length(), 15, 
                "Password length should be 15");
        createdPlayerIdsForCleanup.add(createResponse.asBody().getPlayerId());
        log.info("Test completed: Password with 15 chars accepted successfully");
    }
}
