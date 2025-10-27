package nk.springprojects.reactive.service;

import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.dto.VoteRequest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class RedisVoteCacheService implements VoteCacheService{
    private static final String PENDING_VOTES_KEY= "vote:pending:queue";
    private final ReactiveRedisTemplate<String, VoteRequest> redisTemplate;

    public RedisVoteCacheService(ReactiveRedisTemplate<String, VoteRequest> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> cacheVote(VoteRequest request) {
        log.warn("[CACHE] Enqueuing vote {} to Redis due to DB failure.", request.skilluuid());

        // Use RPUSH (right push) on a Redis List to add the vote to the end of the queue.
        // This is the core "durable queue" action.
        return redisTemplate.opsForList()
                .rightPush(PENDING_VOTES_KEY, request)
                .then(); // Return Mono<Void> for chain continuation
    }

    // You would add getPendingVotes() and removeVote() methods here for the scheduled recovery job.
    // Retrieves votes from the queue (e.g., using LRANGE or other polling mechanisms)
    public Flux<VoteRequest> getPendingVotes() {
        return redisTemplate.opsForList().range(PENDING_VOTES_KEY, 0, -1);
    }
}
