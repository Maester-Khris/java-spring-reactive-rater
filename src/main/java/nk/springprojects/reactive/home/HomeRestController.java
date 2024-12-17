package nk.springprojects.reactive.home;



import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import nk.springprojects.reactive.async.ThreadComponent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/data")
public class HomeRestController {
	
	@Autowired
	ThreadComponent consumer;
	
	@Autowired
	HomeService service;
	
	@Hidden
	@Operation(summary = "Get real time skill data from public vote", description = "This endpoint publish skills update on real-time action after each public vote using server sent event ")
	@GetMapping(value="/skills", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Skill> publishSkills(){
		System.out.println("==========retrieved consumed data==========");
		return consumer.consumedData();
	}
	
	@Operation(summary = "Get the list of registered skill with their data", description = "This endpoint retrieve all skills stored in the database. The definition of a skill is present at the bottom of this page ")
	@GetMapping(value = "/list/skills")
	public Mono<ResponseEntity<List<Skill>>> listAllSkills() {
	    return service.getRepository().findAll()
	        .collectList()
	        .flatMap(skills -> {
	            if (skills.isEmpty()) {
	                return Mono.just(ResponseEntity.notFound().build());
	            } else {
	                return Mono.just(ResponseEntity.ok(skills));
	            }
	        });
	}
	
	@Operation(summary = "Get the list of registered skill with their data", description = "This endpoint retrieve all skills stored in the database. The definition of a skill is present at the bottom of this page ")
	@GetMapping(value = "/list/skills/{id}")
	public Mono<ResponseEntity<Skill>> skillDetail(@RequestParam int id) {
	    return service.getRepository().findById(new Integer(id))
	        .flatMap(skill -> {
	            if (Objects.isNull(skill)) {
	                return Mono.just(ResponseEntity.notFound().build());
	            } else {
	                return Mono.just(ResponseEntity.ok(skill));
	            }
	        });
	}
}
