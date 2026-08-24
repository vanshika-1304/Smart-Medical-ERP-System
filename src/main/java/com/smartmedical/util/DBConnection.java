package com.smartmedical.util;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * DBConnection — Oracle UCP Connection Pool
 * Min: 5, Max: 20 connections (as per BRD NFR)
 */
public class DBConnection {

    private static final Logger logger = Logger.getLogger(DBConnection.class.getName());

    // ── Change these to match your Oracle instance ──────────────────────────
    private static final String DB_URL      = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
    private static final String DB_USER     = "smartmedical";
    private static final String DB_PASSWORD = "SmartMed123";
    private static final int    MIN_POOL    = 5;
    private static final int    MAX_POOL    = 20;
    // ────────────────────────────────────────────────────────────────────────

    private static PoolDataSource pds;

    static {
        try {
            pds = PoolDataSourceFactory.getPoolDataSource();
            pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
            pds.setURL(DB_URL);
            pds.setUser(DB_USER);
            pds.setPassword(DB_PASSWORD);
            pds.setInitialPoolSize(MIN_POOL);
            pds.setMinPoolSize(MIN_POOL);
            pds.setMaxPoolSize(MAX_POOL);
            pds.setConnectionPoolName("SmartMedicalPool");
            pds.setValidateConnectionOnBorrow(true);
            logger.info("Oracle UCP Pool initialized successfully.");
        } catch (SQLException e) {
            logger.severe("Failed to initialize DB pool: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return pds.getConnection();
    }

    /** Always call this in finally block */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try { r.close(); } catch (Exception e) {
                    logger.warning("Failed to close resource: " + e.getMessage());
                }
            }
        }
    }
}
