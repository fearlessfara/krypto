package com.bok.krypto.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
public class Wallet {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true)
    private String address;

    @ManyToOne
    private User user;

    @ManyToOne
    private Krypto krypto;

    @Column
    private BigDecimal availableAmount;

    @Column
    @CreationTimestamp
    private Instant creationTime;

    @Column
    @UpdateTimestamp
    private Instant updateTime;

    @Column
    @Enumerated(value = EnumType.STRING)
    private Status status;

    public Wallet() {
        //hibernate
    }


    public Wallet(User u, Krypto k) {
        this.user = u;
        this.krypto = k;
    }

    @PrePersist
    public void prePersist() {
        this.status = Status.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Krypto getKrypto() {
        return krypto;
    }

    public void setKrypto(Krypto krypto) {
        this.krypto = krypto;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigDecimal availableAmount) {
        this.availableAmount = availableAmount;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("address", address)
                .append("krypto", krypto)
                .append("availableAmount", availableAmount)
                .append("creationTime", creationTime)
                .append("updateTime", updateTime)
                .toString();
    }

    public enum Status {
        PENDING,
        CREATED,
        FAILED
    }
}
