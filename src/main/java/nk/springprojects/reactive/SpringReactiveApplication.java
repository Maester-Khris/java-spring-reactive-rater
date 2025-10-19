package nk.springprojects.reactive;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import nk.springprojects.reactive.service.SkillRatingService;
import nk.springprojects.reactive.model.Skill;
import reactor.core.publisher.Mono;

@SpringBootApplication
@OpenAPIDefinition
@Slf4j
public class SpringReactiveApplication {
	
	@Value("classpath:data/alliconsv1.json")
	private Resource resource;
	
	record Language(String name, String[] aliases) {}
	record Icon(String name, String icon) {}

    //private static final Logger log = LoggerFactory.getLogger(SpringReactiveApplication.class);

	/**
    public static void main(String[] args) {
		SpringApplication.run(SpringReactiveApplication.class, args);
	}*/

    public static void main(String[] args) {
        log.info("[{}] INFO | Application main starting", "skillrater");
        SpringApplication.run(SpringReactiveApplication.class, args);
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("[{}] INFO | Application started and ready", "skillrater");
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        log.info("[{}] INFO | Application stopping", "skillrater");
    }


    //====================== OLD COD: STILL VALID NO MORE USED ================

//	@Bean
//	CommandLineRunner searchIcon(SkillRatingService hservice) throws IOException {
//		boolean flag = false;
//		System.out.println("hello from Icon search Bean");
//		InputStream jsoncont = resource.getInputStream();
//		ObjectMapper mapper = new ObjectMapper();
//		List<Icon> iconlist = mapper.readValue(jsoncont, new TypeReference<List<Icon>>() {});
//		List<String> iconnames = iconlist.stream().map(i ->i.icon).collect(Collectors.toList());
//		String defaultIcon = "devicon-vscode-plain colored";
//		return args ->{
//			if(flag == true) {
//				hservice.getRepository().findTop5ByOrderByRatingDesc()
//					.collectList()
//					.flatMap(skilllist -> {
//						HashMap<Skill, String> skillmap = new HashMap<Skill, String>();
//						skilllist.forEach(s -> {
//							System.out.println("the skill is"+s.getSkillname());
//							String iconname = iconnames.stream().filter(name -> name.trim().contains(s.getSkillname())).findFirst().orElse(defaultIcon);
//							skillmap.put(s, iconname);
//						});
//						System.out.println("content of skill map");
//						skillmap.entrySet().stream().forEach(System.out::println);
//						return Mono.empty();
//					}).subscribe();
//			}
//		};
//	}

//	@Bean
//	CommandLineRunner githubSkillsSeedRunner(SkillRatingService hservice) {
//		boolean flag = false;
//		return args ->{
//			List<Skill> skills =  hservice.getRepository().findAll().collectList().block();
//			System.out.println("the size of all skills exiting is "+skills.size());
//			if(flag==true) {
//				String languages = hservice.retrieveRemoteSkill();
//				ObjectMapper mapper = new ObjectMapper();
//				List<Language> langs = mapper.readValue(languages, new TypeReference<List<Language>>() {});
//				langs.stream().map(l -> l.name).forEach(System.out::println);
//				List<String> langnames = langs.stream().map(l -> l.name).collect(Collectors.toList());
//				langnames.stream().forEach(System.out::println);
//				//now use only the title to initialise localskill db and seed data within databse
//				System.out.println("=====sysinsertion debuted=====");
//				for(String lang: langnames){
//					System.out.println("the lang name is "+lang);
//					hservice.saveSkill(Skill.builder()
//						.skillname(lang)
//						.skilluuid(UUID.randomUUID().toString())
//						.rating(0)
//						.build()).subscribe();
//				}
//				System.out.println("=====sysinsertion complete=====");
//			}
//
//		};
//	}

}
