package fr.aetheris.api;

import fr.aetheris.api.command.DefaultCommandRegistry;
import fr.aetheris.api.endpoint.DefaultEndpointRegistry;
import fr.aetheris.api.event.DefaultEventBus;
import fr.aetheris.api.security.DefaultPermissionService;
import fr.aetheris.api.security.DefaultRoleService;
import fr.aetheris.api.service.DefaultServiceRegistry;
import fr.aetheris.api.storage.DefaultStorageManager;
import fr.aetheris.api.storage.StorageBackendType;
import fr.aetheris.api.storage.StorageManager;
import fr.aetheris.api.storage.file.FileKeyValueStorageProvider;
import fr.aetheris.api.storage.memory.InMemoryStorageProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Aetheris {

    private Aetheris() {
    }

    public static AetherisApi create(Path dataDirectory) {
        createDataDirectory(dataDirectory);

        final StorageManager storageManager = new DefaultStorageManager();
        storageManager.registerProvider(StorageBackendType.MEMORY, new InMemoryStorageProvider());
        storageManager.registerProvider(StorageBackendType.FILE, new FileKeyValueStorageProvider(dataDirectory.resolve("storage")));
        storageManager.setDefaultBackend(StorageBackendType.FILE);

        final DefaultRoleService roleService = new DefaultRoleService();
        final DefaultPermissionService permissionService = new DefaultPermissionService(roleService);

        return new DefaultAetherisApi(
                new DefaultServiceRegistry(),
                new DefaultCommandRegistry(),
                new DefaultEventBus(),
                new DefaultEndpointRegistry(),
                permissionService,
                roleService,
                storageManager
        );
    }

    private static void createDataDirectory(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("dataDirectory must not be null");
        }
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create data directory: " + dataDirectory, exception);
        }
    }
}
