package com.ogm.hrms.repository;

import com.ogm.hrms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** Paginated, non-deleted users with roles eagerly loaded for administrative listing. */
    @EntityGraph(attributePaths = "roles")
    Page<User> findByDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = "roles")
    @Query("select u from User u where u.id = :id and u.deleted = false")
    Optional<User> findActiveByIdWithRoles(@Param("id") Long id);

    /**
     * Loads a non-deleted user together with roles and their permissions, for building the
     * security principal in a single query. Soft-deleted accounts are excluded so they cannot
     * authenticate (business rule: deleted users cannot log in).
     */
    @Query("""
            select distinct u from User u
            left join fetch u.roles r
            left join fetch r.permissions
            where lower(u.email) = lower(:email) and u.deleted = false
            """)
    Optional<User> findActiveByEmailWithRolesAndPermissions(@Param("email") String email);
}
