package com.example.task1.route;

import com.example.task1.generated.User;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.stereotype.Component;

@Component
public class RequestRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        DataFormat jaxb = new JaxbDataFormat("com.example.task1.generated");

        from("file://C:/Projects/java/globus/task1/src/main/resources/test-data")
                .unmarshal(jaxb)
                .log("${body}");
    }
}
