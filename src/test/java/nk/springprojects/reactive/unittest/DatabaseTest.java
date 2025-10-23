package nk.springprojects.reactive.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import nk.springprojects.reactive.service.SkillRatingService;
import nk.springprojects.reactive.model.SkillRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import nk.springprojects.reactive.model.Skill;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
//@Disabled("All tests in this class are temporarily disabled")
@ActiveProfiles("test")
class DatabaseTest {

	@InjectMocks
	private SkillRatingService service;
	
	@Mock
	private SkillRepository repository;
	
	private ObjectMapper objectMapper;
	private Skill skill;
	private final String skillname = "Java";
	private final String skilluuid = "java-sp-boot";
	private final int rating = 3;

	 @BeforeEach
	 public void testCaseSetup() {
		 MockitoAnnotations.openMocks(this);
	     objectMapper = new ObjectMapper();
	        
		 skill = Skill.builder()
					.id(Integer.valueOf(1))
					.skillname(skillname)
					.skilluuid(skilluuid)
					.rating(rating)
					.skillicon("devicon-java-plain")
					.build();
		 
		List<Skill> skillList = new ArrayList<>();
		skillList.add(skill); // Initial skill
		 
		 
		// Define (mock) the behaviour of the repository 
		when(repository.findAll()).thenAnswer(invocation -> Flux.fromIterable(skillList));
		 when(repository.deleteAll()).thenAnswer(invocation -> {
		        skillList.clear(); // Clear the list when deleteAll is called
		        return Mono.empty();
		    });
		 when(repository.findFirstBySkilluuid(skilluuid)).thenAnswer(invocation -> {
		        return skillList.stream()
		                .filter(s -> s.getSkilluuid().equals(skilluuid))
		                .findFirst()
		                .map(Mono::just)
		                .orElse(Mono.empty());
		    });

		    // Stub the save method to update the list
		    when(repository.save(any(Skill.class))).thenAnswer(invocation -> {
		        Skill newSkill = invocation.getArgument(0);
		        skillList.add(newSkill); // Add the new skill to the list
		        return Mono.just(newSkill);
		    });
	 }
	 
	 @Test
	 public void testSkillInsertion () throws Exception{
		long oldSize = repository.findAll().count().block(); // Count the existing skills
		repository.save(skill).subscribe(); // Save the skill
	    long newSize = repository.findAll().count().block(); // Count skills again
	    assertEquals(oldSize + 1, newSize, "The size of skills should be equal to old size + 1");
	 }
	
	@AfterEach
	public void cleanTestBase() {
		 repository.deleteAll().subscribe();
	}
}
