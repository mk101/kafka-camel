package com.example.task1.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SessionRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:session")
                .log("Get session");
    }
}
