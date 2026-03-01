package fr.aetheris.api.event;

@FunctionalInterface
public interface EventListener<T extends AetherisEvent> {

    void onEvent(T event);
}
