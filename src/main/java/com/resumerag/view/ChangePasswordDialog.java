package com.resumerag.view;

import com.resumerag.dao.UserDAO;
import com.resumerag.model.User;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private User currentUser;
    private UserDAO userDAO;

    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ChangePasswordDialog(Frame owner, User currentUser, UserDAO userDAO) {
        super(owner, "修改密码", true);
        this.currentUser = currentUser;
        this.userDAO = userDAO;

        initComponents();
        initLayout();
        initListeners();

        setSize(400, 250);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        oldPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("旧密码:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(oldPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("新密码:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("确认新密码:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> savePassword());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void initListeners() {

    }

    private void savePassword() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!userDAO.login(currentUser.getUsername(), oldPassword).getPassword().equals(currentUser.getPassword())) {
            JOptionPane.showMessageDialog(this, "旧密码不正确", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的新密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentUser.setPassword(newPassword);
        if (userDAO.updateUser(currentUser)) {
            JOptionPane.showMessageDialog(this, "密码修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "密码修改失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
