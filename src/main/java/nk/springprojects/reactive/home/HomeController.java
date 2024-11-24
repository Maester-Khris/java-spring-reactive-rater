package nk.springprojects.reactive.home;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import nk.springprojects.reactive.kafka.ThreadComponent;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
	
	private final HomeService service;
	private final ThreadComponent threadcomponent;
	
	public List<String> skills = new ArrayList<String>(Arrays.asList("Frontend","Backend"));
	record SkillRating(String skillname, String skilluuid, int rating) {
		
	}
	
	@GetMapping("loggedinfo")
	public ResponseEntity<Principal> getLoggeInfo(Principal p){
		System.out.println(p.toString());
		return new ResponseEntity<>(p, HttpStatus.OK);
	}


	@GetMapping
	public Mono<Rendering> home(ServerWebExchange exchange, @AuthenticationPrincipal Object principal){		
		 boolean isUserAuthenticated = Objects.isNull(principal);
		 System.out.println(isUserAuthenticated);
	    return Mono.just(Rendering.view("index")
	            .modelAttribute("skills", service.getRepository().findTop4ByOrderByRatingDesc())
	            .modelAttribute("isUserAuthenticated", isUserAuthenticated)
	            .build());
	}
	
	@GetMapping("/votes")
	public Mono<Rendering> skills(@AuthenticationPrincipal Object principal){
		boolean isUserAuthenticated = Objects.isNull(principal);
		return Mono.just(Rendering.view("skills")
				.modelAttribute("skills", service.getRepository().findTop4ByOrderByRatingDesc())
				.modelAttribute("isUserAuthenticated", isUserAuthenticated)
				.build()
			);
	}
	
	@GetMapping("/myratings")
	public Mono<Rendering> myratings(){
		return Mono.just(Rendering.view("myratings")
				.modelAttribute("skills", service.getRepository().findTop4ByOrderByRatingDesc())
				.build()
			);
	}
	
	
	//.modelAttribute("sortedskills", service.getRepository().findTop4ByOrderByRatingDesc())
	@GetMapping("/rater")
	public Mono<Rendering> rater(){
		return Mono.just(Rendering.view("skillrater")
				.modelAttribute("skills", service.getRepository().findAll())
				.modelAttribute("testskill", service.getRepository().findFirstBySkillname("ASP.NET"))
				.build()
			);
	}
	
	@PostMapping("/post-rating")
	public Mono<ResponseEntity<String>> postSkillRating(@RequestBody SkillRating rating){
		System.out.println("the skill code is "+rating.skilluuid()+" its rating is "+rating.rating());

		return service.updateSkill(rating.skilluuid(), rating.rating())
        .then(service.getRepository().findFirstBySkilluuid(rating.skilluuid()))
        .flatMap(skill -> {
            // Use a Scheduler to handle the web production in a non-blocking way
            return Mono.fromRunnable(() -> threadcomponent.webProducer(skill))
                       .subscribeOn(Schedulers.boundedElastic()) // Use boundedElastic for blocking operations
                       .then(Mono.just(skill));
        })
        .then(Mono.just(new ResponseEntity<>("Your response was registered", HttpStatus.OK)));


		//service.updateSkill(rating.skilluuid(), rating.rating()).subscribe();
		// service.getRepository().findFirstBySkilluuid(rating.skilluuid()).flatMap(skill -> {
		// 	Thread producer = new Thread(new WebThreadProducer(skill));
		// 	producer.start();
		// 	return Mono.just(skill);
		// });
		//return new ResponseEntity<>("Your response was registered", HttpStatus.OK);
		
//		.map(session -> {
//            String data = (String) session.getAttributes().get("message");
//            if (data != null) {
//                session.getAttributes().remove("message");
//            }		            
//        }).block();
	}

}
