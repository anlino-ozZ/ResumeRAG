package com.resumerag.view;

import com.resumerag.dao.DeveloperDAO;
import com.resumerag.dao.UserDAO;
import com.resumerag.model.Developer;
import com.resumerag.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 注册界面 - 策略B：普通用户直接激活，管理员需要审核
 * 开发者：status='active'，可直接登录
 * 申请管理员：status='pending'，需要管理员审核
 */
public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<String> roleCombo;

    private JButton registerBtn;
    private JButton backToLoginBtn;

    private UserDAO userDAO;
    private DeveloperDAO developerDAO;

    public RegisterFrame() {
        this.userDAO = new UserDAO();
        this.developerDAO = new DeveloperDAO();

        initComponents();
        initLayout();
        initListeners();

        setTitle("简历RAG管理系统 - 注册");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        nameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);

        String[] roles = {"开发者", "申请成为管理员"};
        roleCombo = new JComboBox<>(roles);

        registerBtn = new JButton("注册");
        registerBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerBtn.setBackground(new Color(0, 102, 204));
        registerBtn.setForeground(Color.WHITE);

        backToLoginBtn = new JButton("返回登录");
        backToLoginBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);

        // 标题
        JLabel titleLabel = new JLabel("用户注册", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ===== 账号信息区域 =====
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel accountLabel = new JLabel("📝 账号信息");
        accountLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        accountLabel.setForeground(new Color(0, 102, 204));
        formPanel.add(accountLabel, gbc);

        // 用户名
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(createLabel("用户名:"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // 密码
        gbc.gridy = 2;
        gbc.gridx = 0;
        formPanel.add(createLabel("密码:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // 确认密码
        gbc.gridy = 3;
        gbc.gridx = 0;
        formPanel.add(createLabel("确认密码:"), gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        // 分隔线
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);

        // ===== 个人信息区域 =====
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel personalLabel = new JLabel("👤 个人信息");
        personalLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        personalLabel.setForeground(new Color(0, 102, 204));
        formPanel.add(personalLabel, gbc);

        // 姓名
        gbc.gridwidth = 1;
        gbc.gridy = 6;
        gbc.gridx = 0;
        formPanel.add(createLabel("姓名:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        // 电话
        gbc.gridy = 7;
        gbc.gridx = 0;
        formPanel.add(createLabel("电话:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        // 邮箱
        gbc.gridy = 8;
        gbc.gridx = 0;
        formPanel.add(createLabel("邮箱:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // 角色
        gbc.gridy = 9;
        gbc.gridx = 0;
        formPanel.add(createLabel("角色:"), gbc);
        gbc.gridx = 1;
        formPanel.add(roleCombo, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // ===== 按钮面板 =====
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        buttonPanel.add(registerBtn);

        JPanel linkPanel = new JPanel(new FlowLayout());
        linkPanel.setBackground(Color.WHITE);
        JLabel loginLink = new JLabel("已有账号？立即登录");
        loginLink.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        loginLink.setForeground(new Color(0, 102, 204));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkPanel.add(loginLink);

        // 添加点击事件
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                backToLogin();
            }
        });

        buttonPanel.add(linkPanel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private void initListeners() {
        registerBtn.addActionListener(e -> register());
    }

    /**
     * 注册逻辑 - 策略B：普通用户直接激活，管理员需要审核
     * 开发者：role='developer', status='active'（直接可用）
     * 申请管理员：role='pending_admin', status='pending'（需要审核）
     */
    private void register() {
        // 1. 获取表单数据
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        // 2. 验证输入
        if (!validateInput(username, password, confirmPassword, name)) {
            return;
        }

        // 3. 检查用户名是否已存在
        if (userDAO.isUsernameExist(username)) {
            JOptionPane.showMessageDialog(this,
                    "用户名已存在，请换一个",
                    "注册失败",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ===== 策略B：根据角色设置不同的状态 =====
        String role;
        String status;

        if (roleCombo.getSelectedIndex() == 0) {
            // 选择"开发者"：直接激活
            role = "developer";
            status = "active";  // 直接可用
        } else {
            // 选择"申请成为管理员"：需要审核
            role = "pending_admin";  // 特殊角色，表示待审核的管理员
            status = "pending";      // 待审核状态
        }

        // 步骤1: 创建用户对象并设置状态
        User newUser = new User(username, password, role);
        newUser.setStatus(status);  // 设置状态

        boolean userAdded = userDAO.addUser(newUser);
        System.out.println("[注册] 用户创建结果: " + userAdded + ", userId=" + newUser.getUserId());

        if (!userAdded) {
            JOptionPane.showMessageDialog(this,
                    "账号创建失败，请稍后重试",
                    "注册失败",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 步骤2: 获取生成的user_id
        int userId = newUser.getUserId();
        System.out.println("[注册] 生成的用户ID: " + userId);

        // 步骤3: 创建开发者信息（所有用户都需要开发者记录）
        Developer newDev = new Developer(name, phone, email, 0);
        newDev.setUserId(userId);
        newDev.setSelfEvaluation("新注册用户");

        boolean devAdded = developerDAO.addDeveloper(newDev);
        System.out.println("[注册] 开发者记录创建结果: " + devAdded + ", developerId=" + newDev.getDeveloperId());

        if (devAdded) {
            // 注册成功 - 根据角色显示不同提示
            String successMessage = "✅ 注册成功！\n\n";
            successMessage += "用户名：" + username + "\n";
            successMessage += "姓名：" + name + "\n";

            if ("pending".equals(status)) {
                // 管理员申请
                successMessage += "\n⏳ 您的管理员申请已提交，请等待管理员审核。\n";
                successMessage += "审核通过后您将获得管理员权限。";
            } else {
                // 普通开发者
                successMessage += "\n🎉 您现在可以直接使用账号登录系统了。";
            }

            JOptionPane.showMessageDialog(this,
                    successMessage,
                    "注册成功",
                    JOptionPane.INFORMATION_MESSAGE);

            // 返回登录界面
            backToLogin();

        } else {
            // 如果开发者信息创建失败，需要回滚删除刚创建的用户
            userDAO.deleteUser(userId);

            JOptionPane.showMessageDialog(this,
                    "个人信息创建失败，请稍后重试",
                    "注册失败",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 验证输入
     */
    private boolean validateInput(String username, String password, String confirmPassword, String name) {
        // 验证非空
        if (username.isEmpty()) {
            showError("用户名不能为空");
            return false;
        }

        if (password.isEmpty()) {
            showError("密码不能为空");
            return false;
        }

        if (name.isEmpty()) {
            showError("姓名不能为空");
            return false;
        }

        // 验证密码长度
        if (password.length() < 6) {
            showError("密码长度不能少于6位");
            return false;
        }

        // 验证密码一致性
        if (!password.equals(confirmPassword)) {
            showError("两次输入的密码不一致");
            return false;
        }

        // 验证用户名格式（只允许字母、数字、下划线）
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            showError("用户名只能包含字母、数字和下划线");
            return false;
        }

        // 验证邮箱格式（如果填写了）
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("邮箱格式不正确");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "验证失败",
                JOptionPane.ERROR_MESSAGE);
    }

    private void backToLogin() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new RegisterFrame().setVisible(true);
        });
    }
}