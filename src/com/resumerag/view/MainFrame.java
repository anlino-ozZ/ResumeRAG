package com.resumerag.view;

import com.resumerag.dao.*;
import com.resumerag.model.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 系统主界面
 */
public class MainFrame extends JFrame {

    // 当前登录用户
    private User currentUser;

    // 菜单栏组件
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu viewMenu;
    private JMenu helpMenu;
    private JMenuItem logoutItem;
    private JMenuItem exitItem;

    // 主面板 - 使用卡片布局实现功能切换
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // 各个功能面板
    private JPanel dashboardPanel;      // 仪表盘面板
    private JPanel developerPanel;      // 开发者管理面板
    private JPanel searchPanel;         // 检索面板
    private JPanel skillPanel;          // 技能管理面板
    private JPanel projectPanel;        // 项目管理面板
    private JPanel educationPanel;       // 教育管理面板
    private JPanel logPanel;            // 日志统计面板
    private JPanel exportPanel;         // 导出记录面板

    // 工具栏按钮
    private JToolBar toolBar;
    private JButton dashboardBtn;
    private JButton searchBtn;
    private JButton developerBtn;
    private JButton skillBtn;
    private JButton projectBtn;
    private JButton logoutBtn;

    // DAO对象
    private DeveloperDAO developerDAO;
    private SkillDAO skillDAO;
    private ProjectExperienceDAO projectDAO;
    private EducationRecordDAO educationDAO;
    private SearchLogDAO searchLogDAO;
    private ExportRecordDAO exportRecordDAO;

    // 表格模型
    private DefaultTableModel developerTableModel;
    private JTable developerTable;

    private DefaultTableModel searchResultTableModel;
    private JTable searchResultTable;

    public MainFrame(User user) {
        this.currentUser = user;
        this.developerDAO = new DeveloperDAO();
        this.skillDAO = new SkillDAO();
        this.projectDAO = new ProjectExperienceDAO();
        this.educationDAO = new EducationRecordDAO();
        this.searchLogDAO = new SearchLogDAO();
        this.exportRecordDAO = new ExportRecordDAO();

        initComponents();
        initLayout();
        initListeners();

        // 显示仪表盘
        showDashboard();
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        setTitle("简历RAG管理系统 - 当前用户: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // 创建菜单栏
        menuBar = new JMenuBar();
        fileMenu = new JMenu("文件");
        viewMenu = new JMenu("视图");
        helpMenu = new JMenu("帮助");

        logoutItem = new JMenuItem("注销");
        exitItem = new JMenuItem("退出");

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // 创建工具栏
        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        dashboardBtn = new JButton("仪表盘");
        searchBtn = new JButton("开发者检索");
        developerBtn = new JButton("开发者管理");
        skillBtn = new JButton("技能管理");
        projectBtn = new JButton("项目管理");
        logoutBtn = new JButton("注销");

        toolBar.add(dashboardBtn);
        toolBar.addSeparator();
        toolBar.add(searchBtn);
        toolBar.addSeparator();
        toolBar.add(developerBtn);
        toolBar.addSeparator();
        toolBar.add(skillBtn);
        toolBar.addSeparator();
        toolBar.add(projectBtn);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(logoutBtn);

        add(toolBar, BorderLayout.NORTH);

        // 主面板使用卡片布局
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel, BorderLayout.CENTER);

        // 创建各个功能面板
        createDashboardPanel();
        createSearchPanel();
        createDeveloperPanel();
        createSkillPanel();
        createProjectPanel();
        createEducationPanel();
        createLogPanel();
        createExportPanel();
    }

    /**
     * 初始化布局
     */
    private void initLayout() {
        // 将各个面板添加到卡片布局
        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(searchPanel, "search");
        mainPanel.add(developerPanel, "developer");
        mainPanel.add(skillPanel, "skill");
        mainPanel.add(projectPanel, "project");
        mainPanel.add(educationPanel, "education");
        mainPanel.add(logPanel, "log");
        mainPanel.add(exportPanel, "export");
    }

