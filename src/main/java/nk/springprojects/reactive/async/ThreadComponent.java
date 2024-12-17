package nk.springprojects.reactive.async;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import nk.springprojects.reactive.home.HomeService;
import nk.springprojects.reactive.home.Skill;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;


@Component
public class ThreadComponent {

	@Autowired 
	HomeService service;
	Faker faker = new Faker();
	Random random = new Random();
	
	private final Sinks.Many<Skill> sink = Sinks.many().multicast().onBackpressureBuffer();
	private final Sinks.Many<Skill> localsink = Sinks.many().multicast().onBackpressureBuffer();
	private final Set<String> emittedSkills = ConcurrentHashMap.newKeySet();
	
	public List<String> popularSkillNames = new ArrayList<String>(Arrays.asList("Html","Js","Css"));
	public Random rand = new Random();
			
	
	public void initializeThreadSink() {
		for(Skill skill : service.getLocalRating()) {
			emittedSkills.add(skill.getSkilluuid());
			sink.tryEmitNext(skill);
		}
	}
	
	
	/* Here we are going to create and concat two flux
	 * first flux is created each time based of the list of locall skill stored in the serice
	 * second flux is the one that is used by the produced thread
	 * concat and return 
	*/
	
	public Flux<Skill> consumedData() {
		System.out.println("Here I am the consumer");
		System.out.println("=====what the local rating of size: "+service.getLocalRating().size()+" got now===============");
		service.getLocalRating().stream().forEach(System.out::println);		
		
        sink.asFlux().subscribe();
        localsink.asFlux().subscribe();
		return Flux.merge(sink.asFlux(), localsink.asFlux());
	}
	
	public void produceData() {
		// Here we produce new votes for existings skills		
	    System.out.println("My ID as thread: " + Thread.currentThread().getName());
	    service.getRepository().count().doOnSuccess(count -> { })
	    	.flatMap(length -> {
	            return service.getRepository().findById(random.nextInt(0, length.intValue()))  //temporary solution because database index is broken
	                .flatMap(skill -> {
	                    //System.out.println("Skill to vote UUID: " + skill.getSkillname() + " - " + skill.getRating());
	                    if (random.nextBoolean()) { // Simulate a like
	                        skill.setRating(skill.getRating() + 1);
	                    } else { // Simulate a dislike
	                        if (skill.getRating() > 0) {
	                            skill.setRating(skill.getRating() - 1);
	                        }
	                    }
	                    return service.getRepository().save(skill)
	                        .doOnSuccess(updatedSkill -> {
	                            //System.out.println("Skill after vote: " + updatedSkill.getSkillname() + " - " + updatedSkill.getRating());
	                            sink.tryEmitNext(updatedSkill);
	                        });
	                });
	        })
	        .subscribe(
	            result -> System.out.println("Vote processed successfully."),
	            error -> System.err.println("Error occurred: " + error.getMessage())
	        );
	}
	
	public void webProducer(Skill skill) {
		System.out.println("============skill received inside web producer of thread component=============");
		localsink.tryEmitNext(skill);
	}
	
	
//	Skill producedskill = Skill.builder()
//	.skillname(popularSkillNames.get(rand.nextInt(0, 2)))
//	.skilluuid(UUID.randomUUID().toString())
//	.rating(0)
//	.build();
//System.out.println("the produced skill is"+producedskill.toString());
//sink.tryEmitNext(producedskill);
	
}
