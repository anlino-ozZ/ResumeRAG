package com.resumerag.util;

import java.sql.*;

public class DBUtil {
    // MySQL连接参数（根据你的配置修改）
    private static final String URL = "jdbc:mysql://localhost:3306/resume_rag?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USERNAME = "root";  // MySQL用户名
    private static final String PASSWORD = "Zal13715002181,"; // MySQL密码
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // 静态代码块，加载驱动
    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 获取连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    // 关闭资源（通用方法）
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
    // 测试连接
    public static void main(String[] args) {
        Connection conn = null;
        try {
            // 1. 尝试获取连接
            System.out.println("正在连接数据库...");
            conn = DBUtil.getConnection();

            // 2. 如果连接成功
            System.out.println("✅ 数据库连接成功！");
            System.out.println("连接的数据库: " + conn.getCatalog());
            System.out.println("MySQL版本: " + conn.getMetaData().getDatabaseProductVersion());

            // 3. 测试简单查询
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT NOW() as `current_time`");
            if (rs.next()) {
                System.out.println("当前时间: " + rs.getString("current_time"));
            }

            // 4. 关闭资源
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            // 如果连接失败，打印详细错误
            System.out.println("❌ 数据库连接失败！");
            System.out.println("错误信息: " + e.getMessage());
            System.out.println("\n请检查：");
            System.out.println("1. MySQL服务是否启动？");
            System.out.println("2. 用户名密码是否正确？");
            System.out.println("3. 数据库 'resume_rag' 是否创建？");
            e.printStackTrace();
        } finally {
            DBUtil.close(null, null, conn);
        }
    }
}