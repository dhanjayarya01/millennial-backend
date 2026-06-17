package com.mellinnial.plance.repository;

import com.mellinnial.plance.entity.ProjectEntity;
import com.mellinnial.plance.entity.TaskEntity;
import com.mellinnial.plance.entity.TaskStatus;
import com.mellinnial.plance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByProject(ProjectEntity project);
    List<TaskEntity> findByEmployeesContaining(UserEntity employee);
    List<TaskEntity> findByProjectIn(List<ProjectEntity> projects);
    long countByProject(ProjectEntity project);
    long countByProjectAndStatus(ProjectEntity project, TaskStatus status);
}
