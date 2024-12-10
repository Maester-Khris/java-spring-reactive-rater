package nk.springprojects.reactive.home;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import nk.springprojects.reactive.async.ThreadComponent;
import nk.springprojects.reactive.users.MyUserDetailsService;
import nk.springprojects.reactive.users.UserPrincipal;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
	
	@Value("classpath:data/allicons.json")
	private Resource resource;
	
	private final HomeService service;
	private final ThreadComponent threadcomponent;
	
	record Icon(String name, String icon) {}
	record SkillRating(String skillname, String skilluuid, int rating) {}
	record ApiSkillResponse(String message, SkillDefinition skill) {}
	record VoteRequest(String vote, String skilluuid) {}
	public List<String> skills = new ArrayList<String>(Arrays.asList("Frontend","Backend"));
	
	
	/**
	 * Get Views Methods
	*/
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
	public Mono<Rendering> skills(@AuthenticationPrincipal Object principal) throws IOException{
		boolean isUserAuthenticated = Objects.isNull(principal);
		
	    return Mono.just(Rendering.view("skills")
	        .modelAttribute("skills", service.getRepository().findAll())
	        .modelAttribute("isUserAuthenticated", isUserAuthenticated)
	        .build());
	}
	
	@GetMapping("/myratings")
	public Mono<Rendering> myratings(@AuthenticationPrincipal UserPrincipal principal){
		return Mono.just(Rendering.view("myratings")
				.modelAttribute("skills", service.UserSkillRatings(principal.getUserId())) //test if there is no duplicate, try to remove all things from db and start again
				.build()
			);
	}
	
	
	
	/**
	 * Post Views Methods
	 * Use a Scheduler to handle the web production in a non-blocking way
	 * Use boundedElastic for blocking operations
	*/
	@PostMapping("skill-vote")
	public Mono<ResponseEntity<String>> voteSkill(@ModelAttribute VoteRequest voteRequest){
		ObjectMapper mapper = new ObjectMapper();
		//System.out.println("reached out to you vote for"+ voteRequest.skilluuid());
		 return service.getRepository().findFirstBySkilluuid(voteRequest.skilluuid())
			.flatMap(skill ->{
				if(voteRequest.vote().equals("like")) {
					skill.setRating(skill.getRating()+1);
				}else {
					if(skill.getRating()>0) {
						skill.setRating(skill.getRating()-1);
					}
				}
				return service.getRepository().save(skill)
					.doOnSuccess(updatedskill ->{
			             Mono.fromRunnable(() -> threadcomponent.webProducer(updatedskill)).subscribe();
					})
					.flatMap(updatedskill ->{
						try {
			                String jsonres = mapper.writeValueAsString(new ApiSkillResponse("Skill public vote done", updatedskill));
			                return Mono.just(ResponseEntity.ok(jsonres));
			            } catch (JsonProcessingException e) {
			                e.printStackTrace();
			                return Mono.just(ResponseEntity.status(500).body("Error processing JSON"));
			            }
					});
				
	        })
		 .subscribeOn(Schedulers.boundedElastic())
		 .defaultIfEmpty(ResponseEntity.notFound().build());
    }
	
	@PostMapping("skill-rate-update")
	public Mono<ResponseEntity<String>>  updateSkillRate(@AuthenticationPrincipal UserPrincipal principal,  @ModelAttribute SkillRating updatedskill){
		ObjectMapper mapper = new ObjectMapper();
		return service.getRepository().findFirstBySkilluuid(updatedskill.skilluuid())
				.flatMap(skill ->{
					System.out.println("got skill id"+skill.getId());
					return service.getUSRrepository().findFirstByUseridAndSkillid(principal.getUserId(), skill.getId())
						.flatMap(userRating ->{
							userRating.setRating(updatedskill.rating());
							service.getUSRrepository().save(userRating).subscribe();
							return createApiResponse(mapper, "Skill personal rating update done", userRating);
						})
						.switchIfEmpty(createAndSaveNewUserRating(principal.getUserId(), skill.getId(), updatedskill.rating(), mapper));
				});
	}
	
	
	
	/**
	 * Helper Methods
	*/
	private Mono<ResponseEntity<String>> createAndSaveNewUserRating(Integer userId, Integer skillId, int rating, ObjectMapper mapper) {
	    UserSkillRating newUserRating = UserSkillRating.builder()
	            .userid(userId)
	            .skillid(skillId)
	            .rating(rating)
	            .build();

	    return service.getUSRrepository().save(newUserRating)
	        .flatMap(savedUserRating -> createApiResponse(mapper, "Skill personal rating update done", savedUserRating));
	}

	private Mono<ResponseEntity<String>> createApiResponse(ObjectMapper mapper, String message, SkillDefinition skill) {
	    try {
	        String jsonres = mapper.writeValueAsString(new ApiSkillResponse(message, skill));
	        System.out.println("api response " + jsonres);
	        return Mono.just(ResponseEntity.ok(jsonres));
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	        return Mono.just(ResponseEntity.status(500).body("Error processing JSON"));
	    }
	}
	
	
	
	/**
	 * OLD TEST Methods 
	 * Notes: they still working, just not in use
	*/
	@GetMapping("loggedinfo")
	public ResponseEntity<Principal> getLoggeInfo(Principal p){
		System.out.println(p.toString());
		return new ResponseEntity<>(p, HttpStatus.OK);
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
	}
	
	
	
	
//	======================= OLD CODE ===================================	
	
	
//	try {
//        String jsonres = mapper.writeValueAsString(new ApiSkillResponse("Skill personal rating update done", userRating));
//        System.out.println("api response"+jsonres);
//        return Mono.just(ResponseEntity.ok(jsonres));
//    } catch (JsonProcessingException e) {
//        e.printStackTrace();
//        return Mono.just(ResponseEntity.status(500).body("Error processing JSON"));
//    }
	
	
//	return service.getRepository().findAll()
//	.flatMap(skilllist -> {
//		HashMap<Skill, String> skillmap = new HashMap<Skill, String>();
//		skilllist.map(s -> {
//			String normalizedSkillName = s.getSkillname().trim().toLowerCase();
//			String iconname = iconnames.stream()
//                    .map(String::trim)
//                    .map(String::toLowerCase)
//                    .filter(name -> name.contains(normalizedSkillName))
//                    .findFirst()
//                    .orElse(defaultIcon);
//			System.out.println(iconname);
//			skillmap.put(s, iconname);
//			return Mono.empty();
//		});
//		return Mono.just(Rendering.view("skills")
//			.modelAttribute("skills", skillmap)
//			.modelAttribute("isUserAuthenticated", isUserAuthenticated)
//			.build()
//		);
//	});
	
	
// ======================= OLD CODE ===================================	
//	@GetMapping("/rater")
//	public Mono<Rendering> rater(){
//		return Mono.just(Rendering.view("skillrater")
//				.modelAttribute("skills", service.getRepository().findAll())
//				.modelAttribute("testskill", service.getRepository().findFirstBySkillname("ASP.NET"))
//				.build()
//			);
//	}
}
