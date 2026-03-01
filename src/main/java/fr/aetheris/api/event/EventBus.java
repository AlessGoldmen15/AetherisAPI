package fr.aetheris.api.event;

public interface EventBus {

    <T extends AetherisEvent> Subscription subscribe(Class<T> eventType, EventListener<T> listener);

    <T extends AetherisEvent> void publish(T event);

    void clear();
}
