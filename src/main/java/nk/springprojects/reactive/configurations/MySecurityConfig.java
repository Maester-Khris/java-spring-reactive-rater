package nk.springprojects.reactive.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

import nk.springprojects.reactive.users.MyUserDetailsService;

@Configuration
@EnableWebFluxSecurity
public class MySecurityConfig{
	@Autowired
	MyUserDetailsService userdetailservice;
	
	@Bean 
	public PasswordEncoder passEncoder() { 
	   return new BCryptPasswordEncoder(10); 
	}
	
	
	@Bean
	public ReactiveAuthenticationManager authmanager() {
		UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
	                 new UserDetailsRepositoryReactiveAuthenticationManager(userdetailservice);

        authenticationManager.setPasswordEncoder(passEncoder());
        return authenticationManager;
	}
	
	private ServerWebExchangeMatcher isNot(ServerWebExchangeMatcher matcher) {
        return new NegatedServerWebExchangeMatcher(matcher);
    }
	
	
	@Bean 
	public SecurityWebFilterChain securityConfig(ServerHttpSecurity http) {
		DelegatingServerLogoutHandler logoutHandler = new DelegatingServerLogoutHandler(
	            new SecurityContextServerLogoutHandler(), new WebSessionServerLogoutHandler()
	    );
		
		http
			 .csrf((csrf) -> csrf
					 .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
//                     .ignoringRequestMatchers("/api/**")
             )
			 .authorizeExchange((authorize) -> authorize
					 .pathMatchers("/", "/login", "/skills", "/swagger-ui/index.html").permitAll()
					 .pathMatchers("/skill-vote").permitAll()
		             .pathMatchers("/myratings").authenticated()
		             .anyExchange().permitAll())
		    .httpBasic(Customizer.withDefaults())
		    .formLogin((formLogin) -> formLogin
		    		.authenticationManager(authmanager())
		    		.loginPage("/login"))
		    .logout(logout -> logout.logoutHandler(logoutHandler));

		
		return http.build();		
	}
	
}
