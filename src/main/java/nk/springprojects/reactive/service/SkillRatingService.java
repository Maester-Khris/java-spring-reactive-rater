package nk.springprojects.reactive.service;

import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.async.SkillEventBus;
import nk.springprojects.reactive.async.ThreadComponent;
import nk.springprojects.reactive.dto.VoteRequest;
import nk.springprojects.reactive.exception.SkillNotFoundException;
import nk.springprojects.reactive.model.*;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final UserSkillRatingRepository USRrepository;
    private final SkillEventBus eventBus;

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

