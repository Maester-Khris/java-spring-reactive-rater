package nk.springprojects.reactive.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class DBSeedCompletedEvent extends ApplicationEvent {
    public DBSeedCompletedEvent(Object source) {
        super(source);
        log.info("[skillrater] INFO | DB seeding completed event created");
    }
}
