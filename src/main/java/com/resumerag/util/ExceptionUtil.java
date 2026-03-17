package com.resumerag.util;

import javax.swing.*;

/**
 * 异常处理工具类
 * 提供用户友好的错误提示
 */
public class ExceptionUtil {

    /**
     * 显示错误对话框
     */
    public static void showError(String message) {
        JOptionPane.showMessageDialog(null,
                message,
                "错误",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 显示警告对话框
     */
    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null,
                message,
                "警告",
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * 显示信息对话框
     */
    public static void showInfo(String message) {
        JOptionPane.showMessageDialog(null,
                message,
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 获取友好的错误消息
     */
    public static String getFriendlyMessage(Exception e) {
        String message = e.getMessage();

        if (message == null || message.isEmpty()) {
            return "发生未知错误，请稍后重试";
        }

        // 数据库连接错误
        if (message.contains("connection") || message.contains("Connection")) {
            return "数据库连接失败，请检查网络";
        }

        // SQL语法错误
        if (message.contains("SQL") || message.contains("syntax")) {
            return "数据操作失败，请稍后重试";
        }

        // 主键/唯一约束冲突
        if (message.contains("Duplicate") || message.contains("duplicate")) {
            return "该数据已存在，请勿重复添加";
        }

        // 外键约束错误
        if (message.contains("foreign key") || message.contains("IntegrityConstraint")) {
            return "该数据被其他记录引用，无法删除";
        }

        // 默认返回原始错误信息（生产环境可隐藏详情）
        return "操作失败: " + message;
    }

    /**
     * 处理异常并显示友好错误消息
     */
    public static void handleException(Exception e, Class<?> clazz) {
        LoggerUtil.error(clazz, "Exception occurred", e);
        showError(getFriendlyMessage(e));
    }

    /**
     * 处理异常但不显示对话框（用于批量操作）
     */
    public static void logException(Exception e, Class<?> clazz) {
        LoggerUtil.error(clazz, "Exception occurred", e);
    }
}
