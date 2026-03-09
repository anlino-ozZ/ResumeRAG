package com.resumerag.view;

import com.resumerag.dao.DeveloperDAO;
import com.resumerag.dao.SkillDAO;
import com.resumerag.model.Developer;
import com.resumerag.model.DeveloperSkill;
import com.resumerag.model.Skill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

/**
 * 开发者编辑对话框
 */
public class DeveloperDialog extends JDialog {

    private Developer developer;
    private DeveloperDAO developerDAO;
    private SkillDAO skillDAO;

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField emailField;
    private JSpinner expSpinner;
    private JTextArea evaluationArea;
    private JPanel skillPanel;
    private JButton saveBtn;
    private JButton cancelBtn;
    private JButton addSkillBtn;

    private boolean isEdit;

    public DeveloperDialog(Frame owner, String title, Developer developer,
                           DeveloperDAO developerDAO, SkillDAO skillDAO) {
        super(owner, title, true);
        this.developer = developer;
        this.developerDAO = developerDAO;
        this.skillDAO = skillDAO;
        this.isEdit = (developer != null);

        initComponents();
        initLayout();
        initListeners();

        if (isEdit) {
            loadData();
        }

        setSize(500, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        nameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        expSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
        evaluationArea = new JTextArea(5, 20);
        evaluationArea.setLineWrap(true);

        saveBtn = new JButton("保存");
        cancelBtn = new JButton("取消");
        addSkillBtn = new JButton("添加技能");

        skillPanel = new JPanel(new GridBagLayout());
        skillPanel.setBorder(new TitledBorder("技能列表"));
    }

    private void initLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 姓名
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("姓名:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);

        // 电话
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("电话:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(phoneField, gbc);

        // 邮箱
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("邮箱:"), gbc);

        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);

        // 经验年限
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("经验(年):"), gbc);

        gbc.gridx = 1;
        mainPanel.add(expSpinner, gbc);

        // 自我评价
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("自我评价:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        mainPanel.add(new JScrollPane(evaluationArea), gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 技能面板
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel skillBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        skillBtnPanel.add(addSkillBtn);
        southPanel.add(skillBtnPanel, BorderLayout.NORTH);

        JScrollPane skillScroll = new JScrollPane(skillPanel);
        skillScroll.setPreferredSize(new Dimension(450, 150));
        southPanel.add(skillScroll, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void initListeners() {
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        addSkillBtn.addActionListener(e -> showSkillSelectionDialog());
    }

    private void loadData() {
        nameField.setText(developer.getName());
        phoneField.setText(developer.getPhone());
        emailField.setText(developer.getEmail());
        expSpinner.setValue(developer.getYearsOfExperience());
        evaluationArea.setText(developer.getSelfEvaluation());

        // 加载技能
        List<DeveloperSkill> skills = developerDAO.getDeveloperSkills(developer.getDeveloperId());
        for (DeveloperSkill ds : skills) {
            addSkillToPanel(ds);
        }
    }

    private void addSkillToPanel(DeveloperSkill ds) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = skillPanel.getComponentCount();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rowPanel.add(new JLabel(ds.getSkill().getSkillName() + " - " + ds.getProficiencyLevel()));

        JButton removeBtn = new JButton("删除");
        removeBtn.addActionListener(e -> {
            skillPanel.remove(rowPanel);
            skillPanel.revalidate();
            skillPanel.repaint();
        });
        rowPanel.add(removeBtn);

        skillPanel.add(rowPanel, gbc);
    }

    private void showSkillSelectionDialog() {
        List<Skill> allSkills = skillDAO.getAllSkills();

        JComboBox<Skill> skillCombo = new JComboBox<>(allSkills.toArray(new Skill[0]));
        JSpinner proficiencySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("选择技能:"));
        panel.add(skillCombo);
        panel.add(new JLabel("熟练度(1-5):"));
        panel.add(proficiencySpinner);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "添加技能",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            Skill selectedSkill = (Skill) skillCombo.getSelectedItem();
            int proficiency = (Integer) proficiencySpinner.getValue();

            DeveloperSkill ds = new DeveloperSkill();
            ds.setSkill(selectedSkill);
            ds.setSkillId(selectedSkill.getSkillId());
            ds.setProficiency(proficiency);

            addSkillToPanel(ds);
        }
    }

    private void save() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "姓名不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isEdit) {
            developer = new Developer();
        }

        developer.setName(nameField.getText().trim());
        developer.setPhone(phoneField.getText().trim());
        developer.setEmail(emailField.getText().trim());
        developer.setYearsOfExperience((Integer) expSpinner.getValue());
        developer.setSelfEvaluation(evaluationArea.getText());

        boolean success;
        if (isEdit) {
            success = developerDAO.updateDeveloper(developer);
        } else {
            success = developerDAO.addDeveloper(developer);
        }

        if (success) {
            // 先删除旧的技能关联
            if (isEdit) {
                List<DeveloperSkill> oldSkills = developerDAO.getDeveloperSkills(developer.getDeveloperId());
                for (DeveloperSkill ds : oldSkills) {
                    developerDAO.deleteDeveloperSkill(developer.getDeveloperId(), ds.getSkillId());
                }
            }

            // 添加新的技能关联
            for (Component comp : skillPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel rowPanel = (JPanel) comp;
                    JLabel label = (JLabel) rowPanel.getComponent(0);
                    String text = label.getText();

                    // 解析技能名称和熟练度
                    String skillName = text.split(" - ")[0];
                    String profText = text.split(" - ")[1];
                    int proficiency = 3;
                    if ("了解".equals(profText)) proficiency = 1;
                    else if ("入门".equals(profText)) proficiency = 2;
                    else if ("熟练".equals(profText)) proficiency = 3;
                    else if ("精通".equals(profText)) proficiency = 4;
                    else if ("专家".equals(profText)) proficiency = 5;

                    // 查询技能ID
                    Skill skill = skillDAO.getSkillByName(skillName);
                    if (skill != null) {
                        developerDAO.addDeveloperSkill(developer.getDeveloperId(), skill.getSkillId(), proficiency);
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "保存失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}