package com.epam.resourceservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private byte[] data;

    protected Resource() {
    }

    public Resource(byte[] data) {
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }
}
