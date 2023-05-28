package service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import service.events.CancelEvent;

@Service
public class CancelService {
    private boolean cancel;

    @EventListener
    public void cancelEvent(CancelEvent cancelEvent) {
        cancel();
    }

    public void cancel() {
        cancel = true;
    }
    public void reset() {
        cancel = false;
    }

    public boolean isCancel() {
        return cancel;
    }
}
