package nk.springprojects.reactive.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserSkillRatingRepository extends ReactiveCrudRepository<UserSkillRating, Integer> { 

	Flux<UserSkillRating> findAllByUserid(Integer userid);
	Mono<UserSkillRating> findFirstByUseridAndSkillid(Integer userid, Integer skillid);
    //Mono<UserSkillRating> findByUseridAndSkillUuid(Integer userid, String skilluuid);

    default void debugLogCall(String operation) {
        Logger log = LoggerFactory.getLogger(UserSkillRatingRepository.class);
        log.debug("[skillrater] DEBUG | UserSkillRatingRepository operation={}", operation);
    }
}
