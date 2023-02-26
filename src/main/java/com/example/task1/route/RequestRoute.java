package com.example.task1.route;

import com.example.task1.generated.Session;
import jakarta.xml.bind.UnmarshalException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RequestRoute extends RouteBuilder {
    @Value("${kafka.broker1.camel-request-path}")
    private String path;

    @Override
    public void configure() throws Exception {

        try (DataFormat jaxb = new JaxbDataFormat("com.example.task1.generated")) {
            onException(UnmarshalException.class)
                    .handled(true)
                    .setHeader("MessageType", simple("ERROR"))
                    .setBody(simple("Something went wrong while unmarshalling"))
                    .to("direct:status");

            from(path)
                    .routeId("Kafka requests")
                    .to("micrometer:counter:app.message.received")
                    .to("micrometer:timer:app.message.timer?action=start")
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
