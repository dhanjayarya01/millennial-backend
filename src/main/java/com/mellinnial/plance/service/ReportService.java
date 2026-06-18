package com.mellinnial.plance.service;

import com.mellinnial.plance.dto.response.ReportResponseDto;
import com.mellinnial.plance.entity.ProjectEntity;
import com.mellinnial.plance.entity.TaskEntity;
import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.repository.ProjectRepository;
import com.mellinnial.plance.repository.TaskRepository;
import com.mellinnial.plance.repository.UserRepository;
import com.mellinnial.plance.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkLogRepository workLogRepository;

    @Transactional(readOnly = true)
    public ReportResponseDto getProjectReport(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        List<TaskEntity> tasks = taskRepository.findByProject(project);
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == com.mellinnial.plance.entity.TaskStatus.COMPLETED)
                .count();
        long pendingTasks = totalTasks - completedTasks;
        double completion = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100.0 : 0.0;

        double totalHoursLogged = workLogRepository.findAll().stream()
                .filter(w -> w.getTask().getProject().getId().equals(projectId))
                .mapToDouble(w -> w.getHours())
                .sum();

        return ReportResponseDto.builder()
                .completionPercentage(completion)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .totalHoursLogged(totalHoursLogged)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportResponseDto getEmployeeReport(Long employeeId) {
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        List<TaskEntity> tasks = taskRepository.findByEmployeesContaining(employee);
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == com.mellinnial.plance.entity.TaskStatus.COMPLETED)
                .count();
        long pendingTasks = totalTasks - completedTasks;
        double completion = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100.0 : 0.0;

        double totalHoursLogged = workLogRepository.findByAuthorId(employeeId).stream()
                .mapToDouble(w -> w.getHours())
                .sum();

        return ReportResponseDto.builder()
                .completionPercentage(completion)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .totalHoursLogged(totalHoursLogged)
                .build();
    }
}
