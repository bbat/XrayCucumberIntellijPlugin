package com.dedalus.xraycucumber.ui.utils;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class NotificationUtils {
    public static void notifyError(Project project, String content) {
        createNotification(project, content, NotificationType.ERROR);
    }

    public static void notifySuccess(Project project, String content) {
        createNotification(project, content, NotificationType.INFORMATION);
    }

    private static void createNotification(Project project, String content, NotificationType type) {
        NotificationGroup notificationGroup = NotificationGroupManager.getInstance()
                .getNotificationGroup("Custom Notification Group");

        if(notificationGroup == null) {
            throw new RuntimeException("Notification Group doesn't exist");
        } else {
            notificationGroup.createNotification(content, type).notify(project);
        }
    }
}
