package com.example.task1.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String serviceUrl;

    @Column(nullable = false)
    private Timestamp start;

    @Column(nullable = false)
    private Timestamp end;
}
