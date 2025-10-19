package nk.springprojects.reactive.async;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.service.SkillRatingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoteQueue {
    private final SkillRatingService service;

    @Getter
    private final Queue<VoteRequest> buffer = new ConcurrentLinkedDeque<>();

    public Mono<Void> enqueue(VoteRequest voteRequest) {
        buffer.offer(voteRequest);
        return Mono.empty();
    }

    public int size() {
        return buffer.size();
    }

    public void clear() {
        buffer.clear();
    }

    @Scheduled(fixedRate = 5000)
    public void processQueue() {
        VoteRequest request;
        while ((request = buffer.poll()) != null) {
            final VoteRequest current = request;
            service.handleVote(request)
                .subscribe(result ->
                        log.info("[skillrater] INFO | queued vote processed for {}", current.skilluuid())
                );
        }
    }
}
