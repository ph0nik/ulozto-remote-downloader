package app;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@ComponentScan({"controller", "service", "websocket", "util"})
@EnableAsync
public class AppConfiguration {
}
