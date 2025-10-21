package nk.springprojects.reactive.integration;

import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.dto.VoteType;
import nk.springprojects.reactive.model.Skill;
import nk.springprojects.reactive.model.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class PublicVoteTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private SkillRepository skillRepository;

    private Skill skill;

    @BeforeEach
    void setup() {
        skill = new Skill();
        skill.setSkilluuid("abc-123");
        skill.setSkillname("Java");
        skill.setUpvote(0);
        skill.setDownvote(0);
        skillRepository.deleteAll().then(skillRepository.save(skill)).block();
    }

    @Test
    @DisplayName("🗳️ Public POST /skill-vote should register upvote without authentication or CSRF")
    void publicVoteShouldWorkWithoutAuthOrCsrf() {
        VoteRequest request = new VoteRequest(VoteType.UPVOTE, skill.getSkilluuid());

        webTestClient.post()
                .uri("/api/v1/data/skill-vote")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), VoteRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Skill public vote done"));

        Skill updated = skillRepository.findFirstBySkilluuid(skill.getSkilluuid())
                .blockOptional()
                .orElseThrow(() -> new AssertionError("Skill not found"));

        assertThat(updated.getUpvote()).isEqualTo(1);
        assertThat(updated.getDownvote()).isEqualTo(0);
    }

    @Test
    @DisplayName("🗳️ Public POST /skill-vote should also handle downvote correctly")
    void publicDownvoteShouldDecreaseScore() {
        VoteRequest request = new VoteRequest(VoteType.DOWNVOTE, skill.getSkilluuid());

        webTestClient.post()
                .uri("/api/v1/data/skill-vote")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        Skill updated = skillRepository.findFirstBySkilluuid(skill.getSkilluuid())
                .blockOptional()
                .orElseThrow(() -> new AssertionError("Skill not found"));

        assertThat(updated.getDownvote()).isEqualTo(1);
    }

    @Test
    @DisplayName("🚫 Invalid skilluuid should return 404 or 400")
    void invalidSkillShouldReturnError() {
        VoteRequest request = new VoteRequest(VoteType.DOWNVOTE, "java_spboot");

        webTestClient.post()
            .uri("/api/v1/data/skill-vote")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isNotFound();
    }

}
