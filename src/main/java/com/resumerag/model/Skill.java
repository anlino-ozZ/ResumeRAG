package com.resumerag.model;

public class Skill {
    private int skillId;
    private String skillName;
    private String category;  // 如 "编程语言", "框架", "工具"

    public Skill() {}

    public Skill(String skillName, String category) {
        this.skillName = skillName;
        this.category = category;
    }

    // Getter 和 Setter
    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return skillName + "(" + category + ")";
    }
}