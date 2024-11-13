package nk.springprojects.reactive;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
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
	
	@Bean
	CommandLineRunner jsonReadRUnner() {
		boolean flag = false;
		return args ->{
			if(flag == true) {
				System.out.println("testing something out");
				InputStream jsoncont = resource.getInputStream();	
				ObjectMapper mapper = new ObjectMapper();
				List<Icon> iconlist = mapper.readValue(jsoncont, new TypeReference<List<Icon>>() {});
				iconlist.stream().map(i ->i.name).forEach(System.out::println);
			}
		};
	}
	
	@Bean
	CommandLineRunner skillsSeedRunner(HomeService hservice) {
		return args ->{
			List<Skill> skills =  hservice.getRepository().findAll().collectList().block();
			System.out.println("the size of all skills exiting is "+skills.size());
			if(skills.size()==0) {
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
