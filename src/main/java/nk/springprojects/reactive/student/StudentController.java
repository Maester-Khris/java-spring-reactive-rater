package nk.springprojects.reactive.student;

import java.time.Duration;

//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

	private final StudentService service;
	
	@PostMapping
	//@PreAuthorize("hasRole('ROLE_ADMIN')")
	public Mono<Student> save(@RequestBody Student student){
		return service.save(student);
	}
	
	@GetMapping
	//@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
	public Flux<Student> getAll(){
		//return service.findAll();
		//how to implement continuous arrival of data
		return service.findAll().delayElements(Duration.ofSeconds(1));
	}
	
	@GetMapping("/{id}")
	public Mono<Student> getById(@PathVariable("id") Integer studentid){
		return service.findById(studentid);
	}
}
