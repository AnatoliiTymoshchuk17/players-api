package ua.tymo.util;

import ua.tymo.api.model.enums.Gender;
import ua.tymo.api.model.enums.Role;
import ua.tymo.api.model.request.Player;
import ua.tymo.common.env.ConfigFactoryProvider;
import ua.tymo.common.env.TestConfig;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates test data for Player entities.
 * Uses TestConfig for validation rules (age, password length, etc.)
 */
public final class TestDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(TestDataGenerator.class);
    private static final Faker faker = new Faker(new Locale("en"));
    private static final TestConfig config = ConfigFactoryProvider.config();

    private TestDataGenerator() {}

    // ---------- PUBLIC API ----------

    public static Player generateValidPlayer() {
        return generateValidPlayer(Role.USER.getValue());
    }

    public static Player generateValidPlayer(String role) {
        Player player = new Player();
        player.setAge(randomAge());
        player.setGender(randomGender());
        player.setLogin(uniqueLogin());
        player.setPassword(validPassword());
        player.setRole(role);
        player.setScreenName(randomScreenName());
        log.debug("Generated valid player: {}", player);
        return player;
    }

    public static Player generatePlayerWithInvalidAgeYoung() {
        Player player = generateValidPlayer();
        player.setAge(minAge() - 1);
        return player;
    }

    public static Player generatePlayerWithInvalidAgeOld() {
        Player player = generateValidPlayer();
        player.setAge(maxAge() + 1);
        return player;
    }

    public static Player generatePlayerWithInvalidGender() {
        Player player = generateValidPlayer();
        player.setGender("invalid_gender");
        return player;
    }

    public static Player generatePlayerWithInvalidPasswordShort() {
        Player player = generateValidPlayer();
        player.setPassword(alphaNumLettersAndDigits(Math.max(1, minPwdLen() - 1)));
        return player;
    }

    public static Player generatePlayerWithInvalidPasswordLong() {
        Player player = generateValidPlayer();
        int extra = ThreadLocalRandom.current().nextInt(1, 6);
        player.setPassword(alphaNumLettersAndDigits(maxPwdLen() + extra));
        return player;
    }

    public static Player generatePlayerWithInvalidPasswordNoNumbers() {
        Player player = generateValidPlayer();
        int len = randBetween(minPwdLen(), maxPwdLen());
        player.setPassword(lettersOnly(len));
        return player;
    }

    public static Player generatePlayerWithInvalidPasswordNoLetters() {
        Player player = generateValidPlayer();
        int len = randBetween(minPwdLen(), maxPwdLen());
        player.setPassword(digitsOnly(len));
        return player;
    }

    public static Player generatePlayerWithInvalidRole() {
        Player player = generateValidPlayer();
        player.setRole("invalid_role");
        return player;
    }

    public static Player generatePlayerWithDuplicateLogin(String existingLogin) {
        Player player = generateValidPlayer();
        player.setLogin(existingLogin);
        return player;
    }

    public static Player generatePlayerWithDuplicateScreenName(String existingScreenName) {
        Player player = generateValidPlayer();
        player.setScreenName(existingScreenName);
        return player;
    }

    public static Player generateUpdatePlayerWithNewLogin() {
        Player player = new Player();
        player.setLogin(uniqueLogin());
        return player;
    }

    public static Player generateUpdatePlayerWithNewScreenName() {
        Player player = new Player();
        player.setScreenName(randomScreenName());
        return player;
    }

    public static Player generateUpdatePlayerWithNewPassword() {
        Player player = new Player();
        player.setPassword(validPassword());
        return player;
    }

    // ---------- INTERNAL HELPERS ----------

    private static int randomAge() {
        return randBetween(minAge(), maxAge());
    }

    private static String randomGender() {
        return faker.options().option(Gender.MALE.getValue(), Gender.FEMALE.getValue());
    }

    private static String uniqueLogin() {
        String base = faker.internet().username().replaceAll("[^a-zA-Z0-9_\\-]", "");
        String suffix = Integer.toHexString(ThreadLocalRandom.current().nextInt(0xFFFF));
        return (base + "_" + suffix).toLowerCase();
    }

    private static String randomScreenName() {
        return faker.internet().uuid().replace("-", "").substring(0, 8);
    }

    private static String validPassword() {
        int len = randBetween(minPwdLen(), maxPwdLen());
        return alphaNumLettersAndDigits(len);
    }

    private static String alphaNumLettersAndDigits(int length) {
        if (length <= 1) return "a1";
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String all = letters + digits;

        StringBuilder sb = new StringBuilder(length);
        sb.append(letters.charAt(ThreadLocalRandom.current().nextInt(letters.length())));
        sb.append(digits.charAt(ThreadLocalRandom.current().nextInt(digits.length())));
        for (int i = 2; i < length; i++) {
            sb.append(all.charAt(ThreadLocalRandom.current().nextInt(all.length())));
        }
        return sb.toString();
    }

    private static String lettersOnly(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(letters.charAt(ThreadLocalRandom.current().nextInt(letters.length())));
        }
        return sb.toString();
    }

    private static String digitsOnly(int length) {
        String digits = "0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(digits.charAt(ThreadLocalRandom.current().nextInt(digits.length())));
        }
        return sb.toString();
    }

    private static int randBetween(int minIncl, int maxIncl) {
        return ThreadLocalRandom.current().nextInt(minIncl, maxIncl + 1);
    }

    // ---------- CONFIG READERS ----------

    private static int minAge()      { return config.minAge(); }
    private static int maxAge()      { return config.maxAge(); }
    private static int minPwdLen()   { return config.minPasswordLength(); }
    private static int maxPwdLen()   { return config.maxPasswordLength(); }
}
