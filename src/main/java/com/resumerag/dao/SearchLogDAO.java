package com.resumerag.dao;

import com.resumerag.model.SearchLog;
import com.resumerag.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 检索日志数据访问对象
 * 用于记录和统计用户的搜索行为
 */
public class SearchLogDAO {

    private static final Logger logger = LoggerFactory.getLogger(SearchLogDAO.class);

    // ===================== 基础CRUD =====================

    /**
     * 1. 添加搜索日志
     */
    public boolean addSearchLog(SearchLog log) {
        String sql = "INSERT INTO SearchLogs (search_keywords, skill_ids, result_count, search_time, user_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, log.getSearchKeywords());
            pstmt.setString(2, log.getSkillIds());
            pstmt.setInt(3, log.getResultCount());
            pstmt.setTimestamp(4, Timestamp.valueOf(log.getSearchTime() != null ?
                    log.getSearchTime() : LocalDateTime.now()));
            pstmt.setObject(5, log.getUserId());  // 允许为null

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    log.setLogId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            logger.error("添加搜索日志失败", e);
        }
        return false;
    }

    /**
     * 2. 根据ID删除日志
     */
    public boolean deleteSearchLog(int logId) {
        String sql = "DELETE FROM SearchLogs WHERE log_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, logId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("删除搜索日志失败", e);
        }
        return false;
    }

    /**
     * 3. 更新搜索日志（一般不需要，但保留接口）
     */
    public boolean updateSearchLog(SearchLog log) {
        String sql = "UPDATE SearchLogs SET search_keywords = ?, skill_ids = ?, " +
                "result_count = ?, user_id = ? WHERE log_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, log.getSearchKeywords());
            pstmt.setString(2, log.getSkillIds());
            pstmt.setInt(3, log.getResultCount());
            pstmt.setObject(4, log.getUserId());
            pstmt.setInt(5, log.getLogId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("更新搜索日志失败", e);
        }
        return false;
    }

    /**
     * 4. 根据ID查询日志
     */
    public SearchLog getSearchLogById(int logId) {
        String sql = "SELECT l.*, u.username FROM SearchLogs l " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "WHERE l.log_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, logId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSearchLogFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("根据ID查询搜索日志失败", e);
        }
        return null;
    }

    /**
     * 5. 查询所有日志（分页）
     */
    public List<SearchLog> getAllSearchLogs(int pageNum, int pageSize) {
        List<SearchLog> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT l.*, u.username FROM SearchLogs l " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "ORDER BY l.search_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractSearchLogFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("查询所有搜索日志失败", e);
        }
        return list;
    }

    /**
     * 6. 获取日志总数
     */
    public int getSearchLogCount() {
        String sql = "SELECT COUNT(*) FROM SearchLogs";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("获取搜索日志总数失败", e);
        }
        return 0;
    }

    // ===================== 按条件查询 =====================

    /**
     * 7. 根据用户ID查询日志
     */
    public List<SearchLog> getLogsByUserId(int userId, int pageNum, int pageSize) {
        List<SearchLog> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT l.*, u.username FROM SearchLogs l " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "WHERE l.user_id = ? " +
                "ORDER BY l.search_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractSearchLogFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("根据用户ID查询搜索日志失败", e);
        }
        return list;
    }

    /**
     * 8. 根据时间范围查询
     */
    public List<SearchLog> getLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize) {
        List<SearchLog> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT l.*, u.username FROM SearchLogs l " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "WHERE l.search_time BETWEEN ? AND ? " +
                "ORDER BY l.search_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startTime));
            pstmt.setTimestamp(2, Timestamp.valueOf(endTime));
            pstmt.setInt(3, pageSize);
            pstmt.setInt(4, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractSearchLogFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("根据时间范围查询搜索日志失败", e);
        }
        return list;
    }

    /**
     * 9. 根据关键词模糊查询
     */
    public List<SearchLog> searchLogsByKeyword(String keyword, int pageNum, int pageSize) {
        List<SearchLog> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT l.*, u.username FROM SearchLogs l " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "WHERE l.search_keywords LIKE ? OR l.skill_ids LIKE ? " +
                "ORDER BY l.search_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setInt(3, pageSize);
            pstmt.setInt(4, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractSearchLogFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("根据关键词搜索日志失败", e);
        }
        return list;
    }

    /**
     * 10. 查询结果数为0的搜索（无结果搜索）
     */
    public List<SearchLog> getZeroResultLogs(int pageNum, int pageSize) {
        List<SearchLog> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT l.*, u.username FROM SearchLogs l " +
                "LEFT JOIN Users u ON l.user_id = u.user_id " +
                "WHERE l.result_count = 0 " +
                "ORDER BY l.search_time DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractSearchLogFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("查询无结果搜索日志失败", e);
        }
        return list;
    }

    // ===================== 统计分析 =====================

    /**
     * 11. 统计热门搜索关键词
     */
    public List<Object[]> getTopSearchKeywords(int limit) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT search_keywords, COUNT(*) as search_count " +
                "FROM SearchLogs " +
                "WHERE search_keywords IS NOT NULL AND search_keywords != '' " +
                "GROUP BY search_keywords " +
                "ORDER BY search_count DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("search_keywords");
                row[1] = rs.getInt("search_count");
                list.add(row);
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("统计热门搜索关键词失败", e);
        }
        return list;
    }

    /**
     * 12. 统计热门技能搜索
     */
    public List<Object[]> getTopSearchedSkills(int limit) {
        List<Object[]> list = new ArrayList<>();

        // 需要解析skill_ids字段（逗号分隔的技能ID）
        String sql = "SELECT skill_ids, COUNT(*) as search_count " +
                "FROM SearchLogs " +
                "WHERE skill_ids IS NOT NULL AND skill_ids != '' " +
                "GROUP BY skill_ids " +
                "ORDER BY search_count DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getString("skill_ids");
                row[1] = rs.getInt("search_count");
                list.add(row);
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("统计热门技能搜索失败", e);
        }
        return list;
    }

    /**
     * 13. 统计每日搜索量
     */
    public List<Object[]> getDailySearchCount(int days) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT DATE(search_time) as search_date, COUNT(*) as count " +
                "FROM SearchLogs " +
                "WHERE search_time >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "GROUP BY DATE(search_time) " +
                "ORDER BY search_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, days);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getDate("search_date");
                row[1] = rs.getInt("count");
                list.add(row);
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("统计每日搜索量失败", e);
        }
        return list;
    }

    /**
     * 14. 统计每小时搜索量（用于分析使用高峰）
     */
    public List<Object[]> getHourlySearchDistribution() {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT HOUR(search_time) as hour, COUNT(*) as count " +
                "FROM SearchLogs " +
                "GROUP BY HOUR(search_time) " +
                "ORDER BY hour";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = new Object[2];
                row[0] = rs.getInt("hour") + ":00";
                row[1] = rs.getInt("count");
                list.add(row);
            }

        } catch (SQLException e) {
            logger.error("统计每小时搜索量失败", e);
        }
        return list;
    }

    /**
     * 15. 统计用户搜索活跃度
     */
    public List<Object[]> getUserSearchActivity(int limit) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT u.user_id, u.username, COUNT(l.log_id) as search_count " +
                "FROM Users u " +
                "LEFT JOIN SearchLogs l ON u.user_id = l.user_id " +
                "GROUP BY u.user_id, u.username " +
                "ORDER BY search_count DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getString("username");
                row[1] = rs.getInt("search_count");
                row[2] = rs.getInt("user_id");
                list.add(row);
            }
            rs.close();

        } catch (SQLException e) {
            logger.error("统计用户搜索活跃度失败", e);
        }
        return list;
    }

    /**
     * 16. 统计平均结果数量
     */
    public double getAverageResultCount() {
        String sql = "SELECT AVG(result_count) FROM SearchLogs";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("统计平均结果数量失败", e);
        }
        return 0;
    }

    /**
     * 17. 统计搜索成功率（结果>0的比例）
     */
    public double getSearchSuccessRate() {
        String sql = "SELECT " +
                "SUM(CASE WHEN result_count > 0 THEN 1 ELSE 0 END) * 100.0 / COUNT(*) " +
                "FROM SearchLogs";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            logger.error("统计搜索成功率失败", e);
        }
        return 0;
    }

    // ===================== 清理和维护 =====================

    /**
     * 18. 清理指定日期之前的日志
     */
    public int cleanOldLogs(LocalDateTime beforeDate) {
        String sql = "DELETE FROM SearchLogs WHERE search_time < ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(beforeDate));
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("清理旧日志失败", e);
        }
        return 0;
    }

    /**
     * 19. 批量删除日志
     */
    public boolean deleteLogs(List<Integer> logIds) {
        if (logIds == null || logIds.isEmpty()) return false;

        // 构建IN查询
        StringBuilder sql = new StringBuilder("DELETE FROM SearchLogs WHERE log_id IN (");
        for (int i = 0; i < logIds.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < logIds.size(); i++) {
                pstmt.setInt(i + 1, logIds.get(i));
            }

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.error("批量删除日志失败", e);
        }
        return false;
    }

    // ===================== 私有辅助方法 =====================

    private SearchLog extractSearchLogFromResultSet(ResultSet rs) throws SQLException {
        SearchLog log = new SearchLog();
        log.setLogId(rs.getInt("log_id"));
        log.setSearchKeywords(rs.getString("search_keywords"));
        log.setSkillIds(rs.getString("skill_ids"));
        log.setResultCount(rs.getInt("result_count"));
        log.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);

        Timestamp ts = rs.getTimestamp("search_time");
        if (ts != null) {
            log.setSearchTime(ts.toLocalDateTime());
        }

        return log;
    }

    // ===================== 测试方法 =====================

    public static void main(String[] args) {
        SearchLogDAO logDAO = new SearchLogDAO();

        System.out.println("========== 测试SearchLogDAO ==========");

        // 1. 测试添加日志
        System.out.println("\n1. 测试添加搜索日志：");
        SearchLog log1 = new SearchLog();
        log1.setSearchKeywords("Java Spring Boot");
        log1.setSkillIds("1,3");
        log1.setResultCount(5);
        log1.setUserId(1);  // admin用户
        log1.setSearchTime(LocalDateTime.now());

        SearchLog log2 = new SearchLog();
        log2.setSearchKeywords("Python Django");
        log2.setSkillIds("2,9");
        log2.setResultCount(0);  // 无结果
        log2.setUserId(2);  // zhangsan

        boolean added1 = logDAO.addSearchLog(log1);
        boolean added2 = logDAO.addSearchLog(log2);
        System.out.println("日志1添加" + (added1 ? "成功" : "失败") + "，ID：" + log1.getLogId());
        System.out.println("日志2添加" + (added2 ? "成功" : "失败") + "，ID：" + log2.getLogId());

        // 2. 测试根据ID查询
        System.out.println("\n2. 测试根据ID查询：");
        SearchLog log = logDAO.getSearchLogById(log1.getLogId());
        if (log != null) {
            System.out.println("关键词：" + log.getSearchKeywords());
            System.out.println("技能ID：" + log.getSkillIds());
            System.out.println("结果数：" + log.getResultCount());
            System.out.println("搜索时间：" + log.getSearchTime());
        }

        // 3. 测试分页查询
        System.out.println("\n3. 测试分页查询所有日志：");
        List<SearchLog> allLogs = logDAO.getAllSearchLogs(1, 10);
        System.out.println("第1页共" + allLogs.size() + "条记录");
        for (SearchLog l : allLogs) {
            System.out.println("  [" + l.getSearchTime() + "] " + l.getSearchKeywords() +
                    " -> " + l.getResultCount() + "条结果");
        }

        // 4. 测试根据用户查询
        System.out.println("\n4. 测试根据用户ID查询：");
        List<SearchLog> userLogs = logDAO.getLogsByUserId(1, 1, 5);
        System.out.println("用户1的搜索记录：");
        for (SearchLog l : userLogs) {
            System.out.println("  " + l.getSearchKeywords());
        }

        // 5. 测试无结果搜索
        System.out.println("\n5. 测试查询无结果搜索：");
        List<SearchLog> zeroLogs = logDAO.getZeroResultLogs(1, 5);
        System.out.println("无结果的搜索数量：" + zeroLogs.size());
        for (SearchLog l : zeroLogs) {
            System.out.println("  " + l.getSearchKeywords());
        }

        // 6. 测试关键词搜索
        System.out.println("\n6. 测试关键词搜索（关键词：Java）：");
        List<SearchLog> searchResults = logDAO.searchLogsByKeyword("Java", 1, 5);
        for (SearchLog l : searchResults) {
            System.out.println("  " + l.getSearchKeywords());
        }

        // 7. 测试热门关键词统计
        System.out.println("\n7. 测试热门搜索关键词TOP 5：");
        List<Object[]> topKeywords = logDAO.getTopSearchKeywords(5);
        for (Object[] row : topKeywords) {
            System.out.println("  " + row[0] + ": " + row[1] + "次");
        }

        // 8. 测试每日搜索量
        System.out.println("\n8. 测试最近7天搜索量：");
        List<Object[]> dailyStats = logDAO.getDailySearchCount(7);
        for (Object[] row : dailyStats) {
            System.out.println("  " + row[0] + ": " + row[1] + "次");
        }

        // 9. 测试小时分布
        System.out.println("\n9. 测试小时分布：");
        List<Object[]> hourlyStats = logDAO.getHourlySearchDistribution();
        for (Object[] row : hourlyStats) {
            System.out.println("  " + row[0] + ": " + row[1] + "次");
        }

        // 10. 测试用户活跃度
        System.out.println("\n10. 测试用户搜索活跃度：");
        List<Object[]> userActivity = logDAO.getUserSearchActivity(5);
        for (Object[] row : userActivity) {
            System.out.println("  " + row[0] + ": " + row[1] + "次搜索");
        }

        // 11. 测试平均结果数和成功率
        System.out.println("\n11. 测试统计指标：");
        double avgResult = logDAO.getAverageResultCount();
        double successRate = logDAO.getSearchSuccessRate();
        System.out.println("  平均结果数：" + String.format("%.2f", avgResult));
        System.out.println("  搜索成功率：" + String.format("%.2f", successRate) + "%");

        // 12. 测试总数
        System.out.println("\n12. 日志总数：" + logDAO.getSearchLogCount());

        // 13. 测试删除
        System.out.println("\n13. 测试删除日志：");
        if (log != null) {
            boolean deleted = logDAO.deleteSearchLog(log.getLogId());
            System.out.println("删除" + (deleted ? "成功" : "失败"));

            SearchLog deletedLog = logDAO.getSearchLogById(log.getLogId());
            System.out.println("删除后查询：" + (deletedLog == null ? "日志不存在" : "日志还在"));
        }

        // 14. 测试批量删除
        System.out.println("\n14. 测试批量删除：");
        List<Integer> ids = new ArrayList<>();
        ids.add(log2.getLogId());
        // 可以添加更多ID
        boolean batchDeleted = logDAO.deleteLogs(ids);
        System.out.println("批量删除" + (batchDeleted ? "成功" : "失败"));

        System.out.println("\n========== 测试完成 ==========");
    }
}