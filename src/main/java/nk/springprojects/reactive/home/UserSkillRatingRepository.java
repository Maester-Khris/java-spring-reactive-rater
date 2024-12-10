package nk.springprojects.reactive.home;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserSkillRatingRepository extends ReactiveCrudRepository<UserSkillRating, Integer> { 

	Flux<UserSkillRating> findAllByUserid(Integer userid);
	Mono<UserSkillRating> findFirstByUseridAndSkillid(Integer userid, Integer skillid);
}
