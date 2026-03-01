package fr.aetheris.api.security;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultRoleService implements RoleService {

    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> assignments = new ConcurrentHashMap<>();

    @Override
    public void createRole(Role role) {
        validateRole(role);
        roles.put(normalize(role.name()), normalizeRole(role));
    }

    @Override
    public Optional<Role> findRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(roles.get(normalize(roleName)));
    }

    @Override
    public void assignRole(String subjectId, String roleName) {
        requireText(subjectId, "subjectId");
        final String normalizedRoleName = normalize(roleName);
        if (!roles.containsKey(normalizedRoleName)) {
            throw new IllegalArgumentException("Unknown role: " + roleName);
        }
        assignments.computeIfAbsent(subjectId, ignored -> ConcurrentHashMap.newKeySet()).add(normalizedRoleName);
    }

    @Override
    public void unassignRole(String subjectId, String roleName) {
        if (subjectId == null || roleName == null) {
            return;
        }
        final Set<String> roleNames = assignments.get(subjectId);
        if (roleNames == null) {
            return;
        }
        roleNames.remove(normalize(roleName));
        if (roleNames.isEmpty()) {
            assignments.remove(subjectId);
        }
    }

    @Override
    public Set<String> rolesOf(String subjectId) {
        return Set.copyOf(assignments.getOrDefault(subjectId, Set.of()));
    }

    @Override
    public Set<String> resolvePermissions(String subjectId) {
        final Set<String> directRoles = assignments.getOrDefault(subjectId, Set.of());
        final Set<String> permissions = new HashSet<>();
        final Set<String> visited = new HashSet<>();
        final ArrayDeque<String> queue = new ArrayDeque<>(directRoles);

        while (!queue.isEmpty()) {
            final String roleName = queue.poll();
            if (!visited.add(roleName)) {
                continue;
            }

            final Role role = roles.get(roleName);
            if (role == null) {
                continue;
            }
            permissions.addAll(role.permissions());
            queue.addAll(role.parents());
        }

        return permissions;
    }

    @Override
    public void clear() {
        roles.clear();
        assignments.clear();
    }

    private static void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        requireText(role.name(), "role.name");
        Objects.requireNonNull(role.permissions(), "role.permissions must not be null");
        Objects.requireNonNull(role.parents(), "role.parents must not be null");
    }

    private static Role normalizeRole(Role role) {
        final Set<String> normalizedPermissions = role.permissions().stream().map(DefaultRoleService::normalize).collect(
                HashSet::new,
                Set::add,
                Set::addAll
        );
        final Set<String> normalizedParents = role.parents().stream().map(DefaultRoleService::normalize).collect(
                HashSet::new,
                Set::add,
                Set::addAll
        );
        return new Role(normalize(role.name()), normalizedPermissions, normalizedParents);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }
}
