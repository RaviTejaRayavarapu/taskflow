package com.taskflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class TaskFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
    }
}

@Component
class StartupLogger {
    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);
    private final Environment env;

    public StartupLogger(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = env.getProperty("server.port", "3000");
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("  TaskFlow API is running on port: {}", port);
        log.info("  Application available at: http://localhost:{}", port);
        log.info("  Test credentials: test@example.com / password123");
        log.info("═══════════════════════════════════════════════════════════════");
    }
}
