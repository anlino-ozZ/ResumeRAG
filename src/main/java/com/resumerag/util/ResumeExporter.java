package com.resumerag.util;

import com.resumerag.model.Developer;
import com.resumerag.model.DeveloperSkill;
import com.resumerag.model.EducationRecord;
import com.resumerag.model.ProjectExperience;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

/**
 * 简历导出工具类
 */
public class ResumeExporter {

    public enum ExportFormat {
        PDF(".pdf"),
        WORD(".docx");

        private final String extension;

        ExportFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    /**
     * 导出简历
     */
    public static void exportResume(Developer developer,
                                     java.util.List<EducationRecord> educations,
                                     java.util.List<ProjectExperience> projects,
                                     java.util.List<DeveloperSkill> skills,
                                     File outputFile,
                                     ExportFormat format) throws Exception {

        if (format == ExportFormat.PDF) {
            exportToPdf(developer, educations, projects, skills, outputFile);
        } else {
            exportToWord(developer, educations, projects, skills, outputFile);
        }
    }

    /**
     * 导出为PDF格式（实际上生成文本文件，PDF需要额外的库支持）
     */
    private static void exportToPdf(Developer developer,
                                    java.util.List<EducationRecord> educations,
                                    java.util.List<ProjectExperience> projects,
                                    java.util.List<DeveloperSkill> skills,
                                    File outputFile) throws Exception {
        StringBuilder content = new StringBuilder();
        content.append("======================================\n");
        content.append("           简历\n");
        content.append("======================================\n\n");

        // 基本信息
        content.append("【基本信息】\n");
        content.append("姓名: ").append(developer.getName()).append("\n");
        content.append("电话: ").append(developer.getPhone()).append("\n");
        content.append("邮箱: ").append(developer.getEmail()).append("\n");
        content.append("自我评价: ").append(developer.getSelfEvaluation()).append("\n\n");

        // 技能
        content.append("【专业技能】\n");
        if (skills != null && !skills.isEmpty()) {
            for (DeveloperSkill skill : skills) {
                String skillName = (skill.getSkill() != null) ? skill.getSkill().getSkillName() : "未知";
                content.append("- ").append(skillName)
                        .append(" (熟练度: ").append(skill.getProficiencyLevel()).append(")\n");
            }
        } else {
            content.append("暂无\n");
        }
        content.append("\n");

        // 项目经验
        content.append("【项目经验】\n");
        if (projects != null && !projects.isEmpty()) {
            for (ProjectExperience project : projects) {
                content.append("项目名称: ").append(project.getProjectName()).append("\n");
                content.append("项目描述: ").append(project.getDescription()).append("\n");
                content.append("担任角色: ").append(project.getRole()).append("\n");
                content.append("技术栈: ").append(project.getTechStack()).append("\n");
                content.append("项目成果: ").append(project.getAchievement()).append("\n");
                content.append("\n");
            }
        } else {
            content.append("暂无\n\n");
        }

        // 教育背景
        content.append("【教育背景】\n");
        if (educations != null && !educations.isEmpty()) {
            for (EducationRecord edu : educations) {
                content.append("学校: ").append(edu.getSchool()).append("\n");
                content.append("学历: ").append(edu.getDegree()).append("\n");
                content.append("专业: ").append(edu.getMajor()).append("\n");
                content.append("时间: ").append(edu.getStartDate()).append(" - ")
                       .append(edu.getEndDate()).append("\n");
                content.append("\n");
            }
        } else {
            content.append("暂无\n");
        }

        // 写入文件
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.print(content.toString());
        }
    }

    /**
     * 导出为Word格式（生成文本文件，.docx实际需要额外库支持）
     */
    private static void exportToWord(Developer developer,
                                     java.util.List<EducationRecord> educations,
                                     java.util.List<ProjectExperience> projects,
                                     java.util.List<DeveloperSkill> skills,
                                     File outputFile) throws Exception {
        // 与PDF相同的实现（实际生产环境建议使用Apache POI库）
        exportToPdf(developer, educations, projects, skills, outputFile);
    }
}
