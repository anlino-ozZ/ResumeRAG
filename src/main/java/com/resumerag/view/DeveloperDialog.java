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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 开发者编辑对话框
 */
public class DeveloperDialog extends JDialog {

    private Developer developer;
    private DeveloperDAO developerDAO;
    private SkillDAO skillDAO;
    private User currentUser;  // 当前登录用户
    private ExportRecordDAO exportRecordDAO;  // 导出记录DAO

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JSpinner expSpinner;
    private JTextArea evaluationArea;
    private JPanel skillPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JButton addSkillBtn;

    private boolean isEdit;

    public DeveloperDialog(Frame owner, String title, Developer developer,
                           DeveloperDAO developerDAO, SkillDAO skillDAO,
                           User currentUser, ExportRecordDAO exportRecordDAO) {
        super(owner, title, true);
        this.developer = developer;
        this.developerDAO = developerDAO;
        this.skillDAO = skillDAO;
        this.currentUser = currentUser;  // 新增
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

        setSize(500, 600);
        setLocationRelativeTo(owner);
    }

    /**
     * 禁用编辑功能，仅查看详情
     */
    private void disableEditing() {
        nameField.setEditable(false);
        phoneField.setEditable(false);
        emailField.setEditable(false);
        expSpinner.setEnabled(false);
        evaluationArea.setEditable(false);
        addSkillBtn.setVisible(false);
        saveBtn.setVisible(false);
        setTitle("开发者详情 - " + (developer != null ? developer.getName() : ""));
    }

    private void initComponents() {
        nameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        expSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
        evaluationArea = new JTextArea(5, 20);
        evaluationArea.setLineWrap(true);

        saveBtn = new JButton("保存");
        cancelBtn = new JButton("取消");
        addSkillBtn = new JButton("添加技能");

        skillPanel = new JPanel(new GridBagLayout());
        skillPanel.setBorder(new TitledBorder("技能列表"));
    }

    private void initLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 姓名
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("姓名:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);

        // 电话
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("电话:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(phoneField, gbc);

        // 邮箱
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("邮箱:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);

        // 经验年限
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("经验(年):"), gbc);

        gbc.gridx = 1;
        mainPanel.add(expSpinner, gbc);

        // 自我评价
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("自我评价:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        mainPanel.add(new JScrollPane(evaluationArea), gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 技能面板
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel skillBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        skillBtnPanel.add(addSkillBtn);
        southPanel.add(skillBtnPanel, BorderLayout.NORTH);

        JScrollPane skillScroll = new JScrollPane(skillPanel);
        skillScroll.setPreferredSize(new Dimension(450, 150));
        southPanel.add(skillScroll, BorderLayout.CENTER);

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
    private void exportResume() {
        if (developer == null) {
            JOptionPane.showMessageDialog(this, "没有可导出的简历数据", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 选择导出格式
        String[] options = {"PDF", "Word"};
        int formatChoice = JOptionPane.showOptionDialog(
                this,
                "请选择导出格式：",
                "导出简历",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (formatChoice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        ResumeExporter.ExportFormat format = (formatChoice == 0)
                ? ResumeExporter.ExportFormat.PDF
                : ResumeExporter.ExportFormat.WORD;

        // 选择保存路径
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
        // 确保文件扩展名正确
        if (!outputFile.getName().toLowerCase().endsWith(format.getExtension())) {
            outputFile = new File(outputFile.getAbsolutePath() + format.getExtension());
        }

        try {
            // 获取完整数据
            EducationRecordDAO educationDAO = new EducationRecordDAO();
            ProjectExperienceDAO projectDAO = new ProjectExperienceDAO();

            List<EducationRecord> educations = educationDAO.getEducationRecordsByDeveloperId(developer.getDeveloperId());
            List<ProjectExperience> projects = projectDAO.getProjectsByDeveloperId(developer.getDeveloperId());
            List<DeveloperSkill> skills = developerDAO.getDeveloperSkills(developer.getDeveloperId());

            // 执行导出
            ResumeExporter.exportResume(developer, educations, projects, skills, outputFile, format);

            // 记录导出记录
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
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "导出失败：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void initListeners() {
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        addSkillBtn.addActionListener(e -> showSkillSelectionDialog());
    }

    private void loadData() {
        nameField.setText(developer.getName());
        phoneField.setText(developer.getPhone());
        emailField.setText(developer.getEmail());
        expSpinner.setValue(developer.getYearsOfExperience());
        evaluationArea.setText(developer.getSelfEvaluation());

        // 加载技能
        List<DeveloperSkill> skills = developerDAO.getDeveloperSkills(developer.getDeveloperId());
        for (DeveloperSkill ds : skills) {
            addSkillToPanel(ds);
        }
    }

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
            skillPanel.remove(rowPanel);
            skillPanel.revalidate();
            skillPanel.repaint();
        });
        
        // 只有 admin 角色可以看到删除技能按钮
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

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "添加技能",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            Skill selectedSkill = (Skill) skillCombo.getSelectedItem();
            int proficiency = (Integer) proficiencySpinner.getValue();

            DeveloperSkill ds = new DeveloperSkill();
            ds.setSkill(selectedSkill);
            ds.setSkillId(selectedSkill.getSkillId());
            ds.setProficiency(proficiency);

            addSkillToPanel(ds);
        }
    }

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
            updateSkills();
            JOptionPane.showMessageDialog(this, "保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "保存失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSkills() {
        // 获取新技能列表
        List<DeveloperSkill> newSkills = new ArrayList<>();
        for (Component comp : skillPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel rowPanel = (JPanel) comp;
                JLabel label = (JLabel) rowPanel.getComponent(0);
                String text = label.getText();

                String skillName = text.split(" - ")[0];
                String profText = text.split(" - ")[1];
                int proficiency = 3; // 默认值
                if ("了解".equals(profText)) proficiency = 1;
                else if ("入门".equals(profText)) proficiency = 2;
                else if ("熟练".equals(profText)) proficiency = 3;
                else if ("精通".equals(profText)) proficiency = 4;
                else if ("专家".equals(profText)) proficiency = 5;

                Skill skill = skillDAO.getSkillByName(skillName);
                if (skill != null) {
                    newSkills.add(new DeveloperSkill(developer.getDeveloperId(), skill.getSkillId(), proficiency));
                }
            }
        }

        // 获取旧技能列表
        List<DeveloperSkill> oldSkills = isEdit ? developerDAO.getDeveloperSkills(developer.getDeveloperId()) : new ArrayList<>();

        // 找出要删除的技能
        for (DeveloperSkill oldSkill : oldSkills) {
            boolean found = false;
            for (DeveloperSkill newSkill : newSkills) {
                if (oldSkill.getSkillId() == newSkill.getSkillId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                developerDAO.deleteDeveloperSkill(developer.getDeveloperId(), oldSkill.getSkillId());
            }
        }

        // 找出要添加或更新的技能
        for (DeveloperSkill newSkill : newSkills) {
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
}