    /**
     * 初始化监听器
     */
    private void initListeners() {
        // 工具栏按钮事件
        dashboardBtn.addActionListener(e -> showDashboard());
        searchBtn.addActionListener(e -> showSearch());
        developerBtn.addActionListener(e -> showDeveloper());
        skillBtn.addActionListener(e -> showSkill());
        projectBtn.addActionListener(e -> showProject());
        logoutBtn.addActionListener(e -> logout());

        // 菜单项事件
        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> System.exit(0));
    }

    /**
     * 创建仪表盘面板
     */
    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout(10, 10));
        dashboardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 顶部标题
        JLabel titleLabel = new JLabel("系统仪表盘", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        dashboardPanel.add(titleLabel, BorderLayout.NORTH);

        // 中间统计面板
        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        // 统计卡片
        statsPanel.add(createStatCard("开发者总数", String.valueOf(developerDAO.getDeveloperCount()), "👥"));
        statsPanel.add(createStatCard("技能标签数", String.valueOf(skillDAO.getSkillCount()), "🏷️"));
        statsPanel.add(createStatCard("项目总数", String.valueOf(projectDAO.getProjectCount()), "📊"));
        statsPanel.add(createStatCard("教育记录数", String.valueOf(educationDAO.getEducationRecordCount()), "🎓"));

        statsPanel.add(createStatCard("今日搜索", "0", "🔍"));
        statsPanel.add(createStatCard("今日导出", "0", "📥"));
        statsPanel.add(createStatCard("平均结果", "0", "📈"));
        statsPanel.add(createStatCard("成功率", "0%", "✅"));

        dashboardPanel.add(statsPanel, BorderLayout.CENTER);

        // 底部最近活动
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBorder(new TitledBorder("最近活动"));

        JTextArea recentArea = new JTextArea(5, 50);
        recentArea.setEditable(false);
        recentArea.setText("暂无最近活动记录");
        recentPanel.add(new JScrollPane(recentArea), BorderLayout.CENTER);

        dashboardPanel.add(recentPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建统计卡片
     */
    private JPanel createStatCard(String title, String value, String icon) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(245, 245, 245));

        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        valueLabel.setForeground(new Color(0, 102, 204));

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(titleLabel, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);

        return card;
    }

    /**
     * 创建检索面板
     */
    private void createSearchPanel() {
        searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 搜索条件面板
        JPanel searchConditionPanel = new JPanel(new GridBagLayout());
        searchConditionPanel.setBorder(new TitledBorder("搜索条件"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 技能选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchConditionPanel.add(new JLabel("技能标签:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        JCheckBox javaCheck = new JCheckBox("Java");
        JCheckBox springCheck = new JCheckBox("Spring Boot");
        JCheckBox mysqlCheck = new JCheckBox("MySQL");
        JCheckBox pythonCheck = new JCheckBox("Python");
        JPanel skillPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        skillPanel.add(javaCheck);
        skillPanel.add(springCheck);
        skillPanel.add(mysqlCheck);
        skillPanel.add(pythonCheck);
        searchConditionPanel.add(skillPanel, gbc);

        // 经验年限
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        searchConditionPanel.add(new JLabel("最低经验:"), gbc);

        gbc.gridx = 1;
        JSpinner expSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
        searchConditionPanel.add(expSpinner, gbc);

        // 关键词
        gbc.gridx = 2;
        searchConditionPanel.add(new JLabel("关键词:"), gbc);

        gbc.gridx = 3;
        JTextField keywordField = new JTextField(20);
        searchConditionPanel.add(keywordField, gbc);

        // 搜索按钮
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton searchBtn = new JButton("🔍 搜索开发者");
        searchBtn.setPreferredSize(new Dimension(150, 30));
        searchConditionPanel.add(searchBtn, gbc);

        searchPanel.add(searchConditionPanel, BorderLayout.NORTH);

        // 搜索结果表格
        String[] columns = {"ID", "姓名", "经验(年)", "电话", "邮箱", "技能数量", "操作"};
        searchResultTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 只有操作列可编辑（按钮）
            }
        };
        searchResultTable = new JTable(searchResultTableModel);

        // 添加查看详情按钮
        searchResultTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        searchResultTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(searchResultTable);
        scrollPane.setBorder(new TitledBorder("搜索结果"));
        searchPanel.add(scrollPane, BorderLayout.CENTER);

        // 分页面板
        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pagePanel.add(new JLabel("共 0 条记录"));
        pagePanel.add(new JButton("上一页"));
        pagePanel.add(new JLabel("第 1/1 页"));
        pagePanel.add(new JButton("下一页"));
        searchPanel.add(pagePanel, BorderLayout.SOUTH);

        // 搜索按钮事件
        searchBtn.addActionListener(e -> {
            // 构建技能ID字符串
            StringBuilder skillIds = new StringBuilder();
            // TODO: 获取选中的技能ID

            // 执行搜索
            List<Developer> results = developerDAO.searchDevelopersBySkills(
                    skillIds.toString(),
                    (Integer) expSpinner.getValue(),
                    keywordField.getText(),
                    1,
                    10
            );

            // 更新表格
            searchResultTableModel.setRowCount(0);
            for (Developer dev : results) {
                searchResultTableModel.addRow(new Object[]{
                        dev.getDeveloperId(),
                        dev.getName(),
                        dev.getYearsOfExperience(),
                        dev.getPhone(),
                        dev.getEmail(),
                        dev.getSkills() != null ? dev.getSkills().size() : 0,
                        "查看详情"
                });
            }

            // 记录搜索日志
            SearchLog log = new SearchLog();
            log.setSearchKeywords(keywordField.getText());
            log.setSkillIds(skillIds.toString());
            log.setResultCount(results.size());
            log.setUserId(currentUser.getUserId());
            searchLogDAO.addSearchLog(log);
        });
    }

    /**
     * 创建开发者管理面板
     */
    private void createDeveloperPanel() {
        developerPanel = new JPanel(new BorderLayout(10, 10));
        developerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("新增开发者");
        JButton refreshBtn = new JButton("刷新");
        JButton deleteBtn = new JButton("删除选中");

        toolPanel.add(addBtn);
        toolPanel.add(refreshBtn);
        toolPanel.add(deleteBtn);

        developerPanel.add(toolPanel, BorderLayout.NORTH);

        // 开发者表格
        String[] columns = {"ID", "姓名", "电话", "邮箱", "经验(年)", "自我评价", "操作"};
        developerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // 操作列
            }
        };
        developerTable = new JTable(developerTableModel);

        JScrollPane scrollPane = new JScrollPane(developerTable);
        scrollPane.setBorder(new TitledBorder("开发者列表"));
        developerPanel.add(scrollPane, BorderLayout.CENTER);

        // 刷新按钮事件
        refreshBtn.addActionListener(e -> refreshDeveloperTable());

        // 新增按钮事件
        addBtn.addActionListener(e -> {
            DeveloperDialog dialog = new DeveloperDialog(this, "新增开发者", null, developerDAO, skillDAO);
            dialog.setVisible(true);
            refreshDeveloperTable();
        });

        // 初始加载数据
        refreshDeveloperTable();
    }

    /**
     * 刷新开发者表格
     */
    private void refreshDeveloperTable() {
        developerTableModel.setRowCount(0);
        List<Developer> developers = developerDAO.getAllDevelopers(1, 100);
        for (Developer dev : developers) {
            developerTableModel.addRow(new Object[]{
                    dev.getDeveloperId(),
                    dev.getName(),
                    dev.getPhone(),
                    dev.getEmail(),
                    dev.getYearsOfExperience(),
                    truncate(dev.getSelfEvaluation(), 20),
                    "编辑/技能"
            });
        }
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length) + "...";
    }

    /**
     * 创建技能管理面板
     */
    private void createSkillPanel() {
        skillPanel = new JPanel(new BorderLayout(10, 10));
        skillPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("新增技能");
        JButton refreshBtn = new JButton("刷新");
        JButton deleteBtn = new JButton("删除选中");

        JLabel searchLabel = new JLabel("搜索:");
        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("搜索");

        toolPanel.add(addBtn);
        toolPanel.add(refreshBtn);
        toolPanel.add(deleteBtn);
        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
        toolPanel.add(searchLabel);
        toolPanel.add(searchField);
        toolPanel.add(searchBtn);

        skillPanel.add(toolPanel, BorderLayout.NORTH);

        // 技能表格
        String[] columns = {"ID", "技能名称", "分类", "使用人数", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("技能列表"));
        skillPanel.add(scrollPane, BorderLayout.CENTER);

        // 刷新按钮事件
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<Skill> skills = skillDAO.getAllSkills();
            for (Skill s : skills) {
                model.addRow(new Object[]{
                        s.getSkillId(),
                        s.getSkillName(),
                        s.getCategory(),
                        0, // 使用人数 TODO: 从DeveloperSkills统计
                        "编辑/删除"
                });
            }
        });

        // 初始加载
        refreshBtn.getActionListeners()[0].actionPerformed(null);
    }

    /**
     * 创建项目管理面板
     */
    private void createProjectPanel() {
        projectPanel = new JPanel(new BorderLayout(10, 10));
        projectPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        projectPanel.add(new JLabel("项目管理面板 - 开发中", JLabel.CENTER), BorderLayout.CENTER);
    }

    /**
     * 创建教育管理面板
     */
    private void createEducationPanel() {
        educationPanel = new JPanel(new BorderLayout(10, 10));
        educationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        educationPanel.add(new JLabel("教育管理面板 - 开发中", JLabel.CENTER), BorderLayout.CENTER);
    }

    /**
     * 创建日志统计面板
     */
    private void createLogPanel() {
        logPanel = new JPanel(new BorderLayout(10, 10));
        logPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        logPanel.add(new JLabel("日志统计面板 - 开发中", JLabel.CENTER), BorderLayout.CENTER);
    }

    /**
     * 创建导出记录面板
     */
    private void createExportPanel() {
        exportPanel = new JPanel(new BorderLayout(10, 10));
        exportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        exportPanel.add(new JLabel("导出记录面板 - 开发中", JLabel.CENTER), BorderLayout.CENTER);
    }

    /**
     * 显示仪表盘
     */
    private void showDashboard() {
        cardLayout.show(mainPanel, "dashboard");
        setTitle("简历RAG管理系统 - 仪表盘 - " + currentUser.getUsername());
    }

    /**
     * 显示检索面板
     */
    private void showSearch() {
        cardLayout.show(mainPanel, "search");
        setTitle("简历RAG管理系统 - 开发者检索 - " + currentUser.getUsername());
    }

    /**
     * 显示开发者管理面板
     */
    private void showDeveloper() {
        cardLayout.show(mainPanel, "developer");
        setTitle("简历RAG管理系统 - 开发者管理 - " + currentUser.getUsername());
    }

    /**
     * 显示技能管理面板
     */
    private void showSkill() {
        cardLayout.show(mainPanel, "skill");
        setTitle("简历RAG管理系统 - 技能管理 - " + currentUser.getUsername());
    }

    /**
     * 显示项目管理面板
     */
    private void showProject() {
        cardLayout.show(mainPanel, "project");
        setTitle("简历RAG管理系统 - 项目管理 - " + currentUser.getUsername());
    }

    /**
     * 注销
     */
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要注销吗？",
                "注销确认",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        }
    }

    /**
     * 按钮渲染器
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    /**
     * 按钮编辑器
     */
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = value == null ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // 获取当前行对应的开发者ID
                int row = developerTable.getSelectedRow();
                if (row >= 0) {
                    int developerId = (int) developerTableModel.getValueAt(row, 0);
                    String name = (String) developerTableModel.getValueAt(row, 1);

                    // 打开编辑对话框
                    Developer developer = developerDAO.getDeveloperById(developerId);
                    DeveloperDialog dialog = new DeveloperDialog(
                            MainFrame.this,
                            "编辑开发者 - " + name,
                            developer,
                            developerDAO,
                            skillDAO
                    );
                    dialog.setVisible(true);
                    refreshDeveloperTable();
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        // 测试用：直接创建一个admin用户登录
        User testUser = new User();
        testUser.setUserId(1);
        testUser.setUsername("admin");
        testUser.setRole("admin");

        SwingUtilities.invokeLater(() -> {
            new MainFrame(testUser).setVisible(true);
        });
    }
}