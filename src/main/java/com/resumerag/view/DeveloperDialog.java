package com.resumerag.view;

import com.resumerag.dao.DeveloperDAO;
import com.resumerag.dao.EducationRecordDAO;
import com.resumerag.dao.ExportRecordDAO;
import com.resumerag.dao.ProjectExperienceDAO;
import com.resumerag.dao.SkillDAO;
import com.resumerag.model.Developer;
import com.resumerag.model.DeveloperSkill;
import com.resumerag.model.EducationRecord;
import com.resumerag.model.ExportRecord;
import com.resumerag.model.ProjectExperience;
import com.resumerag.model.Skill;
import com.resumerag.model.User;
import com.resumerag.util.ResumeExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 开发者编辑对话框 - 包含完整信息：基本信息、技能、项目经验、教育背景
 */
public class DeveloperDialog extends JDialog {

    private Developer developer;
    private DeveloperDAO developerDAO;
    private SkillDAO skillDAO;
    private EducationRecordDAO educationRecordDAO;
    private ProjectExperienceDAO projectExperienceDAO;
    private User currentUser;
    private ExportRecordDAO exportRecordDAO;

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JSpinner expSpinner;
    private JTextArea evaluationArea;

    private JPanel skillPanel;
    private JPanel projectPanel;
    private JPanel educationPanel;

    private JButton saveBtn;
    private JButton cancelBtn;
    private JButton addSkillBtn;
    private JButton addProjectBtn;
    private JButton addEducationBtn;

    private List<DeveloperSkill> currentSkills = new ArrayList<>();
    private List<ProjectExperience> currentProjects = new ArrayList<>();
    private List<EducationRecord> currentEducations = new ArrayList<>();

    private boolean isEdit;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public DeveloperDialog(Frame owner, String title, Developer developer,
                           DeveloperDAO developerDAO, SkillDAO skillDAO,
                           User currentUser, ExportRecordDAO exportRecordDAO) {
        super(owner, title, true);
        this.developer = developer;
        this.developerDAO = developerDAO;
        this.skillDAO = skillDAO;
        this.educationRecordDAO = new EducationRecordDAO();
        this.projectExperienceDAO = new ProjectExperienceDAO();
        this.currentUser = currentUser;
        this.exportRecordDAO = exportRecordDAO;
        this.isEdit = (developer != null);

        initComponents();
        initLayout();
        initListeners();

        if (isEdit) {
            loadData();
        }

        // 如果是 developer 角色，且不是编辑自己的信息，则禁用编辑功能
        if ("developer".equalsIgnoreCase(currentUser.getRole())) {
            boolean isSelf = (developer != null && developer.getUserId() != null && developer.getUserId() == currentUser.getUserId());
            if (!isSelf) {
                disableEditing();
            }
        }

        setSize(800, 700);
        setLocationRelativeTo(owner);
    }

    private void disableEditing() {
        nameField.setEditable(false);
        phoneField.setEditable(false);
        emailField.setEditable(false);
        expSpinner.setEnabled(false);
        evaluationArea.setEditable(false);
        addSkillBtn.setVisible(false);
        addProjectBtn.setVisible(false);
        addEducationBtn.setVisible(false);
        saveBtn.setVisible(false);
        setTitle("开发者详情 - " + (developer != null ? developer.getName() : ""));

        // 隐藏所有删除按钮
        hideAllDeleteButtons();
    }

