package com.resumerag.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String password;
    private String role;  // 'admin', 'developer', 'pending_admin'
    private String status; // 'pending', 'active', 'rejected'  ← 新增字段
    private LocalDateTime createdTime;

    // 构造方法
    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = "active"; // 默认激活
    }

    // Getter 和 Setter
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // 新增status的getter/setter
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    // 判断状态的方法
    public boolean isActive() {
        return "active".equals(status);
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +  // 显示状态
                '}';
    }
}