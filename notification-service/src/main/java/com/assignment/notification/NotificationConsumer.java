package com.assignment.notification;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final NotificationRepository notifications;

    public NotificationConsumer(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void consume(Map<String, Object> event) {
        String type = String.valueOf(event.get("type"));
        Long employeeId = asLong(event.get("employeeId"));
        Long managerId = asLong(event.get("managerId"));
        Long leaveId = asLong(event.get("leaveId"));
        String message = String.valueOf(event.get("message"));

        notifications.save(new Notification(employeeId, leaveId, type, message));
        if ("LEAVE_APPLIED".equals(type)) {
            notifications.save(new Notification(managerId, leaveId, type,
                    "New leave request waiting for approval"));
        }
        log.info("Notification logged for event {} and leave {}", type, leaveId);
    }

    private Long asLong(Object value) {
        return Long.valueOf(String.valueOf(value));
    }
}
