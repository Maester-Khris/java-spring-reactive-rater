package nk.springprojects.reactive.service;

import java.util.concurrent.CopyOnWriteArrayList;

import nk.springprojects.reactive.exception.SkillNotFoundException;
import nk.springprojects.reactive.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class SkillRatingService {
	private final CopyOnWriteArrayList<Skill> localRating = new CopyOnWriteArrayList<Skill>();
	private final SkillRepository repository;
	private final UserSkillRatingRepository USRrepository;

    public Mono<Skill> saveSkill(Skill s){
        return repository.save(s);
    }

    public Mono<Skill> upVoteSkill(String skilluuid){
        return repository.findFirstBySkilluuid(skilluuid)
                .flatMap(skill -> {
                    skill.setUpvote(skill.getUpvote()+1);
                    skill.updateRating();
                    return repository.save(skill);
                })
                .switchIfEmpty(Mono.error(new SkillNotFoundException("Skill not found with UUID: " + skilluuid)));
    }

    public Mono<Skill> downVoteSkill(String skilluuid){
        return repository.findFirstBySkilluuid(skilluuid)
                .flatMap(skill -> {
                    skill.setDownvote(skill.getDownvote()+1);
                    skill.updateRating();
                    return repository.save(skill);
                })
                .switchIfEmpty(Mono.error(new SkillNotFoundException("Skill not found with UUID: " + skilluuid)));
    }
	
	public Mono<Skill> updateSkill(String skilluuid, int rating){	
		return repository.findFirstBySkilluuid(skilluuid)
	        .flatMap(skill -> {
                skill.updateRating();
	            return repository.save(skill);
	        })
	        .switchIfEmpty(Mono.error(new SkillNotFoundException("Skill not found with UUID: " + skilluuid)));
	}
	
	public Flux<UserSkillRating> UserSkillRatings(Integer userid){
        return  USRrepository.findAllByUserid(userid);
//		Flux<Skill> userRatings = USRrepository.findAllByUserid(userid)
//				.flatMap(userrating ->
//					repository.findById(userrating.getSkillid()).map(skill ->{
//						skill.setRating(userrating.getRating());
//						return skill;
//					})
//				);
		
//		Flux<Skill> allSkills = repository.findAll()
//		        .flatMap(skill ->
//		            userRatings.filter(userSkill -> userSkill.getId().equals(skill.getId())).count()
//		                .flatMap(count -> {
//		                    if (count == 0) {
//		                        skill.setRating(0);
//		                    }
//		                    return Mono.just(skill); // Return the skill (with rating set)
//		                })
//		        );
		
//		return  Flux.concat(userRatings, allSkills);
	}


    // =================== old method: no more usedd ==================
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
