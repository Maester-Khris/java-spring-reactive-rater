package nk.springprojects.reactive.async;

import lombok.extern.slf4j.Slf4j;
import nk.springprojects.reactive.model.Skill;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class SkillEventBus {
    private final Sinks.Many<Skill> sink;
    private final List<Skill> lastKnownSkills = new CopyOnWriteArrayList<>();

    public SkillEventBus() {
        // Multicast to all subscribers; onBackpressureBuffer keeps data stable under load
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public void publish(Skill skill) {
        //System.out.println("====== new published vote on skill uui"+skill.getSkilluuid()+" ===============");
        lastKnownSkills.add(skill);
        sink.tryEmitNext(skill);
    }

    public Flux<Skill> stream() {
        // Keep stream hot; do not complete automatically
        return Flux.concat(
            Flux.fromIterable(lastKnownSkills), // send current state immediately
            sink.asFlux()
                .doOnSubscribe(sub -> log.info("✅ SSE client connected"))
                .doOnCancel(() -> log.info("❌ SSE client disconnected"))
                .onErrorContinue((err, obj) -> log.warn("⚠️ SSE stream error: {}", err.getMessage()))// then live updates
        );
    }
}