    private void hideAllDeleteButtons() {
        for (Component comp : skillPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel row = (JPanel) comp;
                for (Component c : row.getComponents()) {
                    if (c instanceof JButton && "删除".equals(((JButton) c).getText())) {
                        c.setVisible(false);
                    }
                }
            }
        }
    }

    private void initComponents() {
        nameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        expSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
        evaluationArea = new JTextArea(3, 20);
        evaluationArea.setLineWrap(true);

        saveBtn = new JButton("保存");
        cancelBtn = new JButton("取消");
        addSkillBtn = new JButton("添加技能");
        addProjectBtn = new JButton("添加项目");
        addEducationBtn = new JButton("添加教育经历");

        skillPanel = new JPanel(new GridBagLayout());
        skillPanel.setBorder(new TitledBorder("技能列表"));

        projectPanel = new JPanel(new GridBagLayout());
        projectPanel.setBorder(new TitledBorder("项目经验"));

        educationPanel = new JPanel(new GridBagLayout());
        educationPanel.setBorder(new TitledBorder("教育背景"));
    }

    private void initLayout() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // 基本信息标签页
        JPanel baseInfoPanel = createBaseInfoPanel();
        tabbedPane.addTab("基本信息", baseInfoPanel);

        // 技能标签页
        JPanel skillTabPanel = createSkillTabPanel();
        tabbedPane.addTab("技能", skillTabPanel);

        // 项目经验标签页
        JPanel projectTabPanel = createProjectTabPanel();
        tabbedPane.addTab("项目经验", projectTabPanel);

        // 教育背景标签页
        JPanel educationTabPanel = createEducationTabPanel();
        tabbedPane.addTab("教育背景", educationTabPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if (isEdit) {
            JButton exportBtn = new JButton("导出简历");
            exportBtn.addActionListener(e -> exportResume());
            buttonPanel.add(exportBtn);
        }

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createBaseInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 姓名
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("姓名:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // 电话
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("电话:"), gbc);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        // 邮箱
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("邮箱:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // 经验年限
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("经验(年):"), gbc);
        gbc.gridx = 1;
        panel.add(expSpinner, gbc);

        // 自我评价
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("自我评价:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panel.add(new JScrollPane(evaluationArea), gbc);

        return panel;
    }

    private JPanel createSkillTabPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addSkillBtn);
        panel.add(btnPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(skillPanel);
        scrollPane.setPreferredSize(new Dimension(700, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProjectTabPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addProjectBtn);
        panel.add(btnPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(projectPanel);
        scrollPane.setPreferredSize(new Dimension(700, 250));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEducationTabPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addEducationBtn);
        panel.add(btnPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(educationPanel);
        scrollPane.setPreferredSize(new Dimension(700, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void initListeners() {
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        addSkillBtn.addActionListener(e -> showSkillSelectionDialog());
        addProjectBtn.addActionListener(e -> showProjectDialog(null));
        addEducationBtn.addActionListener(e -> showEducationDialog(null));
    }

    private void loadData() {
        nameField.setText(developer.getName());
        phoneField.setText(developer.getPhone());
        emailField.setText(developer.getEmail());
        expSpinner.setValue(developer.getYearsOfExperience());
        evaluationArea.setText(developer.getSelfEvaluation());

        // 加载技能
        currentSkills = developerDAO.getDeveloperSkills(developer.getDeveloperId());
        for (DeveloperSkill ds : currentSkills) {
            addSkillToPanel(ds);
        }

        // 加载项目经验
        currentProjects = projectExperienceDAO.getProjectsByDeveloperId(developer.getDeveloperId());
        for (ProjectExperience p : currentProjects) {
            addProjectToPanel(p);
        }

        // 加载教育背景
        currentEducations = educationRecordDAO.getEducationRecordsByDeveloperId(developer.getDeveloperId());
        for (EducationRecord e : currentEducations) {
            addEducationToPanel(e);
        }
    }

    // ==================== 技能相关 ====================
    private void addSkillToPanel(DeveloperSkill ds) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = skillPanel.getComponentCount();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.add(new JLabel(ds.getSkill().getSkillName() + " - " + ds.getProficiencyLevel()));

        JButton removeBtn = new JButton("删除");
        removeBtn.addActionListener(e -> {
            currentSkills.remove(ds);
            skillPanel.remove(rowPanel);
            skillPanel.revalidate();
            skillPanel.repaint();
        });

        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            removeBtn.setVisible(false);
        }

        rowPanel.add(removeBtn);
        skillPanel.add(rowPanel, gbc);
    }

    private void showSkillSelectionDialog() {
        List<Skill> allSkills = skillDAO.getAllSkills();
        JComboBox<Skill> skillCombo = new JComboBox<>(allSkills.toArray(new Skill[0]));
        JSpinner proficiencySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("选择技能:"));
        panel.add(skillCombo);
        panel.add(new JLabel("熟练度(1-5):"));
        panel.add(proficiencySpinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "添加技能", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Skill selectedSkill = (Skill) skillCombo.getSelectedItem();
            int proficiency = (Integer) proficiencySpinner.getValue();

            DeveloperSkill ds = new DeveloperSkill();
            ds.setSkill(selectedSkill);
            ds.setSkillId(selectedSkill.getSkillId());
            ds.setProficiency(proficiency);
            ds.setDeveloperId(developer != null ? developer.getDeveloperId() : 0);

            currentSkills.add(ds);
            addSkillToPanel(ds);
        }
    }

    // ==================== 项目经验相关 ====================
    private void addProjectToPanel(ProjectExperience p) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = projectPanel.getComponentCount();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 0, 2, 0);

        String dateRange = formatDateRange(p.getStartDate(), p.getEndDate());
        String info = String.format("%s | %s | %s | %s",
                p.getProjectName(), p.getRole(), p.getTechStack(), dateRange);

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.add(new JLabel(info));

        if (p.getDescription() != null && !p.getDescription().isEmpty()) {
            rowPanel.add(new JLabel(" - " + (p.getDescription().length() > 30 ?
                    p.getDescription().substring(0, 30) + "..." : p.getDescription())));
        }

        JButton editBtn = new JButton("编辑");
        editBtn.addActionListener(e -> showProjectDialog(p));

        JButton removeBtn = new JButton("删除");
        removeBtn.addActionListener(e -> {
            currentProjects.remove(p);
            projectPanel.remove(rowPanel);
            projectPanel.revalidate();
            projectPanel.repaint();
        });

        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            editBtn.setVisible(false);
            removeBtn.setVisible(false);
        }

        rowPanel.add(editBtn);
        rowPanel.add(removeBtn);
        projectPanel.add(rowPanel, gbc);
    }

    private void showProjectDialog(ProjectExperience existingProject) {
        JTextField nameField = new JTextField(20);
        JTextField roleField = new JTextField(20);
        JTextField techField = new JTextField(20);
        JTextField descField = new JTextField(20);
        JTextField achievementField = new JTextField(20);
        JSpinner startYearSpinner = new JSpinner(new SpinnerNumberModel(2020, 1990, 2030, 1));
        JSpinner startMonthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        JSpinner endYearSpinner = new JSpinner(new SpinnerNumberModel(2021, 1990, 2030, 1));
        JSpinner endMonthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        JCheckBox currentCheck = new JCheckBox("目前在职");

        if (existingProject != null) {
            nameField.setText(existingProject.getProjectName());
            roleField.setText(existingProject.getRole());
            techField.setText(existingProject.getTechStack());
            descField.setText(existingProject.getDescription());
            achievementField.setText(existingProject.getAchievement());
            if (existingProject.getStartDate() != null) {
                startYearSpinner.setValue(existingProject.getStartDate().getYear());
                startMonthSpinner.setValue(existingProject.getStartDate().getMonthValue());
            }
            if (existingProject.getEndDate() != null) {
                endYearSpinner.setValue(existingProject.getEndDate().getYear());
                endMonthSpinner.setValue(existingProject.getEndDate().getMonthValue());
            } else {
                currentCheck.setSelected(true);
            }
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("项目名称:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("担任角色:"), gbc);
        gbc.gridx = 1;
        panel.add(roleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("技术栈:"), gbc);
        gbc.gridx = 1;
        panel.add(techField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("开始时间:"), gbc);
        gbc.gridx = 1;
        panel.add(new JPanel(new FlowLayout(FlowLayout.LEFT)) {{
            add(startYearSpinner); add(new JLabel("年"));
            add(startMonthSpinner); add(new JLabel("月"));
        }}, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("结束时间:"), gbc);
        gbc.gridx = 1;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(endYearSpinner); datePanel.add(new JLabel("年"));
        datePanel.add(endMonthSpinner); datePanel.add(new JLabel("月"));
        datePanel.add(currentCheck);
        panel.add(datePanel, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("项目描述:"), gbc);
        gbc.gridx = 1;
        panel.add(descField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("项目成果:"), gbc);
        gbc.gridx = 1;
        panel.add(achievementField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
                existingProject != null ? "编辑项目" : "添加项目",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            ProjectExperience p = existingProject != null ? existingProject : new ProjectExperience();
            p.setProjectName(nameField.getText());
            p.setRole(roleField.getText());
            p.setTechStack(techField.getText());
            p.setDescription(descField.getText());
            p.setAchievement(achievementField.getText());

            if (!currentCheck.isSelected()) {
                p.setStartDate(LocalDate.of((Integer) startYearSpinner.getValue(), (Integer) startMonthSpinner.getValue(), 1));
                p.setEndDate(LocalDate.of((Integer) endYearSpinner.getValue(), (Integer) endMonthSpinner.getValue(), 1));
            } else {
                p.setStartDate(LocalDate.of((Integer) startYearSpinner.getValue(), (Integer) startMonthSpinner.getValue(), 1));
                p.setEndDate(null);
            }

            if (existingProject == null) {
                currentProjects.add(p);
            }

            // 刷新显示
            projectPanel.removeAll();
            for (ProjectExperience proj : currentProjects) {
                addProjectToPanel(proj);
            }
            projectPanel.revalidate();
            projectPanel.repaint();
        }
    }

    // ==================== 教育背景相关 ====================
    private void addEducationToPanel(EducationRecord e) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = educationPanel.getComponentCount();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 0, 2, 0);

        String dateRange = formatDateRange(e.getStartDate(), e.getEndDate());
        String info = String.format("%s | %s | %s | %s",
                e.getSchool(), e.getMajor(), e.getDegree(), dateRange);

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.add(new JLabel(info));

        JButton editBtn = new JButton("编辑");
        editBtn.addActionListener(e2 -> showEducationDialog(e));

        JButton removeBtn = new JButton("删除");
        removeBtn.addActionListener(e2 -> {
            currentEducations.remove(e);
            educationPanel.remove(rowPanel);
            educationPanel.revalidate();
            educationPanel.repaint();
        });

        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            editBtn.setVisible(false);
            removeBtn.setVisible(false);
        }

        rowPanel.add(editBtn);
        rowPanel.add(removeBtn);
        educationPanel.add(rowPanel, gbc);
    }

    private void showEducationDialog(EducationRecord existingEducation) {
        String[] degrees = {"本科", "硕士", "博士", "博士后", "大专", "高中"};

        JTextField schoolField = new JTextField(20);
        JTextField majorField = new JTextField(20);
        JComboBox<String> degreeCombo = new JComboBox<>(degrees);
        JSpinner startYearSpinner = new JSpinner(new SpinnerNumberModel(2015, 1980, 2030, 1));
        JSpinner endYearSpinner = new JSpinner(new SpinnerNumberModel(2019, 1980, 2030, 1));

        if (existingEducation != null) {
            schoolField.setText(existingEducation.getSchool());
            majorField.setText(existingEducation.getMajor());
            degreeCombo.setSelectedItem(existingEducation.getDegree());
            if (existingEducation.getStartDate() != null) {
                startYearSpinner.setValue(existingEducation.getStartDate().getYear());
            }
            if (existingEducation.getEndDate() != null) {
                endYearSpinner.setValue(existingEducation.getEndDate().getYear());
            }
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("学校:"), gbc);
        gbc.gridx = 1;
        panel.add(schoolField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("专业:"), gbc);
        gbc.gridx = 1;
        panel.add(majorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("学历:"), gbc);
        gbc.gridx = 1;
        panel.add(degreeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("入学年份:"), gbc);
        gbc.gridx = 1;
        panel.add(startYearSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("毕业年份:"), gbc);
        gbc.gridx = 1;
        panel.add(endYearSpinner, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
                existingEducation != null ? "编辑教育经历" : "添加教育经历",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            EducationRecord e = existingEducation != null ? existingEducation : new EducationRecord();
            e.setSchool(schoolField.getText());
            e.setMajor(majorField.getText());
            e.setDegree((String) degreeCombo.getSelectedItem());
            e.setStartDate(LocalDate.of((Integer) startYearSpinner.getValue(), 9, 1));
            e.setEndDate(LocalDate.of((Integer) endYearSpinner.getValue(), 6, 1));

            if (existingEducation == null) {
                currentEducations.add(e);
            }

            // 刷新显示
            educationPanel.removeAll();
            for (EducationRecord edu : currentEducations) {
                addEducationToPanel(edu);
            }
            educationPanel.revalidate();
            educationPanel.repaint();
        }
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        if (start == null) return "";
        String startStr = start.format(dateFormatter);
        if (end == null) return startStr + " - 至今";
        return startStr + " - " + end.format(dateFormatter);
    }

    // ==================== 保存功能 ====================
    private void save() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "姓名不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "电话不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "邮箱不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isEdit) {
            developer = new Developer();
        }

        developer.setName(nameField.getText().trim());
        developer.setPhone(phoneField.getText().trim());
        developer.setEmail(emailField.getText().trim());
        developer.setYearsOfExperience((Integer) expSpinner.getValue());
        developer.setSelfEvaluation(evaluationArea.getText());

        boolean success;
        if (isEdit) {
            success = developerDAO.updateDeveloper(developer);
        } else {
            success = developerDAO.addDeveloper(developer);
        }

        if (success) {
            // 保存技能
            updateSkills();

            // 保存项目经验
            updateProjects();

            // 保存教育背景
            updateEducations();

            JOptionPane.showMessageDialog(this, "保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "保存失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSkills() {
        List<DeveloperSkill> oldSkills = isEdit ? developerDAO.getDeveloperSkills(developer.getDeveloperId()) : new ArrayList<>();

        // 删除不再有的技能
        for (DeveloperSkill oldSkill : oldSkills) {
            boolean found = false;
            for (DeveloperSkill newSkill : currentSkills) {
                if (oldSkill.getSkillId() == newSkill.getSkillId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                developerDAO.deleteDeveloperSkill(developer.getDeveloperId(), oldSkill.getSkillId());
            }
        }

        // 添加或更新技能
        for (DeveloperSkill newSkill : currentSkills) {
            newSkill.setDeveloperId(developer.getDeveloperId());
            boolean found = false;
            for (DeveloperSkill oldSkill : oldSkills) {
                if (newSkill.getSkillId() == oldSkill.getSkillId()) {
                    if (newSkill.getProficiency() != oldSkill.getProficiency()) {
                        developerDAO.updateDeveloperSkill(developer.getDeveloperId(), newSkill.getSkillId(), newSkill.getProficiency());
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                developerDAO.addDeveloperSkill(developer.getDeveloperId(), newSkill.getSkillId(), newSkill.getProficiency());
            }
        }
    }

    private void updateProjects() {
        if (!isEdit) return;

        List<ProjectExperience> oldProjects = projectExperienceDAO.getProjectsByDeveloperId(developer.getDeveloperId());

        // 删除不再有的项目
        for (ProjectExperience oldProj : oldProjects) {
            boolean found = false;
            for (ProjectExperience newProj : currentProjects) {
                if (newProj.getProjectId() == oldProj.getProjectId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                projectExperienceDAO.deleteProject(oldProj.getProjectId());
            }
        }

        // 添加或更新项目
        for (ProjectExperience p : currentProjects) {
            p.setDeveloperId(developer.getDeveloperId());
            if (p.getProjectId() == 0) {
                projectExperienceDAO.addProject(p);
            } else {
                projectExperienceDAO.updateProject(p);
            }
        }
    }

    private void updateEducations() {
        if (!isEdit) return;

        List<EducationRecord> oldEducations = educationRecordDAO.getEducationRecordsByDeveloperId(developer.getDeveloperId());

        // 删除不再有的教育记录
        for (EducationRecord oldEdu : oldEducations) {
            boolean found = false;
            for (EducationRecord newEdu : currentEducations) {
                if (newEdu.getEducationId() == oldEdu.getEducationId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                educationRecordDAO.deleteEducationRecord(oldEdu.getEducationId());
            }
        }

        // 添加或更新教育记录
        for (EducationRecord e : currentEducations) {
            e.setDeveloperId(developer.getDeveloperId());
            if (e.getEducationId() == 0) {
                educationRecordDAO.addEducationRecord(e);
            } else {
                educationRecordDAO.updateEducationRecord(e);
            }
        }
    }

    // ==================== 导出功能 ====================
    private void exportResume() {
        if (developer == null) {
            JOptionPane.showMessageDialog(this, "没有可导出的简历数据", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] options = {"PDF", "Word"};
        int formatChoice = JOptionPane.showOptionDialog(
                this, "请选择导出格式：", "导出简历",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (formatChoice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        ResumeExporter.ExportFormat format = (formatChoice == 0)
                ? ResumeExporter.ExportFormat.PDF
                : ResumeExporter.ExportFormat.WORD;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存简历");
        fileChooser.setSelectedFile(new File(developer.getName() + "_简历" + format.getExtension()));

        if (format == ResumeExporter.ExportFormat.PDF) {
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF 文件 (*.pdf)", "pdf"));
        } else {
            fileChooser.setFileFilter(new FileNameExtensionFilter("Word 文件 (*.docx)", "docx"));
        }

        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outputFile = fileChooser.getSelectedFile();
        if (!outputFile.getName().toLowerCase().endsWith(format.getExtension())) {
            outputFile = new File(outputFile.getAbsolutePath() + format.getExtension());
        }

        try {
            List<EducationRecord> educations = educationRecordDAO.getEducationRecordsByDeveloperId(developer.getDeveloperId());
            List<ProjectExperience> projects = projectExperienceDAO.getProjectsByDeveloperId(developer.getDeveloperId());
            List<DeveloperSkill> skills = developerDAO.getDeveloperSkills(developer.getDeveloperId());

            ResumeExporter.exportResume(developer, educations, projects, skills, outputFile, format);

            if (exportRecordDAO != null) {
                ExportRecord record = new ExportRecord();
                record.setDeveloperId(developer.getDeveloperId());
                record.setUserId(currentUser != null ? currentUser.getUserId() : null);
                record.setExportTime(LocalDateTime.now());
                record.setFileName(outputFile.getName());
                exportRecordDAO.addExportRecord(record);
            }

            JOptionPane.showMessageDialog(this,
                    "简历导出成功！\n保存位置：" + outputFile.getAbsolutePath(),
                    "导出成功", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "导出失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
