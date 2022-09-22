package service.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CustomEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void cancelEvent() {
        System.out.println("[ custom_publisher ] event");
        CancelEvent cancelEvent = new CancelEvent(this);
        applicationEventPublisher.publishEvent(cancelEvent);
    }
}
