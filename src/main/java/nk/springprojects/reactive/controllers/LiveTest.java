package nk.springprojects.reactive.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import nk.springprojects.reactive.home.Skill;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/live/testing")
public class LiveTest {

	@Operation(summary = "Launch a performance testing on the application and underlying infrastucture", description = "The use of this endpoint endpoint is extremely delicate and only reserved to authorized user. Claim a System admin token to be allowed using it")
	@GetMapping(value = "/performance")
	public ResponseEntity<String> launchPerformancetesting() {
	    return ResponseEntity.ok("Test started");
	}
}
