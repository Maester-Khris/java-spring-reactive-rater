package nk.springprojects.reactive.integration;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import nk.springprojects.reactive.async.VoteQueue;
import nk.springprojects.reactive.configurations.VoteRateLimiterProvider;
import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.dto.VoteType;
import nk.springprojects.reactive.model.Skill;
import nk.springprojects.reactive.model.SkillRepository;
import nk.springprojects.reactive.service.SkillRatingService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Disabled("All tests in this class are temporarily disabled")
public class RestVoteRateLimitingTest {
    //    @MockBean
//    private SkillRepository repository;

//    @MockBean
//    private SkillRepository repository;

//    @Autowired
//    private SkillRatingService service;

    //    @Mock
//    private SkillRepository repository;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private SkillRatingService service; // your service that handles votes

    @Autowired
    private VoteQueue voteQueue; // the reactive queue

    //@Autowired
    @Mock
    private VoteRateLimiterProvider rateLimiterProvider;

    private RateLimiter limiter;

    private VoteRequest testVote;
    private String voteEndpoint = "/api/v1/data/skill-vote";

    private String skillUuid;

    @BeforeEach
    void setUp() {
        Skill skill = new Skill();
        skillUuid = UUID.randomUUID().toString();
        skill.setSkilluuid(skillUuid);
        skill.setSkillname("Spring Boot");
        //repository.save(skill).block();

        service.saveSkill(skill).block();

        testVote = new VoteRequest(VoteType.UPVOTE, skillUuid);

        // Make service.getRepository() return the mock repository
        // Mockito.when(service.getRepository()).thenReturn(repository);
//
//        // Stub repository methods used in your code
//        Mockito.when(repository.findFirstBySkilluuid(Mockito.anyString()))
//                .thenAnswer(invocation -> Mono.just(new Skill()));

//        Mockito.when(repository.save(Mockito.any(Skill.class)))
//                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

//        Mockito.when(service.handleVote(Mockito.any()))
//                .thenAnswer(invocation -> Mono.just(new Skill())); // stub service
    }

//    @BeforeEach
//    void resetRateLimiter() {
//        RateLimiterConfig config = RateLimiterConfig.custom()
//                .limitForPeriod(10)
//                .limitRefreshPeriod(Duration.ofSeconds(1))
//                .timeoutDuration(Duration.ofMillis(0))
//                .build();
//        RateLimiter freshLimiter = RateLimiter.of("publicVoteLimiter", config);
//        Mockito.when(rateLimiterProvider.getPublicVoteLimiter()).thenReturn(freshLimiter);
//    }

    @BeforeEach
    void setupRateLimiter() {
        //limiter = rateLimiterProvider.getPublicVoteLimiter();
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
        // Reset rate limiter to allow next test to start fresh
        //rateLimiterProvider.getPublicVoteLimiter().reset();
        voteQueue.clear(); // clear in-memory queue
    }

    @Test
    void testRateLimitExceededEnqueuesVote() {
        limiter.acquirePermission();
        int limit = 10; // must match your RateLimiter config

        // Send requests equal to limit (should succeed)
        for (int i = 0; i < limit; i++) {
            webTestClient.post().uri(voteEndpoint)
                    .bodyValue(testVote)
                    .exchange()
                    .expectStatus().is2xxSuccessful();
        }

        // Next request exceeds limit → should return 429
        webTestClient.post().uri(voteEndpoint)
                .bodyValue(testVote)
                .exchange()
                .expectStatus().isEqualTo(429);

        // Verify vote is queued
        Assertions.assertEquals(1, voteQueue.size());
    }

    @Test
    void testQueuedVoteProcessedLater() {
        limiter.acquirePermission();
        // Exceed rate limit to enqueue a vote
        int limit = 10;
        for (int i = 0; i < limit; i++) {
            webTestClient.post().uri(voteEndpoint)
                    .bodyValue(testVote)
                    .exchange()
                    .expectStatus().is2xxSuccessful()
                    .returnResult(String.class);
        }

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
