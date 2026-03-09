package com.resumerag.model;

import java.time.LocalDateTime;

public class ExportRecord {
    private int exportId;
    private int developerId;
    private Integer userId;
    private LocalDateTime exportTime;
    private String fileName;

    // 关联对象（方便显示）
    private Developer developer;
    private User user;

    public ExportRecord() {}

    // Getter 和 Setter
    public int getExportId() { return exportId; }
    public void setExportId(int exportId) { this.exportId = exportId; }

    public int getDeveloperId() { return developerId; }
    public void setDeveloperId(int developerId) { this.developerId = developerId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getExportTime() { return exportTime; }
    public void setExportTime(LocalDateTime exportTime) { this.exportTime = exportTime; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Developer getDeveloper() { return developer; }
    public void setDeveloper(Developer developer) { this.developer = developer; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}