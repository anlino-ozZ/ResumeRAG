package com.resumerag.dao;

import com.resumerag.model.Skill;
import com.resumerag.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 技能标签表的数据访问对象
 * 包含技能的增删改查操作
 */
public class SkillDAO {

    // ===================== 增删改查基础方法 =====================

    /**
     * 1. 添加技能
     */
    public boolean addSkill(Skill skill) {
        String sql = "INSERT INTO Skills (skill_name, category) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, skill.getSkillName());
            pstmt.setString(2, skill.getCategory());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    skill.setSkillId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 2. 根据ID删除技能
     */
    public boolean deleteSkill(int skillId) {
        String sql = "DELETE FROM Skills WHERE skill_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, skillId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 3. 更新技能信息
     */
    public boolean updateSkill(Skill skill) {
        String sql = "UPDATE Skills SET skill_name = ?, category = ? WHERE skill_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, skill.getSkillName());
            pstmt.setString(2, skill.getCategory());
            pstmt.setInt(3, skill.getSkillId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 4. 根据ID查询技能
     */
    public Skill getSkillById(int skillId) {
        String sql = "SELECT * FROM Skills WHERE skill_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, skillId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSkillFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 5. 根据技能名称查询
     */
    public Skill getSkillByName(String skillName) {
        String sql = "SELECT * FROM Skills WHERE skill_name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, skillName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSkillFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 6. 查询所有技能（分页）
     */
    public List<Skill> getAllSkills(int pageNum, int pageSize) {
        List<Skill> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT * FROM Skills ORDER BY category, skill_name LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractSkillFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 7. 查询所有技能（不分页，用于下拉框）
     */
    public List<Skill> getAllSkills() {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT * FROM Skills ORDER BY category, skill_name";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractSkillFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 8. 获取技能总数
     */
    public int getSkillCount() {
        String sql = "SELECT COUNT(*) FROM Skills";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================== 业务相关方法 =====================

    /**
     * 9. 根据分类查询技能
     */
    public List<Skill> getSkillsByCategory(String category) {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT * FROM Skills WHERE category = ? ORDER BY skill_name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractSkillFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 10. 获取所有分类
     */
    public List<String> getAllCategories() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM Skills WHERE category IS NOT NULL ORDER BY category";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(rs.getString("category"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 11. 搜索技能（按名称模糊查询）
     */
    public List<Skill> searchSkills(String keyword) {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT * FROM Skills WHERE skill_name LIKE ? OR category LIKE ? ORDER BY category, skill_name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractSkillFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 12. 批量添加技能
     */
    public int addSkills(List<Skill> skills) {
        int successCount = 0;
        String sql = "INSERT INTO Skills (skill_name, category) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 开启事务
            conn.setAutoCommit(false);

            for (Skill skill : skills) {
                pstmt.setString(1, skill.getSkillName());
                pstmt.setString(2, skill.getCategory());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();

            // 计算成功数量
            for (int result : results) {
                if (result > 0) successCount++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return successCount;
    }

    /**
     * 13. 批量删除技能（事务处理）
     */
    public boolean deleteSkills(List<Integer> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) return false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM Skills WHERE skill_id = ?";
            pstmt = conn.prepareStatement(sql);
            for (int id : skillIds) {
                pstmt.setInt(1, id);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
        } finally {
            DBUtil.close(null, pstmt, conn);
        }
        return false;
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 从ResultSet中提取Skill对象
     */
    private Skill extractSkillFromResultSet(ResultSet rs) throws SQLException {
        Skill skill = new Skill();
        skill.setSkillId(rs.getInt("skill_id"));
        skill.setSkillName(rs.getString("skill_name"));
        skill.setCategory(rs.getString("category"));
        return skill;
    }

    // ===================== 测试方法 =====================

    public static void main(String[] args) {
        SkillDAO skillDAO = new SkillDAO();

        System.out.println("========== 测试SkillDAO ==========");

        // 1. 测试添加技能
        System.out.println("\n1. 测试添加技能：");
        Skill newSkill = new Skill("Spring Cloud", "框架");
        boolean added = skillDAO.addSkill(newSkill);
        System.out.println("添加" + (added ? "成功" : "失败") + "，技能ID：" + newSkill.getSkillId());

        // 2. 测试根据ID查询
        System.out.println("\n2. 测试根据ID查询：");
        Skill skill = skillDAO.getSkillById(newSkill.getSkillId());
        System.out.println("查询结果：" + skill);

        // 3. 测试根据名称查询
        System.out.println("\n3. 测试根据名称查询：");
        Skill skill2 = skillDAO.getSkillByName("Spring Cloud");
        System.out.println("查询结果：" + skill2);

        // 4. 测试查询所有技能
        System.out.println("\n4. 测试查询所有技能（不分页）：");
        List<Skill> allSkills = skillDAO.getAllSkills();
        System.out.println("共" + allSkills.size() + "个技能：");
        for (Skill s : allSkills) {
            System.out.println("  " + s);
        }

        // 5. 测试分页查询
        System.out.println("\n5. 测试分页查询（第1页，每页5条）：");
        List<Skill> pageSkills = skillDAO.getAllSkills(1, 5);
        for (Skill s : pageSkills) {
            System.out.println("  " + s);
        }

        // 6. 测试获取所有分类
        System.out.println("\n6. 测试获取所有分类：");
        List<String> categories = skillDAO.getAllCategories();
        System.out.println("分类：" + categories);

        // 7. 测试根据分类查询
        System.out.println("\n7. 测试根据分类查询（框架）：");
        List<Skill> frameworkSkills = skillDAO.getSkillsByCategory("框架");
        for (Skill s : frameworkSkills) {
            System.out.println("  " + s);
        }

        // 8. 测试搜索技能
        System.out.println("\n8. 测试搜索技能（关键词：Java）：");
        List<Skill> searchResults = skillDAO.searchSkills("Java");
        for (Skill s : searchResults) {
            System.out.println("  " + s);
        }

        // 9. 测试更新技能
        System.out.println("\n9. 测试更新技能：");
        if (skill != null) {
            skill.setCategory("微服务");
            boolean updated = skillDAO.updateSkill(skill);
            System.out.println("更新" + (updated ? "成功" : "失败"));

            Skill updatedSkill = skillDAO.getSkillById(skill.getSkillId());
            System.out.println("更新后分类：" + updatedSkill.getCategory());
        }

        // 10. 测试批量添加
        System.out.println("\n10. 测试批量添加：");
        List<Skill> batchSkills = new ArrayList<>();
        batchSkills.add(new Skill("Kafka", "中间件"));
        batchSkills.add(new Skill("RabbitMQ", "中间件"));
        batchSkills.add(new Skill("Nginx", "工具"));
        int successCount = skillDAO.addSkills(batchSkills);
        System.out.println("批量添加成功：" + successCount + "个");

        // 11. 测试删除技能
        System.out.println("\n11. 测试删除技能：");
        if (skill != null) {
            boolean deleted = skillDAO.deleteSkill(skill.getSkillId());
            System.out.println("删除" + (deleted ? "成功" : "失败"));

            Skill deletedSkill = skillDAO.getSkillById(skill.getSkillId());
            System.out.println("删除后查询：" + (deletedSkill == null ? "技能不存在" : "技能还在"));
        }

        // 12. 获取技能总数
        System.out.println("\n12. 技能总数：" + skillDAO.getSkillCount());

        System.out.println("\n========== 测试完成 ==========");
    }
}