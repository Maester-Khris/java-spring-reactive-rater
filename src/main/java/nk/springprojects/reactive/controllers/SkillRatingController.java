package nk.springprojects.reactive.controllers;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.dto.*;
import nk.springprojects.reactive.service.SkillRatingService;
import nk.springprojects.reactive.model.UserSkillRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;


import lombok.RequiredArgsConstructor;
import nk.springprojects.reactive.users.UserPrincipal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class SkillRatingController {
    @Autowired
    SkillRatingService service;

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
}