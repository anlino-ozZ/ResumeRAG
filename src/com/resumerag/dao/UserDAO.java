package com.resumerag.dao;

import com.resumerag.model.User;
import com.resumerag.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户表的数据访问对象
 * 包含用户的增删改查操作
 */
public class UserDAO {

    // ===================== 增删改查基础方法 =====================

    /**
     * 1. 添加用户
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO Users (username, password, role, created_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pstmt.executeUpdate();

            // 获取自动生成的主键
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
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
     * 2. 根据ID删除用户
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 3. 更新用户信息
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE Users SET username = ?, password = ?, role = ? WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getUserId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 4. 根据ID查询用户
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 5. 根据用户名查询用户（登录用）
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 6. 查询所有用户（分页）
     */
    public List<User> getAllUsers(int pageNum, int pageSize) {
        List<User> list = new ArrayList<>();
        int offset = (pageNum - 1) * pageSize;

        String sql = "SELECT * FROM Users ORDER BY user_id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractUserFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 7. 获取用户总数（用于分页）
     */
    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM Users";
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
     * 8. 用户登录验证
     * @return 登录成功返回User对象，失败返回null
     */
    public User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 9. 检查用户名是否存在（注册时用）
     */
    public boolean isUsernameExist(String username) {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 10. 根据角色查询用户
     */
    public List<User> getUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = ? ORDER BY user_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractUserFromResultSet(rs));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===================== 私有辅助方法 =====================

    /**
     * 从ResultSet中提取User对象
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));

        Timestamp ts = rs.getTimestamp("created_time");
        if (ts != null) {
            user.setCreatedTime(ts.toLocalDateTime());
        }

        return user;
    }

    // ===================== 测试方法 =====================

    /**
     * 测试UserDAO的所有功能
     */
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        System.out.println("========== 测试UserDAO ==========");

        // 1. 测试添加用户
        System.out.println("\n1. 测试添加用户：");
        User newUser = new User("test_user", "123456", "developer");
        boolean added = userDAO.addUser(newUser);
        System.out.println("添加" + (added ? "成功" : "失败") + "，用户ID：" + newUser.getUserId());

        // 2. 测试根据ID查询
        System.out.println("\n2. 测试根据ID查询：");
        User user = userDAO.getUserById(newUser.getUserId());
        System.out.println("查询结果：" + user);

        // 3. 测试根据用户名查询
        System.out.println("\n3. 测试根据用户名查询：");
        User user2 = userDAO.getUserByUsername("test_user");
        System.out.println("查询结果：" + user2);

        // 4. 测试登录
        System.out.println("\n4. 测试登录：");
        User loginUser = userDAO.login("test_user", "123456");
        System.out.println("登录" + (loginUser != null ? "成功" : "失败") + "：" + loginUser);

        // 5. 测试用户名是否存在
        System.out.println("\n5. 测试用户名是否存在：");
        boolean exist = userDAO.isUsernameExist("test_user");
        System.out.println("用户名 'test_user' " + (exist ? "已存在" : "不存在"));

        // 6. 测试查询所有用户（分页）
        System.out.println("\n6. 测试分页查询：");
        List<User> userList = userDAO.getAllUsers(1, 5);
        System.out.println("第1页，共" + userList.size() + "条记录：");
        for (User u : userList) {
            System.out.println("  " + u);
        }

        // 7. 测试获取用户总数
        System.out.println("\n7. 用户总数：" + userDAO.getUserCount());

        // 8. 测试根据角色查询
        System.out.println("\n8. 测试根据角色查询：");
        List<User> admins = userDAO.getUsersByRole("admin");
        System.out.println("管理员数量：" + admins.size());

        // 9. 测试更新用户
        System.out.println("\n9. 测试更新用户：");
        if (user != null) {
            user.setPassword("newpass");
            boolean updated = userDAO.updateUser(user);
            System.out.println("更新" + (updated ? "成功" : "失败"));

            // 验证更新
            User updatedUser = userDAO.getUserById(user.getUserId());
            System.out.println("更新后密码：" + updatedUser.getPassword());
        }

        // 10. 测试删除用户
        System.out.println("\n10. 测试删除用户：");
        if (user != null) {
            boolean deleted = userDAO.deleteUser(user.getUserId());
            System.out.println("删除" + (deleted ? "成功" : "失败"));

            // 验证删除
            User deletedUser = userDAO.getUserById(user.getUserId());
            System.out.println("删除后查询：" + (deletedUser == null ? "用户不存在" : "用户还在"));
        }

        System.out.println("\n========== 测试完成 ==========");
    }
}