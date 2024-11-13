package nk.springprojects.reactive.users;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Integer>{

	Mono<User> findByUsername(String username);
}
