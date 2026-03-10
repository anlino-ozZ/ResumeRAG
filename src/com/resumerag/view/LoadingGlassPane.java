package com.resumerag.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoadingGlassPane extends JComponent {
    private final JProgressBar progressBar;

    public LoadingGlassPane() {
        setLayout(new GridBagLayout());
        setOpaque(false);
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        add(progressBar);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    
}