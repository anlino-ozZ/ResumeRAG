package com.resumerag.model;

import java.time.LocalDate;

public class EducationRecord {
    private int educationId;
    private int developerId;
    private String school;
    private String major;
    private String degree;  // 学历：本科、硕士等
    private LocalDate startDate;
    private LocalDate endDate;

    public EducationRecord() {}

    // Getter 和 Setter
    public int getEducationId() { return educationId; }
    public void setEducationId(int educationId) { this.educationId = educationId; }

    public int getDeveloperId() { return developerId; }
    public void setDeveloperId(int developerId) { this.developerId = developerId; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}