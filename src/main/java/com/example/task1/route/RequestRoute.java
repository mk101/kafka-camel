package com.example.task1.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.stereotype.Component;

@Component
public class RequestRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        DataFormat jaxb = new JaxbDataFormat("com.example.task1.generated");

        from("file:C:\\Projects\\java\\globus\\task1\\src\\main\\resources\\test-data")
                .setHeader(KafkaConstants.KEY, constant("Camel"))
                        .to("kafka:requests?brokers={{kafka.broker1.host}}");

        from("kafka:requests?brokers={{kafka.broker1.host}}&groupId=camel")
                .log("Message received from Kafka : ${body}")
                .log("    on the topic ${headers[kafka.TOPIC]}")
                .log("    on the partition ${headers[kafka.PARTITION]}")
                .log("    with the offset ${headers[kafka.OFFSET]}");

        jaxb.close();
    }
}
