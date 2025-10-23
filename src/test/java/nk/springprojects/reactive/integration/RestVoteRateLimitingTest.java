package nk.springprojects.reactive.integration;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import nk.springprojects.reactive.async.VoteQueue;
import nk.springprojects.reactive.configurations.VoteRateLimiterProvider;
import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.dto.VoteType;
import nk.springprojects.reactive.model.Skill;
import nk.springprojects.reactive.service.SkillRatingService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.task.scheduling.enabled=false"
})
@ActiveProfiles("test")
class RestVoteRateLimitingTest {
    @Autowired
    private WebTestClient webTestClient;

    //@Autowired
    @Autowired
    private SkillRatingService service; // your service that handles votes

    @Autowired
    private VoteQueue voteQueue; // the reactive queue

    //@Autowired
    @MockBean
    private VoteRateLimiterProvider rateLimiterProvider;
    private RateLimiter limiter;

    private String skillUuid;
    private VoteRequest testVote;
    private String voteEndpoint = "/api/v1/data/skill-vote";


    @BeforeEach
    void setUp() {
        Skill skill = new Skill();
        skillUuid = UUID.randomUUID().toString();
        skill.setSkilluuid(skillUuid);
        skill.setSkillname("Spring Boot");

        service.saveSkill(skill).block();
        testVote = new VoteRequest(VoteType.UPVOTE, skillUuid);
    }

    @BeforeEach
    void setupRateLimiter() {
        // Create a brand new RateLimiter for each test.
        // This ensures the permit count is reset every time.
        RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(10)
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .timeoutDuration(Duration.ofMillis(0))
            .build();
        limiter = RateLimiter.of("publicVoteLimiter", config);

        // Configure the mocked provider to return the new instance
        Mockito.when(rateLimiterProvider.getPublicVoteLimiter())
                .thenReturn(limiter);
    }

    @AfterEach
    void tearDown() {
        voteQueue.clear(); // clear in-memory queue
    }

    @Test
    @DisplayName("Should return 429 and enqueue vote when rate limit is exceeded")
    void testRateLimitExceededEnqueuesVote() {
        int limit = 10;

        // Send requests equal to limit (should succeed and consume all permits)
        for (int i = 0; i < limit; i++) {
            webTestClient.post().uri(voteEndpoint)
                .bodyValue(testVote)
                .exchange()
                .expectStatus().is2xxSuccessful();
        }

        // Next request exceeds limit → should trigger the onErrorResume fallback
        webTestClient.post().uri(voteEndpoint)
            .bodyValue(testVote)
            .exchange()
            .expectStatus().isEqualTo(429)
            .expectBody(String.class).isEqualTo("Too many votes. Your request has been queued.");

        // Verify vote is queued
        Assertions.assertEquals(1, voteQueue.size());
    }

    @Test
    @DisplayName("Should successfully process a queued vote when manually triggered")
    void testQueuedVoteProcessedLater() {
        // Exceed rate limit to enqueue a vote
        int limit = 10;
        for (int i = 0; i < limit; i++) {
            webTestClient.post().uri(voteEndpoint)
                .bodyValue(testVote)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .returnResult(String.class);
        }

        // The 11th call that queues the vote
        webTestClient.post().uri(voteEndpoint)
            .bodyValue(testVote)
            .exchange()
            .expectStatus().isEqualTo(429);

        // Queue should have 1 vote
        Assertions.assertEquals(1, voteQueue.size());

        // Manually trigger queue processing (simulate @Scheduled)
        voteQueue.processQueue();

        // Allow reactive Mono to complete
        StepVerifier.create(service.handleVote(testVote))
            .expectNextCount(1)
            .verifyComplete();

        // Queue should now be empty
        Assertions.assertEquals(0, voteQueue.size());
    }
}
