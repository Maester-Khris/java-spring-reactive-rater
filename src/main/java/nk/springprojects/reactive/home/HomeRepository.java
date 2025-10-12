package nk.springprojects.reactive.home;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface HomeRepository extends ReactiveCrudRepository<Skill, Integer> {
	
	Mono<Skill> findFirstBySkilluuid(String skilluuid);
	
	Mono<Skill> findFirstBySkillname(String skillname);
	
	Flux<Skill> findTop5ByOrderByRatingDesc();
	
	Flux<Skill> findAllByOrderByRatingDesc();

}
