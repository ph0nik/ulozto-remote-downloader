package websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import websocket.model.DispatchStatus;

@Controller
public class NotificationController {

    private final NotificationDispatcher dispatcher;

    @Autowired
    public NotificationController(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @MessageMapping("/start")
    public void start(StompHeaderAccessor headerAccessor) {
        dispatcher.add(headerAccessor.getSessionId());
    }

    @MessageMapping("/stop")
    public void stop(StompHeaderAccessor headerAccessor) {
        dispatcher.remove(headerAccessor.getSessionId());
    }

    /*
    * Test status to run from console
    * */
    @MessageMapping("/test")
    public void down(StompHeaderAccessor headerAccessor) {
        System.out.println(headerAccessor.getMessage());
        DispatchStatus status = new DispatchStatus();
        status.setFileName("some_file_name.txt");
        status.setFileSource("some_website");
        status.setLength(1000D);
        status.setTotalBytes(50L);
        dispatcher.dispatch(status);
    }
}
