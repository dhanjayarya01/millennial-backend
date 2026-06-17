package com.mellinnial.plance.repository;

import com.mellinnial.plance.entity.RoleEntity;
import com.mellinnial.plance.entity.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(RoleType name);
    boolean existsByName(RoleType name);
}
