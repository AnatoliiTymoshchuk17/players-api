package spribe.task.tests.players;

import spribe.task.api.core.ResponseWrapper;
import spribe.task.api.model.enums.Role;
import spribe.task.api.model.request.Player;
import spribe.task.api.model.response.PlayerResponse;
import spribe.task.api.services.PlayersService;
import org.testng.Assert;
import org.testng.annotations.Test;
import spribe.task.util.TestDataGenerator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;


public class ConcurrencyTests {

    private final PlayersService playersService = new PlayersService();

    @Test(description = "Concurrent player creation generates unique IDs Test")
    public void concurrentPlayerCreationGeneratesUniqueIdsTest() throws Exception {
        int threads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        Set<Integer> createdIds = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                try {
                    Player playerToCreate = TestDataGenerator.generateValidPlayer(Role.USER.getValue());
                    ResponseWrapper<PlayerResponse> createResponse =
                            playersService.create(PlayersService.defaultSupervisor(), playerToCreate).expectStatus(200);
                    createdIds.add(createResponse.asBody().getPlayerId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdownNow();

        Assert.assertEquals(createdIds.size(), threads, "Expected unique IDs for each created player");
    }
}
