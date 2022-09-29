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

    private DispatchStatus incomingStatus;

    // add local status, update it with every dispatch, and send as soon as someone connects to controller

    private final Set<String> listeners = new HashSet<>();

    public NotificationDispatcher(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void setIncomingStatus(DispatchStatus dispatchStatus) {
        incomingStatus = dispatchStatus;
    }

    public void clearIncomingStatus() {
        incomingStatus = null;
    }

    /*
    * Add client id to listeners list and dispatch status if exists immediately to this client.
    * */
    public void add(String sessionId) {
        listeners.add(sessionId);
        if (incomingStatus != null) dispatch(incomingStatus, sessionId);
    }

    /*
    * Remove session listener
    * */
    public void remove(String sessionId) {
        listeners.remove(sessionId);
    }

    /*
    * Dispatch test message
    * */
    public void dispatchTest() {
        for (String listener : listeners) {
            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(listener);
            headerAccessor.setLeaveMutable(true);
            // send test message to all listeners
            template.convertAndSendToUser(listener, NOTIFICATION_DEST, "test_message_sent", headerAccessor.getMessageHeaders());
        }
    }

    /*
    * Dispatch status to client with given session id
    * */
    public void dispatch(DispatchStatus dispatchStatus, String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        template.convertAndSendToUser(sessionId, NOTIFICATION_DEST, dispatchStatus, headerAccessor.getMessageHeaders());
    }

    /*
    * Dispatch status to all connected clients
    * */
    public void dispatch(DispatchStatus dispatchStatus) {
        incomingStatus = dispatchStatus;
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
