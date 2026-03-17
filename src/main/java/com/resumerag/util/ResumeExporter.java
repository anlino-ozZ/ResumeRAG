package com.resumerag.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.resumerag.model.Developer;
import com.resumerag.model.DeveloperSkill;
import com.resumerag.model.EducationRecord;
import com.resumerag.model.ProjectExperience;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * 简历导出工具类 - 支持真正的PDF和Word导出
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

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static PdfFont boldFont;
    private static PdfFont normalFont;
    private static PdfFont chineseFont;

    static {
        try {
            // 尝试加载系统自带的中文字体
            String[] fontPaths = {
                "C:/Windows/Fonts/simsun.ttc",   // 宋体
                "C:/Windows/Fonts/simhei.ttf",  // 黑体
                "/System/Library/Fonts/STHeiti Light.ttc",  // Mac 黑体
                "/System/Library/Fonts/PingFang.ttc"  // Mac 苹方
            };
            boolean fontLoaded = false;
            for (String fontPath : fontPaths) {
                try {
                    java.io.File fontFile = new java.io.File(fontPath);
                    if (fontFile.exists()) {
                        chineseFont = PdfFontFactory.createFont(fontPath);
                        boldFont = chineseFont;
                        normalFont = chineseFont;
                        fontLoaded = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            // 如果没找到中文字体，使用默认字体
            if (!fontLoaded) {
                boldFont = PdfFontFactory.createFont("Helvetica-Bold");
                normalFont = PdfFontFactory.createFont("Helvetica");
                chineseFont = normalFont;
            }
        } catch (Exception e) {
            e.printStackTrace();
            boldFont = null;
            normalFont = null;
            chineseFont = null;
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
     * 导出为真正的PDF格式
     */
    private static void exportToPdf(Developer developer,
                                    java.util.List<EducationRecord> educations,
                                    java.util.List<ProjectExperience> projects,
                                    java.util.List<DeveloperSkill> skills,
                                    File outputFile) throws Exception {

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(outputFile));
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // 设置页面边距
            document.setMargins(36, 36, 36, 36);

            // ===== 标题 =====
            Paragraph title = new Paragraph("个人简历")
                    .setFontSize(28)
                    .setFont(boldFont)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setFontColor(ColorConstants.BLUE)
                    .setMarginBottom(20);
            document.add(title);

            // ===== 基本信息 =====
            addSectionTitle(document, "基本信息", true);
            document.add(createInfoParagraph("姓      名：", nullToEmpty(developer.getName())));
            document.add(createInfoParagraph("联系电话：", nullToEmpty(developer.getPhone())));
            document.add(createInfoParagraph("电子邮箱：", nullToEmpty(developer.getEmail())));
            document.add(createInfoParagraph("工作年限：", developer.getYearsOfExperience() + " 年"));
            document.add(createInfoParagraph("最高学历：", nullToEmpty(developer.getHighestDegree())));
            document.add(createInfoParagraph("自我评价：", nullToEmpty(developer.getSelfEvaluation())));
            document.add(new Paragraph("\n"));

            // ===== 专业技能 =====
            addSectionTitle(document, "专业技能", true);
            if (skills != null && !skills.isEmpty()) {
                // 按熟练度分组显示
                for (int level = 5; level >= 1; level--) {
                    StringBuilder levelSkills = new StringBuilder();
                    for (DeveloperSkill skill : skills) {
                        if (skill.getProficiency() == level) {
                            String skillName = (skill.getSkill() != null) ? skill.getSkill().getSkillName() : "未知";
                            if (levelSkills.length() > 0) levelSkills.append("、");
                            levelSkills.append(skillName);
                        }
                    }
                    if (levelSkills.length() > 0) {
                        String levelName = getLevelName(level);
                        document.add(new Paragraph(levelName + "：" + levelSkills.toString())
                                .setFontSize(11)
                                .setMarginBottom(5));
                    }
                }
            } else {
                document.add(new Paragraph("暂无").setFontColor(ColorConstants.GRAY));
            }
            document.add(new Paragraph("\n"));

            // ===== 项目经验 =====
            addSectionTitle(document, "项目经验", true);
            if (projects != null && !projects.isEmpty()) {
                int index = 1;
                for (ProjectExperience project : projects) {
                    // 项目名称
                    document.add(new Paragraph(index + ". " + nullToEmpty(project.getProjectName()))
                            .setFont(boldFont)
                            .setFontSize(12)
                            .setMarginTop(10));

                    // 时间
                    String period = formatDate(project.getStartDate()) + " 至 " + formatDate(project.getEndDate());
                    document.add(new Paragraph("   时间：" + period).setFontColor(ColorConstants.GRAY).setFontSize(10));

                    // 担任角色
                    document.add(new Paragraph("   担任角色：" + nullToEmpty(project.getRole())));

                    // 技术栈
                    document.add(new Paragraph("   技术栈：" + nullToEmpty(project.getTechStack())));

                    // 项目描述
                    document.add(new Paragraph("   项目描述：" + nullToEmpty(project.getDescription())));

                    // 项目成果
                    document.add(new Paragraph("   项目成果：" + nullToEmpty(project.getAchievement())));

                    index++;
                }
            } else {
                document.add(new Paragraph("暂无").setFontColor(ColorConstants.GRAY));
            }
            document.add(new Paragraph("\n"));

            // ===== 教育背景 =====
            addSectionTitle(document, "教育背景", true);
            if (educations != null && !educations.isEmpty()) {
                int index = 1;
                for (EducationRecord edu : educations) {
                    document.add(new Paragraph(index + ". " + nullToEmpty(edu.getSchool()))
                            .setFont(boldFont)
                            .setFontSize(12)
                            .setMarginTop(8));

                    String period = formatDate(edu.getStartDate()) + " 至 " + formatDate(edu.getEndDate());
                    document.add(new Paragraph("   时间：" + period).setFontColor(ColorConstants.GRAY).setFontSize(10));
                    document.add(new Paragraph("   学历：" + nullToEmpty(edu.getDegree()) + "    专业：" + nullToEmpty(edu.getMajor())));

                    index++;
                }
            } else {
                document.add(new Paragraph("暂无").setFontColor(ColorConstants.GRAY));
            }
        }
    }

    /**
     * 处理null值为空字符串
     */
    private static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    /**
     * 获取熟练度等级名称
     */
    private static String getLevelName(int level) {
        switch (level) {
            case 5: return "熟练";
            case 4: return "熟悉";
            case 3: return "掌握";
            case 2: return "了解";
            case 1: return "入门";
            default: return "其他";
        }
    }

    /**
     * 创建信息段落
     */
    private static Paragraph createInfoParagraph(String label, String value) {
        return new Paragraph(label + value)
                .setFontSize(11)
                .setMarginBottom(3);
    }

    /**
     * 添加章节标题
     */
    private static void addSectionTitle(Document document, String title, boolean bold) {
        Paragraph sectionTitle = new Paragraph(title)
                .setFontSize(16)
                .setFont(bold ? boldFont : normalFont)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginTop(10);
        document.add(sectionTitle);
    }

    /**
     * 添加信息行
     */
    private static void addInfoRow(Document document, String label, String value) {
        Paragraph row = new Paragraph(label + ": " + (value != null ? value : ""))
                .setFontSize(11);
        document.add(row);
    }

    /**
     * 格式化日期
     */
    private static String formatDate(java.time.LocalDate date) {
        if (date == null) return "至今";
        return date.format(DATE_FORMAT);
    }

    /**
     * 导出为真正的Word格式(.docx)
     */
    private static void exportToWord(Developer developer,
                                     java.util.List<EducationRecord> educations,
                                     java.util.List<ProjectExperience> projects,
                                     java.util.List<DeveloperSkill> skills,
                                     File outputFile) throws Exception {

        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(outputFile)) {

            // 标题
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("个人简历");
            titleRun.setBold(true);
            titleRun.setFontSize(26);

            // ===== 基本信息 =====
            addWordSectionTitle(document, "基本信息");
            addWordInfoRow(document, "姓      名：", nullToEmpty(developer.getName()));
            addWordInfoRow(document, "联系电话：", nullToEmpty(developer.getPhone()));
            addWordInfoRow(document, "电子邮箱：", nullToEmpty(developer.getEmail()));
            addWordInfoRow(document, "工作年限：", developer.getYearsOfExperience() + " 年");
            addWordInfoRow(document, "最高学历：", nullToEmpty(developer.getHighestDegree()));
            addWordInfoRow(document, "自我评价：", nullToEmpty(developer.getSelfEvaluation()));

            // ===== 专业技能 =====
            addWordSectionTitle(document, "专业技能");
            if (skills != null && !skills.isEmpty()) {
                // 按熟练度分组显示
                for (int level = 5; level >= 1; level--) {
                    StringBuilder levelSkills = new StringBuilder();
                    for (DeveloperSkill skill : skills) {
                        if (skill.getProficiency() == level) {
                            String skillName = (skill.getSkill() != null) ? skill.getSkill().getSkillName() : "未知";
                            if (levelSkills.length() > 0) levelSkills.append("、");
                            levelSkills.append(skillName);
                        }
                    }
                    if (levelSkills.length() > 0) {
                        String levelName = getLevelName(level);
                        XWPFParagraph pLevel = document.createParagraph();
                        pLevel.createRun().setText(levelName + "：" + levelSkills.toString());
                    }
                }
            } else {
                XWPFParagraph pNone = document.createParagraph();
                pNone.createRun().setText("暂无");
            }

            // ===== 项目经验 =====
            addWordSectionTitle(document, "项目经验");
            if (projects != null && !projects.isEmpty()) {
                int index = 1;
                for (ProjectExperience project : projects) {
                    XWPFParagraph pName = document.createParagraph();
                    XWPFRun runName = pName.createRun();
                    runName.setText(index + ". " + nullToEmpty(project.getProjectName()));
                    runName.setBold(true);

                    String period = formatDate(project.getStartDate()) + " 至 " + formatDate(project.getEndDate());
                    XWPFParagraph pDate = document.createParagraph();
                    XWPFRun runDate = pDate.createRun();
                    runDate.setText("   时间：" + period);
                    runDate.setColor("808080");

                    XWPFParagraph pRole = document.createParagraph();
                    pRole.createRun().setText("   担任角色：" + nullToEmpty(project.getRole()));

                    XWPFParagraph pTech = document.createParagraph();
                    pTech.createRun().setText("   技术栈：" + nullToEmpty(project.getTechStack()));

                    XWPFParagraph pDesc = document.createParagraph();
                    pDesc.createRun().setText("   项目描述：" + nullToEmpty(project.getDescription()));

                    XWPFParagraph pAchieve = document.createParagraph();
                    pAchieve.createRun().setText("   项目成果：" + nullToEmpty(project.getAchievement()));

                    index++;
                }
            } else {
                XWPFParagraph pNone = document.createParagraph();
                pNone.createRun().setText("暂无");
            }

            // ===== 教育背景 =====
            addWordSectionTitle(document, "教育背景");
            if (educations != null && !educations.isEmpty()) {
                int index = 1;
                for (EducationRecord edu : educations) {
                    XWPFParagraph pSchool = document.createParagraph();
                    XWPFRun runSchool = pSchool.createRun();
                    runSchool.setText(index + ". " + nullToEmpty(edu.getSchool()));
                    runSchool.setBold(true);

                    String period = formatDate(edu.getStartDate()) + " 至 " + formatDate(edu.getEndDate());
                    XWPFParagraph pDate = document.createParagraph();
                    XWPFRun runDate = pDate.createRun();
                    runDate.setText("   时间：" + period);
                    runDate.setColor("808080");

                    XWPFParagraph pDegree = document.createParagraph();
                    pDegree.createRun().setText("   学历：" + nullToEmpty(edu.getDegree()) + "    专业：" + nullToEmpty(edu.getMajor()));

                    index++;
                }
            } else {
                XWPFParagraph pNone = document.createParagraph();
                pNone.createRun().setText("暂无");
            }

            document.write(out);
        }
    }

    /**
     * 添加Word章节标题
     */
    private static void addWordSectionTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(100);
        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(16);
    }

    /**
     * 添加Word信息行
     */
    private static void addWordInfoRow(XWPFDocument document, String label, String value) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(label + ": " + (value != null ? value : ""));
    }
}
