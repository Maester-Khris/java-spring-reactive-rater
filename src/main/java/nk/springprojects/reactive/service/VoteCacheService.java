package nk.springprojects.reactive.service;

import nk.springprojects.reactive.dto.VoteRequest;
import reactor.core.publisher.Mono;

public interface VoteCacheService {
    Mono<Void> cacheVote(VoteRequest request);
}
