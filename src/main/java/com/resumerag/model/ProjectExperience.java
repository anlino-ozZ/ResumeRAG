package com.resumerag.model;

import java.time.LocalDate;

public class ProjectExperience {
    private int projectId;
    private int developerId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String role;        // 担任角色
    private String techStack;   // 技术栈，如 "Java, Spring Boot, Redis"
    private String description; // 项目描述
    private String achievement; // 项目成果

    public ProjectExperience() {}

    // Getter 和 Setter
    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getDeveloperId() { return developerId; }
    public void setDeveloperId(int developerId) { this.developerId = developerId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }
}