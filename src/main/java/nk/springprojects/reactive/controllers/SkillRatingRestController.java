package nk.springprojects.reactive.controllers;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.async.SkillEventBus;
import nk.springprojects.reactive.async.VoteQueue;
import nk.springprojects.reactive.configurations.VoteRateLimiterProvider;
import nk.springprojects.reactive.dto.ApiSkillResponse;
import nk.springprojects.reactive.dto.SkillRating;
import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.model.Skill;
import nk.springprojects.reactive.service.SkillRatingService;
import nk.springprojects.reactive.model.UserSkillRating;
import nk.springprojects.reactive.users.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import nk.springprojects.reactive.async.ThreadComponent;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/data")
@Slf4j
public class SkillRatingRestController {
	private final SkillRatingService service;
    private final UserRepository userepos;
    private final SkillEventBus eventBus;
    private final VoteQueue votequeue;
    private final VoteRateLimiterProvider RatelimiterProvider;
	
	@Hidden
	@Operation(summary = "Get real time skill data from public vote", description = "This endpoint publish skills update on real-time action after each public vote using server sent event ")
	@GetMapping(value="/skills", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Skill> publishSkills(){
        return eventBus.stream();
	}
	
	@Operation(summary = "Get the list of registered skill with their data", description = "This endpoint retrieve all skills stored in the database. The definition of a skill is present at the bottom of this page ")
	@GetMapping(value = "/list/skills")
	public Mono<ResponseEntity<List<Skill>>> listAllSkills() {
	    return service.getRepository().findAll()
	        .collectList()
	        .flatMap(skills -> {
	            if (skills.isEmpty()) {
	                return Mono.just(ResponseEntity.notFound().build());
	            } else {
	                return Mono.just(ResponseEntity.ok(skills));
	            }
	        });
	}
	
	@Operation(summary = "Get the list of registered skill with their data", description = "This endpoint retrieve all skills stored in the database. The definition of a skill is present at the bottom of this page ")
	@GetMapping(value = "/list/skills/{id}")
	public Mono<ResponseEntity<Skill>> skillDetail(@RequestParam int id) {
	    return service.getRepository().findById(Integer.valueOf(id))
	        .flatMap(skill -> {
	            if (Objects.isNull(skill)) {
	                return Mono.just(ResponseEntity.notFound().build());
	            } else {
	                return Mono.just(ResponseEntity.ok(skill));
	            }
	        });
	}

    @PostMapping("/skill-vote")
    public Mono<ResponseEntity<String>> voteSkill(@RequestBody VoteRequest voteRequest,  ServerWebExchange exchange) {
        //String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String clientIp = Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(InetSocketAddress::getAddress)
                .map(InetAddress::getHostAddress)
                .orElse("unknown");


        RateLimiter limiter = RatelimiterProvider.getPublicVoteLimiter();
        ObjectMapper mapper = new ObjectMapper();

        return Mono.defer(() -> service.handleVote(voteRequest))
        .transformDeferred(RateLimiterOperator.of(limiter))
            .flatMap(updatedSkill ->{
                log.info("[skillrater] INFO | REST vote | IP={} | skillUUID={} | type={}",
                        clientIp, voteRequest.skilluuid(), voteRequest.voteType());
                try {
                    String json = mapper.writeValueAsString(
                            new ApiSkillResponse("Skill public vote done", updatedSkill)
                    );
                    return Mono.just(ResponseEntity.ok(json));
                } catch (JsonProcessingException e) {
                    return Mono.just(ResponseEntity.status(500).body("Error processing JSON"));
                }
            })
            .onErrorResume(throwable -> {
                if (throwable instanceof RequestNotPermitted) {
                    log.warn("[skillrater] WARN | Rate limit exceeded for IP={}", clientIp);
                    // fallback: push vote request to queue
                    return votequeue.enqueue(voteRequest)
                        .thenReturn(ResponseEntity.status(429)
                            .body("Too many votes. Your request has been queued."));
                }
                return Mono.just(ResponseEntity.status(500).body("Unexpected error"));
            })
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Rate or update a skill on user personal dashboard", description = "This endpoint update the personal rating of a logged user. The definition of a skillRating update is present at the bottom of this page ")
    @PostMapping(value = "/users/{id}/rating-update")
    public Mono<ResponseEntity<String>> registerPersonalRatingUpdate(
            @PathVariable("id") int id,
            @RequestBody List<SkillRating> skillRatings) {

        return userepos.findById(id)
            .flatMap(user ->
                Flux.fromIterable(skillRatings)
                    .flatMap(skillRating ->
                        service.getRepository()
                            .findFirstBySkilluuid(skillRating.skilluuid())
                            .flatMap(skill ->
                                service.getUSRrepository()
                                    .findFirstByUseridAndSkillid(id, skill.getId())
                                    .flatMap(existing -> {
                                        existing.setRating(skillRating.rating());
                                        existing.setProficiency(skillRating.proficiency());
                                        return service.getUSRrepository().save(existing).doOnSuccess(usr-> log.info("[skillrater] INFO | USER personal rating update | skillId={}", usr.getSkillid()));
                                    })
                                    .switchIfEmpty(
                                        Mono.defer(() -> {
                                            UserSkillRating newRating = new UserSkillRating();
                                            newRating.setUserid(id);
                                            newRating.setSkillid(skill.getId());
                                            newRating.setRating(skillRating.rating());
                                            newRating.setProficiency(skillRating.proficiency());
                                            return service.getUSRrepository().save(newRating);
                                        })
                                    )
                            )
                            .switchIfEmpty(
                                Mono.error(new IllegalStateException(
                                        "Skill not found for UUID: " + skillRating.skilluuid()))
                            )
                    )
                    .then(Mono.just(ResponseEntity.ok("Ratings updated successfully for user " + id)))
            )
            .switchIfEmpty(
                Mono.just(ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User not found with id " + id))
            );
    }
}


// ============== OLD Code for voteSkill method ==============
//return service.handleVote(voteRequest)
//    .flatMap(updatedSkill -> {
//        log.info("[skillrater] INFO | REST vote | skillUUId={} | voteType={}", voteRequest.skilluuid(), voteRequest.voteType());
//        try {
//            String json = mapper.writeValueAsString(
//                    new ApiSkillResponse("Skill public vote done", updatedSkill)
//            );
//            return Mono.just(ResponseEntity.ok(json));
//        } catch (JsonProcessingException e) {
//            return Mono.just(ResponseEntity.status(500).body("Error processing JSON"));
//        }
//    })
//    .defaultIfEmpty(ResponseEntity.notFound().build());
