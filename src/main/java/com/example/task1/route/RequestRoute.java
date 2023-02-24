package com.example.task1.route;

import com.example.task1.generated.Session;
import jakarta.xml.bind.UnmarshalException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.stereotype.Component;

@Component
public class RequestRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        try (DataFormat jaxb = new JaxbDataFormat("com.example.task1.generated")) {
            onException(UnmarshalException.class)
                    .handled(true)
                    .setHeader("MessageType", simple("ERROR"))
                    .setBody(simple("Something went wrong while unmarshalling"))
                    .to("direct:status");

            from("kafka:requests?brokers={{kafka.broker1.host}}&groupId=camel")
                    .routeId("Kafka requests")
                    .log("Message received from Kafka : ${body}")
                    .log("    on the topic ${headers[kafka.TOPIC]}")
                    .log("    on the partition ${headers[kafka.PARTITION]}")
                    .log("    with the offset ${headers[kafka.OFFSET]}")
                    .unmarshal(jaxb)
                    .choice()
                        .when(body().isInstanceOf(Session.class))
                            .to("direct:session")
                        .otherwise()
                            .setBody(simple("Message is not a Session"))
                            .setHeader("MessageType", simple("ERROR"))
                            .to("direct:status");
        }
    }
}
