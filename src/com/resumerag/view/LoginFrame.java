package com.resumerag.view;

import com.resumerag.dao.UserDAO;
import com.resumerag.model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
/**
 * 登录窗口
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JButton cancelBtn;

    private UserDAO userDAO;

    public LoginFrame() {
        this.userDAO = new UserDAO();

        initComponents();
        initLayout();
        initListeners();
    }

    private void initComponents() {
        setTitle("简历RAG管理系统 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginBtn = new JButton("登录");
        cancelBtn = new JButton("取消");
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 标题
        JLabel titleLabel = new JLabel("简历RAG管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("用户名:"), gbc);

        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("密码:"), gbc);

        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(loginBtn);
        buttonPanel.add(cancelBtn);

        // 添加注册链接
        JPanel linkPanel = new JPanel(new FlowLayout());
        linkPanel.setBackground(Color.WHITE);
        JLabel registerLink = new JLabel("没有账号？点击注册");
        registerLink.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        registerLink.setForeground(new Color(0, 102, 204));
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkPanel.add(registerLink);

        // 注册链接点击事件
        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterFrame().setVisible(true);
                dispose(); // 关闭登录窗口
            }
        });

        // 组合面板
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(Color.WHITE);
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(linkPanel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void initListeners() {
        loginBtn.addActionListener(e -> login());
        cancelBtn.addActionListener(e -> System.exit(0));

        // 回车键登录
        passwordField.addActionListener(e -> login());
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userDAO.login(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "登录成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

            // 打开主窗口
            MainFrame mainFrame = new MainFrame(user);
            mainFrame.setVisible(true);

            // 关闭登录窗口
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "用户名或密码错误", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}