package com.resumerag.view;

import com.resumerag.dao.*;
import com.resumerag.model.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private JButton educationBtn;
    private JButton logBtn;
    private JButton exportBtn;
    private JButton logoutBtn;

    // DAO对象
    private UserDAO userDAO;
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

    private DefaultTableModel projectTableModel;
    private JTable projectTable;

    private DefaultTableModel educationTableModel;
    private JTable educationTable;

    private DefaultTableModel skillTableModel;
    private DefaultTableModel exportTableModel;

    // 分页相关字段
    private int developerCurrentPage = 1;
    private int developerPageSize = 15;
    private int developerTotalPages = 1;
    private JLabel developerPageLabel;
    private JButton developerPrevBtn;
    private JButton developerNextBtn;

    private int projectCurrentPage = 1;
    private int projectPageSize = 15;
    private int projectTotalPages = 1;
    private JLabel projectPageLabel;
    private JButton projectPrevBtn;
    private JButton projectNextBtn;

    private int educationCurrentPage = 1;
    private int educationPageSize = 15;
    private int educationTotalPages = 1;
    private JLabel educationPageLabel;
    private JButton educationPrevBtn;
    private JButton educationNextBtn;

    private LoadingGlassPane loadingGlassPane;

    private Developer currentDeveloper; // 只有当角色是 developer 时才不为 null

    public MainFrame(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        this.developerDAO = new DeveloperDAO();
        this.skillDAO = new SkillDAO();
        this.projectDAO = new ProjectExperienceDAO();
        this.educationDAO = new EducationRecordDAO();
        this.searchLogDAO = new SearchLogDAO();
        this.exportRecordDAO = new ExportRecordDAO();

        // 如果是开发者，获取其对应的开发者信息
        if ("developer".equalsIgnoreCase(currentUser.getRole())) {
            this.currentDeveloper = developerDAO.getDeveloperByUserId(currentUser.getUserId());
            
            // 如果数据库中没有关联记录，尝试根据姓名创建一个关联
            if (this.currentDeveloper == null) {
                this.currentDeveloper = new Developer();
                this.currentDeveloper.setUserId(currentUser.getUserId());
                this.currentDeveloper.setName(currentUser.getUsername());
                this.currentDeveloper.setPhone("");
                this.currentDeveloper.setEmail("");
                this.currentDeveloper.setYearsOfExperience(0);
                this.currentDeveloper.setSelfEvaluation("新注册开发者");
                
                if (developerDAO.addDeveloper(this.currentDeveloper)) {
                    System.out.println("已为用户 " + currentUser.getUsername() + " 自动创建开发者档案");
                }
            }
        }

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
        educationBtn = new JButton("教育管理");
        logBtn = new JButton("日志统计");
        exportBtn = new JButton("导出记录");
        logoutBtn = new JButton("注销");

        toolBar.add(dashboardBtn);
        toolBar.addSeparator();

        // 根据角色显示功能按钮
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            toolBar.add(searchBtn);
            toolBar.addSeparator();
            toolBar.add(developerBtn);
            toolBar.addSeparator();
            toolBar.add(skillBtn);
            toolBar.addSeparator();
            toolBar.add(projectBtn);
            toolBar.addSeparator();
            toolBar.add(educationBtn);
            toolBar.addSeparator();
            toolBar.add(logBtn);
            toolBar.addSeparator();
            toolBar.add(exportBtn);
        } else if ("developer".equalsIgnoreCase(currentUser.getRole())) {
            // 开发者不能进行开发者检索和查看日志统计
            toolBar.add(developerBtn);
            toolBar.addSeparator();
            toolBar.add(skillBtn);
            toolBar.addSeparator();
            toolBar.add(projectBtn);
            toolBar.addSeparator();
            toolBar.add(educationBtn);
            toolBar.addSeparator();
            toolBar.add(exportBtn);
        }

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
        educationBtn.addActionListener(e -> showEducation());
        logBtn.addActionListener(e -> showLog());
        exportBtn.addActionListener(e -> showExport());
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

        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            // 管理员显示全系统统计
            statsPanel.add(createStatCard("开发者总数", String.valueOf(developerDAO.getDeveloperCount()), "👥"));
            statsPanel.add(createStatCard("技能标签数", String.valueOf(skillDAO.getSkillCount()), "🏷️"));
            statsPanel.add(createStatCard("项目总数", String.valueOf(projectDAO.getProjectCount()), "📊"));
            statsPanel.add(createStatCard("教育记录数", String.valueOf(educationDAO.getEducationRecordCount()), "🎓"));

            statsPanel.add(createStatCard("今日搜索", "0", "🔍"));
            statsPanel.add(createStatCard("今日导出", "0", "📥"));
            statsPanel.add(createStatCard("平均结果", "0", "📈"));
            statsPanel.add(createStatCard("成功率", "0%", "✅"));
        } else {
            // 开发者只显示自己的统计
            int devId = currentDeveloper != null ? currentDeveloper.getDeveloperId() : 0;
            Developer fullDev = devId > 0 ? developerDAO.getDeveloperWithDetails(devId) : null;
            
            int skillCount = fullDev != null && fullDev.getSkills() != null ? fullDev.getSkills().size() : 0;
            int projectCount = fullDev != null && fullDev.getProjects() != null ? fullDev.getProjects().size() : 0;
            int eduCount = fullDev != null && fullDev.getEducationRecords() != null ? fullDev.getEducationRecords().size() : 0;
            int exportCount = devId > 0 ? exportRecordDAO.getExportRecordsByDeveloperId(devId, 1, 1).size() : 0; // 简单计数

            statsPanel.add(createStatCard("我的技能", String.valueOf(skillCount), "🏷️"));
            statsPanel.add(createStatCard("我的项目", String.valueOf(projectCount), "📊"));
            statsPanel.add(createStatCard("我的教育记录", String.valueOf(eduCount), "🎓"));
            statsPanel.add(createStatCard("我的导出记录", String.valueOf(exportCount), "📥"));
            
            // 填充空白卡片保持布局
            statsPanel.add(new JPanel());
            statsPanel.add(new JPanel());
            statsPanel.add(new JPanel());
            statsPanel.add(new JPanel());
        }

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
        searchResultTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public Object getCellEditorValue() {
                if (isPushed) {
                    int row = searchResultTable.convertRowIndexToModel(searchResultTable.getEditingRow());
                    if (row >= 0) {
                        int developerId = (int) searchResultTableModel.getValueAt(row, 0);
                        Developer developer = developerDAO.getDeveloperWithDetails(developerId);

                        if (developer != null) {
                            DeveloperDialog dialog = new DeveloperDialog(
                                    MainFrame.this,
                                    "开发者详情 - " + developer.getName(),
                                    developer,
                                    developerDAO,
                                    skillDAO,
                                    currentUser,
                                    exportRecordDAO
                            );
                            dialog.setVisible(true);
                        }
                    }
                }
                isPushed = false;
                return label;
            }
        });

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

        // 开发者角色不能新增或删除其他开发者
        if ("developer".equalsIgnoreCase(currentUser.getRole())) {
            addBtn.setVisible(false);
            deleteBtn.setVisible(false);
        }

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
        developerTable.setDefaultRenderer(Object.class, new AlternatingRowColorRenderer());

        // 设置按钮渲染器和编辑器
        developerTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        developerTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public Object getCellEditorValue() {
                if (isPushed) {
                    int row = developerTable.convertRowIndexToModel(developerTable.getEditingRow());
                    if (row >= 0) {
                        int developerId = (int) developerTableModel.getValueAt(row, 0);
                        Developer developer = developerDAO.getDeveloperWithDetails(developerId);

                        if (developer != null) {
                            DeveloperDialog dialog = new DeveloperDialog(
                                    MainFrame.this,
                                    "编辑开发者 - " + developer.getName(),
                                    developer,
                                    developerDAO,
                                    skillDAO,
                                    currentUser,
                                    exportRecordDAO
                            );
                            dialog.setVisible(true);
                            refreshDeveloperTable();
                        }
                    }
                }
                isPushed = false;
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(developerTable);
        scrollPane.setBorder(new TitledBorder("开发者列表"));
        developerPanel.add(scrollPane, BorderLayout.CENTER);

        // 刷新按钮事件
        refreshBtn.addActionListener(e -> refreshDeveloperTable());

        // 新增按钮事件
        addBtn.addActionListener(e -> {
            DeveloperDialog dialog = new DeveloperDialog(
                    this,
                    "新增开发者",
                    null,
                    developerDAO,
                    skillDAO,
                    currentUser,
                    exportRecordDAO
            );
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
        List<Developer> developers;
        
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            developers = developerDAO.getAllDevelopers(1, 100);
        } else {
            developers = new ArrayList<>();
            if (currentDeveloper != null) {
                developers.add(currentDeveloper);
            }
        }
        
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

    private void refreshSkillTable() {
        skillTableModel.setRowCount(0);
        List<Skill> skills = skillDAO.getAllSkills();
        for (Skill s : skills) {
            skillTableModel.addRow(new Object[]{
                    s.getSkillId(),
                    s.getSkillName(),
                    s.getCategory(),
                    0, // 使用人数 TODO: 从DeveloperSkills统计
                    "编辑/删除"
            });
        }
    }

    private void refreshProjectTable() {
        projectTableModel.setRowCount(0);
        List<ProjectExperience> projects;
        
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            projects = projectDAO.getAllProjects(1, 100);
        } else {
            projects = currentDeveloper != null ? projectDAO.getProjectsByDeveloperId(currentDeveloper.getDeveloperId(), 1, 100) : new ArrayList<>();
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (ProjectExperience p : projects) {
            Developer dev = developerDAO.getDeveloperById(p.getDeveloperId());
            projectTableModel.addRow(new Object[]{
                    p.getProjectId(),
                    p.getProjectName(),
                    dev != null ? dev.getName() : "未知",
                    p.getRole(),
                    p.getTechStack(),
                    p.getStartDate() != null ? p.getStartDate().format(formatter) : "",
                    p.getEndDate() != null ? p.getEndDate().format(formatter) : "",
                    "编辑/删除"
            });
        }
    }

    private void refreshEducationTable() {
        educationTableModel.setRowCount(0);
        List<EducationRecord> records;
        
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            records = educationDAO.getAllEducationRecords(1, 100);
        } else {
            records = currentDeveloper != null ? educationDAO.getEducationRecordsByDeveloperId(currentDeveloper.getDeveloperId(), 1, 100) : new ArrayList<>();
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (EducationRecord er : records) {
            Developer dev = developerDAO.getDeveloperById(er.getDeveloperId());
            educationTableModel.addRow(new Object[]{
                    er.getEducationId(),
                    dev != null ? dev.getName() : "未知",
                    er.getSchool(),
                    er.getMajor(),
                    er.getDegree(),
                    er.getStartDate() != null ? er.getStartDate().format(formatter) : "",
                    er.getEndDate() != null ? er.getEndDate().format(formatter) : "",
                    "编辑/删除"
            });
        }
    }

    private void refreshExportTable() {
        exportTableModel.setRowCount(0);
        List<ExportRecord> records;
        
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            records = exportRecordDAO.getAllExportRecords(1, 100);
        } else {
            records = currentDeveloper != null ? exportRecordDAO.getExportRecordsByDeveloperId(currentDeveloper.getDeveloperId(), 1, 100) : new ArrayList<>();
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (ExportRecord er : records) {
            Developer dev = developerDAO.getDeveloperById(er.getDeveloperId());
            User user = er.getUserId() != null ? userDAO.getUserById(er.getUserId()) : null;

            exportTableModel.addRow(new Object[]{
                    er.getExportId(),
                    er.getExportTime() != null ? er.getExportTime().format(formatter) : "",
                    dev != null ? dev.getName() : "未知",
                    user != null ? user.getUsername() : "未知",
                    er.getFileName(),
                    "删除"
            });
        }
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
        
        // 开发者只能查看技能列表
        if ("developer".equalsIgnoreCase(currentUser.getRole())) {
            addBtn.setVisible(false);
            deleteBtn.setVisible(false);
        }

        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
        toolPanel.add(searchLabel);
        toolPanel.add(searchField);
        toolPanel.add(searchBtn);

        skillPanel.add(toolPanel, BorderLayout.NORTH);

        // 技能表格
        String[] columns = {"ID", "技能名称", "分类", "使用人数", "操作"};
        skillTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        JTable table = new JTable(skillTableModel);
        table.setDefaultRenderer(Object.class, new AlternatingRowColorRenderer());

        // 设置按钮渲染器和编辑器
        table.getColumn("操作").setCellRenderer(new ButtonRenderer());
        table.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public Object getCellEditorValue() {
                if (isPushed) {
                    int row = table.convertRowIndexToModel(table.getEditingRow());
                    if (row >= 0) {
                        int skillId = (int) skillTableModel.getValueAt(row, 0);
                        Skill skill = skillDAO.getSkillById(skillId);

                        if (skill != null) {
                            SkillDialog dialog = new SkillDialog(
                                    MainFrame.this,
                                    "编辑技能",
                                    skill,
                                    skillDAO,
                                    currentUser
                            );
                            dialog.setVisible(true);
                            refreshSkillTable();
                        }
                    }
                }
                isPushed = false;
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("技能列表"));
        skillPanel.add(scrollPane, BorderLayout.CENTER);

        // 刷新按钮事件
        refreshBtn.addActionListener(e -> refreshSkillTable());

        // 新增按钮事件
        addBtn.addActionListener(e -> {
            SkillDialog dialog = new SkillDialog(this, "新增技能", null, skillDAO, currentUser);
            dialog.setVisible(true);
            refreshSkillTable();
        });

        // 初始加载
        refreshSkillTable();
    }

    /**
     * 创建项目管理面板
     */
    private void createProjectPanel() {
        projectPanel = new JPanel(new BorderLayout(10, 10));
        projectPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("新增项目");
        JButton refreshBtn = new JButton("刷新");
        JButton deleteBtn = new JButton("删除选中");

        JLabel searchLabel = new JLabel("搜索项目:");
        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("搜索");

        JLabel filterLabel = new JLabel("按开发者:");
        JComboBox<Developer> developerCombo = new JComboBox<>();
        developerCombo.addItem(new Developer(0, "全部开发者")); // 自定义构造方法

        toolPanel.add(addBtn);
        toolPanel.add(refreshBtn);
        toolPanel.add(deleteBtn);
        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
        toolPanel.add(searchLabel);
        toolPanel.add(searchField);
        toolPanel.add(searchBtn);
        toolPanel.add(filterLabel);
        toolPanel.add(developerCombo);

        // 开发者只能管理自己的项目，隐藏开发者筛选
        if ("developer".equalsIgnoreCase(currentUser.getRole())) {
            filterLabel.setVisible(false);
            developerCombo.setVisible(false);
        }

        projectPanel.add(toolPanel, BorderLayout.NORTH);

        // 项目表格
        String[] columns = {"ID", "项目名称", "开发者", "角色", "技术栈", "开始时间", "结束时间", "操作"};
        projectTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        projectTable = new JTable(projectTableModel);
        projectTable.setDefaultRenderer(Object.class, new AlternatingRowColorRenderer());

        // 设置按钮渲染器和编辑器
        projectTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        projectTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public Object getCellEditorValue() {
                if (isPushed) {
                    int row = projectTable.convertRowIndexToModel(projectTable.getEditingRow());
                    if (row >= 0) {
                        int projectId = (int) projectTableModel.getValueAt(row, 0);
                        ProjectExperience project = projectDAO.getProjectById(projectId);

                        if (project != null) {
                            ProjectDialog dialog = new ProjectDialog(
                                    MainFrame.this,
                                    "编辑项目",
                                    project,
                                    projectDAO,
                                    developerDAO,
                                    currentUser
                            );
                            dialog.setVisible(true);
                            refreshProjectTable();
                        }
                    }
                }
                isPushed = false;
                return label;
            }
        });

        projectTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        projectTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        projectTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        projectTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        projectTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        projectTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        projectTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        projectTable.getColumnModel().getColumn(7).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(projectTable);
        scrollPane.setBorder(new TitledBorder("项目列表"));
        projectPanel.add(scrollPane, BorderLayout.CENTER);

        // 分页
        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pagePanel.add(new JLabel("共 0 条记录"));
        projectPrevBtn = new JButton("上一页");
        projectPageLabel = new JLabel("第 1/1 页");
        projectNextBtn = new JButton("下一页");
        pagePanel.add(projectPrevBtn);
        pagePanel.add(projectPageLabel);
        pagePanel.add(projectNextBtn);
        projectPanel.add(pagePanel, BorderLayout.SOUTH);

        // 加载开发者下拉框
        List<Developer> developers = developerDAO.getAllDevelopers(1, 100);
        for (Developer dev : developers) {
            developerCombo.addItem(dev);
        }

        // 刷新按钮事件
        refreshBtn.addActionListener(e -> refreshProjectTable());

        // 搜索按钮事件
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            refreshProjectTable(); // 简单起见，这里直接刷新，实际搜索应调用搜索方法
        });

        // 新增按钮事件
        addBtn.addActionListener(e -> {
            ProjectDialog dialog = new ProjectDialog(
                    MainFrame.this,
                    "新增项目",
                    null,
                    projectDAO,
                    developerDAO,
                    currentUser
            );
            dialog.setVisible(true);
            refreshProjectTable();
        });

        // 初始加载
        refreshProjectTable();
    }
    private void createEducationPanel() {
        educationPanel = new JPanel(new BorderLayout(10, 10));
        educationPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("新增教育记录");
        JButton refreshBtn = new JButton("刷新");
        JButton deleteBtn = new JButton("删除选中");

        JLabel searchLabel = new JLabel("搜索学校/专业:");
        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("搜索");

        JLabel filterLabel = new JLabel("学历:");
        JComboBox<String> degreeCombo = new JComboBox<>(new String[]{"全部", "专科", "本科", "硕士", "博士"});

        toolPanel.add(addBtn);
        toolPanel.add(refreshBtn);
        toolPanel.add(deleteBtn);
        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
        toolPanel.add(searchLabel);
        toolPanel.add(searchField);
        toolPanel.add(searchBtn);
        toolPanel.add(filterLabel);
        toolPanel.add(degreeCombo);

        educationPanel.add(toolPanel, BorderLayout.NORTH);

        // 教育记录表格
        String[] columns = {"ID", "开发者", "学校", "专业", "学历", "开始时间", "结束时间", "操作"};
        educationTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        educationTable = new JTable(educationTableModel);
        educationTable.setDefaultRenderer(Object.class, new AlternatingRowColorRenderer());

        // 设置按钮渲染器和编辑器
        educationTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        educationTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public Object getCellEditorValue() {
                if (isPushed) {
                    int row = educationTable.convertRowIndexToModel(educationTable.getEditingRow());
                    if (row >= 0) {
                        int educationId = (int) educationTableModel.getValueAt(row, 0);
                        EducationRecord education = educationDAO.getEducationRecordById(educationId);

                        if (education != null) {
                            EducationDialog dialog = new EducationDialog(
                                    MainFrame.this,
                                    "编辑教育记录",
                                    education,
                                    educationDAO,
                                    developerDAO,
                                    currentUser
                            );
                            dialog.setVisible(true);
                            refreshEducationTable();
                        }
                    }
                }
                isPushed = false;
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(educationTable);
        scrollPane.setBorder(new TitledBorder("教育记录列表"));
        educationPanel.add(scrollPane, BorderLayout.CENTER);

        // 分页
        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pagePanel.add(new JLabel("共 0 条记录"));
        educationPrevBtn = new JButton("上一页");
        educationPageLabel = new JLabel("第 1/1 页");
        educationNextBtn = new JButton("下一页");
        pagePanel.add(educationPrevBtn);
        pagePanel.add(educationPageLabel);
        pagePanel.add(educationNextBtn);
        educationPanel.add(pagePanel, BorderLayout.SOUTH);

        // 刷新按钮事件
        refreshBtn.addActionListener(e -> refreshEducationTable());

        // 搜索按钮事件
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            refreshEducationTable();
        });

        // 新增按钮事件
        addBtn.addActionListener(e -> {
            EducationDialog dialog = new EducationDialog(
                    MainFrame.this,
                    "新增教育记录",
                    null,
                    educationDAO,
                    developerDAO,
                    currentUser
            );
            dialog.setVisible(true);
            refreshEducationTable();
        });

        // 初始加载
        refreshEducationTable();
    }

    /**
     * 创建日志统计面板
     */
    private void createLogPanel() {
        logPanel = new JPanel(new BorderLayout(10, 10));
        logPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 统计卡片面板
        JPanel statsCardsPanel = new JPanel(new GridLayout(1, 4, 10, 10));

        // 搜索总数
        JPanel totalCard = createStatCard("总搜索次数", String.valueOf(searchLogDAO.getSearchLogCount()), "🔍");
        statsCardsPanel.add(totalCard);

        // 平均结果数
        double avgResult = searchLogDAO.getAverageResultCount();
        JPanel avgCard = createStatCard("平均结果数", String.format("%.1f", avgResult), "📊");
        statsCardsPanel.add(avgCard);

        // 搜索成功率
        double successRate = searchLogDAO.getSearchSuccessRate();
        JPanel rateCard = createStatCard("搜索成功率", String.format("%.1f%%", successRate), "✅");
        statsCardsPanel.add(rateCard);

        // 今日搜索
        JPanel todayCard = createStatCard("今日搜索", "0", "📅");
        statsCardsPanel.add(todayCard);

        logPanel.add(statsCardsPanel, BorderLayout.NORTH);

        // 标签页面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 热门搜索关键词
        JPanel hotKeywordsPanel = createHotKeywordsPanel();
        tabbedPane.addTab("热门搜索词", hotKeywordsPanel);

        // 搜索趋势
        JPanel trendPanel = createSearchTrendPanel();
        tabbedPane.addTab("搜索趋势", trendPanel);

        // 用户活跃度
        JPanel userActivityPanel = createUserActivityPanel();
        tabbedPane.addTab("用户活跃度", userActivityPanel);

        // 搜索日志列表
        JPanel logListPanel = createSearchLogListPanel();
        tabbedPane.addTab("日志详情", logListPanel);

        logPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * 创建热门关键词面板
     */
    private JPanel createHotKeywordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"排名", "搜索关键词", "搜索次数", "占比"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        List<Object[]> topKeywords = searchLogDAO.getTopSearchKeywords(10);
        int total = searchLogDAO.getSearchLogCount();
        int rank = 1;

        for (Object[] row : topKeywords) {
            String keyword = (String) row[0];
            int count = (Integer) row[1];
            double percentage = total > 0 ? (count * 100.0 / total) : 0;

            model.addRow(new Object[]{
                    rank++,
                    keyword,
                    count,
                    String.format("%.1f%%", percentage)
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建搜索趋势面板
     */
    private JPanel createSearchTrendPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"日期", "搜索次数"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        List<Object[]> dailyStats = searchLogDAO.getDailySearchCount(30);
        for (Object[] row : dailyStats) {
            model.addRow(new Object[]{
                    row[0],
                    row[1]
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建用户活跃度面板
     */
    private JPanel createUserActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"用户名", "搜索次数", "占比"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        List<Object[]> userActivity = searchLogDAO.getUserSearchActivity(10);
        int total = searchLogDAO.getSearchLogCount();

        for (Object[] row : userActivity) {
            String username = (String) row[0];
            int count = (Integer) row[1];
            double percentage = total > 0 ? (count * 100.0 / total) : 0;

            model.addRow(new Object[]{
                    username,
                    count,
                    String.format("%.1f%%", percentage)
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建搜索日志列表面板
     */
    private JPanel createSearchLogListPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"时间", "用户", "搜索词", "技能ID", "结果数", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        JTable table = new JTable(model);

        List<SearchLog> logs = searchLogDAO.getAllSearchLogs(1, 100);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (SearchLog log : logs) {
            User user = userDAO.getUserById(log.getUserId());
            model.addRow(new Object[]{
                    log.getSearchTime() != null ? log.getSearchTime().format(formatter) : "",
                    user != null ? user.getUsername() : "未知",
                    log.getSearchKeywords(),
                    log.getSkillIds(),
                    log.getResultCount(),
                    "删除"
            });
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建导出记录面板
     */
    private void createExportPanel() {
        exportPanel = new JPanel(new BorderLayout(10, 10));
        exportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("刷新");
        JButton deleteBtn = new JButton("删除选中");
        JButton cleanBtn = new JButton("清理旧记录");

        JLabel searchLabel = new JLabel("搜索文件名:");
        JTextField searchField = new JTextField(15);
        JButton searchBtn = new JButton("搜索");

        JLabel filterLabel = new JLabel("按开发者:");
        JComboBox<Developer> developerCombo = new JComboBox<>();
        developerCombo.addItem(new Developer(0, "全部开发者"));

        // 加载开发者
        List<Developer> developers = developerDAO.getAllDevelopers(1, 100);
        for (Developer dev : developers) {
            developerCombo.addItem(dev);
        }

        toolPanel.add(refreshBtn);
        toolPanel.add(deleteBtn);
        toolPanel.add(cleanBtn);
        toolPanel.add(new JSeparator(SwingConstants.VERTICAL));
        toolPanel.add(searchLabel);
        toolPanel.add(searchField);
        toolPanel.add(searchBtn);
        toolPanel.add(filterLabel);
        toolPanel.add(developerCombo);

        // 开发者只能管理自己的导出记录，隐藏开发者筛选和清理按钮
        if ("developer".equalsIgnoreCase(currentUser.getRole())) {
            filterLabel.setVisible(false);
            developerCombo.setVisible(false);
            cleanBtn.setVisible(false);
        }

        exportPanel.add(toolPanel, BorderLayout.NORTH);

        // 统计卡片面板
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // 导出总数
        int totalExports = exportRecordDAO.getExportRecordCount();
        JPanel totalCard = createStatCard("导出总数", String.valueOf(totalExports), "📥");
        statsPanel.add(totalCard);

        // 今日导出
        JPanel todayCard = createStatCard("今日导出", "0", "📅");
        statsPanel.add(todayCard);

        // 最常导出开发者
        List<Object[]> topDevs = exportRecordDAO.getTopExportedDevelopers(1);
        String topDev = topDevs.isEmpty() ? "暂无" : (String) topDevs.get(0)[0];
        JPanel topDevCard = createStatCard("热门开发者", topDev, "👤");
        statsPanel.add(topDevCard);

        // 最活跃用户
        List<Object[]> topUsers = exportRecordDAO.getMostActiveExporters(1);
        String topUser = topUsers.isEmpty() ? "暂无" : (String) topUsers.get(0)[0];
        JPanel topUserCard = createStatCard("活跃用户", topUser, "👥");
        statsPanel.add(topUserCard);

        exportPanel.add(statsPanel, BorderLayout.NORTH);

        // 导出记录表格
        String[] columns = {"ID", "导出时间", "开发者", "导出人", "文件名", "操作"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // 操作列
            }
        };
        JTable table = new JTable(model);

        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new TitledBorder("导出记录列表"));
        exportPanel.add(scrollPane, BorderLayout.CENTER);

        // 分页
        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pagePanel.add(new JLabel("共 " + totalExports + " 条记录"));
        pagePanel.add(new JButton("上一页"));
        pagePanel.add(new JLabel("第 1/1 页"));
        pagePanel.add(new JButton("下一页"));
        exportPanel.add(pagePanel, BorderLayout.SOUTH);

        // 刷新按钮事件
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<ExportRecord> records = exportRecordDAO.getAllExportRecords(1, 100);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (ExportRecord er : records) {
                Developer dev = developerDAO.getDeveloperById(er.getDeveloperId());
                User user = er.getUserId() != null ? userDAO.getUserById(er.getUserId()) : null;

                model.addRow(new Object[]{
                        er.getExportId(),
                        er.getExportTime() != null ? er.getExportTime().format(formatter) : "",
                        dev != null ? dev.getName() : "未知",
                        user != null ? user.getUsername() : "未知",
                        er.getFileName(),
                        "删除"
                });
            }
        });

        // 搜索按钮事件
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            Developer selectedDev = (Developer) developerCombo.getSelectedItem();

            model.setRowCount(0);
            List<ExportRecord> records;

            if (!keyword.isEmpty()) {
                records = exportRecordDAO.searchExportRecordsByFileName(keyword, 1, 100);
            } else if (selectedDev != null && selectedDev.getDeveloperId() != 0) {
                records = exportRecordDAO.getExportRecordsByDeveloperId(selectedDev.getDeveloperId(), 1, 100);
            } else {
                records = exportRecordDAO.getAllExportRecords(1, 100);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (ExportRecord er : records) {
                Developer dev = developerDAO.getDeveloperById(er.getDeveloperId());
                User user = er.getUserId() != null ? userDAO.getUserById(er.getUserId()) : null;

                model.addRow(new Object[]{
                        er.getExportId(),
                        er.getExportTime() != null ? er.getExportTime().format(formatter) : "",
                        dev != null ? dev.getName() : "未知",
                        user != null ? user.getUsername() : "未知",
                        er.getFileName(),
                        "删除"
                });
            }
        });

        // 删除按钮事件（表格中的删除）
        table.getColumn("操作").setCellRenderer(new ButtonRenderer());
        table.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox()) {
            @Override
            public Object getCellEditorValue() {
                if (isPushed) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int exportId = (int) model.getValueAt(row, 0);
                        int confirm = JOptionPane.showConfirmDialog(
                                MainFrame.this,
                                "确定要删除这条导出记录吗？",
                                "确认删除",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (confirm == JOptionPane.YES_OPTION) {
                            boolean deleted = exportRecordDAO.deleteExportRecord(exportId);
                            if (deleted) {
                                JOptionPane.showMessageDialog(MainFrame.this, "删除成功");
                                refreshBtn.getActionListeners()[0].actionPerformed(null);
                            } else {
                                JOptionPane.showMessageDialog(MainFrame.this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
                isPushed = false;
                return label;
            }
        });

        // 删除选中按钮
        deleteBtn.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的记录", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "确定要删除选中的 " + selectedRows.length + " 条记录吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                List<Integer> ids = new ArrayList<>();
                for (int row : selectedRows) {
                    ids.add((Integer) model.getValueAt(row, 0));
                }

                boolean success = exportRecordDAO.deleteExportRecords(ids);
                if (success) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                    refreshBtn.getActionListeners()[0].actionPerformed(null);
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 清理旧记录按钮
        cleanBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(
                    this,
                    "保留最近多少天的记录？(输入天数)",
                    "清理旧记录",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input != null) {
                try {
                    int days = Integer.parseInt(input);
                    int deleted = exportRecordDAO.cleanOldRecords(days);
                    JOptionPane.showMessageDialog(
                            this,
                            "已清理 " + deleted + " 条旧记录",
                            "清理完成",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    refreshBtn.getActionListeners()[0].actionPerformed(null);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "请输入有效的天数", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 初始加载
        refreshBtn.getActionListeners()[0].actionPerformed(null);
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
     * 显示教育管理面板
     */
    private void showEducation() {
        cardLayout.show(mainPanel, "education");
        setTitle("简历RAG管理系统 - 教育管理 - " + currentUser.getUsername());
    }

    /**
     * 显示日志统计面板
     */
    private void showLog() {
        cardLayout.show(mainPanel, "log");
        setTitle("简历RAG管理系统 - 日志统计 - " + currentUser.getUsername());
    }

    /**
     * 显示导出记录面板
     */
    private void showExport() {
        cardLayout.show(mainPanel, "export");
        setTitle("简历RAG管理系统 - 导出记录 - " + currentUser.getUsername());
    }
    /**
     * 显示用户管理面板
     */
    private void createUserManagePanel() {
        // 显示待审核用户列表
        // 管理员可以审核通过/拒绝
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
        protected String label;
        protected boolean isPushed;

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
                // 这里原本写死了 developerTable，现在改为通用处理
                // 或者在子类中重写此方法
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

    /**
     * 自定义表格渲染器，实现交替行颜色
     */
    class AlternatingRowColorRenderer extends DefaultTableCellRenderer {
        private final Color evenColor = new Color(240, 240, 240);
        private final Color oddColor = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? evenColor : oddColor);
            }
            return c;
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