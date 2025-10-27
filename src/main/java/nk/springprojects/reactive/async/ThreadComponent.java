package nk.springprojects.reactive.async;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.dto.VoteType;
import nk.springprojects.reactive.service.SkillRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile({"dev", "prod"})
@Slf4j
public class ThreadComponent implements ApplicationListener<DBSeedCompletedEvent>, ThreadVoter {
    private final SkillRatingService service;
    private final AtomicBoolean canStart = new AtomicBoolean(false);

    @Async("voteSimulationExecutor")
    @Scheduled(fixedRate = 3000)
    public void simulateVotes() {
        if (!canStart.get()) return;

        String voteThreadName = Thread.currentThread().getName();
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
                return service.handleVote(request).doOnSuccess(result ->
                        log.info(
                                "[skillrater] INFO | launched by Thread: {} | new vote simulation added for {}",
                               voteThreadName, // ✅ Added thread name here
                                request.getClass().getSimpleName()
                        )
                );  // ✅ this now triggers eventBus.publish internally
            })
            .subscribe();
    }

    @Override
    public void onApplicationEvent(DBSeedCompletedEvent event) {
        canStart.set(true);
    }
}
