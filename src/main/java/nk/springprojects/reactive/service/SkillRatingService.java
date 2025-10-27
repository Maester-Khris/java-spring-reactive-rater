package nk.springprojects.reactive.service;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.async.SkillEventBus;
import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.exception.SkillNotFoundException;
import nk.springprojects.reactive.model.*;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class SkillRatingService {
    private final CopyOnWriteArrayList<Skill> localRating = new CopyOnWriteArrayList<Skill>();
    private final SkillRepository repository;
    private final UserSkillRatingRepository userSkillRatingRepository;
    private final SkillEventBus eventBus;
    private static final String NOT_FOUND_MSG = "Skill not found with UUID: ";
//    private final Retry dbWriteRetry;
//    private final VoteCacheService voteCacheService;

    public Mono<Skill> saveSkill(Skill s){
        return repository.save(s);
    }

    public Mono<Skill> handleVote(VoteRequest voteRequest) {
        return repository.findFirstBySkilluuid(voteRequest.skilluuid())
            .flatMap(skill -> {
                log.debug("[skillrater] DEBUG | Computing new rating for skillId={}", skill.getId());
                skill.applyVote(voteRequest.voteType());
                return repository.save(skill)
                        .doOnSuccess(eventBus::publish);
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Skill> upVoteSkill(String skilluuid){
        return repository.findFirstBySkilluuid(skilluuid)
            .flatMap(skill -> {
                skill.setUpvote(skill.getUpvote()+1);
                skill.updateRating();
                return repository.save(skill);
            })
            .switchIfEmpty(Mono.error(new SkillNotFoundException(NOT_FOUND_MSG+": " + skilluuid)));
    }

    public Mono<Skill> downVoteSkill(String skilluuid){
        return repository.findFirstBySkilluuid(skilluuid)
            .flatMap(skill -> {
                skill.setDownvote(skill.getDownvote()+1);
                skill.updateRating();
                return repository.save(skill);
            })
            .switchIfEmpty(Mono.error(new SkillNotFoundException(NOT_FOUND_MSG+": " + skilluuid)));
    }
	
	public Mono<Skill> updateSkill(String skilluuid){	
		return repository.findFirstBySkilluuid(skilluuid)
	        .flatMap(skill -> {
                skill.updateRating();
	            return repository.save(skill);
	        })
	        .switchIfEmpty(Mono.error(new SkillNotFoundException(NOT_FOUND_MSG+": " + skilluuid)));
	}
	
    public Flux<UserSkillRating> userSkillRatings(Integer userid){
        return  userSkillRatingRepository.findAllByUserid(userid);
    }


    // =================== old method: no more usedd ==================
    public String retrieveRemoteSkill(){
        WebClient restclient = WebClient.create("https://api.github.com/languages");
        return restclient.get().retrieve().bodyToMono(String.class).block();
    }

    // =================== update with caching fallback: no more used ==================
//    public Mono<Skill> handleVote(VoteRequest voteRequest) {
//        // 1. Core Persistence Logic: A Supplier for the DB write operation
//        // This is the function we want to protect and retry.
//        Supplier<Mono<Skill>> dbPersistenceSupplier = () ->
//            repository.findFirstBySkilluuid(voteRequest.skilluuid())
//                .flatMap(skill -> {
//                    log.debug("[skillrater] DEBUG | Computing new rating for skillId={}", skill.getId());
//                    skill.applyVote(voteRequest.voteType());
//                    return repository.save(skill)
//                        .doOnSuccess(eventBus::publish);
//                })
//                // If the DB call is blocking or slow, this ensures it runs on a suitable thread pool.
//                .subscribeOn(Schedulers.boundedElastic());
//
//        // 2. Fallback Logic: Write to the persistent cache (Redis)
//        Function<Throwable, Mono<Skill>> cacheFallback = (throwable) -> {
//            log.warn("[skillrater] WARN | DB unavailable. Falling back to Cache for vote: {}",
//                voteRequest.skilluuid(), throwable.getMessage());
//
//            // Save the raw request to the cache/queue for later async retry
//            // This should return the Skill object or a placeholder to continue the reactive chain.
//            return voteCacheService.cacheVote(voteRequest)
//                .flatMap(cachedVote -> {
//                    // If successful in caching, return a Mono<Skill> (e.g., the existing skill state)
//                    // so the rest of the chain (controller logging, response) can proceed successfully.
//                    return repository.findFirstBySkilluuid(voteRequest.skilluuid());
//                });
//        };
//
//        // 3. Application of Retry and Fallback
//        return Mono.defer(dbPersistenceSupplier)
//                // Use the standard Reactor retryWhen operator, passing the Resilience4j Retry instance
//                // via the RetryOperator utility.
//                .retryWhen(dbWriteRetry. reactorRetry())
//                // If retries fail, execute the fallback logic.
//                // The exceptions caught by retryWhen (and defined in dbWriteRetry) will eventually
//                // fall through to onErrorResume if maxAttempts is reached.
//                .onErrorResume(cacheFallback);
//    }
}

