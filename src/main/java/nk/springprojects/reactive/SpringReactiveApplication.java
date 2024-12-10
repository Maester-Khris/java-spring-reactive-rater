package nk.springprojects.reactive;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import nk.springprojects.reactive.home.HomeService;
import nk.springprojects.reactive.home.Skill;
import nk.springprojects.reactive.student.Student;
import nk.springprojects.reactive.student.StudentService;
import reactor.core.publisher.Mono;

@SpringBootApplication
@OpenAPIDefinition
public class SpringReactiveApplication {
	
	@Value("classpath:data/allicons.json")
	private Resource resource;
	
	record Language(String name, String[] aliases) {}
	record Icon(String name, String icon) {}

	public static void main(String[] args) {
		SpringApplication.run(SpringReactiveApplication.class, args);
	}
	
	// ========================= Helper ===============================
	
	@Bean
	CommandLineRunner jsonReadRUnner() {
		boolean flag = false;
		return args ->{
			if(flag == true) {
				System.out.println("testing something out");
				InputStream jsoncont = resource.getInputStream();	
				ObjectMapper mapper = new ObjectMapper();
				List<Icon> iconlist = mapper.readValue(jsoncont, new TypeReference<List<Icon>>() {});
				List<String> iconnames = iconlist.stream().map(i ->i.icon).collect(Collectors.toList());
				//iconlist.stream().map(i ->i.name).forEach(System.out::println);
				for(String name: iconnames) {
					System.out.println("icon names "+name);
				}
			}
		};
	}
	
   // ========================= Seeder and DB Manager Helper ===============================
	
	@Bean
	CommandLineRunner emptyDB(HomeService hservice) {
		boolean flag = false;
		return args ->{
			if(flag == true) {
				List<Skill> skills =  hservice.getRepository().findAll().collectList().block();
				if(skills.size()>0) {
					hservice.getRepository().deleteAll().subscribe();
				}
			}
		};
		
	}
	
	@Bean
	CommandLineRunner devIconbSkillsSeedRunner(HomeService hservice) {
		boolean flag = false;
		return args ->{
			System.out.println("hello from DevIcon skill seeder Bean");
			List<Skill> skills =  hservice.getRepository().findAll().collectList().block();
			System.out.println("the size of all skills exiting is "+skills.size());
			if(skills.size()<=0) {
				InputStream jsoncont = resource.getInputStream();	
				ObjectMapper mapper = new ObjectMapper();
				List<Icon> iconlist = mapper.readValue(jsoncont, new TypeReference<List<Icon>>() {});
				List<String> iconlist_names = iconlist.stream().map(iconskill -> iconskill.name).collect(Collectors.toList());
				
				for(Icon icon: iconlist) {
					System.out.println("===========> Seeding started ============");
					hservice.saveSkill(Skill.builder()
						.skillname(icon.name())
						.skillicon(icon.icon())
						.skilluuid(UUID.randomUUID().toString())
						.rating(0)
						.build()).subscribe();
				}
			}
		};
	}
	
	@Bean
	CommandLineRunner searchIcon(HomeService hservice) throws IOException {
		boolean flag = true;
		System.out.println("hello from Icon search Bean");
		InputStream jsoncont = resource.getInputStream();	
		ObjectMapper mapper = new ObjectMapper();
		List<Icon> iconlist = mapper.readValue(jsoncont, new TypeReference<List<Icon>>() {});
		List<String> iconnames = iconlist.stream().map(i ->i.icon).collect(Collectors.toList());
		String defaultIcon = "devicon-vscode-plain colored";
		return args ->{
			if(flag == true) {
				hservice.getRepository().findTop4ByOrderByRatingDesc()
					.collectList()
					.flatMap(skilllist -> {
						HashMap<Skill, String> skillmap = new HashMap<Skill, String>();
						skilllist.forEach(s -> {
							System.out.println("the skill is"+s.getSkillname());
							String iconname = iconnames.stream().filter(name -> name.trim().contains(s.getSkillname())).findFirst().orElse(defaultIcon);
							skillmap.put(s, iconname);
						});
						System.out.println("content of skill map");
						skillmap.entrySet().stream().forEach(System.out::println);
						return Mono.empty();
					}).subscribe();
			}
		};
	}
	
	
	
	//=============== OLD CODE ============================================
	
	
	@Bean
	CommandLineRunner githubSkillsSeedRunner(HomeService hservice) {
		boolean flag = false;
		return args ->{
			List<Skill> skills =  hservice.getRepository().findAll().collectList().block();
			System.out.println("the size of all skills exiting is "+skills.size());
			if(flag==true) {
				String languages = hservice.retrieveRemoteSkill();
				ObjectMapper mapper = new ObjectMapper();
				List<Language> langs = mapper.readValue(languages, new TypeReference<List<Language>>() {});
				langs.stream().map(l -> l.name).forEach(System.out::println);
				List<String> langnames = langs.stream().map(l -> l.name).collect(Collectors.toList());
				langnames.stream().forEach(System.out::println);
				//now use only the title to initialise localskill db and seed data within databse
				System.out.println("=====sysinsertion debuted=====");
				for(String lang: langnames){
					System.out.println("the lang name is "+lang);
					hservice.saveSkill(Skill.builder()
						.skillname(lang)
						.skilluuid(UUID.randomUUID().toString())
						.rating(0)
						.build()).subscribe();
				}
				System.out.println("=====sysinsertion complete=====");
			}
		
		};
	}
	
	@Bean
	CommandLineRunner fakeStudentSeedRunner(StudentService service) {
		Faker faker = new Faker();
		boolean testflag = false;
		return args ->{
			if(testflag==true) {
				for(int i=0; i<5; i++) {
					service.save(
							Student.builder()
							.firstname(faker.name().firstName())
							.lastname(faker.name().lastName())
							.age(faker.number().numberBetween(1, 73))
							.build()
						).subscribe();
				}
			}
		};
	}

}
