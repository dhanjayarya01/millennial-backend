package com.mellinnial.plance.repository;

import com.mellinnial.plance.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByProjectId(Long projectId);
    List<AuditLogEntity> findByEntity(String entity);
    List<AuditLogEntity> findAllByOrderByTimestampDesc();
}
