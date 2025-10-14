package nk.springprojects.reactive.async;

import org.springframework.beans.factory.annotation.Autowired;

import nk.springprojects.reactive.model.Skill;

public class WebThreadProducer implements Runnable {
    public WebThreadProducer() {
    }

    @Override
    public void run() {
        System.out.println("");
    }
}
