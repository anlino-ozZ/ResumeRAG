package com.resumerag.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 加载中对话框 - 显示加载动画
 */
public class LoadingDialog extends JDialog {

    private static LoadingDialog dialog;
    private JProgressBar progressBar;

    public LoadingDialog(Frame parent, String message) {
        super(parent, "请等待", false);
        initComponents(message);
    }

    private void initComponents(String message) {
        setUndecorated(true);
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.CARD_BG_COLOR);

        // 消息标签
        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        messageLabel.setFont(UITheme.NORMAL_FONT);
        messageLabel.setForeground(UITheme.TEXT_COLOR);

        // 进度条
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 6));
        progressBar.setForeground(UITheme.PRIMARY_COLOR);
        progressBar.setBackground(new Color(220, 220, 220));
        progressBar.setBorderPainted(false);

        // 加载图标
        JLabel loadingIcon = new JLabel("加载中...");
        loadingIcon.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 16));
        loadingIcon.setForeground(UITheme.PRIMARY_COLOR);
        loadingIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 面板
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(UITheme.CARD_BG_COLOR);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY_COLOR, 2),
                new EmptyBorder(30, 40, 30, 40)
        ));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(loadingIcon);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(messageLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(progressBar);

        add(centerPanel, BorderLayout.CENTER);
        pack();
    }

    /**
     * 显示加载对话框
     */
    public static void showLoading(Frame parent, String message) {
        if (dialog != null && dialog.isVisible()) {
            dialog.dispose();
        }
        dialog = new LoadingDialog(parent, message);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /**
     * 隐藏加载对话框
     */
    public static void closeLoading() {
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }

    /**
     * 在后台任务中运行并显示加载动画
     */
    public static void showWhileRunning(Frame parent, String message, Runnable task) {
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                SwingUtilities.invokeLater(() -> showLoading(parent, message));
                task.run();
                return null;
            }

            @Override
            protected void done() {
                closeLoading();
            }
        };
        worker.execute();
    }
}
