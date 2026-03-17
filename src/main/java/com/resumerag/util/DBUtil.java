package com.resumerag.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * 数据库工具类 - 使用HikariCP连接池
 */
public class DBUtil {

    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);
    private static HikariDataSource dataSource;
    private static Properties props = new Properties();

    static {
        try {
            // 加载配置文件
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            props.load(is);

            // 添加调试输出，查看实际加载的值
            logger.debug("加载的数据库配置：URL: {}, 用户名: {}", 
                props.getProperty("db.url"), 
                props.getProperty("db.username"));
            logger.debug("密码长度: {}", props.getProperty("db.password").length());

            // 配置HikariCP
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(props.getProperty("db.driver"));
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));

            // 连接池配置
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.maximumPoolSize", "10")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.connectionTimeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.idleTimeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.maxLifetime", "1800000")));

            // 连接测试
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);

            // 开启监控
            config.setLeakDetectionThreshold(10000);

            dataSource = new HikariDataSource(config);

            logger.info("数据库连接池初始化成功");

        } catch (Exception e) {
            logger.error("数据库连接池初始化失败", e);
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 关闭资源
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { logger.warn("关闭ResultSet失败", e); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { logger.warn("关闭Statement失败", e); }
        try { if (conn != null) conn.close(); } catch (SQLException e) { logger.warn("关闭Connection失败", e); }
    }

    /**
     * 关闭数据源
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * 测试连接
     */
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = getConnection();
            logger.info("数据库连接成功！数据库: {}", conn.getCatalog());

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT NOW() as time");
            if (rs.next()) {
                logger.info("当前时间: {}", rs.getString("time"));
            }

        } catch (SQLException e) {
            logger.error("数据库连接测试失败", e);
        } finally {
            close(null, null, conn);
            closeDataSource();
        }
    }
}