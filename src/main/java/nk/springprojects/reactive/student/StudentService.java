package nk.springprojects.reactive.student;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StudentService {

	private final StudentRepository repository;
	
	private final Student[] localStudents;
	
	public Mono<Student> save(Student student){
		return repository.save(student);
	}
	
	public Flux<Student> findAll(){
		return repository.findAll();
	}
	
	public Mono<Student> findById(Integer studentid){
		return repository.findById(studentid);
	}
}
