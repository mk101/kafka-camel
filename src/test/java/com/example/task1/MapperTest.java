package com.example.task1;

import com.example.task1.dto.SessionDto;
import com.example.task1.generated.Service;
import com.example.task1.generated.User;
import com.example.task1.mapper.SessionMapper;
import com.example.task1.mapper.SessionMapperImpl;
import com.example.task1.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(classes = MapperTest.TestConfig.class)
public class MapperTest {
    private com.example.task1.generated.Session generated;
    private Session model;
    private SessionDto dto;

    @Autowired
    private SessionMapper mapper;

    @Configuration
    public static class TestConfig {
        @Bean
        public SessionMapper mapper() {
            return new SessionMapperImpl();
        }
    }

    @BeforeEach
    public void setup() {
        Calendar time = Calendar.getInstance();
        Timestamp timestamp = new Timestamp(time.getTimeInMillis());
        int userId = 1;
        String serviceUrl = "localhost:8080";

        User user = new User();
        user.setId(userId);
        user.setEmail("test@test.ru");
        user.setName("Test");

        Service service = new Service();
        service.setName("Test");
        service.setUrl(serviceUrl);

        generated = new com.example.task1.generated.Session();
        generated.setService(service);
        generated.setUser(user);
        generated.setStart(time);
        generated.setEnd(time);

        model = new Session();
        model.setStart(timestamp);
        model.setEnd(timestamp);
        model.setUserId(userId);
        model.setServiceUrl(serviceUrl);

        dto = new SessionDto(userId, serviceUrl,timestamp.toString(), timestamp.toString());
    }

    @Test
    public void modelToDto() {
        SessionDto testDto = mapper.mapWithoutId(model);
        assertEquals(testDto, dto);
    }

    @Test
    public void generatedToModel() {
        Session testModel = mapper.mapGenerated(generated);
        assertEquals(testModel, model);
    }
}
