package com.dedalus.xraycucumber.ui;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class NotificationUtils {

    private final Project project;

    public NotificationUtils(Project project) {
        this.project = project;
    }

    public void notifyError(String content) {
        createNotification(content, NotificationType.ERROR);
    }

    public void notifyInfo(final String content) {
        createNotification(content, NotificationType.INFORMATION);
    }

    public void notifySuccess(String content) {
        createNotification(content, NotificationType.INFORMATION);
    }

    private void createNotification(String content, NotificationType type) {
        NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Custom Notification Group");

        if (notificationGroup == null) {
            throw new IllegalStateException("Notification Group doesn't exist");
        } else {
            notificationGroup.createNotification(content, type).notify(project);
        }
    }
}
