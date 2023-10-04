package com.dedalus.xraycucumber.ui;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class NotificationUtils {
    public static void notifyError(Project project, String errorContent) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Custom Notification Group")
                .createNotification(errorContent, NotificationType.ERROR)
                .notify(project);
    }

    public static void notifySuccess(Project project, String notificationContent) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Custom Notification Group")
                .createNotification(notificationContent, NotificationType.INFORMATION)
                .notify(project);
    }
}
