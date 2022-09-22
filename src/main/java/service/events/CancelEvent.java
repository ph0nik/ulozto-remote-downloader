package service.events;

import org.springframework.context.ApplicationEvent;

public class CancelEvent extends ApplicationEvent {
    public CancelEvent(Object source) {
        super(source);
    }
}
