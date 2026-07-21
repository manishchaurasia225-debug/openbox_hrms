package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Role;
import com.ogm.hrms.enums.RoleName;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @EntityGraph(attributePaths = "permissions")
    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}
