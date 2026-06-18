package com.mellinnial.plance.repository;

import com.mellinnial.plance.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    Optional<NotificationEntity> findByTaskIdAndUserIdAndType(Long taskId, Long userId, String type);
    boolean existsByTaskIdAndUserIdAndType(Long taskId, Long userId, String type);
    java.util.List<NotificationEntity> findByTaskIdIn(java.util.List<Long> taskIds);
}
