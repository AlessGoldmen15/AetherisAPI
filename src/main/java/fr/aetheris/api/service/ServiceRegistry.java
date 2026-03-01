package fr.aetheris.api.service;

import java.util.Optional;
import java.util.Set;

public interface ServiceRegistry {

    <T extends AetherisService> void register(Class<T> serviceClass, T service);

    <T extends AetherisService> Optional<T> find(Class<T> serviceClass);

    <T extends AetherisService> T require(Class<T> serviceClass);

    boolean unregister(Class<? extends AetherisService> serviceClass);

    Set<Class<? extends AetherisService>> registeredTypes();

    void clear();
}
