package fr.aetheris.api;

import fr.aetheris.api.command.CommandRegistry;
import fr.aetheris.api.endpoint.EndpointRegistry;
import fr.aetheris.api.event.EventBus;
import fr.aetheris.api.security.PermissionService;
import fr.aetheris.api.security.RoleService;
import fr.aetheris.api.service.ServiceRegistry;
import fr.aetheris.api.storage.StorageManager;

final class DefaultAetherisApi implements AetherisApi {

    private final ServiceRegistry services;
    private final CommandRegistry commands;
    private final EventBus events;
    private final EndpointRegistry endpoints;
    private final PermissionService permissions;
    private final RoleService roles;
    private final StorageManager storage;

    DefaultAetherisApi(
            ServiceRegistry services,
            CommandRegistry commands,
            EventBus events,
            EndpointRegistry endpoints,
            PermissionService permissions,
            RoleService roles,
            StorageManager storage
    ) {
        this.services = services;
        this.commands = commands;
        this.events = events;
        this.endpoints = endpoints;
        this.permissions = permissions;
        this.roles = roles;
        this.storage = storage;
    }

    @Override
    public ServiceRegistry services() {
        return services;
    }

    @Override
    public CommandRegistry commands() {
        return commands;
    }

    @Override
    public EventBus events() {
        return events;
    }

    @Override
    public EndpointRegistry endpoints() {
        return endpoints;
    }

    @Override
    public PermissionService permissions() {
        return permissions;
    }

    @Override
    public RoleService roles() {
        return roles;
    }

    @Override
    public StorageManager storage() {
        return storage;
    }

    @Override
    public void close() {
        services.clear();
        commands.clear();
        events.clear();
        endpoints.clear();
        permissions.clear();
        roles.clear();
        storage.close();
    }
}
