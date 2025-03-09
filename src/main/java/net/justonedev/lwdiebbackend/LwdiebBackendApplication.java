package net.justonedev.lwdiebbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;

@SpringBootApplication
public class LwdiebBackendApplication {

    public static void main(String[] args) {
        var app = new SpringApplication(LwdiebBackendApplication.class);
        app.addListeners(
                event -> {
                    if (event instanceof ApplicationFailedEvent) {
                        System.err.println(
                                "Backend failed: "
                                        + ((ApplicationFailedEvent) event).getException());
                    }
                });
        app.run(args);
    }

}
