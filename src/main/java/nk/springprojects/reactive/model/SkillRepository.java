package nk.springprojects.reactive.model;

import nk.springprojects.reactive.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SkillRepository extends ReactiveCrudRepository<Skill, Integer> {
	
	Mono<Skill> findFirstBySkilluuid(String skilluuid);
	
	Mono<Skill> findFirstBySkillname(String skillname);
	
	Flux<Skill> findTop5ByOrderByRatingDesc();
	
	Flux<Skill> findAllByOrderByRatingDesc();

    default void debugLogCall(String operation) {
        Logger log = LoggerFactory.getLogger(SkillRepository.class);
        log.debug("[skillrater] DEBUG | SkillRepository operation={}", operation);
    }

}
