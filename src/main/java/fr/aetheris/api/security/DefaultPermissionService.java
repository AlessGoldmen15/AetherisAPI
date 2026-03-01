package fr.aetheris.api.security;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultPermissionService implements PermissionService {

    private final RoleService roleService;
    private final Map<String, Set<String>> directPermissions = new ConcurrentHashMap<>();

    public DefaultPermissionService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void grant(String subjectId, String permission) {
        validate(subjectId, permission);
        directPermissions.computeIfAbsent(subjectId, ignored -> ConcurrentHashMap.newKeySet()).add(normalize(permission));
    }

    @Override
    public void revoke(String subjectId, String permission) {
        if (subjectId == null || permission == null) {
            return;
        }
        final Set<String> permissions = directPermissions.get(subjectId);
        if (permissions == null) {
            return;
        }
        permissions.remove(normalize(permission));
        if (permissions.isEmpty()) {
            directPermissions.remove(subjectId);
        }
    }

    @Override
    public boolean hasPermission(String subjectId, String permission) {
        validate(subjectId, permission);
        final String normalizedPermission = normalize(permission);
        final Set<String> direct = directPermissions.getOrDefault(subjectId, Set.of());
        return direct.contains(normalizedPermission) || roleService.resolvePermissions(subjectId).contains(normalizedPermission);
    }

    @Override
    public void clear() {
        directPermissions.clear();
    }

    private static void validate(String subjectId, String permission) {
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("subjectId must not be blank");
        }
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("permission must not be blank");
        }
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
