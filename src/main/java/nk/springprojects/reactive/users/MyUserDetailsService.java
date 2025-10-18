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

	/*@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		 * return repository.findByUsername(username).subscribe(user ->{ if (user ==
		 * null) { throw new
		 * UsernameNotFoundException("404 No user with that name found"); } return new
		 * UserPrincipal(user); });
		 
		
		System.out.println("looking for a user engaged");
		return (UserDetails) repository.findByUsername(username).map(user -> {
			if (user == null) {
				throw new UsernameNotFoundException("404 No user with that name found");
			}
			return new UserPrincipal(user);
		}).switchIfEmpty(Mono.error(new UsernameNotFoundException("404 No user with that name found")));
		 
		
		
	}*/

	/*
	 * @Override public Mono<UserDetails> findByUsername(String username) {
	 * System.out.println("looking for a user engaged"); return
	 * repository.findByUsername(username).map(user -> { if (user == null) { throw
	 * new UsernameNotFoundException("404 No user with that name found"); } return
	 * new UserPrincipal(user); }).switchIfEmpty(Mono.error(new
	 * UsernameNotFoundException("404 No user with that name found")));; }
	 */

}
