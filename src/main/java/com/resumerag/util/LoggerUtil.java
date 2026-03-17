package com.resumerag.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * 统一管理日志输出，替代System.out.println
 */
public class LoggerUtil {

    // 为每个类创建独立的日志记录器
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    // 便捷方法 - 获取调用类的日志记录器
    public static Logger getLogger() {
        return LoggerFactory.getLogger(org.slf4j.helpers.NOPLogger.class);
    }

    /**
     * 记录调试信息
     */
    public static void debug(Class<?> clazz, String message) {
        getLogger(clazz).debug(message);
    }

    /**
     * 记录一般信息
     */
    public static void info(Class<?> clazz, String message) {
        getLogger(clazz).info(message);
    }

    /**
     * 记录警告信息
     */
    public static void warn(Class<?> clazz, String message) {
        getLogger(clazz).warn(message);
    }

    /**
     * 记录错误信息
     */
    public static void error(Class<?> clazz, String message) {
        getLogger(clazz).error(message);
    }

    /**
     * 记录异常信息
     */
    public static void error(Class<?> clazz, String message, Throwable t) {
        getLogger(clazz).error(message, t);
    }

    /**
     * 记录方法入口（调试用）
     */
    public static void methodEntry(Class<?> clazz, String methodName) {
        getLogger(clazz).debug(">>> 进入方法: {}", methodName);
    }

    /**
     * 记录方法出口（调试用）
     */
    public static void methodExit(Class<?> clazz, String methodName) {
        getLogger(clazz).debug("<<< 退出方法: {}", methodName);
    }
}
