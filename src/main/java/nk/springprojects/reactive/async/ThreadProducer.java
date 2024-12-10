package nk.springprojects.reactive.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ThreadProducer {

	@Autowired
	ThreadComponent threadComponent;
	
	@Async
	@Scheduled(fixedRate = 3000)
	public void producer1() {
		threadComponent.produceData();
	}
	
	@Async
	@Scheduled(fixedRate = 30000)
	public void producer2() {
		threadComponent.produceData();
	}
	
	@Async
	@Scheduled(fixedRate = 10000)
	public void producer3() {
		threadComponent.produceData();
	}
}
