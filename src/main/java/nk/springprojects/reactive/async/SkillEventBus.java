package nk.springprojects.reactive.async;

import nk.springprojects.reactive.model.Skill;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
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
                .doOnSubscribe(sub -> System.out.println("✅ SSE client connected"))
                .doOnCancel(() -> System.out.println("❌ SSE client disconnected"))
                .onErrorContinue((err, obj) -> System.err.println("⚠️ SSE stream error: " + err.getMessage()))// then live updates
        );
//        return sink.asFlux()
//            .doOnSubscribe(sub -> System.out.println("✅ SSE client connected"))
//            .doOnCancel(() -> System.out.println("❌ SSE client disconnected"))
//            .onErrorContinue((err, obj) -> System.err.println("⚠️ SSE stream error: " + err.getMessage()));
    }
}
