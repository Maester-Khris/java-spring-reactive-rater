package nk.springprojects.reactive.configurations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.async.DBSeedCompletedEvent;
import nk.springprojects.reactive.service.SkillRatingService;
import nk.springprojects.reactive.model.Skill;
import nk.springprojects.reactive.users.User;
import nk.springprojects.reactive.users.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
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
@Slf4j
public class ApplicationSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("classpath:data/alliconsv1.json")
    private Resource resource;
    record Icon(String name, String icon) {}

    @Value("${app.guest.username}")
    private String guestUsername;

    @Value("${app.guest.password}")
    private String guestPassword;

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
            userRepository.existsByUsername(guestUsername)
                .flatMap(exists -> {
                    if (!exists) {
                        User guest = new User();
                        guest.setUseruuid(UUID.randomUUID().toString());
                        guest.setUsername(guestUsername);
                        guest.setPassword(passwordEncoder.encode(guestPassword));
                        guest.setCreated_at(LocalDateTime.now());
                        log.info("✅ Creating default guest user: " + guestUsername);
                        return userRepository.save(guest);
                    } else {
                        log.info("ℹ️ Guest user already exists, skipping creation.");
                        return Mono.empty();
                    }
                })
                .subscribe(); // trigger execution
        };
    }

    // this is the default application seeder bean
    @Bean
    CommandLineRunner devIconbSkillsSeedRunner(SkillRatingService hservice, ApplicationEventPublisher publisher) {
        boolean flag = false;
        return args ->{
            List<Skill> skills =  hservice.getRepository().findAll().collectList().block();
            if(skills.isEmpty()) {
                InputStream jsoncont = resource.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                List<ApplicationSeeder.Icon> iconlist = mapper.readValue(jsoncont, new TypeReference<List<ApplicationSeeder.Icon>>() {});
                List<String> iconlist_names = iconlist.stream().map(iconskill -> iconskill.name).collect(Collectors.toList());

                log.info("===========> DB Seeding started");
                for(ApplicationSeeder.Icon icon: iconlist) {
                    hservice.saveSkill(Skill.builder()
                            .skillname(icon.name())
                            .skillicon(icon.icon())
                            .skilluuid(UUID.randomUUID().toString())
                            .rating(0)
                            .build()).subscribe();
                }
                log.info("===========> DB Seeding finished");
                publisher.publishEvent(new DBSeedCompletedEvent(this));
            }
        };
    }
}
