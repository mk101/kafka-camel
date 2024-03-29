package com.example.task1.route;

import com.example.task1.generated.Status;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import java.util.Calendar;

@Component
public class StatusRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        try (JaxbDataFormat jaxb = new JaxbDataFormat("com.example.task1.generated")) {
            from("direct:status")
                    .choice()
                    .when(header("MessageType").contains("ERROR"))
                        .to("micrometer:counter:app.message.failed")
                        .log(LoggingLevel.ERROR, "${body}")
                    .otherwise()
                        .to("micrometer:counter:app.message.success")
                        .log("Message saved in database and kafka")
                        .setBody(simple("Message saved in database and kafka"))
                    .end()
                    .process(exchange -> {
                        String message = exchange.getIn().getBody(String.class);

                        Status status = new Status();
                        status.setStatusType(exchange.getIn().getHeader("MessageType", String.class));
                        status.setMessage(message);
                        status.setTime(Calendar.getInstance());

                        exchange.getMessage().setBody(status, Status.class);
                    })
                    .marshal(jaxb)
                    .setHeader(KafkaConstants.KEY, simple("camel"))
                    .to("kafka:status_topic?brokers={{kafka.broker1.host}}")
                    .to("micrometer:timer:app.message.timer?action=stop");
        }
    }
}
