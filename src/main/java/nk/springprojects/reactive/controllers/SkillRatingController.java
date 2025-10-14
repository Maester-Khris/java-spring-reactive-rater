package nk.springprojects.reactive.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import nk.springprojects.reactive.dto.*;
import nk.springprojects.reactive.model.SkillDefinition;
import nk.springprojects.reactive.service.SkillRatingService;
import nk.springprojects.reactive.model.UserSkillRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nk.springprojects.reactive.async.ThreadComponent;
import nk.springprojects.reactive.users.UserPrincipal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class SkillRatingController {
    @Autowired
    SkillRatingService service;
//    @Autowired
//    ThreadComponent threadcomponent;


    /**
     * =================================== Helper Methods ============================================
     */
    @GetMapping("loggedinfo")
    public ResponseEntity<Principal> getLoggeInfo(Principal p) {
        System.out.println(p.toString());
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    /**
     * =================================== Get Views Methods ===========================================
     */
    @GetMapping
    public Mono<Rendering> home(ServerWebExchange exchange, @AuthenticationPrincipal Object principal) {
        boolean isUserAuthenticated = !Objects.isNull(principal);
        System.out.println("check if user is authenticated: " + isUserAuthenticated);
        return Mono.just(Rendering.view("index")
                .modelAttribute("skills", service.getRepository().findTop5ByOrderByRatingDesc())
                .modelAttribute("isUserAuthenticated", isUserAuthenticated)
                .build());
    }

    @GetMapping("/votes")
    public Mono<Rendering> skills(@AuthenticationPrincipal Object principal) throws IOException {
        boolean isUserAuthenticated = !Objects.isNull(principal);
        System.out.println("check if user is authenticated: " + isUserAuthenticated);
        System.out.println("user authenticated: " + principal);
        return Mono.just(Rendering.view("skills")
            .modelAttribute("skills", service.getRepository().findAll())
            .modelAttribute("top5skills", service.getRepository().findTop5ByOrderByRatingDesc())
            .modelAttribute("isUserAuthenticated", isUserAuthenticated)
            .build());
    }

    @GetMapping("/myratings")
    public Mono<Rendering> myratings(@AuthenticationPrincipal UserPrincipal principal) {
        Flux<UserRatingInfo> userRatedFlux = service.UserSkillRatings(principal.getUserId())
            .flatMap(userRating ->
                service.getRepository()
                    .findById(userRating.getSkillid())
                    .map(skill -> new UserRatingInfo(
                        skill.getSkillname(),
                        skill.getSkilluuid(),
                        skill.getSkillicon(),
                        userRating.getRating(),
                        userRating.getProficiency()
                    ))
            );

        Mono<Set<Integer>> ratedSkillIdsMono = service.UserSkillRatings(principal.getUserId())
            .map(UserSkillRating::getSkillid)
            .collect(Collectors.toSet());

        Mono<List<SysRatingInfo>> systemSkillListMono = ratedSkillIdsMono.flatMapMany(ratedIds ->
            service.getRepository().findAll()
                .map(skill -> new SysRatingInfo(
                    skill.getSkillname(),
                    skill.getSkilluuid(),
                    skill.getSkillicon(),
                    ratedIds.contains(skill.getId()) // check if rated
                ))
        ).collectList();

        Mono<List<UserRatingInfo>> userRatedListMono = userRatedFlux.collectList();
        return Mono.zip(userRatedListMono, systemSkillListMono)
            .map(tuple -> Rendering.view("myratings")
                .modelAttribute("userRatedList", tuple.getT1())
                .modelAttribute("systemSkillList", tuple.getT2())
                    .modelAttribute("userId", principal.getUserId())
                .build()
            );
    }


    /**
     * Post Views Methods
     * Use a Scheduler to handle the web production in a non-blocking way
     * Use boundedElastic for blocking operations
     */
//    @PostMapping("skill-vote")
//    public Mono<ResponseEntity<String>>voteSkill(@ModelAttribute VoteRequest voteRequest) {
//        ObjectMapper mapper = new ObjectMapper();
//        return service.handleVote(voteRequest)
//            .map(updatedSkill -> {
//                try{
//                    String jsonres = mapper.writeValueAsString(new ApiSkillResponse("Skill public vote done", updatedSkill));
//                    return Mono.just(ResponseEntity.ok(jsonres));
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                    return Mono.just(ResponseEntity.status(500).body("Error processing JSON"));
//                }
//            })
//            .defaultIfEmpty(ResponseEntity.notFound().build());
//    }
//    @PostMapping("/post-rating")
//    public Mono<ResponseEntity<String>> postSkillRating(@RequestBody SkillRating rating) {
//        System.out.println("the skill code is " + rating.skilluuid() + " its rating is " + rating.rating());
//
//        return service.updateSkill(rating.skilluuid(), rating.rating())
//            .then(service.getRepository().findFirstBySkilluuid(rating.skilluuid()))
//            .flatMap(skill -> {
//                // Use a Scheduler to handle the web production in a non-blocking way
//                return Mono.fromRunnable(() -> threadcomponent.webProducer(skill))
//                    .subscribeOn(Schedulers.boundedElastic()) // Use boundedElastic for blocking operations
//                    .then(Mono.just(skill));
//            })
//            .then(Mono.just(new ResponseEntity<>("Your response was registered", HttpStatus.OK)));
//    }


    /**
     * ======================================== OLD TEST Methods ===================================
     * Notes: they still working, just not in use
     */
//    @PostMapping("skill-rate-update")
//    public Mono<ResponseEntity<String>> updateSkillRate(@AuthenticationPrincipal UserPrincipal principal, @ModelAttribute SkillRating updatedskill) {
//        ObjectMapper mapper = new ObjectMapper();
//        return service.getRepository().findFirstBySkilluuid(updatedskill.skilluuid())
//            .flatMap(skill ->
//                service.getUSRrepository()
//                    .findFirstByUseridAndSkillid(principal.getUserId(), skill.getId())
//                    .flatMap(userRating -> {
//                        userRating.setRating(updatedskill.rating());
//                        userRating.setProficiency(updatedskill.proficiency());
//
//                        return service.getUSRrepository().save(userRating)
//                                .flatMap(saved -> createApiResponse(mapper, "Skill personal rating update done", saved));
//                    })
//                    // If userRating doesn't exist, create a new one
//                    .switchIfEmpty(createAndSaveNewUserRating(principal.getUserId(),
//                            skill.getId(), updatedskill.rating(), updatedskill.proficiency(), mapper))
//            );
//    }

//    private Mono<ResponseEntity<String>> createAndSaveNewUserRating(Integer userId, Integer skillId, int rating, int prof, ObjectMapper mapper) {
//        UserSkillRating newUserRating = UserSkillRating.builder()
//                .userid(userId)
//                .skillid(skillId)
//                .rating(rating)
//                .proficiency(prof)
//                .build();
//
//        return service.getUSRrepository().save(newUserRating)
//                .flatMap(savedUserRating -> createApiResponse(mapper, "Skill personal rating update done", savedUserRating));
//    }
//
//    private Mono<ResponseEntity<String>> createApiResponse(ObjectMapper mapper, String message, SkillDefinition skill) {
//        try {
//            String jsonres = mapper.writeValueAsString(new ApiSkillResponse(message, skill));
//            System.out.println("api response " + jsonres);
//            return Mono.just(ResponseEntity.ok(jsonres));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//            return Mono.just(ResponseEntity.status(500).body("Error processing JSON"));
//        }
//    }
}

//        return service.getRepository().findFirstBySkilluuid(voteRequest.skilluuid())
//            .flatMap(skill -> {
//                skill.applyVote(voteRequest.voteType());
//                return service.getRepository().save(skill)
//                    .doOnSuccess(updatedskill -> {
//                        Mono.fromRunnable(() -> threadcomponent.webProducer(updatedskill)).subscribe();
//                    })
//                    .flatMap(updatedskill -> {
//                        try {
//                            String jsonres = mapper.writeValueAsString(new ApiSkillResponse("Skill public vote done", updatedskill));
//                            return Mono.just(ResponseEntity.ok(jsonres));
//                        } catch (JsonProcessingException e) {
//                            e.printStackTrace();
//                            return Mono.just(ResponseEntity.status(500).body("Error processing JSON"));
//                        }
//                    });
//            })
//            .subscribeOn(Schedulers.boundedElastic())
//            .defaultIfEmpty(ResponseEntity.notFound().build());