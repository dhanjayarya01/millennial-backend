package com.mellinnial.plance.service;

import com.mellinnial.plance.entity.NotificationEntity;
import com.mellinnial.plance.entity.TaskEntity;
import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.repository.NotificationRepository;
import com.mellinnial.plance.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskDeadlineScheduler {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkDeadlines() {
        log.info("Running task deadline checks...");
        List<TaskEntity> activeTasks = taskRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (TaskEntity task : activeTasks) {
            if (task.getStatus() == com.mellinnial.plance.entity.TaskStatus.COMPLETED) {
                continue;
            }

            LocalDate deadlineDate = task.getDeadline();
            if (deadlineDate == null) {
                continue;
            }

            LocalDateTime deadlineTime = deadlineDate.atTime(17, 0, 0);
            Duration duration = Duration.between(now, deadlineTime);
            long hoursLeft = duration.toHours();

            if (task.getEmployees() == null || task.getEmployees().isEmpty()) {
                continue;
            }

            UserEntity employee = task.getEmployees().get(0);
            UserEntity manager = task.getProject().getManager();

            if (duration.isNegative() || duration.isZero()) {
                triggerAlert(task, employee, "overdue", "Task Overdue Alert",
                        "Your task '" + task.getName() + "' is overdue! Deadline was " + deadlineDate, "red");
                if (manager != null) {
                    triggerAlert(task, manager, "overdue_manager", "Task Overdue (Manager Alert)",
                            "The task '" + task.getName() + "' assigned to " + employee.getFullName() + " is overdue.", "red");
                }
            } else {
                if (hoursLeft <= 1) {
                    triggerAlert(task, employee, "reminder_1h", "Task Due in 1 Hour",
                            "Your task '" + task.getName() + "' is due in 1 hour!", "red");
                } else if (hoursLeft <= 12) {
                    triggerAlert(task, employee, "reminder_12h", "Task Due in 12 Hours",
                            "Your task '" + task.getName() + "' is due in 12 hours.", "yellow");
                } else if (hoursLeft <= 24) {
                    triggerAlert(task, employee, "reminder_24h", "Task Due in 24 Hours",
                            "Your task '" + task.getName() + "' is due in 24 hours.", "yellow");
                } else if (hoursLeft <= 48) {
                    triggerAlert(task, employee, "reminder_48h", "Task Due in 48 Hours",
                            "Your task '" + task.getName() + "' is due in 48 hours.", "green");
                }
            }
        }
    }

    private void triggerAlert(TaskEntity task, UserEntity user, String type, String title, String description, String urgency) {
        if (notificationRepository.existsByTaskIdAndUserIdAndType(task.getId(), user.getId(), type)) {
            return;
        }

        NotificationEntity notification = NotificationEntity.builder()
                .task(task)
                .user(user)
                .type(type)
                .build();
        notificationRepository.save(notification);

        notificationService.sendSseNotification(title, description, urgency);

        String emailHtml = "<p>Hello " + user.getFullName() + ",</p><p>" + description + "</p>";
        notificationService.sendEmail(user.getEmail(), title, emailHtml);

        auditLogService.logAction(null, "sent_reminder_" + type, "Task", task.getName(), "—", "Sent to " + user.getFullName(), task.getProject().getId());
    }
}
