package nk.springprojects.reactive.configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nk.springprojects.reactive.service.SkillRatingService;
import nk.springprojects.reactive.model.Skill;
import nk.springprojects.reactive.users.User;
import nk.springprojects.reactive.users.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class ApplicationSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("classpath:data/alliconsv1.json")
    private Resource resource;
    record Icon(String name, String icon) {}

    @Bean
    CommandLineRunner emptyDB(SkillRatingService hservice) {
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
    public ApplicationRunner initGuestUser() {
        return args -> {
            String guestUsername = "guest";
            String guestPassword = "demo123";

            userRepository.existsByUsername(guestUsername)
                .flatMap(exists -> {
                    if (!exists) {
                        User guest = new User();
                        guest.setUseruuid(UUID.randomUUID().toString());
                        guest.setUsername(guestUsername);
                        guest.setPassword(passwordEncoder.encode(guestPassword));
                        guest.setCreated_at(LocalDateTime.now());
                        System.out.println("✅ Creating default guest user: " + guestUsername);
                        return userRepository.save(guest);
                    } else {
                        System.out.println("ℹ️ Guest user already exists, skipping creation.");
                        return Mono.empty();
                    }
                })
                .subscribe(); // trigger execution
        };
    }

    // this is the default application seeder bean
    @Bean
    CommandLineRunner devIconbSkillsSeedRunner(SkillRatingService hservice) {
        boolean flag = false;
        return args ->{
            System.out.println("hello from Dev skill seeder Bean");
            List<Skill> skills =  hservice.getRepository().findAll().collectList().block();
            System.out.println("the size of all skills exiting is "+skills.size());
            if(skills.isEmpty()) {
                InputStream jsoncont = resource.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                List<ApplicationSeeder.Icon> iconlist = mapper.readValue(jsoncont, new TypeReference<List<ApplicationSeeder.Icon>>() {});
                List<String> iconlist_names = iconlist.stream().map(iconskill -> iconskill.name).collect(Collectors.toList());

                System.out.println("===========> DB Seeding started ============");
                for(ApplicationSeeder.Icon icon: iconlist) {
                    hservice.saveSkill(Skill.builder()
                            .skillname(icon.name())
                            .skillicon(icon.icon())
                            .skilluuid(UUID.randomUUID().toString())
                            .rating(0)
                            .build()).subscribe();
                }
                System.out.println("===========> DB Seeding finished ============");
            }
        };
    }
}
