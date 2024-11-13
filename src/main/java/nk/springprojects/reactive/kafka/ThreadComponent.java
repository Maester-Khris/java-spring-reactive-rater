package nk.springprojects.reactive.kafka;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import nk.springprojects.reactive.home.HomeService;
import nk.springprojects.reactive.home.Skill;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;


@Component
public class ThreadComponent {

	@Autowired 
	HomeService service;
	Faker faker = new Faker();
	
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
		System.out.println("Here i a the producer with ideintifier: "+Thread.currentThread().getName());
		Skill producedskill = Skill.builder()
				.skillname(popularSkillNames.get(rand.nextInt(0, 2)))
				.skilluuid(UUID.randomUUID().toString())
				.rating(0)
				.build();
		
		System.out.println("the produced skill is"+producedskill.toString());
		sink.tryEmitNext(producedskill);	
	}
	
	public void webProducer(Skill skill) {
		System.out.println("============skill received inside thread component=============");
		localsink.tryEmitNext(skill);
	}
	
}
