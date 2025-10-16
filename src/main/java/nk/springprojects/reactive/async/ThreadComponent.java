package nk.springprojects.reactive.async;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.dto.VoteType;
import nk.springprojects.reactive.service.SkillRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import nk.springprojects.reactive.model.Skill;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;


@Component
@RequiredArgsConstructor
@Profile({"dev", "prod"})
public class ThreadComponent {

    private final SkillRatingService service;

    @Async
    @Scheduled(fixedRate = 3000)
    public void simulateVotes() {
        service.getRepository().count()
            .flatMap(count ->
                    service.getRepository()
                            .findById(ThreadLocalRandom.current().nextInt(1, count.intValue()))
            )
            .flatMap(skill -> {
                // randomly decide upvote/downvote
                VoteRequest request = new VoteRequest(
                        ThreadLocalRandom.current().nextBoolean() ? VoteType.UPVOTE : VoteType.DOWNVOTE,
                        skill.getSkilluuid()
                );
                return service.handleVote(request);  // ✅ this now triggers eventBus.publish internally
            })
            .subscribe();
    }
}
