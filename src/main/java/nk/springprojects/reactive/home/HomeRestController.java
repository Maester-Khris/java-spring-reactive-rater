package nk.springprojects.reactive.home;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nk.springprojects.reactive.kafka.ThreadComponent;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/data")
public class HomeRestController {
	
	@Autowired
	ThreadComponent consumer;
	@Autowired
	HomeService service;
	
	@GetMapping(value="/skills", produces=MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Skill> publishSkills(){
		System.out.println("==========retrieved consumed data==========");
		return consumer.consumedData();
	}
	
	@GetMapping(value="/list/skills")
	public List<String> listAllSkills() {
		return service.getLocalSkills();
	}
}
