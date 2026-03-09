package com.resumerag.dao;

import com.resumerag.model.ExportRecord;
import com.resumerag.model.Developer;
import com.resumerag.model.User;
import com.resumerag.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 导出记录数据访问对象
 * 用于记录简历导出历史
 */
public class ExportRecordDAO {

    private DeveloperDAO developerDAO = new DeveloperDAO();
    private UserDAO userDAO = new UserDAO();

    // ===================== 基础CRUD =====================

    /**
     * 1. 添加导出记录
     */
    public boolean addExportRecord(ExportRecord record) {
        String sql = "INSERT INTO ExportRecords (developer_id, user_id, export_time, file_name) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, record.getDeveloperId());
            pstmt.setObject(2, record.getUserId());  // 允许为null
            pstmt.setTimestamp(3, Timestamp.valueOf(record.getExportTime() != null ?
                    record.getExportTime() : LocalDateTime.now()));
            pstmt.setString(4, record.getFileName());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    record.setExportId(rs.getInt(1));
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
     * 2. 根据ID删除导出记录
     */
    public boolean deleteExportRecord(int exportId) {
        String sql = "DELETE FROM ExportRecords WHERE export_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, exportId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 3. 更新导出记录（一般不需要，但保留接口）
     */
    public boolean updateExportRecord(ExportRecord record) {
        String sql = "UPDATE ExportRecords SET file_name = ? WHERE export_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getFileName());
            pstmt.setInt(2, record.getExportId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 4. 根据ID查询导出记录（包含关联信息）
     */
    public ExportRecord getExportRecordById(int exportId) {
        String sql = "SELECT e.*, d.name as developer_name, u.username " +
                "FROM ExportRecords e " +
                "LEFT JOIN Developers d ON e.developer_id = d.developer_id " +
                "LEFT JOIN Users u ON e.user_id = u.user_id " +
                "WHERE e.export_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, exportId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractExportRecordFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 5. 查询所有导出记录（分页，包含关联信息）
     */
    public List<ExportRecord> getAllExportRecords(int pageNum, int pageSize) {
        List<ExportRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name, u.username " +
                "FROM ExportRecords e " +
                "LEFT JOIN Developers d ON e.developer_id = d.developer_id " +
                "LEFT JOIN Users u ON e.user_id = u.user_id " +
                "ORDER BY e.export_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractExportRecordFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 6. 获取导出记录总数
     */
    public int getExportRecordCount() {
        String sql = "SELECT COUNT(*) FROM ExportRecords";
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

    // ===================== 按条件查询 =====================

    /**
     * 7. 根据开发者ID查询导出记录
     */
    public List<ExportRecord> getExportRecordsByDeveloperId(int developerId, int pageNum, int pageSize) {
        List<ExportRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name, u.username " +
                "FROM ExportRecords e " +
                "LEFT JOIN Developers d ON e.developer_id = d.developer_id " +
                "LEFT JOIN Users u ON e.user_id = u.user_id " +
                "WHERE e.developer_id = ? " +
                "ORDER BY e.export_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractExportRecordFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 8. 根据用户ID查询导出记录（谁导出的）
     */
    public List<ExportRecord> getExportRecordsByUserId(int userId, int pageNum, int pageSize) {
        List<ExportRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name, u.username " +
                "FROM ExportRecords e " +
                "LEFT JOIN Developers d ON e.developer_id = d.developer_id " +
                "LEFT JOIN Users u ON e.user_id = u.user_id " +
                "WHERE e.user_id = ? " +
                "ORDER BY e.export_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractExportRecordFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 9. 根据时间范围查询导出记录
     */
    public List<ExportRecord> getExportRecordsByDateRange(LocalDateTime startTime, LocalDateTime endTime,
                                                          int pageNum, int pageSize) {
        List<ExportRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name, u.username " +
                "FROM ExportRecords e " +
                "LEFT JOIN Developers d ON e.developer_id = d.developer_id " +
                "LEFT JOIN Users u ON e.user_id = u.user_id " +
                "WHERE e.export_time BETWEEN ? AND ? " +
                "ORDER BY e.export_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startTime));
            pstmt.setTimestamp(2, Timestamp.valueOf(endTime));
            pstmt.setInt(3, pageSize);
            pstmt.setInt(4, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractExportRecordFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 10. 根据文件名模糊查询
     */
    public List<ExportRecord> searchExportRecordsByFileName(String fileName, int pageNum, int pageSize) {
        List<ExportRecord> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT e.*, d.name as developer_name, u.username " +
                "FROM ExportRecords e " +
                "LEFT JOIN Developers d ON e.developer_id = d.developer_id " +
                "LEFT JOIN Users u ON e.user_id = u.user_id " +
                "WHERE e.file_name LIKE ? " +
                "ORDER BY e.export_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + fileName + "%");
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractExportRecordFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===================== 统计分析 =====================

    /**
     * 11. 统计最常被导出的开发者
     */
    public List<Object[]> getTopExportedDevelopers(int limit) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT d.developer_id, d.name, COUNT(e.export_id) as export_count " +
                "FROM Developers d " +
                "LEFT JOIN ExportRecords e ON d.developer_id = e.developer_id " +
                "GROUP BY d.developer_id, d.name " +
                "ORDER BY export_count DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getString("name");
                row[1] = rs.getInt("export_count");
                row[2] = rs.getInt("developer_id");
                list.add(row);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 12. 统计最活跃的导出用户
     */
    public List<Object[]> getMostActiveExporters(int limit) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT u.user_id, u.username, COUNT(e.export_id) as export_count " +
                "FROM Users u " +
                "LEFT JOIN ExportRecords e ON u.user_id = e.user_id " +
                "GROUP BY u.user_id, u.username " +
                "ORDER BY export_count DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getString("username");
                row[1] = rs.getInt("export_count");
                row[2] = rs.getInt("user_id");
                list.add(row);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 13. 统计每日导出量
     */
    public List<Object[]> getDailyExportCount(int days) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT DATE(export_time) as export_date, COUNT(*) as count " +
                "FROM ExportRecords " +
                "WHERE export_time >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "GROUP BY DATE(export_time) " +
                "ORDER BY export_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, days);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getDate("export_date");
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
     * 14. 统计导出文件类型分布（根据文件扩展名）
     */
    public List<Object[]> getFileTypeDistribution() {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT " +
                "CASE " +
                "  WHEN file_name LIKE '%.pdf' THEN 'PDF' " +
                "  WHEN file_name LIKE '%.doc' THEN 'DOC' " +
                "  WHEN file_name LIKE '%.docx' THEN 'DOCX' " +
                "  WHEN file_name LIKE '%.txt' THEN 'TXT' " +
                "  ELSE '其他' " +
                "END as file_type, " +
                "COUNT(*) as count " +
                "FROM ExportRecords " +
                "GROUP BY file_type " +
                "ORDER BY count DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("file_type");
                row[1] = rs.getInt("count");
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 15. 获取最近N条导出记录（用于首页显示）
     */
    public List<ExportRecord> getRecentExports(int limit) {
        List<ExportRecord> list = new ArrayList<>();

        String sql = "SELECT e.*, d.name as developer_name, u.username " +
                "FROM ExportRecords e " +
                "LEFT JOIN Developers d ON e.developer_id = d.developer_id " +
                "LEFT JOIN Users u ON e.user_id = u.user_id " +
                "ORDER BY e.export_time DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractExportRecordFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===================== 批量操作 =====================

    /**
     * 16. 批量添加导出记录（事务处理）
     */
    public int addExportRecords(List<ExportRecord> records) {
        int successCount = 0;
        String sql = "INSERT INTO ExportRecords (developer_id, user_id, export_time, file_name) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql);

            for (ExportRecord record : records) {
                pstmt.setInt(1, record.getDeveloperId());
                pstmt.setObject(2, record.getUserId());
                pstmt.setTimestamp(3, Timestamp.valueOf(record.getExportTime() != null ?
                        record.getExportTime() : LocalDateTime.now()));
                pstmt.setString(4, record.getFileName());
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
     * 17. 批量删除导出记录（事务处理）
     */
    public boolean deleteExportRecords(List<Integer> exportIds) {
        if (exportIds == null || exportIds.isEmpty()) return false;

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            StringBuilder sql = new StringBuilder("DELETE FROM ExportRecords WHERE export_id IN (");
            for (int i = 0; i < exportIds.size(); i++) {
                if (i > 0) sql.append(",");
                sql.append("?");
            }
            sql.append(")");

            pstmt = conn.prepareStatement(sql.toString());

            for (int i = 0; i < exportIds.size(); i++) {
                pstmt.setInt(i + 1, exportIds.get(i));
            }

            pstmt.executeUpdate();
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

    /**
     * 18. 清理旧导出记录（保留最近N天）
     */
    public int cleanOldRecords(int keepDays) {
        String sql = "DELETE FROM ExportRecords WHERE export_time < DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, keepDays);
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ===================== 私有辅助方法 =====================

    private ExportRecord extractExportRecordFromResultSet(ResultSet rs) throws SQLException {
        ExportRecord record = new ExportRecord();
        record.setExportId(rs.getInt("export_id"));
        record.setDeveloperId(rs.getInt("developer_id"));
        record.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        record.setFileName(rs.getString("file_name"));

        Timestamp ts = rs.getTimestamp("export_time");
        if (ts != null) {
            record.setExportTime(ts.toLocalDateTime());
        }

        // 如果有开发者名称，可以创建关联对象
        try {
            String devName = rs.getString("developer_name");
            if (devName != null) {
                Developer dev = new Developer();
                dev.setDeveloperId(record.getDeveloperId());
                dev.setName(devName);
                record.setDeveloper(dev);
            }
        } catch (SQLException e) {
            // 没有这个字段就忽略
        }

        try {
            String username = rs.getString("username");
            if (username != null && record.getUserId() != null) {
                User user = new User();
                user.setUserId(record.getUserId());
                user.setUsername(username);
                record.setUser(user);
            }
        } catch (SQLException e) {
            // 没有这个字段就忽略
        }

        return record;
    }

    // ===================== 测试方法 =====================

    public static void main(String[] args) {
        ExportRecordDAO exportDAO = new ExportRecordDAO();

        System.out.println("========== 测试ExportRecordDAO ==========");

        // 1. 测试添加导出记录
        System.out.println("\n1. 测试添加导出记录：");
        ExportRecord record1 = new ExportRecord();
        record1.setDeveloperId(1);  // 张三
        record1.setUserId(1);        // admin
        record1.setFileName("张三_简历_20240115.pdf");
        record1.setExportTime(LocalDateTime.now());

        ExportRecord record2 = new ExportRecord();
        record2.setDeveloperId(2);  // 李四
        record2.setUserId(2);        // zhangsan
        record2.setFileName("李四_简历_20240115.docx");
        record2.setExportTime(LocalDateTime.now().minusHours(2));

        boolean added1 = exportDAO.addExportRecord(record1);
        boolean added2 = exportDAO.addExportRecord(record2);
        System.out.println("记录1添加" + (added1 ? "成功" : "失败") + "，ID：" + record1.getExportId());
        System.out.println("记录2添加" + (added2 ? "成功" : "失败") + "，ID：" + record2.getExportId());

        // 2. 测试根据ID查询
        System.out.println("\n2. 测试根据ID查询：");
        ExportRecord record = exportDAO.getExportRecordById(record1.getExportId());
        if (record != null) {
            System.out.println("文件名：" + record.getFileName());
            System.out.println("开发者ID：" + record.getDeveloperId());
            System.out.println("用户ID：" + record.getUserId());
            System.out.println("导出时间：" + record.getExportTime());
            if (record.getDeveloper() != null) {
                System.out.println("开发者姓名：" + record.getDeveloper().getName());
            }
            if (record.getUser() != null) {
                System.out.println("用户名：" + record.getUser().getUsername());
            }
        }

        // 3. 测试分页查询
        System.out.println("\n3. 测试分页查询所有记录：");
        List<ExportRecord> allRecords = exportDAO.getAllExportRecords(1, 10);
        System.out.println("第1页共" + allRecords.size() + "条记录");
        for (ExportRecord r : allRecords) {
            String devName = r.getDeveloper() != null ? r.getDeveloper().getName() : "未知";
            String userName = r.getUser() != null ? r.getUser().getUsername() : "未知";
            System.out.println("  [" + r.getExportTime() + "] " + devName +
                    " 被 " + userName + " 导出 -> " + r.getFileName());
        }

        // 4. 测试根据开发者查询
        System.out.println("\n4. 测试根据开发者ID查询：");
        List<ExportRecord> devRecords = exportDAO.getExportRecordsByDeveloperId(1, 1, 5);
        System.out.println("开发者1的导出记录：");
        for (ExportRecord r : devRecords) {
            System.out.println("  " + r.getFileName());
        }

        // 5. 测试根据用户查询
        System.out.println("\n5. 测试根据用户ID查询：");
        List<ExportRecord> userRecords = exportDAO.getExportRecordsByUserId(1, 1, 5);
        System.out.println("用户1的导出记录：");
        for (ExportRecord r : userRecords) {
            System.out.println("  " + r.getFileName());
        }

        // 6. 测试文件名搜索
        System.out.println("\n6. 测试文件名搜索（关键词：张三）：");
        List<ExportRecord> searchResults = exportDAO.searchExportRecordsByFileName("张三", 1, 5);
        for (ExportRecord r : searchResults) {
            System.out.println("  " + r.getFileName());
        }

        // 7. 测试最近导出记录
        System.out.println("\n7. 测试最近5条导出记录：");
        List<ExportRecord> recent = exportDAO.getRecentExports(5);
        for (ExportRecord r : recent) {
            System.out.println("  " + r.getFileName());
        }

        // 8. 测试统计功能
        System.out.println("\n8. 测试最常被导出的开发者TOP 3：");
        List<Object[]> topDevelopers = exportDAO.getTopExportedDevelopers(3);
        for (Object[] row : topDevelopers) {
            System.out.println("  " + row[0] + ": " + row[1] + "次");
        }

        System.out.println("\n9. 测试最活跃的导出用户TOP 3：");
        List<Object[]> topUsers = exportDAO.getMostActiveExporters(3);
        for (Object[] row : topUsers) {
            System.out.println("  " + row[0] + ": " + row[1] + "次");
        }

        System.out.println("\n10. 测试文件类型分布：");
        List<Object[]> fileTypes = exportDAO.getFileTypeDistribution();
        for (Object[] row : fileTypes) {
            System.out.println("  " + row[0] + ": " + row[1] + "个");
        }

        // 11. 测试总数
        System.out.println("\n11. 导出记录总数：" + exportDAO.getExportRecordCount());

        // 12. 测试更新
        System.out.println("\n12. 测试更新记录：");
        if (record != null) {
            record.setFileName("张三_简历_更新版.pdf");
            boolean updated = exportDAO.updateExportRecord(record);
            System.out.println("更新" + (updated ? "成功" : "失败"));

            ExportRecord updatedRecord = exportDAO.getExportRecordById(record.getExportId());
            System.out.println("更新后文件名：" + updatedRecord.getFileName());
        }

        // 13. 测试删除
        System.out.println("\n13. 测试删除记录：");
        if (record != null) {
            boolean deleted = exportDAO.deleteExportRecord(record.getExportId());
            System.out.println("删除" + (deleted ? "成功" : "失败"));

            ExportRecord deletedRecord = exportDAO.getExportRecordById(record.getExportId());
            System.out.println("删除后查询：" + (deletedRecord == null ? "记录不存在" : "记录还在"));
        }

        // 14. 测试批量操作
        System.out.println("\n14. 测试批量添加：");
        List<ExportRecord> batchList = new ArrayList<>();

        ExportRecord r1 = new ExportRecord();
        r1.setDeveloperId(1);
        r1.setUserId(1);
        r1.setFileName("批量导出1.pdf");

        ExportRecord r2 = new ExportRecord();
        r2.setDeveloperId(2);
        r2.setUserId(2);
        r2.setFileName("批量导出2.docx");

        batchList.add(r1);
        batchList.add(r2);

        int batchSuccess = exportDAO.addExportRecords(batchList);
        System.out.println("批量添加成功：" + batchSuccess + "条");

        System.out.println("\n========== 测试完成 ==========");
    }
}