package com.resumerag.dao;

import com.resumerag.model.*;
import com.resumerag.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开发者数据访问对象
 * 包含开发者的增删改查，以及核心的多对多关联检索
 */
public class DeveloperDAO {

    private static final Logger logger = LoggerFactory.getLogger(DeveloperDAO.class);

    private SkillDAO skillDAO = new SkillDAO();
    private ProjectExperienceDAO projectExperienceDAO = new ProjectExperienceDAO();
    private EducationRecordDAO educationRecordDAO = new EducationRecordDAO();

    // ===================== 基础CRUD =====================

    /**
     * 1. 添加开发者
     */
    public boolean addDeveloper(Developer developer) {
        String sql = "INSERT INTO Developers (user_id, name, phone, email, years_of_experience, self_evaluation, created_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setObject(1, developer.getUserId());  // 允许为null
            pstmt.setString(2, developer.getName());
            pstmt.setString(3, developer.getPhone());
            pstmt.setString(4, developer.getEmail());
            pstmt.setInt(5, developer.getYearsOfExperience());
            pstmt.setString(6, developer.getSelfEvaluation());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    developer.setDeveloperId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            logger.error("添加开发者失败", e);
        }
        return false;
    }

    /**
     * 2. 根据ID删除开发者
     */
    public boolean deleteDeveloper(int developerId) {
        String sql = "DELETE FROM Developers WHERE developer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("删除开发者失败", e);
        }
        return false;
    }

    /**
     * 3. 更新开发者信息
     */
    public boolean updateDeveloper(Developer developer) {
        String sql = "UPDATE Developers SET name = ?, phone = ?, email = ?, " +
                "years_of_experience = ?, self_evaluation = ? WHERE developer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, developer.getName());
            pstmt.setString(2, developer.getPhone());
            pstmt.setString(3, developer.getEmail());
            pstmt.setInt(4, developer.getYearsOfExperience());
            pstmt.setString(5, developer.getSelfEvaluation());
            pstmt.setInt(6, developer.getDeveloperId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("更新开发者信息失败", e);
        }
        return false;
    }

    /**
     * 4. 根据ID查询开发者（基本信息）
     */
    public Developer getDeveloperById(int developerId) {
        String sql = "SELECT * FROM Developers WHERE developer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractDeveloperFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("根据ID查询开发者失败", e);
        }
        return null;
    }

    /**
     * 根据用户ID获取对应的开发者信息
     */
    public Developer getDeveloperByUserId(int userId) {
        String sql = "SELECT * FROM Developers WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractDeveloperFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("根据用户ID查询开发者失败", e);
        }
        return null;
    }

    /**
     * 5. 根据ID查询开发者（包含所有关联信息：技能、项目、教育）
     */
    public Developer getDeveloperWithDetails(int developerId) {
        Developer developer = getDeveloperById(developerId);
        if (developer != null) {
            // 加载技能
            developer.setSkills(getDeveloperSkills(developerId));
            // 加载项目
            developer.setProjects(getDeveloperProjects(developerId));
            // 加载教育记录
            developer.setEducationRecords(getDeveloperEducation(developerId));
        }
        return developer;
    }

    /**
     * 6. 查询所有开发者（分页，基本信息）
     */
    public List<Developer> getAllDevelopers(int pageNum, int pageSize) {
        List<Developer> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT * FROM Developers ORDER BY developer_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractDeveloperFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("查询所有开发者失败", e);
        }
        return list;
    }

    /**
     * 7. 获取开发者总数
     */
    public int getDeveloperCount() {
        String sql = "SELECT COUNT(*) FROM Developers";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("获取开发者总数失败", e);
        }
        return 0;
    }

    // ===================== 技能关联操作 =====================

    /**
     * 8. 为开发者添加技能
     */
    public boolean addDeveloperSkill(int developerId, int skillId, int proficiency) {
        String sql = "INSERT INTO DeveloperSkills (developer_id, skill_id, proficiency) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            pstmt.setInt(2, skillId);
            pstmt.setInt(3, proficiency);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 9. 更新开发者技能熟练度
     */
    public boolean updateDeveloperSkill(int developerId, int skillId, int proficiency) {
        String sql = "UPDATE DeveloperSkills SET proficiency = ? WHERE developer_id = ? AND skill_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, proficiency);
            pstmt.setInt(2, developerId);
            pstmt.setInt(3, skillId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 10. 删除开发者技能
     */
    public boolean deleteDeveloperSkill(int developerId, int skillId) {
        String sql = "DELETE FROM DeveloperSkills WHERE developer_id = ? AND skill_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            pstmt.setInt(2, skillId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 11. 获取开发者的所有技能（含熟练度）
     */
    public List<DeveloperSkill> getDeveloperSkills(int developerId) {
        List<DeveloperSkill> list = new ArrayList<>();
        String sql = "SELECT ds.*, s.skill_name, s.category " +
                "FROM DeveloperSkills ds " +
                "INNER JOIN Skills s ON ds.skill_id = s.skill_id " +
                "WHERE ds.developer_id = ? " +
                "ORDER BY ds.proficiency DESC, s.category";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DeveloperSkill ds = new DeveloperSkill();
                ds.setDevSkillId(rs.getInt("dev_skill_id"));
                ds.setDeveloperId(rs.getInt("developer_id"));
                ds.setSkillId(rs.getInt("skill_id"));
                ds.setProficiency(rs.getInt("proficiency"));

                // 关联技能对象
                Skill skill = new Skill();
                skill.setSkillId(rs.getInt("skill_id"));
                skill.setSkillName(rs.getString("skill_name"));
                skill.setCategory(rs.getString("category"));
                ds.setSkill(skill);

                list.add(ds);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===================== 项目经历操作 =====================

    /**
     * 12. 添加项目经历
     */
    public boolean addProjectExperience(ProjectExperience project) {
        String sql = "INSERT INTO ProjectExperiences (developer_id, project_name, start_date, end_date, " +
                "role, tech_stack, description, achievement) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, project.getDeveloperId());
            pstmt.setString(2, project.getProjectName());
            pstmt.setDate(3, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
            pstmt.setDate(4, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
            pstmt.setString(5, project.getRole());
            pstmt.setString(6, project.getTechStack());
            pstmt.setString(7, project.getDescription());
            pstmt.setString(8, project.getAchievement());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    project.setProjectId(rs.getInt(1));
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
     * 13. 获取开发者的所有项目
     */
    public List<ProjectExperience> getDeveloperProjects(int developerId) {
        List<ProjectExperience> list = new ArrayList<>();
        String sql = "SELECT * FROM ProjectExperiences WHERE developer_id = ? ORDER BY start_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractProjectFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===================== 教育记录操作 =====================

    /**
     * 14. 添加教育记录
     */
    public boolean addEducationRecord(EducationRecord education) {
        String sql = "INSERT INTO EducationRecords (developer_id, school, major, degree, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, education.getDeveloperId());
            pstmt.setString(2, education.getSchool());
            pstmt.setString(3, education.getMajor());
            pstmt.setString(4, education.getDegree());
            pstmt.setDate(5, education.getStartDate() != null ? Date.valueOf(education.getStartDate()) : null);
            pstmt.setDate(6, education.getEndDate() != null ? Date.valueOf(education.getEndDate()) : null);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    education.setEducationId(rs.getInt(1));
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
     * 15. 获取开发者的教育记录
     */
    public List<EducationRecord> getDeveloperEducation(int developerId) {
        List<EducationRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM EducationRecords WHERE developer_id = ? ORDER BY start_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractEducationFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===================== 核心检索功能（多对多关联） =====================

    /**
     * 16. 根据技能组合搜索开发者（核心功能）
     * @param skillIds 技能ID列表，如 "1,3,5"
     * @param minExperience 最小经验年限
     * @param keyword 关键词（项目名称或描述）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 符合条件的开发者列表（包含技能信息）
     */
    public List<Developer> searchDevelopersBySkills(String skillIds, int minExperience,
                                                    String keyword, int pageNum, int pageSize) {
        List<Developer> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        // 如果没指定技能，返回空
        if (skillIds == null || skillIds.trim().isEmpty()) {
            return list;
        }

        String[] skillIdArray = skillIds.split(",");
        int skillCount = skillIdArray.length;

        // 构建动态SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.* FROM Developers d ");
        sql.append("INNER JOIN DeveloperSkills ds ON d.developer_id = ds.developer_id ");
        sql.append("WHERE d.years_of_experience >= ? ");
        sql.append("AND ds.skill_id IN (").append(skillIds).append(") ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND EXISTS (SELECT 1 FROM ProjectExperiences p WHERE p.developer_id = d.developer_id ");
            sql.append("AND (p.project_name LIKE ? OR p.description LIKE ? OR p.tech_stack LIKE ?)) ");
        }

        sql.append("GROUP BY d.developer_id ");
        sql.append("HAVING COUNT(DISTINCT ds.skill_id) = ? ");
        sql.append("ORDER BY d.years_of_experience DESC, d.developer_id ");
        sql.append("LIMIT ? OFFSET ?");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            pstmt.setInt(paramIndex++, minExperience);

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword + "%";
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
            }

            pstmt.setInt(paramIndex++, skillCount);
            pstmt.setInt(paramIndex++, pageSize);
            pstmt.setInt(paramIndex++, offset);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Developer dev = extractDeveloperFromResultSet(rs);
                // 加载这个开发者的技能（用于显示）
                dev.setSkills(getDeveloperSkills(dev.getDeveloperId()));
                list.add(dev);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 17. 复合搜索（技能+关键词+经验范围）
     */
    public List<Developer> advancedSearch(String skillIds, Integer minExp, Integer maxExp,
                                          String keyword, String sortBy, int pageNum, int pageSize) {
        List<Developer> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT d.* FROM Developers d ");
        sql.append("LEFT JOIN DeveloperSkills ds ON d.developer_id = ds.developer_id ");
        sql.append("LEFT JOIN ProjectExperiences p ON d.developer_id = p.developer_id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // 经验范围
        if (minExp != null) {
            sql.append("AND d.years_of_experience >= ? ");
            params.add(minExp);
        }
        if (maxExp != null) {
            sql.append("AND d.years_of_experience <= ? ");
            params.add(maxExp);
        }

        // 技能条件
        if (skillIds != null && !skillIds.trim().isEmpty()) {
            sql.append("AND ds.skill_id IN (").append(skillIds).append(") ");
        }

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (d.name LIKE ? OR d.self_evaluation LIKE ? OR ");
            sql.append("p.project_name LIKE ? OR p.description LIKE ? OR p.tech_stack LIKE ?) ");
            String pattern = "%" + keyword + "%";
            for (int i = 0; i < 5; i++) {
                params.add(pattern);
            }
        }

        // 排序
        if ("exp".equals(sortBy)) {
            sql.append("ORDER BY d.years_of_experience DESC ");
        } else if ("name".equals(sortBy)) {
            sql.append("ORDER BY d.name ");
        } else {
            sql.append("ORDER BY d.developer_id DESC ");
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractDeveloperFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 17. 高级搜索（技能+学历+经验+关键词）
     */
    public List<Developer> advancedSearchWithEducation(String skillIds, String degrees,
                                                       Integer minExp, Integer maxExp,
                                                       String keyword, String sortBy,
                                                       int pageNum, int pageSize) {
        List<Developer> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT d.* FROM Developers d ");
        sql.append("LEFT JOIN DeveloperSkills ds ON d.developer_id = ds.developer_id ");
        sql.append("LEFT JOIN ProjectExperiences p ON d.developer_id = p.developer_id ");
        sql.append("LEFT JOIN EducationRecords e ON d.developer_id = e.developer_id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // 经验范围
        if (minExp != null) {
            sql.append("AND d.years_of_experience >= ? ");
            params.add(minExp);
        }
        if (maxExp != null) {
            sql.append("AND d.years_of_experience <= ? ");
            params.add(maxExp);
        }

        // 学历条件
        if (degrees != null && !degrees.trim().isEmpty() && !"全部".equals(degrees)) {
            sql.append("AND e.degree = ? ");
            params.add(degrees);
        }

        // 技能条件
        if (skillIds != null && !skillIds.trim().isEmpty()) {
            sql.append("AND ds.skill_id IN (").append(skillIds).append(") ");
        }

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (d.name LIKE ? OR d.self_evaluation LIKE ? OR ");
            sql.append("p.project_name LIKE ? OR p.description LIKE ? OR p.tech_stack LIKE ?) ");
            String pattern = "%" + keyword + "%";
            for (int i = 0; i < 5; i++) {
                params.add(pattern);
            }
        }

        // 排序
        if ("exp".equals(sortBy)) {
            sql.append("ORDER BY d.years_of_experience DESC ");
        } else if ("name".equals(sortBy)) {
            sql.append("ORDER BY d.name ");
        } else {
            sql.append("ORDER BY d.developer_id DESC ");
        }

        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Developer dev = extractDeveloperFromResultSet(rs);
                // 获取最高学历
                dev.setHighestDegree(getHighestDegree(conn, dev.getDeveloperId()));
                list.add(dev);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取开发者的最高学历
     */
    private String getHighestDegree(Connection conn, int developerId) {
        String[] degreeOrder = {"博士", "硕士", "本科", "大专", "高中", "博士后"};
        String sql = "SELECT degree FROM EducationRecords WHERE developer_id = ? ORDER BY FIELD(degree, ?, ?, ?, ?, ?, ?) LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, developerId);
            for (int i = 0; i < degreeOrder.length; i++) {
                pstmt.setString(i + 2, degreeOrder[i]);
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("degree");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 18. 获取搜索结果总数（用于分页）
     */
    public int getSearchCount(String skillIds, int minExperience, String keyword) {
        if (skillIds == null || skillIds.trim().isEmpty()) {
            return 0;
        }

        String[] skillIdArray = skillIds.split(",");
        int skillCount = skillIdArray.length;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT d.developer_id) FROM Developers d ");
        sql.append("INNER JOIN DeveloperSkills ds ON d.developer_id = ds.developer_id ");
        sql.append("WHERE d.years_of_experience >= ? ");
        sql.append("AND ds.skill_id IN (").append(skillIds).append(") ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND EXISTS (SELECT 1 FROM ProjectExperiences p WHERE p.developer_id = d.developer_id ");
            sql.append("AND (p.project_name LIKE ? OR p.description LIKE ?)) ");
        }

        sql.append("GROUP BY d.developer_id ");
        sql.append("HAVING COUNT(DISTINCT ds.skill_id) = ?");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            pstmt.setInt(paramIndex++, minExperience);

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword + "%";
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
            }

            pstmt.setInt(paramIndex++, skillCount);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================== 统计分析 =====================

    /**
     * 19. 按技能统计开发者数量
     */
    public Map<String, Integer> countDevelopersBySkill() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT s.skill_name, COUNT(ds.developer_id) as dev_count " +
                "FROM Skills s " +
                "LEFT JOIN DeveloperSkills ds ON s.skill_id = ds.skill_id " +
                "GROUP BY s.skill_id, s.skill_name " +
                "ORDER BY dev_count DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("skill_name"), rs.getInt("dev_count"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * 20. 按经验年限统计
     */
    public Map<String, Integer> countDevelopersByExperience() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT " +
                "CASE " +
                "WHEN years_of_experience < 1 THEN '1年以下' " +
                "WHEN years_of_experience BETWEEN 1 AND 3 THEN '1-3年' " +
                "WHEN years_of_experience BETWEEN 4 AND 6 THEN '4-6年' " +
                "ELSE '7年以上' " +
                "END as exp_level, " +
                "COUNT(*) as count " +
                "FROM Developers " +
                "GROUP BY exp_level";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("exp_level"), rs.getInt("count"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * 17. 批量删除开发者（事务处理）
     */
    public boolean deleteDevelopers(List<Integer> developerIds) {
        if (developerIds == null || developerIds.isEmpty()) return false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM Developers WHERE developer_id = ?";
            pstmt = conn.prepareStatement(sql);
            for (int id : developerIds) {
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

    /**
     * 统计某个技能被多少开发者使用
     */
    public int getDeveloperCountBySkill(int skillId) {
        String sql = "SELECT COUNT(*) FROM DeveloperSkills WHERE skill_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, skillId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================== 私有辅助方法 =====================

    private Developer extractDeveloperFromResultSet(ResultSet rs) throws SQLException {
        Developer dev = new Developer();
        dev.setDeveloperId(rs.getInt("developer_id"));
        dev.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        dev.setName(rs.getString("name"));
        dev.setPhone(rs.getString("phone"));
        dev.setEmail(rs.getString("email"));
        dev.setYearsOfExperience(rs.getInt("years_of_experience"));
        dev.setSelfEvaluation(rs.getString("self_evaluation"));

        Timestamp ts = rs.getTimestamp("created_time");
        if (ts != null) {
            dev.setCreatedTime(ts.toLocalDateTime());
        }

        return dev;
    }

    private ProjectExperience extractProjectFromResultSet(ResultSet rs) throws SQLException {
        ProjectExperience project = new ProjectExperience();
        project.setProjectId(rs.getInt("project_id"));
        project.setDeveloperId(rs.getInt("developer_id"));
        project.setProjectName(rs.getString("project_name"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            project.setStartDate(startDate.toLocalDate());
        }

        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            project.setEndDate(endDate.toLocalDate());
        }

        project.setRole(rs.getString("role"));
        project.setTechStack(rs.getString("tech_stack"));
        project.setDescription(rs.getString("description"));
        project.setAchievement(rs.getString("achievement"));

        return project;
    }

    private EducationRecord extractEducationFromResultSet(ResultSet rs) throws SQLException {
        EducationRecord edu = new EducationRecord();
        edu.setEducationId(rs.getInt("education_id"));
        edu.setDeveloperId(rs.getInt("developer_id"));
        edu.setSchool(rs.getString("school"));
        edu.setMajor(rs.getString("major"));
        edu.setDegree(rs.getString("degree"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            edu.setStartDate(startDate.toLocalDate());
        }

        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            edu.setEndDate(endDate.toLocalDate());
        }

        return edu;
    }

    // ===================== 测试方法 =====================

    public static void main(String[] args) {
        DeveloperDAO devDAO = new DeveloperDAO();

        System.out.println("========== 测试DeveloperDAO ==========");

        // 1. 测试添加开发者
        System.out.println("\n1. 测试添加开发者：");
        Developer newDev = new Developer("王五", "13800138003", "wangwu@example.com", 2);
        newDev.setSelfEvaluation("2年后端开发经验，熟悉Java和Spring Boot");
        boolean added = devDAO.addDeveloper(newDev);
        System.out.println("添加" + (added ? "成功" : "失败") + "，开发者ID：" + newDev.getDeveloperId());

        // 2. 测试添加技能关联
        System.out.println("\n2. 测试添加技能关联：");
        boolean skillAdded = devDAO.addDeveloperSkill(newDev.getDeveloperId(), 1, 4); // Java 熟练度4
        skillAdded = devDAO.addDeveloperSkill(newDev.getDeveloperId(), 3, 3) && skillAdded; // Spring Boot 熟练度3
        System.out.println("技能关联添加" + (skillAdded ? "成功" : "失败"));

        // 3. 测试添加项目
        System.out.println("\n3. 测试添加项目：");
        ProjectExperience project = new ProjectExperience();
        project.setDeveloperId(newDev.getDeveloperId());
        project.setProjectName("在线商城");
        project.setStartDate(LocalDate.of(2023, 1, 1));
        project.setEndDate(LocalDate.of(2023, 12, 31));
        project.setRole("后端开发");
        project.setTechStack("Java, Spring Boot, MySQL");
        project.setDescription("开发订单和用户模块");
        project.setAchievement("完成高并发订单处理");
        boolean projectAdded = devDAO.addProjectExperience(project);
        System.out.println("项目添加" + (projectAdded ? "成功" : "失败") + "，项目ID：" + project.getProjectId());

        // 4. 测试查询开发者详情
        System.out.println("\n4. 测试查询开发者详情：");
        Developer dev = devDAO.getDeveloperWithDetails(newDev.getDeveloperId());
        System.out.println("开发者：" + dev.getName() + "，经验：" + dev.getYearsOfExperience() + "年");
        System.out.println("技能列表：");
        if (dev.getSkills() != null) {
            for (DeveloperSkill ds : dev.getSkills()) {
                System.out.println("  " + ds.getSkill().getSkillName() + " (熟练度：" + ds.getProficiency() + ")");
            }
        }
        System.out.println("项目列表：");
        if (dev.getProjects() != null) {
            for (ProjectExperience p : dev.getProjects()) {
                System.out.println("  " + p.getProjectName() + " - " + p.getRole());
            }
        }

        // 5. 核心功能测试：按技能搜索
        System.out.println("\n5. 核心功能测试：按技能搜索开发者");
        System.out.println("搜索同时拥有Java和Spring Boot的开发者：");
        List<Developer> results = devDAO.searchDevelopersBySkills("1,3", 0, null, 1, 10);
        for (Developer d : results) {
            System.out.println("  " + d.getName() + " (经验：" + d.getYearsOfExperience() + "年)");
        }

        // 6. 测试统计功能
        System.out.println("\n6. 测试统计功能：");
        Map<String, Integer> skillStats = devDAO.countDevelopersBySkill();
        System.out.println("技能统计：");
        for (Map.Entry<String, Integer> entry : skillStats.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() + "人");
        }

        System.out.println("\n========== 测试完成 ==========");
    }
}