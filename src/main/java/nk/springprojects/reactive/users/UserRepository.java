package nk.springprojects.reactive.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Integer>{

	Mono<User> findByUsername(String username);
    Mono<Boolean> existsByUsername(String username);

    default void debugLogCall(String operation) {
        Logger log = LoggerFactory.getLogger(UserRepository.class);
        log.debug("[skillrater] DEBUG | UserRepository operation={}", operation);
    }
}
