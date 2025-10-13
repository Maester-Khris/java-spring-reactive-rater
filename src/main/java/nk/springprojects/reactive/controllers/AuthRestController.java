package nk.springprojects.reactive.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

	// @GetMapping("/csrf-token")
	// public Mono<DefaultCsrfToken> requestToken(DefaultCsrfToken token){
	// 	return Mono.just(token);
	// }
}
