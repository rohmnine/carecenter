package com.example.carecenter.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String className;
    private String parentContact;
    private String allergyInfo;

    @ManyToOne
    private Room room;

    @ManyToOne
    private User parent;
}