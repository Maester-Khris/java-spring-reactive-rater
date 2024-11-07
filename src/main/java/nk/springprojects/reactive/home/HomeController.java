package nk.springprojects.reactive.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;

import lombok.RequiredArgsConstructor;
import nk.springprojects.reactive.kafka.WebThreadProducer;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
	
	private final HomeService service;
	
	public List<String> skills = new ArrayList<String>(Arrays.asList("Frontend","Backend"));
	record SkillRating(String skillname, String skilluuid, int rating) {
		
	}

	
	@GetMapping
	public Mono<Rendering> home(){
		return Mono.just(Rendering.view("index")
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
	public ResponseEntity<String> postSkillRating(@RequestBody SkillRating rating){
		System.out.println("the skill code is "+rating.skilluuid()+" its rating is "+rating.rating());
		service.updateSkill(rating.skilluuid(), rating.rating()).subscribe();
		
		service.getRepository().findFirstBySkilluuid(rating.skilluuid()).flatMap(skill -> {
			Thread producer = new Thread(new WebThreadProducer(skill));
			producer.start();
			return Mono.just(skill);
		});
		
		return new ResponseEntity<>("Your response was registered", HttpStatus.OK);
	}

}
