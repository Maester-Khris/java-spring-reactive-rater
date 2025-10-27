package nk.springprojects.reactive.controllers;

import org.springframework.security.web.server.csrf.DefaultCsrfToken;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @GetMapping("/csrf-token")
    public Mono<CsrfToken> requestToken(Mono<CsrfToken> token){
        return token;
    }

}
