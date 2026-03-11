package com.resumerag.dao;

import com.resumerag.model.ProjectExperience;
import com.resumerag.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目经历数据访问对象
 * 包含项目经历的增删改查，以及按条件检索
 */
public class ProjectExperienceDAO {

    // ===================== 基础CRUD =====================

    /**
     * 1. 添加项目经历
     */
    public boolean addProject(ProjectExperience project) {
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
     * 2. 根据ID删除项目经历
     */
    public boolean deleteProject(int projectId) {
        String sql = "DELETE FROM ProjectExperiences WHERE project_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 3. 更新项目经历
     */
    public boolean updateProject(ProjectExperience project) {
        String sql = "UPDATE ProjectExperiences SET project_name = ?, start_date = ?, end_date = ?, " +
                "role = ?, tech_stack = ?, description = ?, achievement = ? WHERE project_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, project.getProjectName());
            pstmt.setDate(2, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
            pstmt.setDate(3, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
            pstmt.setString(4, project.getRole());
            pstmt.setString(5, project.getTechStack());
            pstmt.setString(6, project.getDescription());
            pstmt.setString(7, project.getAchievement());
            pstmt.setInt(8, project.getProjectId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 4. 根据ID查询项目经历
     */
    public ProjectExperience getProjectById(int projectId) {
        String sql = "SELECT * FROM ProjectExperiences WHERE project_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, projectId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractProjectFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 5. 根据开发者ID查询所有项目
     */
    public List<ProjectExperience> getProjectsByDeveloperId(int developerId) {
        return getProjectsByDeveloperId(developerId, 1, 100);
    }

    /**
     * 根据开发者ID查询项目（分页）
     */
    public List<ProjectExperience> getProjectsByDeveloperId(int developerId, int pageNum, int pageSize) {
        List<ProjectExperience> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM ProjectExperiences WHERE developer_id = ? ORDER BY start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
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

    /**
     * 6. 查询所有项目（分页）
     */
    public List<ProjectExperience> getAllProjects(int pageNum, int pageSize) {
        List<ProjectExperience> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT p.*, d.name as developer_name " +
                "FROM ProjectExperiences p " +
                "INNER JOIN Developers d ON p.developer_id = d.developer_id " +
                "ORDER BY p.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ProjectExperience project = extractProjectFromResultSet(rs);
                // 可以设置开发者名称（如果需要）
                list.add(project);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 7. 获取项目总数
     */
    public int getProjectCount() {
        String sql = "SELECT COUNT(*) FROM ProjectExperiences";
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

    /**
     * 8. 获取某个开发者的项目数量
     */
    public int getProjectCountByDeveloper(int developerId) {
        String sql = "SELECT COUNT(*) FROM ProjectExperiences WHERE developer_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
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

    // ===================== 检索功能 =====================

    /**
     * 9. 根据关键词搜索项目（项目名称、描述、技术栈）
     */
    public List<ProjectExperience> searchProjects(String keyword, int pageNum, int pageSize) {
        List<ProjectExperience> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT p.*, d.name as developer_name " +
                "FROM ProjectExperiences p " +
                "INNER JOIN Developers d ON p.developer_id = d.developer_id " +
                "WHERE p.project_name LIKE ? OR p.description LIKE ? OR p.tech_stack LIKE ? " +
                "ORDER BY p.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setInt(4, pageSize);
            pstmt.setInt(5, offset);

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

    /**
     * 10. 根据技术栈搜索项目
     */
    public List<ProjectExperience> searchProjectsByTechStack(String techStack, int pageNum, int pageSize) {
        List<ProjectExperience> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT p.*, d.name as developer_name " +
                "FROM ProjectExperiences p " +
                "INNER JOIN Developers d ON p.developer_id = d.developer_id " +
                "WHERE p.tech_stack LIKE ? " +
                "ORDER BY p.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + techStack + "%");
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

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

    /**
     * 11. 根据时间范围查询项目
     */
    public List<ProjectExperience> getProjectsByDateRange(LocalDate startDate, LocalDate endDate, int pageNum, int pageSize) {
        List<ProjectExperience> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT p.*, d.name as developer_name " +
                "FROM ProjectExperiences p " +
                "INNER JOIN Developers d ON p.developer_id = d.developer_id " +
                "WHERE p.start_date >= ? AND p.end_date <= ? " +
                "ORDER BY p.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            pstmt.setInt(3, pageSize);
            pstmt.setInt(4, offset);

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

    /**
     * 12. 根据角色查询项目
     */
    public List<ProjectExperience> getProjectsByRole(String role, int pageNum, int pageSize) {
        List<ProjectExperience> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT p.*, d.name as developer_name " +
                "FROM ProjectExperiences p " +
                "INNER JOIN Developers d ON p.developer_id = d.developer_id " +
                "WHERE p.role = ? " +
                "ORDER BY p.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

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

    // ===================== 统计功能 =====================

    /**
     * 13. 统计常用技术栈
     */
    public List<Object[]> getTopTechStacks(int limit) {
        List<Object[]> list = new ArrayList<>();

        // 简单的技术栈统计（需要解析tech_stack字段）
        String sql = "SELECT tech_stack, COUNT(*) as count FROM ProjectExperiences " +
                "WHERE tech_stack IS NOT NULL AND tech_stack != '' " +
                "GROUP BY tech_stack ORDER BY count DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("tech_stack");
                row[1] = rs.getInt("count");
                list.add(row);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 14. 统计项目角色分布
     */
    public List<Object[]> getRoleDistribution() {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT role, COUNT(*) as count FROM ProjectExperiences " +
                "WHERE role IS NOT NULL AND role != '' " +
                "GROUP BY role ORDER BY count DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("role");
                row[1] = rs.getInt("count");
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 15. 统计每年项目数量
     */
    public List<Object[]> getProjectsByYear() {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT YEAR(start_date) as year, COUNT(*) as count " +
                "FROM ProjectExperiences WHERE start_date IS NOT NULL " +
                "GROUP BY YEAR(start_date) ORDER BY year DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getInt("year");
                row[1] = rs.getInt("count");
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===================== 批量操作 =====================

    /**
     * 16. 批量添加项目经历（事务处理）
     */
    public int addProjects(List<ProjectExperience> projects) {
        int successCount = 0;
        String sql = "INSERT INTO ProjectExperiences (developer_id, project_name, start_date, end_date, " +
                "role, tech_stack, description, achievement) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);  // 开启事务

            pstmt = conn.prepareStatement(sql);

            for (ProjectExperience project : projects) {
                pstmt.setInt(1, project.getDeveloperId());
                pstmt.setString(2, project.getProjectName());
                pstmt.setDate(3, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
                pstmt.setDate(4, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
                pstmt.setString(5, project.getRole());
                pstmt.setString(6, project.getTechStack());
                pstmt.setString(7, project.getDescription());
                pstmt.setString(8, project.getAchievement());

                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();  // 提交事务

            // 计算成功数量
            for (int result : results) {
                if (result > 0) successCount++;
            }

        } catch (SQLException e) {
            // 回滚事务
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("事务回滚");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            DBUtil.close(null, pstmt, conn);
        }

        return successCount;
    }

    /**
     * 17. 批量删除项目经历（事务处理）
     */
    public boolean deleteProjects(List<Integer> projectIds) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String sql = "DELETE FROM ProjectExperiences WHERE project_id = ?";
            pstmt = conn.prepareStatement(sql);

            for (int projectId : projectIds) {
                pstmt.setInt(1, projectId);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            DBUtil.close(null, pstmt, conn);
        }
        return false;
    }

    // ===================== 私有辅助方法 =====================

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

    // ===================== 测试方法 =====================

    public static void main(String[] args) {
        ProjectExperienceDAO projectDAO = new ProjectExperienceDAO();

        System.out.println("========== 测试ProjectExperienceDAO ==========");

        // 1. 测试添加项目
        System.out.println("\n1. 测试添加项目：");
        ProjectExperience newProject = new ProjectExperience();
        newProject.setDeveloperId(1);  // 假设开发者ID为1（张三）
        newProject.setProjectName("微服务架构改造");
        newProject.setStartDate(LocalDate.of(2024, 1, 1));
        newProject.setEndDate(LocalDate.of(2024, 6, 30));
        newProject.setRole("技术负责人");
        newProject.setTechStack("Spring Cloud, Docker, Kubernetes");
        newProject.setDescription("将单体应用拆分为微服务架构");
        newProject.setAchievement("系统性能提升50%，成功上线");

        boolean added = projectDAO.addProject(newProject);
        System.out.println("添加" + (added ? "成功" : "失败") + "，项目ID：" + newProject.getProjectId());

        // 2. 测试根据ID查询
        System.out.println("\n2. 测试根据ID查询：");
        ProjectExperience project = projectDAO.getProjectById(newProject.getProjectId());
        if (project != null) {
            System.out.println("项目名称：" + project.getProjectName());
            System.out.println("角色：" + project.getRole());
            System.out.println("技术栈：" + project.getTechStack());
        }

        // 3. 测试根据开发者ID查询
        System.out.println("\n3. 测试根据开发者ID查询：");
        List<ProjectExperience> devProjects = projectDAO.getProjectsByDeveloperId(1);
        System.out.println("开发者1的项目数量：" + devProjects.size());
        for (ProjectExperience p : devProjects) {
            System.out.println("  " + p.getProjectName() + " (" + p.getRole() + ")");
        }

        // 4. 测试搜索功能
        System.out.println("\n4. 测试关键词搜索（关键词：微服务）：");
        List<ProjectExperience> searchResults = projectDAO.searchProjects("微服务", 1, 5);
        System.out.println("搜索结果数量：" + searchResults.size());
        for (ProjectExperience p : searchResults) {
            System.out.println("  " + p.getProjectName());
        }

        // 5. 测试根据技术栈搜索
        System.out.println("\n5. 测试根据技术栈搜索（技术栈：Docker）：");
        List<ProjectExperience> techResults = projectDAO.searchProjectsByTechStack("Docker", 1, 5);
        System.out.println("搜索结果数量：" + techResults.size());
        for (ProjectExperience p : techResults) {
            System.out.println("  " + p.getProjectName() + " - " + p.getTechStack());
        }

        // 6. 测试统计功能
        System.out.println("\n6. 测试技术栈统计：");
        List<Object[]> topTech = projectDAO.getTopTechStacks(5);
        for (Object[] row : topTech) {
            System.out.println("  " + row[0] + ": " + row[1] + "个项目");
        }

        // 7. 测试角色分布
        System.out.println("\n7. 测试角色分布：");
        List<Object[]> roleDist = projectDAO.getRoleDistribution();
        for (Object[] row : roleDist) {
            System.out.println("  " + row[0] + ": " + row[1] + "个");
        }

        // 8. 测试分页查询
        System.out.println("\n8. 测试分页查询所有项目（第1页，每页5条）：");
        List<ProjectExperience> allProjects = projectDAO.getAllProjects(1, 5);
        System.out.println("第1页共" + allProjects.size() + "条记录");
        for (ProjectExperience p : allProjects) {
            System.out.println("  " + p.getProjectName());
        }

        // 9. 测试更新项目
        System.out.println("\n9. 测试更新项目：");
        if (project != null) {
            project.setRole("架构师");
            project.setAchievement("项目获得公司年度创新奖");
            boolean updated = projectDAO.updateProject(project);
            System.out.println("更新" + (updated ? "成功" : "失败"));

            ProjectExperience updatedProject = projectDAO.getProjectById(project.getProjectId());
            System.out.println("更新后角色：" + updatedProject.getRole());
            System.out.println("更新后成果：" + updatedProject.getAchievement());
        }

        // 10. 测试批量添加
        System.out.println("\n10. 测试批量添加项目：");
        List<ProjectExperience> batchProjects = new ArrayList<>();

        ProjectExperience p1 = new ProjectExperience();
        p1.setDeveloperId(2);
        p1.setProjectName("数据中台建设");
        p1.setRole("大数据开发");
        p1.setTechStack("Hadoop, Spark, Hive");

        ProjectExperience p2 = new ProjectExperience();
        p2.setDeveloperId(2);
        p2.setProjectName("实时计算平台");
        p2.setRole("平台开发");
        p2.setTechStack("Flink, Kafka, ClickHouse");

        batchProjects.add(p1);
        batchProjects.add(p2);

        int successCount = projectDAO.addProjects(batchProjects);
        System.out.println("批量添加成功：" + successCount + "个");

        // 11. 测试项目总数
        System.out.println("\n11. 项目总数：" + projectDAO.getProjectCount());

        // 12. 测试删除项目
        System.out.println("\n12. 测试删除项目：");
        if (project != null) {
            boolean deleted = projectDAO.deleteProject(project.getProjectId());
            System.out.println("删除" + (deleted ? "成功" : "失败"));

            ProjectExperience deletedProject = projectDAO.getProjectById(project.getProjectId());
            System.out.println("删除后查询：" + (deletedProject == null ? "项目不存在" : "项目还在"));
        }

        System.out.println("\n========== 测试完成 ==========");
    }
}