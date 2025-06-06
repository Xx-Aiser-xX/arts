package org.example.arts.entities;

import jakarta.persistence.*;

import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntityId {
    private UUID id;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", insertable = false, updatable = false  )
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
}

