package fr.aetheris.api.security;

import java.util.Optional;
import java.util.Set;

public interface RoleService {

    void createRole(Role role);

    Optional<Role> findRole(String roleName);

    void assignRole(String subjectId, String roleName);

    void unassignRole(String subjectId, String roleName);

    Set<String> rolesOf(String subjectId);

    Set<String> resolvePermissions(String subjectId);

    void clear();
}
