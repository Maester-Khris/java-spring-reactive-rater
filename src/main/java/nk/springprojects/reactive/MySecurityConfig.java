package nk.springprojects.reactive;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import lombok.RequiredArgsConstructor;

@Configuration
public class MySecurityConfig{
	
	//private final UserDetailsService userDetailService;
	
	 @Bean 
	 public PasswordEncoder passEncoder() { 
		  return new BCryptPasswordEncoder(); 
	 }
	 
//	 @Bean 
//	 public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
//		http.csrf().disable() 
//			.authorizeHttpRequests((authorize)->{
//				authorize.requestMatchers("api/v1/**").permitAll();
//				authorize.anyRequest().authenticated();
//			}).httpBasic(Customizer.withDefaults());
//		
//		return http.build();
//	 }
//	 
//	 @Bean
//	 public UserDetailsService userDetailService() {
//		 UserDetails john = User.builder()
//				 .username("john")
//				 .password(passEncoder().encode("isjohn"))
//				 .roles("USER")
//				 .build();
//		 
//		 UserDetails marta = User.builder()
//				 .username("marta")
//				 .password(passEncoder().encode("ismarta"))
//				 .roles("ADMIN")
//				 .build();
//		 
//		 return new InMemoryUserDetailsManager(john, marta);
//	 }
	 
	
}
