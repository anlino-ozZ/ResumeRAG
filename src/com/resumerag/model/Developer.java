package com.resumerag.model;

import java.time.LocalDateTime;
import java.util.List;

public class Developer {
    private int developerId;
    private Integer userId;  // 可能为null
    private String name;
    private String phone;
    private String email;
    private int yearsOfExperience;
    private String selfEvaluation;
    private LocalDateTime createdTime;

    // 面向对象：关联的其他对象（这些不从数据库直接查询，由Service层组装）
    private List<EducationRecord> educationRecords;
    private List<ProjectExperience> projects;
    private List<DeveloperSkill> skills;

    // 构造方法
    public Developer() {}

    public Developer(String name, String phone, String email, int yearsOfExperience) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.yearsOfExperience = yearsOfExperience;
    }

    // Getter 和 Setter
    public int getDeveloperId() { return developerId; }
    public void setDeveloperId(int developerId) { this.developerId = developerId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }

    public String getSelfEvaluation() { return selfEvaluation; }
    public void setSelfEvaluation(String selfEvaluation) { this.selfEvaluation = selfEvaluation; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public List<EducationRecord> getEducationRecords() { return educationRecords; }
    public void setEducationRecords(List<EducationRecord> educationRecords) { this.educationRecords = educationRecords; }

    public List<ProjectExperience> getProjects() { return projects; }
    public void setProjects(List<ProjectExperience> projects) { this.projects = projects; }

    public List<DeveloperSkill> getSkills() { return skills; }
    public void setSkills(List<DeveloperSkill> skills) { this.skills = skills; }

    @Override
    public String toString() {
        return "Developer{" +
                "developerId=" + developerId +
                ", name='" + name + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                '}';
    }
}