package websocket;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import websocket.model.DispatchStatus;

import java.util.HashSet;
import java.util.Set;

@Component
public class NotificationDispatcher {

    private static final String NOTIFICATION_DEST = "/notification/item";

    private final SimpMessagingTemplate template;

    // add local status, update it with every dispatch, and send as soon as someone connects to controller

    private Set<String> listeners = new HashSet<>();

    public NotificationDispatcher(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void add(String sessionId) {
        listeners.add(sessionId);
    }

    public void remove(String sessionId) {
        listeners.remove(sessionId);
    }

    public void dispatch() {
        for (String listener : listeners) {
            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(listener);
            headerAccessor.setLeaveMutable(true);

            // send test message to all listeners
            template.convertAndSendToUser(listener, NOTIFICATION_DEST, "test_message_sent", headerAccessor.getMessageHeaders());
        }
    }

    public void dispatch(DispatchStatus dispatchStatus) {
        for (String listener : listeners) {
            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(listener);
            headerAccessor.setLeaveMutable(true);
            template.convertAndSendToUser(listener, NOTIFICATION_DEST, dispatchStatus, headerAccessor.getMessageHeaders());
        }
    }

    public void sessionDisconnectHandler(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        remove(sessionId);
    }
}
