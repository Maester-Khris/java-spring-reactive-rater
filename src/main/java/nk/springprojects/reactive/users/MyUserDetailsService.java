package nk.springprojects.reactive.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MyUserDetailsService implements ReactiveUserDetailsService{
	
	@Autowired
	UserRepository repository;

	@Override
	public Mono<UserDetails> findByUsername(String username) {
		return repository.findByUsername(username)
			.map(user -> {
				if (user == null) {
					throw new UsernameNotFoundException("404 No user with that name found");
				}
                log.info("[skillrater] INFO | Loading user details | username={}", "[MASKED]");
				return new UserPrincipal(user);
			});
	}
}
