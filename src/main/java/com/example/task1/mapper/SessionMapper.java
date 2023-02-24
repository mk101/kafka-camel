package com.example.task1.mapper;

import com.example.task1.dto.SessionDto;
import com.example.task1.model.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.sql.Timestamp;
import java.util.Calendar;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "serviceUrl", source = "serviceUrl")
    @Mapping(target = "start", source = "start")
    @Mapping(target = "end", source = "end")
    SessionDto mapWithoutId(Session session);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "service.url", target = "serviceUrl")
    @Mapping(source = "start", target = "start")
    @Mapping(source = "end", target = "end")
    Session mapGenerated(com.example.task1.generated.Session generated);

    default String mapTimestamp(Timestamp timestamp) {
        return timestamp.toString();
    }

    default Timestamp mapCalendar(Calendar calendar) {
        return new Timestamp(calendar.getTimeInMillis());
    }
}
