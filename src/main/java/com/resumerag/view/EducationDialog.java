package com.resumerag.view;

import com.resumerag.dao.DeveloperDAO;
import com.resumerag.dao.EducationRecordDAO;
import com.resumerag.model.Developer;
import com.resumerag.model.EducationRecord;
import com.resumerag.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 教育记录编辑对话框
 */
public class EducationDialog extends JDialog {

    private EducationRecord education;
    private EducationRecordDAO educationDAO;
    private DeveloperDAO developerDAO;
    private User currentUser;

    private JComboBox<Developer> developerCombo;
    private JTextField schoolField;
    private JTextField majorField;
    private JComboBox<String> degreeCombo;
    private JTextField startDateField;
    private JTextField endDateField;

    private JButton saveBtn;
    private JButton cancelBtn;

    private boolean isEdit;

    public EducationDialog(Frame owner, String title, EducationRecord education,
                           EducationRecordDAO educationDAO, DeveloperDAO developerDAO, User currentUser) {
        super(owner, title, true);
        this.education = education;
        this.educationDAO = educationDAO;
        this.developerDAO = developerDAO;
        this.currentUser = currentUser;
        this.isEdit = (education != null);

        initComponents();
        initLayout();
        initListeners();

        if (isEdit) {
            loadData();
        }

        setSize(500, 400);
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
            // 开发者只能为自己添加教育记录
            Developer currentDev = developerDAO.getDeveloperByUserId(currentUser.getUserId());
            if (currentDev != null) {
                developerCombo.addItem(currentDev);
                developerCombo.setEnabled(false); // 锁定
            }
        }

        schoolField = new JTextField(20);
        majorField = new JTextField(20);

        String[] degrees = {"专科", "本科", "硕士", "博士", "其他"};
        degreeCombo = new JComboBox<>(degrees);
        degreeCombo.setEditable(true);

        startDateField = new JTextField(10);
        endDateField = new JTextField(10);

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

        // 学校
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("学校:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(schoolField, gbc);

        // 专业
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("专业:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(majorField, gbc);

        // 学历
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("学历:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(degreeCombo, gbc);

        // 日期面板
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.add(new JLabel("入学:"));
        datePanel.add(startDateField);
        datePanel.add(new JLabel("毕业:"));
        datePanel.add(endDateField);
        datePanel.add(new JLabel("(yyyy-MM-dd)"));

        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("时间:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(datePanel, gbc);

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
            if (dev.getDeveloperId() == education.getDeveloperId()) {
                developerCombo.setSelectedIndex(i);
                break;
            }
        }

        schoolField.setText(education.getSchool());
        majorField.setText(education.getMajor());
        degreeCombo.setSelectedItem(education.getDegree());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (education.getStartDate() != null) {
            startDateField.setText(education.getStartDate().format(formatter));
        }
        if (education.getEndDate() != null) {
            endDateField.setText(education.getEndDate().format(formatter));
        }
    }

    private void save() {
        // 验证输入
        if (schoolField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "学校不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (majorField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "专业不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Developer selectedDev = (Developer) developerCombo.getSelectedItem();
        if (selectedDev == null || selectedDev.getDeveloperId() == 0) {
            JOptionPane.showMessageDialog(this, "请选择开发者", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isEdit) {
            education = new EducationRecord();
        }

        education.setDeveloperId(selectedDev.getDeveloperId());
        education.setSchool(schoolField.getText().trim());
        education.setMajor(majorField.getText().trim());
        education.setDegree((String) degreeCombo.getSelectedItem());

        // 解析日期
        try {
            if (!startDateField.getText().trim().isEmpty()) {
                education.setStartDate(LocalDate.parse(startDateField.getText().trim()));
            }
            if (!endDateField.getText().trim().isEmpty()) {
                education.setEndDate(LocalDate.parse(endDateField.getText().trim()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "日期格式错误，请使用 yyyy-MM-dd 格式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success;
        if (isEdit) {
            success = educationDAO.updateEducationRecord(education);
        } else {
            success = educationDAO.addEducationRecord(education);
        }

        if (success) {
            dispose();
        }
    }
}