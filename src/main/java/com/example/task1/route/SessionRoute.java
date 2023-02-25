package com.example.task1.route;

import com.example.task1.dto.SessionDto;
import com.example.task1.mapper.SessionMapper;
import com.example.task1.model.Session;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionRoute extends RouteBuilder {

    private final SessionMapper mapper;

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.ERROR, "Something went wrong")
                .setHeader("MessageType", simple("ERROR"))
                .to("direct:status");

        from("direct:session")
                .routeId("Session processing")
                .to("direct:save_to_db")
                .to("direct:save_to_kafka")
                .setHeader("MessageType", simple("SUCCESS"))
                .to("direct:status");

        from("direct:save_to_db")
                .routeId("Save to database")
                .process(exchange -> {
                    com.example.task1.generated.Session in = exchange.getIn().getBody(com.example.task1.generated.Session.class);
                    Session session = mapper.mapGenerated(in);

                    exchange.getMessage().setBody(session, Session.class);
                })
                .log("Saving ${body} to database...")
                .to("jpa:com.example.task1.model.Session");

        from("direct:save_to_kafka")
                .routeId("Save to kafka")
                .process(exchange -> {
                    Session session = exchange.getIn().getBody(Session.class);
                    SessionDto dto = mapper.mapWithoutId(session);

                    exchange.getMessage().setBody(dto, SessionDto.class);
                })
                .log("Saving ${body} to kafka")
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(KafkaConstants.KEY, simple("camel"))
                .to("kafka:results?brokers={{kafka.broker2.host}}");
    }
}
