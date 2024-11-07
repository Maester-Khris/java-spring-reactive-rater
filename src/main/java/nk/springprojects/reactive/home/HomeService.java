package nk.springprojects.reactive.home;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Mono;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class HomeService {
	
	private final CopyOnWriteArrayList<String> localSkills = new CopyOnWriteArrayList<String>(List.of("Html", "Css"));
	
	private final CopyOnWriteArrayList<Skill> localRating = new CopyOnWriteArrayList<Skill>();
	
	private final HomeRepository repository;
	
	
	public Mono<Skill> saveSkill(Skill s){
		return repository.save(s);
	}
	
	
	public Mono<Skill> updateSkill(String skilluuid, int rating){	
		return repository.findFirstBySkilluuid(skilluuid)
	        .flatMap(skill -> {
	            skill.setRating(rating);
	            //localRating.add(skill);
	            return repository.save(skill);
	        })
	        .switchIfEmpty(Mono.error(new SkillNotFoundException("Skill not found with UUID: " + skilluuid)));
	}
	
	
	/**
	 * @return a String json array that contains a list of programming languages gathered from github public api
	 * one object structure:  {"name": "F#","aliases": ["f#","fsharp"] },
	*/
	public String retrieveRemoteSkill(){
		WebClient restclient = WebClient.create("https://api.github.com/languages");
		String languages = restclient.get().retrieve().bodyToMono(String.class).block();
		return languages;
	}
	
	//=========== Blocking way to update item in database ============
	//Mono<Skill> s = repository.findFirstBySkilluuid(skilluuid);
	//Skill skill = s.block();
	//System.out.println("the retrieved skill is "+ skill);
	//skill.setRating(rating);
	//return repository.save(skill);
	//update to a reactive approach with streams and method chaining
}
