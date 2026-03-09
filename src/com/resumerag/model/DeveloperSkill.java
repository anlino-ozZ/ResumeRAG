package com.resumerag.model;

public class DeveloperSkill {
    private int devSkillId;
    private int developerId;
    private int skillId;
    private int proficiency;  // 熟练度：1-5

    // 关联对象（方便显示）
    private Skill skill;  // 技能详情

    public DeveloperSkill() {}

    public DeveloperSkill(int developerId, int skillId, int proficiency) {
        this.developerId = developerId;
        this.skillId = skillId;
        this.proficiency = proficiency;
    }

    // Getter 和 Setter
    public int getDevSkillId() { return devSkillId; }
    public void setDevSkillId(int devSkillId) { this.devSkillId = devSkillId; }

    public int getDeveloperId() { return developerId; }
    public void setDeveloperId(int developerId) { this.developerId = developerId; }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public int getProficiency() { return proficiency; }
    public void setProficiency(int proficiency) { this.proficiency = proficiency; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    // 熟练度文字描述
    public String getProficiencyLevel() {
        switch (proficiency) {
            case 1: return "了解";
            case 2: return "入门";
            case 3: return "熟练";
            case 4: return "精通";
            case 5: return "专家";
            default: return "未知";
        }
    }
}