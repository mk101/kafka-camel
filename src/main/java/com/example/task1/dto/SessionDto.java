package com.example.task1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("service_url")
    private String serviceUrl;

    private String start;

    private String end;
}
