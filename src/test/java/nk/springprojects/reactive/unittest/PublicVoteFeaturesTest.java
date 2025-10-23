package nk.springprojects.reactive.unittest;

import nk.springprojects.reactive.exception.SkillNotFoundException;
import nk.springprojects.reactive.model.Skill;
import nk.springprojects.reactive.model.SkillRepository;
import nk.springprojects.reactive.service.SkillRatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PublicVoteFeaturesTest {
    @InjectMocks
    private SkillRatingService service;

    @Mock
    private SkillRepository repository;

    private Skill skill;
    private final String skilluuid ="java-sp-boot";

    @BeforeEach
    public void setup() {
        skill = Skill.builder()
            .id(1)
            .skilluuid(skilluuid)
            .skillname("Java")
            .upvote(0)
            .downvote(0)
            .rating(0)
            .skillicon("devicon-java-plain")
            .build();

        List<Skill> skillList = new ArrayList<>();
        skillList.add(skill);

        // Mock repository behavior
        lenient().when(repository.findFirstBySkilluuid(skilluuid))
            .thenAnswer(invocation -> {
                return skillList.stream()
                    .filter(s -> s.getSkilluuid().equals(skilluuid))
                    .findFirst()
                    .map(Mono::just)
                    .orElse(Mono.empty());
            });

        lenient().when(repository.save(any(Skill.class)))
            .thenAnswer(invocation -> {
                Skill updatedSkill = invocation.getArgument(0);
                // Replace the skill in the list
                skillList.clear();
                skillList.add(updatedSkill);
                return Mono.just(updatedSkill);
            });
    }

    @Test
    @DisplayName("Unit test feature -> Upvote Skill")
    public void testUpVoteSkill() {
        StepVerifier.create(service.upVoteSkill(skilluuid))
            .assertNext(updatedSkill -> {
                assertEquals(1, updatedSkill.getUpvote(), "Upvote should increase by 1");
                assertEquals(0, updatedSkill.getDownvote(), "Downvote should remain unchanged");
                assertEquals(updatedSkill.getUpvote() - updatedSkill.getDownvote(), updatedSkill.getRating());
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Unit test feature -> downvote Skill")
    public void testDownVoteSkill() {
        StepVerifier.create(service.downVoteSkill(skilluuid))
            .assertNext(updatedSkill -> {
                assertEquals(1, updatedSkill.getDownvote(), "Downvote should increase by 1");
                assertEquals(0, updatedSkill.getUpvote(), "Upvote should remain unchanged");
                assertEquals(
                    Math.max(updatedSkill.getUpvote() - updatedSkill.getDownvote(), 0),
                    updatedSkill.getRating(),
                    "Rating should never be lower than 0"
                );
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Unit test feature -> vote on not found skill")
    public void testUpVoteSkill_NotFound() {
        String invalidUuid = "invalid-uuid";

        when(repository.findFirstBySkilluuid(invalidUuid))
            .thenReturn(Mono.empty());

        StepVerifier.create(service.upVoteSkill(invalidUuid))
            .expectErrorMatches(throwable ->
                throwable instanceof SkillNotFoundException &&
                    throwable.getMessage().contains("Skill not found"))
            .verify();
    }

    @Test
    @DisplayName("Unit test feature -> Upvote/downvote Skill not found")
    public void testDownVoteSkill_NotFound() {
        String invalidUuid = "invalid-uuid";

        when(repository.findFirstBySkilluuid(invalidUuid))
            .thenReturn(Mono.empty());

        StepVerifier.create(service.downVoteSkill(invalidUuid))
            .expectErrorMatches(throwable ->
                throwable instanceof SkillNotFoundException &&
                    throwable.getMessage().contains("Skill not found"))
            .verify();
    }
}
