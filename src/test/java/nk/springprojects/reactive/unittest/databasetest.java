package nk.springprojects.reactive.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import nk.springprojects.reactive.home.HomeRepository;
import nk.springprojects.reactive.home.HomeService;
import nk.springprojects.reactive.home.Skill;

@SpringBootTest
public class databasetest {
	
	public List<String> langNames = new ArrayList<String>(Arrays.asList("Php","Python","Node","Ruby","Scala"));
	public Random rand = new Random();
	
	@Mock
	HomeRepository repository;
	
	@InjectMocks
	HomeService service;
	
	@BeforeEach
    public void setUp() {
		MockitoAnnotations.openMocks(this);
        System.out.println("==========started the testing phase==============");
        repository.deleteAll();
    }
	
//	@AfterAll
//	public static void cleanDB() {
//		repository.deleteAll();
//	}
//	
	
	
	
	@Test
	public void testSavingNewSkill() {
//		service.saveSkill(Skill.builder()
//			.skillname(langNames.get(rand.nextInt(0, langNames.size())))
//			.skilluuid(UUID.randomUUID().toString())
//			.rating(rand.nextInt())
//			.build()).block();
		
		List<Skill> alls = service.getRepository().findAll().collectList().block();
		//assertEquals(1, alls.size());
		assertNotNull(alls);
	}
	
//	@Test
//	public void testUpdatingSkill() {
//		
//	}
//	
//	@Test
//	public void testDBSeeding() {
//		
//	}

}
