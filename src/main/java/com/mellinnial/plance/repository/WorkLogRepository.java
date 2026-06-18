package com.mellinnial.plance.repository;

import com.mellinnial.plance.entity.TaskEntity;
import com.mellinnial.plance.entity.WorkLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkLogRepository extends JpaRepository<WorkLogEntity, Long> {
    List<WorkLogEntity> findByTaskId(Long taskId);
    List<WorkLogEntity> findByTaskIdIn(List<Long> taskIds);
    List<WorkLogEntity> findByAuthorId(Long authorId);
}
