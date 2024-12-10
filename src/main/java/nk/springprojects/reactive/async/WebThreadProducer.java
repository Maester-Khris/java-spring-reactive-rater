package nk.springprojects.reactive.async;

import org.springframework.beans.factory.annotation.Autowired;

import nk.springprojects.reactive.home.Skill;

public class WebThreadProducer implements Runnable {
	
	@Autowired
	ThreadComponent threadcomponent;
	private Skill skill;

	public WebThreadProducer(Skill s) {
        this.skill = s;
    }
	
	@Override
    public void run() {
        System.out.println("========= new execution of web producer==========");
        threadcomponent.webProducer(skill);
    }
}
