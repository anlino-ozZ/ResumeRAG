package com.resumerag.view;

import com.resumerag.dao.SkillDAO;
import com.resumerag.model.Skill;
import com.resumerag.model.User;

import javax.swing.*;
import java.awt.*;

public class SkillDialog extends JDialog {
    private Skill skill;
    private SkillDAO skillDAO;
    private User currentUser;
    private boolean isEdit;

    private JTextField nameField;
    private JTextField categoryField;
    private JButton saveBtn;

    public SkillDialog(Frame owner, String title, Skill skill, SkillDAO skillDAO, User currentUser) {
        super(owner, title, true);
        this.skill = skill;
        this.skillDAO = skillDAO;
        this.currentUser = currentUser;
        this.isEdit = (skill != null);

        if (!isEdit) {
            this.skill = new Skill();
        }

        initComponents();
        initLayout();
        
        // 开发者只能查看技能详情，不能修改
        if ("developer".equalsIgnoreCase(currentUser.getRole())) {
            disableEditing();
        }

        setSize(300, 200);
        setLocationRelativeTo(owner);
    }

    private void disableEditing() {
        nameField.setEditable(false);
        categoryField.setEditable(false);
        if (saveBtn != null) {
            saveBtn.setVisible(false);
        }
        setTitle("技能详情");
    }

    private void initComponents() {
        nameField = new JTextField(20);
        categoryField = new JTextField(20);

        if (isEdit) {
            nameField.setText(skill.getSkillName());
            categoryField.setText(skill.getCategory());
        }
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("技能名称:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("分类:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(categoryField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        add(mainPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void save() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "技能名称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        skill.setSkillName(name);
        skill.setCategory(category);

        boolean success;
        if (isEdit) {
            success = skillDAO.updateSkill(skill);
        } else {
            success = skillDAO.addSkill(skill);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "保存失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
