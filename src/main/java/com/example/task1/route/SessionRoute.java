package com.example.task1.route;

import com.example.task1.mapper.SessionMapper;
import com.example.task1.model.Session;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionRoute extends RouteBuilder {

    private final SessionMapper mapper;

    @Override
    public void configure() throws Exception {
        from("direct:session")
                .process(exchange -> {
                    com.example.task1.generated.Session in = exchange.getIn().getBody(com.example.task1.generated.Session.class);
                    Session session = mapper.mapGenerated(in);

                    exchange.getMessage().setBody(session, Session.class);
                })
                .log("${body}")
                .to("jpa:com.example.task1.model.Session")
                .setHeader("MessageType", simple("SUCCESS"))
                .to("direct:status");
    }
}
