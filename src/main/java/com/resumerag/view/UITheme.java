package com.resumerag.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * UI主题统一管理类
 * 提供统一的字体、颜色、样式方案
 */
public class UITheme {

    // ===== 颜色方案 =====
    public static final Color PRIMARY_COLOR = new Color(52, 152, 219);       // 主色-现代蓝
    public static final Color PRIMARY_DARK = new Color(41, 128, 185);        // 主色-深蓝
    public static final Color PRIMARY_LIGHT = new Color(149, 165, 166);     // 主色-浅蓝
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);       // 成功-绿色
    public static final Color WARNING_COLOR = new Color(241, 196, 15);       // 警告-黄色
    public static final Color DANGER_COLOR = new Color(231, 76, 60);         // 危险-红色
    public static final Color INFO_COLOR = new Color(52, 152, 219);          // 信息-蓝色
    public static final Color PURPLE_COLOR = new Color(155, 89, 182);       // 紫色

    public static final Color BG_COLOR = new Color(236, 240, 241);          // 背景色-浅灰
    public static final Color CARD_BG_COLOR = Color.WHITE;                  // 卡片背景
    public static final Color TEXT_COLOR = new Color(44, 62, 80);           // 主文字-深蓝灰
    public static final Color TEXT_SECONDARY = new Color(127, 140, 141);    // 次要文字
    public static final Color BORDER_COLOR = new Color(189, 195, 199);      // 边框色

    public static final Color TABLE_EVEN_ROW = new Color(249, 251, 251);    // 表格偶数行
    public static final Color TABLE_ODD_ROW = Color.WHITE;                  // 表格奇数行
    public static final Color TABLE_HEADER_BG = new Color(44, 62, 80);       // 表头背景-深蓝灰
    public static final Color TABLE_HEADER_TEXT = Color.WHITE;               // 表头文字

    // ===== 字体方案 =====
    public static final String FONT_FAMILY = "微软雅黑";
    public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font HEADING_FONT = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font SUBHEADING_FONT = new Font(FONT_FAMILY, Font.BOLD, 17);
    public static final Font NORMAL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font(FONT_FAMILY, Font.BOLD, 14);

    // ===== 获取Swing内置图标的方法 =====
    public static Icon getSystemIcon(String key) {
        Icon icon = UIManager.getIcon(key);
        return icon;
    }

    public static Icon getSearchIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getAddIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getEditIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getDeleteIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getSaveIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getRefreshIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getUserIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getAdminIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getLoginIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getLogoutIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getExportIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getSkillIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getEducationIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getProjectIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getStarIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getCheckIcon() { return getSystemIcon("FileView.directoryIcon"); }
    public static Icon getListIcon() { return getSystemIcon("FileView.directoryIcon"); }

    /**
     * 应用全局主题到Swing组件
     */
    public static void applyGlobalTheme() {
        UIManager.put("Label.font", NORMAL_FONT);
        UIManager.put("Button.font", BUTTON_FONT);
        UIManager.put("TextField.font", NORMAL_FONT);
        UIManager.put("TextArea.font", NORMAL_FONT);
        UIManager.put("ComboBox.font", NORMAL_FONT);
        UIManager.put("Table.font", NORMAL_FONT);
        UIManager.put("TableHeader.font", new Font(FONT_FAMILY, Font.BOLD, 13));
        UIManager.put("Menu.font", NORMAL_FONT);
        UIManager.put("MenuItem.font", NORMAL_FONT);
        UIManager.put("TabbedPane.font", SUBHEADING_FONT);
    }

    /**
     * 创建样式化的主按钮（现代风格）
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, PRIMARY_DARK),
                new EmptyBorder(10, 24, 10, 24)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    public static JButton createPrimaryButton(Icon icon, String text) {
        JButton btn = new JButton(text, icon);
        btn.setFont(BUTTON_FONT);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, PRIMARY_DARK),
                new EmptyBorder(10, 24, 10, 24)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    /**
     * 创建样式化的次按钮（现代风格）
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(10, 24, 10, 24)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    /**
     * 创建危险按钮（现代风格）
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setBackground(DANGER_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, new Color(192, 57, 43)),
                new EmptyBorder(10, 24, 10, 24)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    /**
     * 创建成功按钮
     */
    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
        btn.setBackground(SUCCESS_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, new Color(39, 174, 96)),
                new EmptyBorder(10, 24, 10, 24)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    /**
     * 创建带图标的工具栏按钮
     */
    public static JButton createIconButton(String icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        if (tooltip != null) btn.setToolTipText(tooltip);
        return btn;
    }

    public static JButton createIconButton(Icon icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        if (tooltip != null) btn.setToolTipText(tooltip);
        return btn;
    }

    /**
     * 创建带图标和文字的按钮
     */
    public static JButton createIconTextButton(String icon, String text) {
        JButton btn = new JButton(icon + " " + text);
        btn.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(CARD_BG_COLOR);
        btn.setOpaque(true);
        return btn;
    }

    public static JButton createIconTextButton(Icon icon, String text) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(CARD_BG_COLOR);
        btn.setOpaque(true);
        return btn;
    }

    /**
     * 创建带图标的工具栏主按钮
     */
    public static JButton createToolbarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_FAMILY, Font.PLAIN, 13));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        return btn;
    }

    /**
     * 创建标题标签
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    /**
     * 创建副标题标签
     */
    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBHEADING_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    /**
     * 创建卡片面板（带阴影效果）
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(18, 18, 18, 18)
        ));
        return panel;
    }

    /**
     * 创建带标题的面板
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = createCardPanel();
        JLabel titleLabel = createSubtitleLabel(title);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.setLayout(new BorderLayout(0, 10));
        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }

    /**
     * 创建现代风格的文本框（带简约边框）
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            public void setBorder(Border border) {
                // 不使用自定义边框，只使用默认的内边距
                super.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            }
        };
        field.setFont(NORMAL_FONT);
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setOpaque(true);
        field.setFocusable(true);
        field.setMargin(new Insets(8, 10, 8, 10));
        return field;
    }

    /**
     * 创建现代风格的密码框（带简约边框）
     */
    public static JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns) {
            @Override
            public void setBorder(Border border) {
                super.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            }
        };
        field.setFont(NORMAL_FONT);
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setOpaque(true);
        field.setFocusable(true);
        field.setMargin(new Insets(8, 10, 8, 10));
        return field;
    }

    /**
     * 创建现代风格的下拉框
     */
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(NORMAL_FONT);
        combo.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return combo;
    }

    /**
     * 创建现代风格的文本域
     */
    public static JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setFont(NORMAL_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, BORDER_COLOR),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return area;
    }

    /**
     * 设置表格隔行变色
     */
    public static void setStripedTable(JTable table) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? TABLE_EVEN_ROW : TABLE_ODD_ROW);
                    setForeground(TEXT_COLOR);
                } else {
                    setBackground(PRIMARY_COLOR);
                    setForeground(Color.WHITE);
                }
                setBorder(null);
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_TEXT);
        header.setFont(new Font(FONT_FAMILY, Font.BOLD, 13));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 40));
    }

    /**
     * 设置表格为可选中样式
     */
    public static void configureTable(JTable table) {
        table.setRowHeight(32);
        table.setSelectionBackground(PRIMARY_COLOR);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setBorder(null);
        setStripedTable(table);
    }

    /**
     * 创建搜索面板
     */
    public static JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        panel.setBackground(CARD_BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(12, 16, 12, 16)
        ));
        return panel;
    }

    /**
     * 创建Loading面板
     */
    public static JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBackground(new Color(0, 0, 0, 50));

        JPanel innerPanel = new JPanel(new BorderLayout(0, 20));
        innerPanel.setBackground(CARD_BG_COLOR);
        innerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                new EmptyBorder(30, 40, 30, 40)
        ));
        innerPanel.setPreferredSize(new Dimension(220, 140));

        JLabel loadingIcon = new JLabel("Loading...", JLabel.CENTER);
        loadingIcon.setFont(new Font(FONT_FAMILY, Font.BOLD, 22));
        loadingIcon.setForeground(PRIMARY_COLOR);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(160, 6));
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBackground(new Color(220, 220, 220));

        JLabel loadingText = new JLabel("请稍候...", JLabel.CENTER);
        loadingText.setFont(NORMAL_FONT);
        loadingText.setForeground(TEXT_SECONDARY);

        innerPanel.add(loadingIcon, BorderLayout.NORTH);
        innerPanel.add(progressBar, BorderLayout.CENTER);
        innerPanel.add(loadingText, BorderLayout.SOUTH);

        panel.add(innerPanel);
        return panel;
    }

    /**
     * 创建统计卡片
     */
    public static JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SMALL_FONT);
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 32));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}
