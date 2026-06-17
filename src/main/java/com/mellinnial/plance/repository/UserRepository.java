package com.mellinnial.plance.repository;

import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.entity.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.role.name = :roleName AND u.active = true")
    List<UserEntity> findActiveUsersByRole(@Param("roleName") RoleType roleName);

    @Query("SELECT u FROM UserEntity u WHERE u.role.name = :roleName")
    Page<UserEntity> findByRole(@Param("roleName") RoleType roleName, Pageable pageable);

    long countByActiveTrue();

    long countByRole_Name(RoleType roleName);
}
