package nk.springprojects.reactive.controllers;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;

import nk.springprojects.reactive.users.User;
import nk.springprojects.reactive.users.UserRepository;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class AuthController {
	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
	
	@Autowired
	UserRepository repository;

    @Value("${app.guest.password}")
    private String guestPassword;
	
	@GetMapping("/login")
	public Mono<Rendering> displayLogin(){
        log.info("[skillrater] INFO | login attempt");
		return Mono.just(Rendering.view("login")
                .modelAttribute("guestpassword", guestPassword)
				.build()
			);
	}
	
	@PostMapping("/register")
	public Mono<Void> register (@ModelAttribute User newuser, ServerWebExchange exchange) {
		// user management service code 
		System.out.println("this is the new user " + newuser.toString());
	    newuser.setPassword(encoder.encode(newuser.getPassword()));
	    newuser.setCreated_at(LocalDateTime.now());
	    newuser.setUseruuid(UUID.randomUUID().toString());
	    System.out.println("full user" + newuser.toString());
	    repository.save(newuser).subscribe();
	    
	    // user request redirection 
	    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create("/"));
        return exchange.getResponse().setComplete();  
	}
	
	@GetMapping("/hello")	
	public ResponseEntity<String> hello () {
		return ResponseEntity.ok().body("salut les terriens");
	}
	
	
	
//	public ResponseEntity<String> register (@ModelAttribute User newuser) {
//		return ResponseEntity.accepted().body("user well registered");
//    RedirectAttributes redirectAttributes,
//    redirectAttributes.addFlashAttribute("message", "user well registered");
//    return "redirect:/";
//  ================ tried to insert data in session so that it can be used to display to the user ====================
//  exchange.getSession()
//    	    .flatMap(session -> {
//    	        session.getAttributes().put("message", "User successfully registered");
//    	        System.out.println("========== the content of the session:"+session.toString());
//    	        return Mono.just(session); // Return the session for further processing
//    	    })
//    	    .flatMap(session -> {
//    	        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
//    	        exchange.getResponse().getHeaders().setLocation(URI.create("/"));
//    	        return exchange.getResponse().setComplete(); // Complete the response
//    	    })
//    	    .subscribe(); // Subscribe to execute the chain
//
//    return Mono.empty();        
//    redirection with data to the home page
//    exchange.getSession().flatMap(session -> {
//        session.getAttributes().put("message", "User successfully registered");
//        return Mono.empty();
//    }).subscribe();

//	}

}