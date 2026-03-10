package com.resumerag.dao;

import com.resumerag.model.EducationRecord;
import com.resumerag.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 教育记录数据访问对象
 * 包含教育经历的增删改查
 */
public class EducationRecordDAO {

    // ===================== 基础CRUD =====================

    /**
     * 1. 添加教育记录
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
     * 2. 根据ID删除教育记录
     */
    public boolean deleteEducationRecord(int educationId) {
        String sql = "DELETE FROM EducationRecords WHERE education_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, educationId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 3. 更新教育记录
     */
    public boolean updateEducationRecord(EducationRecord education) {
        String sql = "UPDATE EducationRecords SET school = ?, major = ?, degree = ?, " +
                "start_date = ?, end_date = ? WHERE education_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, education.getSchool());
            pstmt.setString(2, education.getMajor());
            pstmt.setString(3, education.getDegree());
            pstmt.setDate(4, education.getStartDate() != null ? Date.valueOf(education.getStartDate()) : null);
            pstmt.setDate(5, education.getEndDate() != null ? Date.valueOf(education.getEndDate()) : null);
            pstmt.setInt(6, education.getEducationId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 4. 根据ID查询教育记录
     */
    public EducationRecord getEducationRecordById(int educationId) {
        String sql = "SELECT * FROM EducationRecords WHERE education_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, educationId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractEducationFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 5. 根据开发者ID查询所有教育记录
     */
    public List<EducationRecord> getEducationRecordsByDeveloperId(int developerId) {
        return getEducationRecordsByDeveloperId(developerId, 1, 100);
    }

    /**
     * 根据开发者ID查询教育记录（分页）
     */
    public List<EducationRecord> getEducationRecordsByDeveloperId(int developerId, int pageNum, int pageSize) {
        List<EducationRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;
        String sql = "SELECT * FROM EducationRecords WHERE developer_id = ? ORDER BY start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
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

    /**
     * 6. 查询所有教育记录（分页）
     */
    public List<EducationRecord> getAllEducationRecords(int pageNum, int pageSize) {
        List<EducationRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name " +
                "FROM EducationRecords e " +
                "INNER JOIN Developers d ON e.developer_id = d.developer_id " +
                "ORDER BY e.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
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

    /**
     * 7. 获取教育记录总数
     */
    public int getEducationRecordCount() {
        String sql = "SELECT COUNT(*) FROM EducationRecords";
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
     * 8. 获取某个开发者的教育记录数量
     */
    public int getEducationCountByDeveloper(int developerId) {
        String sql = "SELECT COUNT(*) FROM EducationRecords WHERE developer_id = ?";
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
     * 9. 根据学校搜索
     */
    public List<EducationRecord> searchBySchool(String school, int pageNum, int pageSize) {
        List<EducationRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name " +
                "FROM EducationRecords e " +
                "INNER JOIN Developers d ON e.developer_id = d.developer_id " +
                "WHERE e.school LIKE ? " +
                "ORDER BY e.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + school + "%");
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

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

    /**
     * 10. 根据专业搜索
     */
    public List<EducationRecord> searchByMajor(String major, int pageNum, int pageSize) {
        List<EducationRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name " +
                "FROM EducationRecords e " +
                "INNER JOIN Developers d ON e.developer_id = d.developer_id " +
                "WHERE e.major LIKE ? " +
                "ORDER BY e.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + major + "%");
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

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

    /**
     * 11. 根据学历搜索
     */
    public List<EducationRecord> searchByDegree(String degree, int pageNum, int pageSize) {
        List<EducationRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name " +
                "FROM EducationRecords e " +
                "INNER JOIN Developers d ON e.developer_id = d.developer_id " +
                "WHERE e.degree = ? " +
                "ORDER BY e.start_date DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, degree);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);

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

    /**
     * 12. 复合搜索
     */
    public List<EducationRecord> advancedSearch(String school, String major, String degree,
                                                Integer minYear, Integer maxYear, int pageNum, int pageSize) {
        List<EducationRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.*, d.name as developer_name ");
        sql.append("FROM EducationRecords e ");
        sql.append("INNER JOIN Developers d ON e.developer_id = d.developer_id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (school != null && !school.trim().isEmpty()) {
            sql.append("AND e.school LIKE ? ");
            params.add("%" + school + "%");
        }

        if (major != null && !major.trim().isEmpty()) {
            sql.append("AND e.major LIKE ? ");
            params.add("%" + major + "%");
        }

        if (degree != null && !degree.trim().isEmpty()) {
            sql.append("AND e.degree = ? ");
            params.add(degree);
        }

        if (minYear != null) {
            sql.append("AND YEAR(e.start_date) >= ? ");
            params.add(minYear);
        }

        if (maxYear != null) {
            sql.append("AND YEAR(e.start_date) <= ? ");
            params.add(maxYear);
        }

        sql.append("ORDER BY e.start_date DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

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

    // ===================== 统计功能 =====================

    /**
     * 13. 统计学历分布
     */
    public List<Object[]> getDegreeDistribution() {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT degree, COUNT(*) as count FROM EducationRecords " +
                "WHERE degree IS NOT NULL AND degree != '' " +
                "GROUP BY degree ORDER BY count DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("degree");
                row[1] = rs.getInt("count");
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 14. 统计毕业学校TOP N
     */
    public List<Object[]> getTopSchools(int limit) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT school, COUNT(*) as count FROM EducationRecords " +
                "WHERE school IS NOT NULL AND school != '' " +
                "GROUP BY school ORDER BY count DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("school");
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
     * 15. 统计专业TOP N
     */
    public List<Object[]> getTopMajors(int limit) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT major, COUNT(*) as count FROM EducationRecords " +
                "WHERE major IS NOT NULL AND major != '' " +
                "GROUP BY major ORDER BY count DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("major");
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
     * 16. 统计毕业年份分布
     */
    public List<Object[]> getGraduationYearDistribution() {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT YEAR(end_date) as year, COUNT(*) as count " +
                "FROM EducationRecords WHERE end_date IS NOT NULL " +
                "GROUP BY YEAR(end_date) ORDER BY year DESC";

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
     * 17. 批量添加教育记录（事务处理）
     */
    public int addEducationRecords(List<EducationRecord> records) {
        int successCount = 0;
        String sql = "INSERT INTO EducationRecords (developer_id, school, major, degree, start_date, end_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql);

            for (EducationRecord record : records) {
                pstmt.setInt(1, record.getDeveloperId());
                pstmt.setString(2, record.getSchool());
                pstmt.setString(3, record.getMajor());
                pstmt.setString(4, record.getDegree());
                pstmt.setDate(5, record.getStartDate() != null ? Date.valueOf(record.getStartDate()) : null);
                pstmt.setDate(6, record.getEndDate() != null ? Date.valueOf(record.getEndDate()) : null);
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();

            for (int result : results) {
                if (result > 0) successCount++;
            }

        } catch (SQLException e) {
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
     * 18. 批量删除教育记录（事务处理）
     */
    public boolean deleteEducationRecords(List<Integer> educationIds) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String sql = "DELETE FROM EducationRecords WHERE education_id = ?";
            pstmt = conn.prepareStatement(sql);

            for (int id : educationIds) {
                pstmt.setInt(1, id);
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
        EducationRecordDAO eduDAO = new EducationRecordDAO();

        System.out.println("========== 测试EducationRecordDAO ==========");

        // 1. 测试添加教育记录
        System.out.println("\n1. 测试添加教育记录：");
        EducationRecord newEdu = new EducationRecord();
        newEdu.setDeveloperId(1);  // 张三
        newEdu.setSchool("清华大学");
        newEdu.setMajor("计算机科学与技术");
        newEdu.setDegree("硕士");
        newEdu.setStartDate(LocalDate.of(2018, 9, 1));
        newEdu.setEndDate(LocalDate.of(2021, 6, 30));

        boolean added = eduDAO.addEducationRecord(newEdu);
        System.out.println("添加" + (added ? "成功" : "失败") + "，教育记录ID：" + newEdu.getEducationId());

        // 2. 测试根据ID查询
        System.out.println("\n2. 测试根据ID查询：");
        EducationRecord edu = eduDAO.getEducationRecordById(newEdu.getEducationId());
        if (edu != null) {
            System.out.println("学校：" + edu.getSchool());
            System.out.println("专业：" + edu.getMajor());
            System.out.println("学历：" + edu.getDegree());
            System.out.println("时间：" + edu.getStartDate() + " 至 " + edu.getEndDate());
        }

        // 3. 测试根据开发者ID查询
        System.out.println("\n3. 测试根据开发者ID查询：");
        List<EducationRecord> devEdu = eduDAO.getEducationRecordsByDeveloperId(1);
        System.out.println("开发者1的教育记录数量：" + devEdu.size());
        for (EducationRecord e : devEdu) {
            System.out.println("  " + e.getSchool() + " - " + e.getMajor() + " (" + e.getDegree() + ")");
        }

        // 4. 测试根据学校搜索
        System.out.println("\n4. 测试根据学校搜索（清华）：");
        List<EducationRecord> schoolResults = eduDAO.searchBySchool("清华", 1, 5);
        for (EducationRecord e : schoolResults) {
            System.out.println("  " + e.getSchool() + " - " + e.getMajor());
        }

        // 5. 测试根据专业搜索
        System.out.println("\n5. 测试根据专业搜索（计算机）：");
        List<EducationRecord> majorResults = eduDAO.searchByMajor("计算机", 1, 5);
        for (EducationRecord e : majorResults) {
            System.out.println("  " + e.getMajor() + " - " + e.getSchool());
        }

        // 6. 测试根据学历搜索
        System.out.println("\n6. 测试根据学历搜索（硕士）：");
        List<EducationRecord> degreeResults = eduDAO.searchByDegree("硕士", 1, 5);
        for (EducationRecord e : degreeResults) {
            System.out.println("  " + e.getDegree() + " - " + e.getSchool());
        }

        // 7. 测试复合搜索
        System.out.println("\n7. 测试复合搜索（清华+计算机）：");
        List<EducationRecord> advancedResults = eduDAO.advancedSearch("清华", "计算机", null, null, null, 1, 5);
        for (EducationRecord e : advancedResults) {
            System.out.println("  " + e.getSchool() + " - " + e.getMajor());
        }

        // 8. 测试统计功能
        System.out.println("\n8. 测试学历分布：");
        List<Object[]> degreeDist = eduDAO.getDegreeDistribution();
        for (Object[] row : degreeDist) {
            System.out.println("  " + row[0] + ": " + row[1] + "人");
        }

        System.out.println("\n9. 测试TOP学校：");
        List<Object[]> topSchools = eduDAO.getTopSchools(3);
        for (Object[] row : topSchools) {
            System.out.println("  " + row[0] + ": " + row[1] + "人");
        }

        // 10. 测试分页查询
        System.out.println("\n10. 测试分页查询所有教育记录：");
        List<EducationRecord> allRecords = eduDAO.getAllEducationRecords(1, 5);
        System.out.println("第1页共" + allRecords.size() + "条记录");
        for (EducationRecord e : allRecords) {
            System.out.println("  " + e.getSchool() + " - " + e.getMajor());
        }

        // 11. 测试更新
        System.out.println("\n11. 测试更新教育记录：");
        if (edu != null) {
            edu.setDegree("博士");
            edu.setEndDate(LocalDate.of(2024, 6, 30));
            boolean updated = eduDAO.updateEducationRecord(edu);
            System.out.println("更新" + (updated ? "成功" : "失败"));

            EducationRecord updatedEdu = eduDAO.getEducationRecordById(edu.getEducationId());
            System.out.println("更新后学历：" + updatedEdu.getDegree());
            System.out.println("更新后毕业时间：" + updatedEdu.getEndDate());
        }

        // 12. 测试批量添加
        System.out.println("\n12. 测试批量添加：");
        List<EducationRecord> batchList = new ArrayList<>();

        EducationRecord e1 = new EducationRecord();
        e1.setDeveloperId(2);
        e1.setSchool("北京大学");
        e1.setMajor("软件工程");
        e1.setDegree("本科");

        EducationRecord e2 = new EducationRecord();
        e2.setDeveloperId(2);
        e2.setSchool("北京大学");
        e2.setMajor("计算机系统结构");
        e2.setDegree("硕士");

        batchList.add(e1);
        batchList.add(e2);

        int batchSuccess = eduDAO.addEducationRecords(batchList);
        System.out.println("批量添加成功：" + batchSuccess + "条");

        // 13. 测试总数
        System.out.println("\n13. 教育记录总数：" + eduDAO.getEducationRecordCount());

        // 14. 测试删除
        System.out.println("\n14. 测试删除教育记录：");
        if (edu != null) {
            boolean deleted = eduDAO.deleteEducationRecord(edu.getEducationId());
            System.out.println("删除" + (deleted ? "成功" : "失败"));

            EducationRecord deletedEdu = eduDAO.getEducationRecordById(edu.getEducationId());
            System.out.println("删除后查询：" + (deletedEdu == null ? "记录不存在" : "记录还在"));
        }

        System.out.println("\n========== 测试完成 ==========");
    }
}