package com.resumerag.view;

import com.resumerag.dao.DeveloperDAO;
import com.resumerag.dao.ProjectExperienceDAO;
import com.resumerag.model.Developer;
import com.resumerag.model.ProjectExperience;
import com.resumerag.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 项目编辑对话框
 */
public class ProjectDialog extends JDialog {

    private ProjectExperience project;
    private ProjectExperienceDAO projectDAO;
    private DeveloperDAO developerDAO;
    private User currentUser;

    private JComboBox<Developer> developerCombo;
    private JTextField nameField;
    private JComboBox<String> roleCombo;
    private JTextField techStackField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextArea descriptionArea;
    private JTextArea achievementArea;

    private JButton saveBtn;
    private JButton cancelBtn;

    private boolean isEdit;

    public ProjectDialog(Frame owner, String title, ProjectExperience project,
                         ProjectExperienceDAO projectDAO, DeveloperDAO developerDAO, User currentUser) {
        super(owner, title, true);
        this.project = project;
        this.projectDAO = projectDAO;
        this.developerDAO = developerDAO;
        this.currentUser = currentUser;
        this.isEdit = (project != null);

        initComponents();
        initLayout();
        initListeners();

        if (isEdit) {
            loadData();
        }

        setSize(500, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // 开发者下拉框
        developerCombo = new JComboBox<>();
        
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            List<Developer> developers = developerDAO.getAllDevelopers(1, 100);
            for (Developer dev : developers) {
                developerCombo.addItem(dev);
            }
        } else {
            // 开发者只能为自己添加项目
            Developer currentDev = developerDAO.getDeveloperByUserId(currentUser.getUserId());
            if (currentDev != null) {
                developerCombo.addItem(currentDev);
                developerCombo.setEnabled(false); // 锁定
            }
        }

        nameField = new JTextField(20);

        // 角色下拉框
        String[] roles = {"后端开发", "前端开发", "全栈开发", "移动开发",
                "架构师", "项目经理", "产品经理", "测试工程师", "运维工程师"};
        roleCombo = new JComboBox<>(roles);
        roleCombo.setEditable(true);

        techStackField = new JTextField(20);
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        descriptionArea = new JTextArea(5, 20);
        achievementArea = new JTextArea(3, 20);

        descriptionArea.setLineWrap(true);
        achievementArea.setLineWrap(true);

        saveBtn = new JButton("保存");
        cancelBtn = new JButton("取消");
    }

    private void initLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 开发者
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("开发者:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(developerCombo, gbc);

        // 项目名称
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("项目名称:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);

        // 角色
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("担任角色:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(roleCombo, gbc);

        // 技术栈
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("技术栈:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(techStackField, gbc);

        // 日期面板
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.add(new JLabel("开始:"));
        datePanel.add(startDateField);
        datePanel.add(new JLabel("结束:"));
        datePanel.add(endDateField);
        datePanel.add(new JLabel("(yyyy-MM-dd)"));

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("时间:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(datePanel, gbc);

        // 项目描述
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("项目描述:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        mainPanel.add(new JScrollPane(descriptionArea), gbc);

        // 项目成果
        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(new JLabel("项目成果:"), gbc);

        gbc.gridx = 1;
        gbc.weighty = 0.5;
        mainPanel.add(new JScrollPane(achievementArea), gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void loadData() {
        // 选择对应的开发者
        for (int i = 0; i < developerCombo.getItemCount(); i++) {
            Developer dev = developerCombo.getItemAt(i);
            if (dev.getDeveloperId() == project.getDeveloperId()) {
                developerCombo.setSelectedIndex(i);
                break;
            }
        }

        nameField.setText(project.getProjectName());
        roleCombo.setSelectedItem(project.getRole());
        techStackField.setText(project.getTechStack());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (project.getStartDate() != null) {
            startDateField.setText(project.getStartDate().format(formatter));
        }
        if (project.getEndDate() != null) {
            endDateField.setText(project.getEndDate().format(formatter));
        }

        descriptionArea.setText(project.getDescription());
        achievementArea.setText(project.getAchievement());
    }

    private void save() {
        // 验证输入
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "项目名称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (roleCombo.getSelectedItem() == null || ((String) roleCombo.getSelectedItem()).trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "担任角色不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Developer selectedDev = (Developer) developerCombo.getSelectedItem();
        if (selectedDev == null || selectedDev.getDeveloperId() == 0) {
            JOptionPane.showMessageDialog(this, "请选择开发者", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isEdit) {
            project = new ProjectExperience();
        }

        project.setDeveloperId(selectedDev.getDeveloperId());
        project.setProjectName(nameField.getText().trim());
        project.setRole((String) roleCombo.getSelectedItem());
        project.setTechStack(techStackField.getText().trim());

        // 解析日期
        try {
            if (!startDateField.getText().trim().isEmpty()) {
                project.setStartDate(LocalDate.parse(startDateField.getText().trim()));
            }
            if (!endDateField.getText().trim().isEmpty()) {
                project.setEndDate(LocalDate.parse(endDateField.getText().trim()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误，请使用 yyyy-MM-dd 格式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        project.setDescription(descriptionArea.getText());
        project.setAchievement(achievementArea.getText());

        boolean success;
        if (isEdit) {
            success = projectDAO.updateProject(project);
        } else {
            success = projectDAO.addProject(project);
        }

        if (success) {
            dispose();
        }
    }
}