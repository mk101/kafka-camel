package com.example.task1;

import com.example.task1.generated.Service;
import com.example.task1.generated.User;
import com.example.task1.mapper.SessionMapper;
import com.example.task1.model.Session;
import com.example.task1.repository.SessionRepository;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@EnableAutoConfiguration
@SpringBootTest(properties = {"kafka.broker1.host=localhost:59092", "kafka.broker2.host=localhost:59092", "kafka.broker1.camel-request-path=direct:kafka_requests"})
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:59092", "port=59092"}, topics = {"requests", "results", "status_topic"})
@MockEndpoints
public class CamelTest {
    @Autowired
    private EmbeddedKafkaBroker broker;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private SessionRepository repository;

    @Autowired
    private SessionMapper mapper;

    @EndpointInject("mock:direct:status")
    public MockEndpoint statusEndpoint;

    @EndpointInject("mock:direct:session")
    public MockEndpoint sessionEndpoint;

    @Test
    public void wrongFormatIn() throws InterruptedException {
        statusEndpoint.setExpectedMessageCount(1);
        sessionEndpoint.setExpectedMessageCount(0);

        producerTemplate.sendBody("direct:kafka_requests", "Plaint text");
        sessionEndpoint.assertIsSatisfied();
        statusEndpoint.assertIsSatisfied();

        assertEquals(repository.count(), 0);
    }

    @Test
    public void correctFormatIn() throws InterruptedException {
        statusEndpoint.setExpectedMessageCount(1);
        sessionEndpoint.setExpectedCount(1);

        String body = """
                <?xml version="1.0" encoding="UTF-8" ?>
                                
                <Session xmlns="/jaxb/gen">
                    <user>
                        <id>1</id>
                        <name>Test Testov</name>
                        <email>test@test.ru</email>
                    </user>
                    <service>
                        <name>Test service</name>
                        <url>https://127.0.0.1:80/api/v1/test</url>
                    </service>
                    <start>2023-02-24T11:50:42.344+03:00</start>
                    <end>2023-02-24T12:27:11.557+03:00</end>
                </Session>
                """;

        producerTemplate.sendBody("direct:kafka_requests", body);

        statusEndpoint.assertIsSatisfied();
        sessionEndpoint.assertIsSatisfied();

        assertEquals(repository.count(), 1);
    }

    @Test
    public void SaveToDatabase() {
        Calendar time = Calendar.getInstance();
        User user = new User();
        user.setId(1);
        user.setEmail("test@test.ru");
        user.setName("Test");

        Service service = new Service();
        service.setName("Test");
        service.setUrl("localhost:8080");

        com.example.task1.generated.Session generated = new com.example.task1.generated.Session();
        generated.setService(service);
        generated.setUser(user);
        generated.setStart(time);
        generated.setEnd(time);
        producerTemplate.sendBody("direct:save_to_db", generated);

        Session session = mapper.mapGenerated(generated);
        session.setId(1);

        assertEquals(repository.count(), 1);
        assertEquals(session, repository.findById(1).orElse(null));
    }

    @Test
    public void saveToKafka() throws InterruptedException {
        Session model = new Session();
        model.setStart(new Timestamp(Instant.now().getNano()));
        model.setEnd(new Timestamp(Instant.now().getNano()));
        model.setUserId(1);
        model.setServiceUrl("localhost:8080");
        model.setId(1);

        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", broker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties("results");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, broker.getPartitionsPerTopic());

        producerTemplate.sendBody("direct:save_to_kafka", model);

        ConsumerRecord<String, String> singleRecord = records.poll(100, TimeUnit.MILLISECONDS);
        assertNotNull(singleRecord);
        assertTrue(singleRecord.value().contains("\"service_url\":\"localhost:8080\""));

        container.stop();
    }
}
