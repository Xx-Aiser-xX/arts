package org.example.arts.entities;

import jakarta.persistence.*;
import org.example.arts.exceptions.IncorrectDataException;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Sub extends BaseEntityId {
    private User subscriber;
    private User target;
    private LocalDateTime subscriptionDate;
    private boolean deleted;

    protected Sub() {}

    public Sub(User subscriber, User target) {
        setSubscriber(subscriber);
        setTarget(target);
        setSubscriptionDate(LocalDateTime.now());
        setDeleted(false);
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscriber_id", referencedColumnName = "id")
    public User getSubscriber() {
        return subscriber;
    }
    public void setSubscriber(User subscriber) {
        if (subscriber == null)
            throw new IncorrectDataException("subscriber is null");
        this.subscriber = subscriber;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", referencedColumnName = "id")
    public User getTarget() {
        return target;
    }
    public void setTarget(User target) {
        if (target == null)
            throw new IncorrectDataException("target is null");
        this.target = target;
    }

    @Column(name = "subscription_date", nullable = false)
    public LocalDateTime getSubscriptionDate() {
        return subscriptionDate;
    }
    public void setSubscriptionDate(LocalDateTime subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    @Column(name = "is_deleted", nullable = false)
    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
