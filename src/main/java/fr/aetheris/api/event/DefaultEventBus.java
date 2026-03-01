package fr.aetheris.api.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DefaultEventBus implements EventBus {

    private final Map<Class<? extends AetherisEvent>, CopyOnWriteArrayList<EventListener<? extends AetherisEvent>>> listeners =
            new ConcurrentHashMap<>();

    @Override
    public <T extends AetherisEvent> Subscription subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> unsubscribe(eventType, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AetherisEvent> void publish(T event) {
        if (event == null) {
            return;
        }
        final List<EventListener<? extends AetherisEvent>> listenersForType = new ArrayList<>(
                listeners.getOrDefault(event.getClass(), new CopyOnWriteArrayList<>())
        );
        for (EventListener<? extends AetherisEvent> listener : listenersForType) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }

    @Override
    public void clear() {
        listeners.clear();
    }

    private <T extends AetherisEvent> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        final List<EventListener<? extends AetherisEvent>> listenersForType = listeners.get(eventType);
        if (listenersForType == null) {
            return;
        }
        listenersForType.remove(listener);
        if (listenersForType.isEmpty()) {
            listeners.remove(eventType);
        }
    }
}
